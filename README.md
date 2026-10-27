# Climin TV Friend: Android TV Debloater & Climin UI Launcher

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-0.0.2-success.svg)](docs/changelog.md)
[![APK Size](https://img.shields.io/badge/APK_Size-1.4_MB-informational.svg)](launcher/app/build/outputs/apk/release/app-release.apk)
[![Target SDK](https://img.shields.io/badge/Target_SDK-34_(Android_14)-orange.svg)](launcher/app/build.gradle)
[![Min SDK](https://img.shields.io/badge/Min_SDK-26_(Android_8.0)-purple.svg)](launcher/app/build.gradle)
[![Styles](https://img.shields.io/badge/Styles-5_selectable_looks-indigo.svg)](UI_MAP.md)
[![Languages](https://img.shields.io/badge/Languages-26-brightgreen.svg)](launcher/app/src/main/assets/i18n)

A non-destructive Android TV cleanup tool and a native television launcher, **Climin UI**, written in
plain Java (no Kotlin, no web views) for 1080p and 4K televisions down to Android 8.0.

The launcher ships **five selectable looks** — Glass UI (the default, the one you see in the
screenshots), Material 3 Expressive, Win UI, Cyberpunk and Retro UI — switched live from
**Settings › Theme**, and every one of them is the same launcher: same motion, same geometry, same
icons, same navigation.

This project is inspired by CleanYourTV and FreeMyTV.

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
│ • Pure-Python ADB client (tv.py)      │   │ • Package: io.climintv.home           │
│ • Recovery ledger (recovery.py)       │   │ • Five looks, one behaviour           │
│ • Drift check after TV updates        │   │ • Liquid-glass home (Glass UI)        │
│ • Privacy / ACR / telemetry audit     │   │ • TMDB picks from installed apps      │
│ • Chunked APK push (b64push.py)       │   │ • TV Input Framework (HDMI/AV/USB)    │
│ • 297 device profiles (examples/)     │   │ • Open-Meteo weather, Binance prices  │
│ • Disable / restore, no uninstall     │   │ • Photo screensaver + sleep timer     │
└───────────────────────────────────────┘   │ • 26 languages                        │
                                            └───────────────────────────────────────┘
```

---

## 2. Features

### Home screen (Glass UI by default)

- **For You picks**: three cards refreshed every hour, built from what is actually installed on the
  TV (Netflix, Prime Video, Disney+, Max, Apple TV, Hotstar, Spotify, VLC, …) and what streams where
  in the user's country, through TMDB with a free API key (`Config.TMDB_KEY`). Explore / Movies /
  Shows tabs switch the row. Without a key the home screen shows the clock, the weather line and the
  price card instead of picks.
- **Wide and square cards**: a banner-priority hero card plus two square covers, with title, meta
  line, provider pill and the service's own app on OK. Photos come from TMDB and are cached as
  32-bit bitmaps, together with the picks, so the row still renders after a restart without a
  network.
- **Glass chrome**: liquid-glass platter and buttons over a blurred, slowly drifting backdrop, a
  live clock with seconds, and the price ticker (Binance, the coins you enable, 24 h change; off
  until the quotes widget is turned on).
- **My Photos**: your own photos become the screensaver and, without picks, the blurred backdrop
  behind the home screen (see [Your own photos](#your-own-photos)).
- **Shelf**: the apps on the home platter. Hold OK on a tile to move it with the D-pad, OK to drop;
  hold OK on an app in **All Apps** to pin or unpin it from the shelf.
- **All Apps**: a full-screen five-column grid (not a cramped dialog) with the same pin/unpin hold.
- **Drawer**: opens under the shelf (the screen moves up) with the TV's real inputs (TV Input
  Framework: HDMI 1–3, AV, USB), shortcuts (All Apps, Home Settings, TV Settings), the weather card
  and the price card.
- **Five top buttons**: Inputs, Wi-Fi, Theme (Automatic → Light → Dark), Home Settings, TV Settings.

### Weather and prices (no API key needed)

- **Open-Meteo** weather: current temperature, condition, feels-like, humidity, wind, high/low, a
  six-slot hourly row (or five days), city search, °C/°F.
- **Prices**: Binance ticker for the coins you enable (Bitcoin, Ethereum, Solana; USDT in BRL) with
  24 h change, in the drawer and under the screensaver clock.

### Screensaver

- The picks' photos full screen, one every 30 seconds, with a slow Ken Burns zoom and a 3 s crossfade;
  each photo is loaded 10% larger than the screen so the zoom never upscales.
- iPhone-lock-screen-style corner: date, weather, **liquid-glass clock** (lensed digits over the
  photo), prices underneath. Scrims are painted into the bitmap instead of drawn as a layer per
  frame.
- Starts only where the system lets a screensaver replace the stock one; any remote key exits.
  **Settings › Screensaver** previews it full screen without touching the system setting.
- Follows the selected look: ink, scrim tone and (for the flat looks) the clock style.

### Your own photos

`tools/photos.py` sends photos from a computer to the TV:

```bash
TV_HOST=<YOUR_TV_IP> tools/venv/bin/python -I tools/photos.py ~/Pictures/holiday/*.jpg
```

It takes JPEG, PNG, WebP and (where the TV decodes it) HEIC, skips anything under 640 px on the short
side, and gets each file there by the first path that works:

- **the app's own folder**, `/sdcard/Android/data/io.climintv.home/files/photos`, which adb may write
  on Android 8–10;
- **the photo inbox**, the app's content provider (`content://io.climintv.home.photos`), where
  Android 11+ and Fire OS 8 keep adb out of that folder. Only adb's shell may write there, and a send
  cut short is discarded instead of imported.

The launcher imports what is waiting the next time the home screen, the screensaver or Settings
opens: each photo is turned upright, cropped to 16:9, re-encoded at 2112×1188 as JPEG (which also
drops EXIF and GPS) and kept in the order it was sent, up to 100. While **Home Settings › My Photos**
is on, the screensaver shows them instead of the picks' photos, and without picks one of them is the
home screen's backdrop. OK on that row removes them all.

### Sleep timer

- Minutes left with a live countdown; **Show Time Left** draws a small pill **over any app** (six
  positions around the screen) reading `Off in 12 min`, and the look tints the pill and its ink.
- Where a TV refuses "draw over other apps", the home screen shows the minutes instead.

### Settings (nine panes)

| Pane | What it holds |
|---|---|
| Home Settings | Language, temperature unit, My Photos, navigation sounds, weather widget, city, forecast, quotes widget, and the coins shown (Bitcoin, Ethereum, Solana, plus a Dollar row where the currency is BRL) |
| Greetings | Placeholder — the pane currently says "Coming soon." |
| Theme | **Mode** (Automatic / Light / Dark) and **Style** (the five looks), applied live |
| Screensaver | **Preview**: the screensaver full screen without changing the system setting (the TV still decides when it starts) |
| Shelf | The home platter: add, remove, reorder |
| Top Buttons | Which five buttons sit top right, and their order |
| Remote Buttons | What each branded remote button opens (Netflix, YouTube, Prime, Disney+, … per brand) |
| Sleep Timer | Minutes, Show Time Left, Position |
| About | Wordmark, version, description |

Settings are a two-panel layout drawn by the launcher itself, with its own focus, fades and springs;
they follow the look as well. Everything is local — no account, no telemetry, no network listener in the app.

### The five looks

| Look | Surfaces | Shape |
|---|---|---|
| **Glass UI** (default) | Liquid glass: blur + refraction + sheen, the launcher's original look | 30 / 34 px radii |
| **Material 3 Expressive** | Opaque tonal container, 1 px outline, purple seed, tinted focus | 28 / 16 px |
| **Win UI** | Frosted acrylic layer, thin stroke, Segoe blue accent, rectangular focus | 8 px |
| **Cyberpunk** | Near-black panel, glowing cyan hairline, hard edges | 2 px, chamfer |
| **Retro UI** | Flat beige plastic by day, warm CRT at night, hard offset block | 0 px, 2 px border |

A look changes **palette, surface recipe and shape** — never motion, geometry, icons, navigation or
type: every look draws the same Inter typeface. Glass UI stays the pixel reference, and every colour
is a day/night pair blended by the existing 2 s crossfade. `UI_MAP.md` carries the per-look token and
contrast tables.

### Languages

26 catalogues (`launcher/app/src/main/assets/i18n`): English, Czech, Danish, German, Greek, Spanish,
Finnish, French, Hindi, Indonesian, Italian, Japanese, Korean, Norwegian, Dutch, Polish, Portuguese,
Romanian, Russian, Swedish, Thai, Turkish, Ukrainian, Vietnamese, Chinese (Simplified, Traditional).

### Safety

- Climin UI is installed as a normal app; it only takes the Home role when the install script asks.
- A watchdog counts crashes: three within five minutes and the TV's original home screen is given
  back automatically.
- The debloat half never uninstalls anything (see below), and the tool refuses lists that would
  remove what the TV needs.

---

## 3. Minimum Requirements

| Component | Minimum | Recommended |
|---|---|---|
| **Android TV / Google TV** | 8.0 (API 26) | 9.0 – 14.0 (API 28 – 34) |
| **Hardware** | 1.0 GHz quad-core (Amlogic, MediaTek, Realtek) | 1.5 GHz+ quad-core |
| **RAM** | 1 GB (the launcher measured ≈ 41 MB PSS on the reference 720p TV) | 1.5 – 2 GB |
| **Free storage** | 10 MB (release APK is 1.4 MB) | 50 MB+ |
| **Display** | 720p / 1080p / 4K | 1080p / 4K @ 60 Hz |
| **Remote** | Standard 5-way D-pad | Bluetooth / IR TV remote |
| **Host PC (for ADB)** | Linux, macOS or Windows with Python 3.8+ | Python 3.10+ and `adb` |

Fire TV and a few models (e.g. Kalley 43G200) are deliberately kept as *app only*: on those the Home
button cannot be taken over safely, and the launcher says so instead of trying.

---

## 4. Installation

### Method 1: Build and install as the home screen (Climin UI mode)

```bash
git clone https://github.com/zvirox/climin-tv-friend.git
cd climin-tv-friend

# 1. Python environment for the tools (once)
python3 -m venv tools/venv
tools/venv/bin/pip install -r tools/requirements.txt

# 2. Check the TV answers on its network ADB port (`adb connect` is enough to enable it)
adb connect <YOUR_TV_IP>:5555

# 3. Build, install, verify it starts, then take the Home role
TV_HOST=<YOUR_TV_IP> ./launcher/build-and-install.sh
```

The script checks the TV's SDK against the app's `minSdk`, pushes the APK over the TV's own shell
(no `adb install` needed), starts it once and only accepts **Status: ok**, and only then disables a
*recognised* stock launcher (`tvlauncher`, `launcherx`, `leanbacklauncher`, `mitv.tvhome`) and sets
`io.climintv.home/.MainActivity` as Home. Anything unexpected (the setup wizard, another launcher,
Android's chooser) stops it with an explanation and changes nothing.

### Method 2: Normal app mode (test without touching Home)

```bash
cd launcher
./gradlew assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
adb shell am start -n io.climintv.home/.MainActivity
```

### Method 3: Debloat first, launcher later

```bash
# Validate a list against the safety rules for a device profile
python3 tools/check.py examples/xiaomi-mitv-aetp0-dofus/debloat.txt

# Apply it (non-destructive: pm disable-user, never uninstall)
TV_HOST=<YOUR_TV_IP> ./tools/disable.sh examples/xiaomi-mitv-aetp0-dofus/debloat.txt

# Undo it exactly, from the recorded pre-change state
TV_HOST=<YOUR_TV_IP> ./tools/restore.sh examples/xiaomi-mitv-aetp0-dofus/debloat.txt
```

### Method 4: Sideload the APK

Take `launcher/app/build/outputs/apk/release/app-release.apk` from a build (or the APK attached to
the v0.0.2 release, once published — size and SHA-256 in
[`docs/releases/v0.0.2.md`](docs/releases/v0.0.2.md)), copy it to a USB stick, install it with any TV
file manager and launch **Climin UI** from the app drawer.

---

## 5. Remote Controls

| Button | Action |
|---|---|
| **D-pad UP / DOWN** | Title tabs and cards → shelf → drawer → back |
| **D-pad LEFT / RIGHT** | Cards, tabs, shelf; moves the tile in reorder mode |
| **OK** | Opens the focused app, pick, input or setting |
| **Hold OK (home shelf)** | Reorder: `← →` to move, OK to drop, Back to cancel |
| **Hold OK (All Apps)** | Pin / unpin the app on the home shelf |
| **BACK** | Closes All Apps or Settings, leaves reorder mode, returns Home |
| **Branded buttons** | Netflix, YouTube, Prime Video, Disney+… mapped per brand in Settings › Remote Buttons |
| **All Apps button** | Opens All Apps (including Xiaomi's 284 and other OEM codes) |

---

## 6. The Debloat Tools

Everything under `tools/` talks to the TV with its own small ADB client (`tv.py`), so the tools
themselves do not need the `adb` binary — only Python 3 and a reachable TV with debugging allowed.

- **`disable.sh`** — validates the list (bare package names only) and disables each package with
  `pm disable-user --user 0`. Nothing is uninstalled; a package can always come back.
- **`recovery.py` / `restore.sh`** — before the first change, the TV's real state is recorded
  (which packages were already disabled, which component owned Home, the animation scales). Restore
  replays that record, so undo means undo — not "enable everything".
- **`check.py`** — compares the TV with the list it was given and exits non-zero on drift, which is
  what a firmware update causes (it re-enables packages and hands Home back). `--fix` re-applies the
  list without touching the recovery record.
- **`privacy.py`** — audits what the firmware runs for voice, ACR and telemetry, with the evidence
  for each name, and can write the candidates out as a debloat list.
- **`b64push.py`** — pushes large files (the APK, photos) through one shell channel in 48 KB base64
  chunks and verifies the copy with `md5sum`, for TVs whose ADB sync push or `adb install` stalls.
- **`photos.py`** — sends your own photos to the launcher over the app's folder or the photo inbox,
  whichever the Android version allows (see [Your own photos](#your-own-photos)).
- **`adb-closed.py`** — checks that the TV's ADB port is closed again after debugging is switched off.
- **`bench.py`, `trace.py`, `screendiff.py`** — frame-time benchmarks, Perfetto traces and
  screenshot diffs used while tuning the launcher.
- **`examples/`** — 297 per-model folders (Xiaomi, TCL, Sony, Hisense, Philips, Skyworth, Chromecast,
  Onn, NVIDIA, projectors, …) with the packages that model ships and a tested list.

The safety rules are shared by every entry point: system-critical and launcher-critical packages are
refused by pattern (`CORE`), packages owning the saved Wi-Fi network (Google's and OEM `setupwraith`)
are protected so a TV cannot lose its network, and a stock launcher is only disabled once Climin UI
has proven it answers Home.

---

## 7. Testing & Verification

```bash
# D-pad navigation, focus order and screenshot sanity by ADB
adb shell input keyevent 20
adb shell screencap -p /sdcard/tv_test.png && adb pull /sdcard/tv_test.png

# Frame timing while navigating
TV_HOST=<YOUR_TV_IP> tools/venv/bin/python tools/bench.py

# Screenshot A/B diff (used to prove Glass UI stayed pixel-identical)
tools/venv/bin/python tools/screendiff.py before.png after.png
```

`docs/testing.md` has the full focus matrix and ADB runbooks, `docs/architecture.md` the internals.

---

## 8. Documentation

- [`docs/changelog.md`](docs/changelog.md) — every release, including **v0.0.2**.
- [`docs/releases/v0.0.2.md`](docs/releases/v0.0.2.md) — release notes and asset checksums.
- [`docs/architecture.md`](docs/architecture.md) — process model, springs, the theme seam, the drawing
  pipeline and the measured size and memory.
- [`docs/testing.md`](docs/testing.md) — remote navigation matrix and ADB runbooks.
- [`docs/roadmap.md`](docs/roadmap.md) — what is planned next.
- [`UI_MAP.md`](UI_MAP.md) — section-by-section map of every screen, with the look tokens and
  contrast tables.
- [`RULES.md`](RULES.md) — the rule book: which behaviour is frozen and how a change is proposed.

---

## 9. License

Licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
Compatible with open-source Android TV and Leanback ecosystems.
