# Climin TV Friend: Android TV Debloater & Climin UI Launcher

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![APK Size](https://img.shields.io/badge/APK_Size-248_KB-success.svg)](launcher/app/build/outputs/apk/release/app-release.apk)
[![Target SDK](https://img.shields.io/badge/Target_SDK-34_(Android_14)-orange.svg)](launcher/app/build.gradle)
[![Min SDK](https://img.shields.io/badge/Min_SDK-26_(Android_8.0)-purple.svg)](launcher/app/build.gradle)
[![RAM Footprint](https://img.shields.io/badge/RAM_Footprint-<35_MB-brightgreen.svg)](docs/architecture.md)
[![Design System](https://img.shields.io/badge/Design_System-Material_3_Expressive-indigo.svg)](docs/architecture.md)

A lightweight, non-destructive Android TV debloating system and custom native television launcher designed with **Google's Material 3 Expressive UI**.

Based on the systems architecture of CleanYourTV, **Climin TV Friend** provides the exact same non-destructive safety invariants and security protocol, paired with **Climin UI**—a clean, tactile, razor-sharp TV launcher calibrated for physical 1080p and 4K displays.

---

## 1. System Topology

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CLIMIN TV FRIEND TOPOLOGY                       │
└────────────────────────────────────────────────────────────────────────┘
                                     │
           ┌─────────────────────────┴─────────────────────────┐
           ▼                                                   ▼
┌───────────────────────────────────────┐   ┌───────────────────────────────────────┐
│           DEBLOAT SUBSYSTEM           │   │               CLIMIN UI               │
│               (tools/)                │   │              (launcher/)              │
├───────────────────────────────────────┤   ├───────────────────────────────────────┤
│ • Pure-Python ADB Client (tv.py)      │   │ • Package: io.climintv.home           │
│ • State Snapshot Ledger (recovery.py) │   │ • Material 3 Expressive Tonal Tokens  │
│ • Home Guard Invariant Enforcement    │   │ • Interior.dev Tactile Spring Physics │
│ • Setupwraith Wi-Fi Drop Protector    │   │ • High-DPI (XXHIGH) 480dpi Assets     │
│ • 48 KB Base64 Binary Streamer        │   │ • Native TV Input Framework (TIF)     │
│ • Zero-Port Security Hardening Audit  │   │ • TMDB Super Mode Recommendation Row  │
│ • 290+ Curated OEM Device Trees       │   │ • Open-Meteo Zero-Key Weather Hub     │
└───────────────────────────────────────┘   │ • Custom M3Switch & TvScrollView      │
                                            └───────────────────────────────────────┘
```

---

## 2. Key Features

### Climin UI: Material 3 Expressive Launcher
- **Compact & Calibrated Scaling (~1.33× – 1.5× Reduction)**:
  - Specially calibrated for physical 42"+ 1080p and 4K television screens where 720p framebuffers typically create an oversized, bloated appearance.
- **High-DPI Sharpness & 32-Bit Framebuffer**:
  - Extracts native uncompressed 480 dpi (`DENSITY_XXHIGH`) vector drawables and banners.
  - Enforces `PixelFormat.RGBA_8888` on the Activity window, eliminating 16-bit color banding, blurry text, and jagged card borders.
- **Full 16:9 Widescreen App Carousel with Tactile Drag & Reorder**:
  - Full-bleed widescreen app cards with banner-priority fallback.
  - **Hold OK button** on any app card on Home to engage tactile reorder mode (`◀ MOVE ▶`), shift positions with D-pad Left/Right, and drop.
- **Full-Screen All Applications Screen**:
  - Clean 5-column grid replacing cramped square popup dialogs.
  - **Hold OK button** on any app in the grid to pin or unpin it from the Home shelf, marked with visible `★ HOME` badges.
- **TMDB Super Mode (Streaming Recommendations)**:
  - Streaming content recommendations powered by The Movie Database (TMDB) API.
  - Displays movie/show backdrop art, TMDb rating, release year, streaming provider pills (Netflix, Prime Video, YouTube, Hotstar, etc.), and deep links.
  - Alternating wide 16:9 and square card geometry.
- **Zero-Key Live Weather Hub**:
  - Real-time weather powered by the Open-Meteo REST API (no private API key required).
  - Displays current temperature, weather conditions, feels-like temperature, humidity, wind speed, high/low, and a 6-slot hourly forecast.
  - Built-in city selector and Celsius (°C) / Fahrenheit (°F) units toggle.
- **Tactile Material 3 Settings Menu**:
  - Apple TV-style split view with categorized controls.
  - Custom animated toggle switches (`M3Switch`), completely eliminating the black-on-black unstyled switch text bug common on Android TV.
- **Hardware Integrations (TIF & Accessibility Key Interception)**:
  - Native Android TV Input Framework (TIF) passthrough for HDMI 1, HDMI 2, HDMI 3, AV, and USB inputs.
  - `HomeKeyService` intercepts remote `KEYCODE_ALL_APPS` ($284$) on Xiaomi PatchWall remotes to reliably return to Climin UI.
- **Ultra-Lean Resource Budget**:
  - Release APK size: **248 KB** (well within the strict 512 KB budget).
  - RAM usage: **under 35 MB active RSS**.

### Debloating Subsystem (`tools/`)
- **100% Non-Destructive**: Employs `pm disable-user --user 0`; never deletes files on `/system` or `/vendor`.
- **Immutable Safety Gate (`CORE_RE`)**: Hardcoded regex barrier protecting critical system services from being disabled.
- **Setupwraith Wi-Fi Drop Prevention**: Protects Google TV provisioning packages that hold saved network credentials.
- **Home Guard**: Prohibits disabling stock launchers until Climin UI is verified installed and answering the Home intent.
- **Instant Rollback**: Every operation is logged to a recovery ledger for single-command restoration.

---

## 3. Minimum Requirements

| Component | Minimum Requirement | Recommended |
|---|---|---|
| **Operating System** | Android TV / Google TV 8.0 (API 26) | Android TV 9.0 – 14.0 (API 28 – 34) |
| **Hardware** | 1.0 GHz Quad-Core SoC (Amlogic, MediaTek, Realtek) | Quad-Core 1.5 GHz+ |
| **RAM** | 1 GB RAM (Climin UI uses $< 35\text{ MB}$) | 1.5 GB – 2 GB RAM |
| **Free Storage** | 10 MB free space (APK is only 248 KB) | 50 MB+ |
| **Display Panel** | 720p / 1080p / 4K Ultra HD | 1080p / 4K @ 60 Hz |
| **Remote Control** | Standard 5-way D-Pad remote control | Standard Bluetooth / IR TV Remote |
| **Host PC (for ADB)** | Linux, macOS, or Windows 10/11 with Python 3.8+ | Python 3.10+ & Android Debug Bridge (`adb`) |

---

## 4. Installation Guide

### Method 1: Automated Installation via AI Agent (Recommended)
If you are using an AI coding assistant (such as **Antigravity**, **Claude Code**, **Cursor**, or **Cline**), you can prompt the agent to handle the installation and verification automatically.

Copy and paste this prompt to your AI assistant:

```text
Connect to my Android TV over ADB at <TV_IP_ADDRESS>:5555, compile the Climin UI release APK in launcher/, install it as a normal test application, launch io.climintv.home/.MainActivity, and take a screenshot to verify that D-pad navigation, the live clock, and streaming carousels are working correctly. Do not alter TV system settings or set default launcher without my explicit confirmation.
```

The AI agent will:
1. Connect to the TV via ADB.
2. Build the release APK (`./gradlew assembleRelease`).
3. Stream the APK to the TV (`adb install -r launcher/app/build/outputs/apk/release/app-release.apk`).
4. Start the launcher activity.
5. Capture and verify a live screenshot.

---

### Method 2: Manual Installation via ADB (Normal App Mode for Testing)

To test Climin UI on your TV without modifying your stock home launcher or system settings:

#### Step 1: Enable Developer Options & USB Debugging on TV
1. On your TV, navigate to **Settings → Device Preferences → About**.
2. Scroll to **Build** and click the remote **OK** button **7 times** until you see *"You are now a developer!"*.
3. Go back to **Device Preferences → Developer Options**.
4. Enable **USB Debugging**.
5. Note your TV's IP address in **Settings → Network & Internet → [Your Wi-Fi network]** (e.g. `192.168.1.50`).

#### Step 2: Build & Install Climin UI
From your computer terminal:

```bash
# 1. Clone the repository
git clone https://github.com/zvirox/climin-tv-friend.git
cd climin-tv-friend

# 2. Connect to your TV over ADB
adb connect <YOUR_TV_IP>:5555
# (Accept the USB debugging prompt on your TV screen)

# 3. Compile the release APK
cd launcher
./gradlew assembleRelease

# 4. Install Climin UI onto your TV
adb -s <YOUR_TV_IP>:5555 install -r app/build/outputs/apk/release/app-release.apk

# 5. Launch Climin UI
adb -s <YOUR_TV_IP>:5555 shell am start -n io.climintv.home/.MainActivity
```

---

### Method 3: Full Debloat & Default Home Launcher Installation

To debloat OEM telemetry, disable stock recommendation bloat, and establish Climin UI as the primary launcher:

```bash
# 1. Verify safety rules for your device profile
python3 tools/check.py examples/xiaomi-mitv-aetp0-dofus/debloat.txt

# 2. Build and install Climin UI as default launcher
TV_HOST=<YOUR_TV_IP> ./launcher/build-and-install.sh

# 3. Execute non-destructive debloater
TV_HOST=<YOUR_TV_IP> ./tools/disable.sh examples/xiaomi-mitv-aetp0-dofus/debloat.txt

# 4. Instant rollback (if ever needed)
TV_HOST=<YOUR_TV_IP> ./tools/restore.sh examples/xiaomi-mitv-aetp0-dofus/debloat.txt
```

---

### Method 4: Sideloading via USB Drive / File Transfer
1. Download `app-release.apk` from the [GitHub Releases](https://github.com/zvirox/climin-tv-friend/releases) page or copy `launcher/app/build/outputs/apk/release/app-release.apk` to a USB flash drive.
2. Plug the USB drive into your TV.
3. Open any TV File Manager (e.g. AnExplorer, FX File Explorer) and select `app-release.apk` to install.
4. Launch **Climin UI** from your TV's app drawer.

---

## 5. TV Remote Controls Reference

| Button | Action in Climin UI |
|---|---|
| **D-Pad UP / DOWN** | Seamlessly navigates between Header, Super Mode, App Carousel, Inputs, and Weather |
| **D-Pad LEFT / RIGHT** | Scrolls carousels horizontally / shifts apps in reorder mode |
| **OK / Center Click** | Launches focused application, streaming item, or input source |
| **Hold OK (App Carousel)** | Enters drag-to-reorder mode (`◀ MOVE ▶`); press Left/Right to shift, OK to drop |
| **Hold OK (All Apps)** | Pins / unpins application to Home shelf (updates `★ HOME` badge) |
| **BACK Button** | Closes full-screen dialogs (All Apps, Settings) / returns to Home |

---

## 6. Testing & Quality Verification

Run automated D-pad navigation and visual sanity tests:

```bash
# Automated D-pad remote navigation verification
bash docs/testing.md

# Capture TV screenshot for sharpness inspection
adb -s <YOUR_TV_IP>:5555 shell screencap -p /sdcard/tv_test.png
adb -s <YOUR_TV_IP>:5555 pull /sdcard/tv_test.png ./tv_test.png
```

---

## 7. Documentation Directory

Detailed architectural specifications, testing runbooks, prompt templates, and roadmaps are organized in the `docs/` folder:

- [`docs/prompt.md`](docs/prompt.md): Master AI agent system prompt directives, invariants, and task history.
- [`docs/changelog.md`](docs/changelog.md): Detailed versioned changelog tracking releases, bug fixes, and improvements.
- [`docs/architecture.md`](docs/architecture.md): In-depth system topology, focus navigation graph, and high-DPI pipeline.
- [`docs/testing.md`](docs/testing.md): Remote navigation test scripts, D-pad test matrix, and ADB runbooks.
- [`docs/roadmap.md`](docs/roadmap.md): Upcoming features, multi-category tabs, and future milestones.

---

## 8. License

Licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.
Compatible with open-source Android TV and Leanback ecosystems.
