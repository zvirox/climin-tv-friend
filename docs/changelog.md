# Changelog

All notable changes to the **Climin TV Friend** project and the **Climin UI** native Android TV launcher will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.0.2] - 2026-10-27

The theme release: Glass UI stops being the only skin and becomes one of five selectable looks,
with the settings panes rebuilt around it. Nothing that already worked changed behaviour — motion,
geometry, icons and navigation are untouched, and Glass UI's rendering was held pixel-identical
through every step.

### Added

#### Five selectable looks (Settings › Theme › Style)
- **Glass UI** — the launcher's original liquid glass, still the default and the reference.
- **Material 3 Expressive** — opaque tonal containers, a 1 px outline, the purple seed, an
  accent-tinted focus, 28 / 16 px radii.
- **Win UI** — frosted acrylic panels, thin strokes, Segoe blue, a rectangular focus, 8 px radii.
- **Cyberpunk** — near-black panels with a glowing cyan hairline and a 2 px chamfer.
- **Retro UI** — flat beige plastic by day, warm CRT at night, a hard offset block and a 2 px border,
  0 px radii.
- Switching a look in Settings repaints the home screen without a restart, and the preference
  survives a reboot.
- A look supplies palette, surface recipe and shape only. Motion, geometry, icons, navigation and
  type are the same in all five; every colour stays a day/night pair blended by the existing 2 s
  crossfade.

#### Settings, rebuilt
- Two-panel layout (glass in Glass UI) drawn by the launcher itself, with its own focus, fades and
  springs, in nine panes: **Home Settings, Greetings, Theme, Screensaver, Shelf, Top Buttons, Remote
  Buttons, Sleep Timer, About**.
- **Theme** pane: **Mode** (Automatic / Light / Dark) and the new **Style** row, with TalkBack labels
  and a footer saying the look applies to the whole launcher.
- **About** pane: wordmark, version number and a short description.
- **Screensaver** pane: a full-screen **Preview** row that runs the screensaver without touching the
  system setting.
- **Shelf**, **Top Buttons** and **Remote Buttons** panes for the home platter, the five top-right
  buttons and the branded remote keys.
- Every new label exists in all 26 language catalogues.

#### Home screen
- **Five top buttons**: Inputs, Wi-Fi, Theme (Automatic → Light → Dark), Home Settings, TV Settings.

#### Screensaver, sleep pill and toast follow the look
- **Screensaver**: ink, scrim tone and clock style come from the look's night column (the screensaver
  is always dark). Glass UI keeps pure white ink, a pure black scrim and the liquid-glass clock
  lens; Win UI keeps the lens; Material 3, Cyberpunk and Retro draw the same digits as plain text in
  their own ink.
- **Sleep timer pill**: a new `Palette.pill` slot, ink from `onPhoto` and a corner radius from the
  look's shape — so the "Off in 12 min" pill matches the launcher over any app.
- **Toast**: the bell and text use the look's night ink (the pill stays dark in every look).

#### Documentation
- [`RULES.md`](../RULES.md): the project rule book — which behaviour is frozen, and the approval
  template for a change that would touch it.
- [`UI_MAP.md`](../UI_MAP.md): a section-by-section map of the home screen, drawer, All Apps and
  settings, plus per-look token and contrast tables.
- README: rewritten for 0.0.2, including a **Your own photos** section (the name Settings › My Photos
  points at), the five looks, the real settings panes and the MIT licence.

#### Tools
- **`tools/photos.py`**: send your own photos to the TV over whichever path the Android version
  allows — the app's external folder, or the `PhotoInbox` content provider on Android 11+ and Fire
  OS 8. It checks the image size first, keeps the send order, and verifies every transfer.
- **`tools/b64push.py`**: the verified chunked transfer is now `send(dev, local, remote)`, so a tool
  that already holds a connection can use it. The sync push in `adb_shell` stalls against some TVs
  (this Xiaomi/MiTV among them), which is why both tools speak base64 over the shell.

