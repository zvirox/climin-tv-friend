# Changelog

All notable changes to the **Climin TV Friend** project and the **Climin UI** native Android TV launcher will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.0.1] - 2026-10-25 (Initial Stable Release)

This is the initial production-ready stable release of **Climin TV Friend** and the **Climin UI** Android TV launcher (`io.climintv.home`).

### Features & Highlights

#### 1. Material 3 Expressive UI & Tactile Motion
- **Solid Tonal Containers**: Eliminates performance-heavy frosted glass blurs in favor of Material 3 Expressive solid tonal surfaces (`surfaceContainerLowest` through `surfaceContainerHighest`).
- **Tactile Spring Physics (`FocusAnimator`)**: Smooth spring overshoot focus animations with dynamic elevation morphing and press-squish feedback on remote clicks.
- **Physical Display Scaling (~1.33× – 1.5× Reduction)**: Perfectly calibrated for 42"+ 1080p and 4K TV screens with 720p framebuffers, eliminating oversized/zoomed UI elements.
- **High-DPI Sharpness**:
  - Direct 480 dpi (`DENSITY_XXHIGH`) asset extraction via `res.getDrawableForDensity`.
  - 32-bit hardware window allocation (`PixelFormat.RGBA_8888`), eliminating 16-bit color banding and blurry borders.
  - Bitmap anti-aliasing and bilinear filtering on all app banners and icons.

#### 2. TMDB Super Mode (Streaming Content Recommendations)
- Live trending movies and series recommendations powered by The Movie Database (TMDB) API.
- Alternating 16:9 widescreen and square cards with TMDb ratings, release years, and streaming provider pills (Netflix, Prime Video, YouTube, Disney+, Hotstar, VLC).
- High-quality 32-bit `ARGB_8888` backdrop image caching with dithered decodes.
- Built-in offline fallback feed for instant rendering without network lag.

#### 3. Widescreen App Carousel & Full-Screen All Apps
- **Full 16:9 Hero Cards**: Direct banner-priority display for installed TV streaming applications.
- **Hold-to-Reorder**: Hold remote OK / Center button on any home card to engage tactile reorder mode (`◀ MOVE ▶`), shift positions horizontally with D-pad, and press OK to drop.
- **Full-Screen All Applications Grid**: 5-column grid replacing cramped modal popups.
- **Hold to Pin/Unpin**: Hold OK on any app in the grid to pin or unpin it from the Home shelf, marked with visible `★ HOME` badges.

#### 4. Zero-Key Live Weather Hub
- Real-time weather powered by the Open-Meteo REST API (zero API key required).
- Current condition, temperature, feels-like, wind speed, humidity, and 6-slot hourly forecast chips.
- City picker and Celsius (°C) / Fahrenheit (°F) toggle.

#### 5. Tactile Material 3 Settings Menu
- Apple TV-style split view with clean categorization (Display & Experience, App Selector & Home Shelf, Clock & Weather, System).
- Custom animated toggle switches (`M3Switch`), eliminating Android TV black-on-black unstyled switch bugs.

#### 6. Hardware Integrations & Safe Debloating
- **TV Input Framework (TIF)**: Passthrough channel switching for HDMI 1, HDMI 2, HDMI 3, AV, and USB inputs.
- **Remote Key Interception (`HomeKeyService`)**: Intercepts `KEYCODE_ALL_APPS` ($284$) on Xiaomi PatchWall remotes to reliably return to Climin UI.
- **TvScrollView Focus Guard**: Custom scroll container preventing focus hijacking during D-pad navigation.
- **Non-Destructive Debloat Subsystem (`tools/`)**:
  - Package disabling via `pm disable-user --user 0` (zero system brick risk).
  - Immutable safety gate (`CORE_RE`) and setupwraith Wi-Fi drop protection.
  - Recovery ledger supporting instant single-command rollbacks.
  - 290+ device profiles for Xiaomi, TCL, Sony, Chromecast, and Onn devices.

#### 7. Ultra-Lean Footprint
- **APK Size**: **248 KB** (well under the 512 KB budget).
- **RAM Usage**: Under **35 MB active RSS**.
- **Cold Boot Time**: Under **200 ms** on budget Quad-Core TV hardware.
