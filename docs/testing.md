# Your TV Friend: Testing & Verification Runbook

End-to-end verification for the **Your UI** launcher and the `tools/` debloat engine on Android TV
and Google TV hardware. The launcher draws everything itself and keeps focus in its own code, so
most of this is done by pressing a remote (or the same keycodes over ADB) and looking at the screen.

---

## 1. Quick Test Matrix

| Test | Scope | What to verify | How |
|---|---|---|---|
| **Build** | Gradle / R8 | Release build succeeds; APK is ~1.4 MB | `cd launcher && ./gradlew assembleRelease` |
| **D-pad focus** | Navigation | Tabs → cards → shelf → drawer → back, and up again, with no dead end | Keycodes 19, 20, 21, 22 |
| **OK / hold OK** | Interaction | OK opens; holding OK on the shelf enters reorder; holding OK in All Apps pins/unpins | Keycode 23, held |
| **All Apps** | Sheet | Opens over the home screen (fade + scale), 5 columns, Back closes it | Drawer shortcut or the OEM All Apps key |
| **Settings** | Nine panes | Every pane opens; Theme switches look live; TalkBack reads the rows | Top button *Home Settings* |
| **Icon sharpness** | Rendering | Icons and banners are crisp on a 1080p/4K panel | Screenshot at native size |
| **Debloat safety** | Tools | List is refused if it would remove something the TV needs | `python3 tools/check.py <list>` |

---

## 2. Remote Navigation Checks

Run against a connected TV. The number of Down presses to reach the drawer depends on how many apps
are on the shelf, so treat the counts below as a starting point and watch the screen.

```bash
DEVICE_IP="192.168.31.247:5555"
adb connect "$DEVICE_IP"
adb -s "$DEVICE_IP" shell am start -n io.yourtv.home/.MainActivity
```

```bash
# Tabs (Explore / Movies / Shows) and the cards
adb -s "$DEVICE_IP" shell input keyevent 22   # RIGHT: next tab
adb -s "$DEVICE_IP" shell input keyevent 21   # LEFT: previous tab
adb -s "$DEVICE_IP" shell input keyevent 20   # DOWN: from the tabs into the cards

# Cards -> shelf -> drawer
adb -s "$DEVICE_IP" shell input keyevent 20
adb -s "$DEVICE_IP" shell input keyevent 20
adb -s "$DEVICE_IP" shell input keyevent 20   # the drawer should now be open

# Back up to the top
for i in 1 2 3; do adb -s "$DEVICE_IP" shell input keyevent 19; done
```

```bash
# All Apps: from the drawer's "All Apps" shortcut, or the OEM key (Xiaomi 284)
adb -s "$DEVICE_IP" shell input keyevent 284
sleep 1
adb -s "$DEVICE_IP" shell input keyevent 4     # BACK closes the sheet

# Settings: top button "Home Settings"
adb -s "$DEVICE_IP" shell am start -n io.yourtv.home/.SettingsActivity
sleep 1
# D-pad through the nine panes, then Back to the home screen
for i in 1 2 3 4 5 6 7 8 9 10; do adb -s "$DEVICE_IP" shell input keyevent 20; sleep 0.3; done
adb -s "$DEVICE_IP" shell input keyevent 4
```

Expected, and worth checking on every look:

- Focus never disappears: after any sequence of arrows there is always one visibly focused item.
- The focused tile grows and its neighbours settle back (one continuous motion, not two states).
- Back closes All Apps first, then the drawer, then returns Home.
- Holding OK on the shelf enters reorder (`← →` moves, OK drops, Back cancels) and holding OK on a
  tile in All Apps pins/unpins it.
- The look chosen in **Settings › Theme › Style** is already applied when Settings closes.

---

## 3. Visual Quality & Screenshot Verification

```bash
# Capture and pull
adb -s 192.168.31.247:5555 shell screencap -p /sdcard/tv_test.png
adb -s 192.168.31.247:5555 pull /sdcard/tv_test.png ./tv_test.png

file tv_test.png
# 1280 x 720 (or 1920 x 1080), 8-bit/color RGBA
```

- **A 0-byte capture is not a bug in the launcher**: while a DRM-protected app (the TV's own
  `com.android.tv`) has focus, `screencap` returns nothing (`SurfaceFlinger: FB is protected:
  PERMISSION_DENIED`). Force-stop that app before capturing.
- Icons are extracted at **XXXHIGH (640 dpi)** and downscaled to the tile, so they stay sharp on
  1080p and 4K panels; a screenshot taken at native resolution is the check.
- On the screensaver, the clock digits are the look's own: liquid-glass lens for Glass UI and Win UI,
  plain text for Material 3, Cyberpunk and Retro.
- `tools/screendiff.py before.png after.png` compares two captures; it was used to prove Glass UI
  stayed pixel-identical while the other looks were added.

---

## 4. Debloater Safety Verification

Before running `tools/disable.sh` on any TV model:

```bash
# 1. Audit the list against the safety rules
python3 tools/check.py examples/xiaomi-mitv-aetp0-dofus/debloat.txt

# 2. Check the connected TV's packages
python3 tools/tv.py 'pm list packages'

# 3. Verify Wi-Fi ownership (the package owning the saved network must be protected)
python3 tools/tv.py 'dumpsys wifi' | grep -i cname
```

A list must be refused when it names a system-critical package (`CORE_RE`), the package that owns
the saved Wi-Fi network, or a package under an active HDMI/AV input. Nothing is uninstalled:
`disable.sh` only calls `pm disable-user --user 0`, and `restore.sh` replays the recorded
pre-change state.

---

## 5. What Was Verified for 0.0.2

On a TCL Android 9 (API 28) TV at 1280×720, with the release APK installed as a normal app:

- Home, drawer, All Apps and all nine settings panes, in all five looks, by day and by night.
- Screensaver preview in Glass UI and Retro UI (clock style and scrim per look).
- Sleep-timer pill in all five looks, day and night, drawn over another app.
- Glass UI re-captured after every step with a 0-pixel diff.
- PSS of `:home`: `TOTAL 42225 KB` (`dumpsys meminfo`), release APK 1,405,520 bytes.

Not exercised on hardware in this cycle: the Android 11–12L screensaver variant
(`ScreensaverAlone`), the Android 8–10 dream path, and Fire TV.