### Changed
- The **Theme** pane replaces the old Mode row in Home Settings; the home **Theme** button still
  cycles Automatic → Light → Dark exactly as before.
- Settings no longer has the **Home Screen** and **TV Settings** panes; the first became the five top
  buttons, the second is one of them.
- `versionCode` 2, `versionName` 0.0.2.

### Fixed
- **Cyberpunk**: the focus container is now painted under the translucent focus fill, and the focus
  label / positive-delta contrast was raised to AA (5.44:1 and 5.54:1).
- **Retro UI**: the dim label and positive-delta contrast was raised to AA (5.38:1 and 5.60:1).
- Screensaver: text and scrims no longer assume white-on-black once a non-Glass look is selected.
- **My Photos**: the Settings count, the screensaver picks and the home backdrop no longer stay stale
  when photos are imported in the same second or by the other process. The list's cache was keyed on
  the folder's time alone, which Android keeps in whole seconds; it now also keys on the entries.

### Known limitations
- Verified on a TCL Android 9 (API 28) TV at 1280×720. The Android 11 – 12L screensaver variant
  (`ScreensaverAlone`) and the Android 8 – 10 dream path were not exercised on hardware this cycle.
- Fire TV and a few models stay "app only": they cannot give the Home role over safely.

---

## [0.0.1] - 2026-10-25 (Initial Stable Release)

The first stable release: the non-destructive debloat tools under `tools/`, and the **Glass UI**
launcher (`io.climintv.home`) as the TV's home screen.

### Added

#### Glass UI launcher
- **Home screen**: a single custom-drawn view in a 1920×1080 design space — a **For You** row of three
  TMDB picks (only from streaming apps actually installed), a blurred backdrop, a glass shelf with
  the apps, a live clock, and the price ticker.
- **Shelf**: hold OK to reorder the tiles with the D-pad.
- **All Apps**: a full-screen five-column grid; hold OK on a tile to pin or unpin it from the shelf.
- **Drawer**: the TV's real inputs (TV Input Framework: HDMI, AV, USB), shortcuts, a weather card
  (Open-Meteo, six-slot hourly row or five days) and a prices card (Binance, 24 h change).
- **Screensaver**: the picks' photos full screen with a slow zoom and a crossfade; date, weather,
  liquid-glass clock and prices in an iPhone-lock-screen-style corner; any remote key exits.
- **Sleep timer**: minutes left with a live countdown, and a small pill over any app where the TV
  allows drawing over other apps.
- **Settings**: a two-panel layout drawn by the launcher itself (not platform widgets), with the
  launcher's own focus, fades and springs.
- **Remote keys**: branded buttons mapped per brand; the OEM All Apps codes (Xiaomi's 284 and
  others) open the grid.
- **26 language catalogues** in `assets/i18n`, with named-placeholder patterns and plural rules.

#### Debloat tools (`tools/`)
- `disable.sh` — validates a bare-package list and disables each package with
  `pm disable-user --user 0`; nothing is uninstalled.
- `restore.sh` / `recovery.py` — records the TV's real pre-change state and replays it, so undo
  means undo.
- `check.py` — reports drift after a firmware update, and re-applies the list with `--fix`.
- `privacy.py` — audits voice, ACR and telemetry packages with the evidence for each name.
- `b64push.py` — pushes the APK through one shell channel in 48 KB base64 chunks.
- `adb-closed.py` — checks that the TV's ADB port is closed again after debugging is switched off.
- 297 per-model profiles in `examples/`.

### Note

The 0.0.1 notes as first published described internals that were never in the code
(`FocusAnimator`, `M3Switch`, `TvScrollView`, `HomeKeyService`, 480 dpi icon extraction, a 248 KB
APK, < 35 MB RSS). They have been rewritten above to match what actually shipped. Real measurements
for the current build are in the 0.0.2 entry and in `docs/architecture.md`.
