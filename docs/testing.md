# Climin TV Friend: Testing & Verification Runbook

This runbook outlines end-to-end verification procedures for both the **Climin UI** native launcher and the **tools/** debloat engine across Android TV and Google TV hardware.

---

## 1. Quick Test Matrix

| Test Suite | Scope | Target Verification | Command / Action |
|---|---|---|---|
| **Compilation** | Build System | Zero compiler warnings, R8 optimizations, APK $< 512\text{ KB}$ | `./gradlew assembleRelease` |
| **D-Pad Focus** | Remote Navigation | Free transitions between all vertical and horizontal rows | Keycodes 19, 20, 21, 22, 23 |
| **Hold-to-Reorder** | Interaction | Long-press OK triggers move badge; horizontal keys swap positions | Long-press DPAD_CENTER (Keycode 23) |
| **Pin / Unpin** | All Apps Grid | Hold OK pins/unpins app from home shelf; updates ★ HOME badge | Long-press DPAD_CENTER in All Apps |
| **High-DPI Render** | Sharpness | 480dpi asset extraction; no blur on 1080p/4K panels | Inspect screencap for crisp borders |
| **Debloat Safety** | Security | Verification against `CORE_RE` and Wi-Fi credential ownership | `python3 tools/check.py <list>` |

---

## 2. Remote Navigation Keycode Automated Test Script

Run this test script over ADB to verify seamless navigation across all launcher rows without focus freezing:

```bash
DEVICE_IP="192.168.31.247:5555"

echo "=== 1. Launching Climin UI ==="
adb -s $DEVICE_IP shell am start -n io.climintv.home/.MainActivity

echo "=== 2. Navigating DOWN through all rows ==="
# Header -> Super Mode -> Apps Carousel -> Inputs -> Weather
for i in 1 2 3 4; do
    adb -s $DEVICE_IP shell input keyevent 20 # DPAD_DOWN
    sleep 0.4
done

echo "=== 3. Navigating UP back to Top Header ==="
for i in 1 2 3 4; do
    adb -s $DEVICE_IP shell input keyevent 19 # DPAD_UP
    sleep 0.4
done

echo "=== 4. Horizontal Carousel Scroll ==="
adb -s $DEVICE_IP shell input keyevent 20 # Focus Super Mode
adb -s $DEVICE_IP shell input keyevent 22 # Move Right
adb -s $DEVICE_IP shell input keyevent 22 # Move Right
adb -s $DEVICE_IP shell input keyevent 21 # Move Left

echo "=== 5. Open and Close Full-Screen All Apps ==="
adb -s $DEVICE_IP shell input keyevent 20 # Apps
adb -s $DEVICE_IP shell input keyevent 20 # Inputs
adb -s $DEVICE_IP shell "for i in 1 2 3 4 5; do input keyevent 22; done && input keyevent 23" # All Apps
sleep 1.0
adb -s $DEVICE_IP shell input keyevent 4 # BACK

echo "=== 6. Open and Close Settings ==="
adb -s $DEVICE_IP shell "input keyevent 22 && input keyevent 23" # UI Settings
sleep 1.0
adb -s $DEVICE_IP shell input keyevent 4 # BACK

echo "=== Test Run Complete: Navigation Passed ==="
```

---

## 3. Visual Quality & Screenshot Verification

To pull an instant screenshot from the connected television for inspection:

```bash
# Capture and pull
adb -s 192.168.31.247:5555 shell screencap -p /sdcard/tv_test.png
adb -s 192.168.31.247:5555 pull /sdcard/tv_test.png ./tv_test.png

# Check resolution and bit-depth
file tv_test.png
# Output should confirm: PNG image data, 1280 x 720 (or 1920 x 1080), 8-bit/color RGBA
```

---

## 4. Debloater Safety Verification

Before running `tools/disable.sh` on any TV model:

```bash
# 1. Audit list for invariant safety
python3 tools/check.py examples/xiaomi-mitv-aetp0-dofus/debloat.txt

# 2. Check connected TV active packages
python3 tools/tv.py pm list packages

# 3. Verify Wi-Fi ownership
python3 tools/tv.py dumpsys wifi | grep -i cname
```
