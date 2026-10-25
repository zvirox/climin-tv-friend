# Changelog

All notable changes to the **Climin TV Friend** project and the **Climin UI** native Android TV launcher will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.2.0] - 2026-10-25

### Added
- **High-DPI Asset Loading Pipeline**: Added `loadHighResIcon` and `loadHighResBanner` utilizing `Resources.getDrawableForDensity(resId, DisplayMetrics.DENSITY_XXHIGH)` to extract uncompressed 480 dpi assets from installed TV applications.
- **32-Bit RGBA Window Format**: Explicitly set `getWindow().setFormat(PixelFormat.RGBA_8888)` in `MainActivity.onCreate()` to prevent 16-bit `RGB_565` color banding and blurry card outlines on modern panels.
- **ARGB_8888 Image Decoder**: Updated `ImageLoader` to decode remote TMDB backdrops and posters using `Bitmap.Config.ARGB_8888` and dither filtering, cached with 95% JPEG quality.
- **Rebranding**: Renamed launcher application to **Climin UI** and standardized project name as **Climin TV Friend** (`climin-tv-friend`).
- **Documentation Hub**: Created `docs/` containing `prompt.md`, `changelog.md`, `architecture.md`, `testing.md`, and `roadmap.md`.

### Changed
- **Global Scaling Calibration (~1.33× – 1.5× Reduction)**:
  - Addressed physical panel upscaling on 42-inch Xiaomi TVs (1080p panel with 720p framebuffer, $1.5\times$ upscaling ratio).
  - Reduced Top Header live clock from `72sp` $\to$ `50sp`, seconds from `28sp` $\to$ `18sp`, action buttons from `48dp` $\to$ `40dp`.
  - Reduced Super Mode carousel height from `146dp` $\to$ `112dp` (cards `180dp × 104dp`).
  - Reduced App Selector carousel height from `124dp` $\to$ `94dp` (cards `144dp × 86dp`, 16:9 widescreen ratio).
  - Reduced Inputs & Shortcuts bar height from `60dp` $\to$ `44dp` (chips `34dp` height).
  - Reduced Weather widget height from `88dp` $\to$ `64dp` (forecast chips `44dp × 44dp`).
  - Adjusted All Apps grid cards to `136dp × 96dp` (5 columns) with `44dp` icons and `11sp` labels.
  - Adjusted Settings dialog left panel to `240dp` and rows to compact margins.
- **Focus Scaling Multipliers**:
  - Fine-tuned `FocusAnimator` scale multipliers: App cards to `1.05f` (was `1.10f`), circular buttons to `1.08f` (was `1.16f`), pills to `1.04f` (was `1.08f`).
  - Reduced focus elevation translation to `4dp` to eliminate shadow blur.

---

## [1.1.0] - 2026-10-25

### Added
- **FreeMyTV Reverse-Engineered Architecture**: Analyzed FreeMyTV decompiled structures using Reverse Engineering Anything (REA) techniques to build native leanback equivalents.
- **Tactile App Drag & Reorder**:
  - Implemented hold-to-reorder by pressing and holding the remote OK / Center button on any home card.
  - Added visual `◀ MOVE ▶` badge indicator and D-pad horizontal item movement (`moveItem(from, to)`).
  - Persisted user app orders in `AppPreferences`.
- **Full-Screen All Applications Screen**:
  - Full-screen modal replacing constrained square popup dialogs.
  - Hold OK to pin / unpin apps directly from the All Apps grid to the Home shelf.
  - Visual `★ HOME` status badges on pinned items.
- **Custom Material 3 Switch Component (`M3Switch`)**:
  - Replaced unstyled platform `Switch` widgets which suffered from black-on-black text rendering bugs on Android TV.
  - Added sliding thumb animations with ON/OFF textual and color state indicators.
- **Custom `TvScrollView`**:
  - Overrode `restoreDefaultFocus()` and `onRequestFocusInDescendants()` to prevent Android `ScrollView` focus hijacking during D-pad transitions.

### Fixed
- **D-Pad DOWN Freeze**:
  - Fixed remote focus trap when navigating from TMDB Super Mode recommendations down to the App Selector.
  - Eliminated disruptive `appsMenuAdapter.notifyDataSetChanged()` calls from `applyPalette()` which had been destroying active focused views 600ms after focus landed.

---

## [1.0.0] - 2026-10-25

### Added
- **Climin UI Material 3 Expressive Launcher**:
  - Native Android TV launcher designed with solid tonal tokens (`surfaceContainerLowest` through `surfaceContainerHighest`).
  - Fluid spring physics focus system (`FocusAnimator`) using `androidx.dynamicanimation`.
  - Live seconds clock aligned to system millisecond boundaries.
  - Native TV Input Framework (TIF) passthrough for HDMI 1, 2, 3, AV, and USB inputs.
  - Remote key interception (`HomeKeyService` on keycode 284) to reclaim OEM remote home buttons.
  - Zero-network base profile running in under 35 MB RAM with an APK footprint of only 248 KB.
- **TMDB Super Mode Streaming Recommendations**:
  - REST client connecting to The Movie Database (TMDB) API for trending movies and TV series.
  - Alternating 16:9 widescreen and square cards with ratings, release years, and streaming provider pills.
  - Deep linking support launching YouTube, Netflix, Prime Video, Disney+, Hotstar, and VLC.
  - Offline fallback recommendations ensuring immediate visual populating without initial network delay.
- **Zero-Key Weather Integration**:
  - Open-Meteo REST API integration with 6-slot hourly forecast chips.
  - City selector and Celsius/Fahrenheit toggle.
- **Debloat Subsystem (`tools/`)**:
  - Non-destructive package disabling engine via `pm disable-user --user 0`.
  - Immutable safety regex (`CORE_RE`) and setupwraith Wi-Fi drop protection.
  - Recovery ledger supporting instant, safe rollbacks.
  - 290+ device profiles for Xiaomi, TCL, Sony, Chromecast, and Onn devices.
