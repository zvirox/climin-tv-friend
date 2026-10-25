# Climin TV Friend: AI Agent Prompting Directives & System Prompts

This document serves as the master engineering prompt specification for AI agents, developers, and autonomous assistants contributing to the **Climin TV Friend** repository (`climin-tv-friend`) and its native Android TV launcher, **Climin UI** (`io.climintv.home`).

---

## 1. Project Identity & Invariants

```yaml
project_name: climin-tv-friend
launcher_app_name: Climin UI
package_name: io.climintv.home
main_activity: io.climintv.home.MainActivity
target_environment: Android TV & Google TV (API 26+)
design_system: Material 3 Expressive (Tactile Motion & Solid Tokens)
apk_size_budget: "< 512 KB" (Target: ~248 KB)
ram_budget: "< 35 MB" active RSS
```

### Non-Negotiable Engineering Invariants
1. **Zero-Brick Guarantee**:
   - Never invoke `pm uninstall` on system packages. Only use reversible `pm disable-user --user 0`.
   - Never disable packages matching the immutable `CORE_RE` regex (`android`, `systemui`, `permissioncontroller`, `packageinstaller`, `settings`, `ext.services`).
   - Safeguard `setupwraith` whenever Wi-Fi credentials are owned by Google Provisioning.
2. **APK Budget Compliance**:
   - Release APK must stay strictly below **512 KB**.
   - No heavy 3rd-party dependencies (e.g., Glide, Picasso, Retrofit, OkHttp). Image loading, HTTP streaming, JSON parsing, and animations must rely on lean Android platform APIs and lightweight AndroidX libraries.
3. **10-Foot Leanback Experience**:
   - No touch-first patterns. All navigation must be 100% controllable via standard 5-way D-pad (UP, DOWN, LEFT, RIGHT, CENTER/OK, BACK).
   - Visible, high-contrast focus rings and spring scale overshoots (`FocusAnimator`).
   - No tiny text: minimum font size `10sp`, standard body `12sp - 14sp`, headers `20sp - 28sp`.
4. **Physical Panel Calibration & High Sharpness**:
   - Framebuffer on many TVs (including Xiaomi TVs) renders at 720p and upscales to 1080p. Keep UI dimensions compact (app cards `144dp × 86dp`, super cards `180dp × 104dp`).
   - Always load high-DPI assets using `res.getDrawableForDensity(id, DisplayMetrics.DENSITY_XXHIGH)` to prevent low-res TV assets.
   - Enforce `getWindow().setFormat(PixelFormat.RGBA_8888)` to ensure 32-bit hardware rendering.

---

## 2. Core Subsystems

### Subsystem A: The Debloat Engine (`tools/`)
- Pure-Python ADB client (`tv.py`) that operates without external ADB binary dependencies when needed.
- Automated snapshotting (`recovery.py`) that generates a rollback ledger before making any device changes.
- Chunked 48 KB binary streaming pipeline (`b64push.py`) capable of sideloading APKs over restricted Android TV debug shells.

### Subsystem B: Climin UI Launcher (`launcher/`)
- **Top Header**: Big live clock with separate accent seconds, AM/PM, full date, and circular shortcut actions.
- **Super Mode (TMDB Recommendations)**: Mixed 16:9 and square streaming cards with TMDb ratings, providers, and deep links.
- **App Carousel**: Full 16:9 widescreen tiles with banner-priority display, anti-aliased bitmap filtering, and drag-to-reorder via long-press OK.
- **Inputs & Quick Settings Bar**: Native TvInputManager passthrough (HDMI 1, 2, 3, AV, USB) and system settings shortcuts.
- **Zero-Key Weather**: Open-Meteo REST integration displaying current condition, temperature, feels-like, wind, humidity, and 6-slot hourly forecast.
- **Tactile Material 3 Settings**: Apple TV-style split view featuring custom Material 3 switch widgets (`M3Switch`), eliminating unstyled platform widgets and black text artifacts.

---

## 3. Agent Task Execution Workflows

When an AI agent is instructed to perform tasks on this codebase, follow these prompt patterns:

### Pattern 1: Implementing a New UI Feature
```markdown
ROLE: Android TV System Engineer & Material 3 UI/UX Designer
TASK: Implement [feature] in Climin UI.
RULES:
1. Preserve the 512 KB APK limit. Inspect build output after compilation.
2. Keep all card dimensions compact to prevent zoomed appearances on 42"+ 1080p TVs.
3. Hook into `FocusAnimator` for tactile spring physics and elevation changes.
4. Ensure full keyboard/D-pad accessibility. Override `dispatchKeyEvent` or `FocusSearch` if navigation traps occur.
5. Apply dynamic theme tokens via `ThemeManager.getCurrentPalette(context)`.
```

### Pattern 2: Debugging Remote Navigation Issues
```markdown
ROLE: Android Input & Focus Navigation Specialist
TASK: Resolve focus trap between [View A] and [View B].
RULES:
1. Never poll or sleep. Use `adb logcat -s MainActivity` and `dumpsys activity top` to locate the focused view.
2. Ensure parents do not intercept focus with `descendantFocusability="afterDescendants"`.
3. Check `TvScrollView`: confirm `restoreDefaultFocus` and `onRequestFocusInDescendants` prevent focus hijacking.
4. Avoid calling `adapter.notifyDataSetChanged()` during active focus transitions.
```

### Pattern 3: Adding Device Support to Debloater
```markdown
ROLE: Android TV Security & Debloating Engineer
TASK: Create a new OEM device profile under `examples/[oem]-[model]/`.
RULES:
1. Always run `python3 tools/check.py <profile>/debloat.txt` against `CORE_RE`.
2. Inspect `tv.json` to identify vendor-specific PatchWall, telemetry, or bloatware packages.
3. Validate rollback safety with `tools/restore.sh`.
```

---

## 4. Prompt Log & Task History

| Timestamp | User Request Summary | Agent Implementation | Outcome |
|---|---|---|---|
| 2026-10-25 12:00 | Initialize M3 Expressive TV launcher | Implemented tactile squircle cards, spring motion, TIF HDMI inputs | 245 KB lean launcher |
| 2026-10-25 14:00 | Add TMDB Super Mode & Weather | Integrated TMDB API key, Open-Meteo 6-slot forecast, settings dialog | Streaming recommendations live |
| 2026-10-25 16:00 | Fix TV remote freeze & unstyled switches | Reverse engineered FreeMyTV; built `M3Switch`, `TvScrollView`, hold OK to drag apps | Flawless navigation on Xiaomi TV |
| 2026-10-25 21:00 | Fix zoomed 1.5x scaling & blurry icons on 42" 1080p | Scaled dimensions down 1.33x–1.5x; loaded 480dpi XXHIGH assets; RGBA_8888 window format | Razor-sharp compact UI on physical TV |
| 2026-10-25 22:35 | Rebrand to Climin UI / Climin TV Friend & publish docs | Updated strings, gradle, README, created docs hub & pushed to GitHub | Up-to-date repository |

---

## 5. Maintenance Checklist for Agents

- [ ] Check APK size: `ls -lh launcher/app/build/outputs/apk/release/app-release.apk` (< 512 KB).
- [ ] Run lint vital checks: `./gradlew assembleRelease`.
- [ ] Verify D-pad navigation cycle: Header $\leftrightarrow$ Super Mode $\leftrightarrow$ App Selector $\leftrightarrow$ Inputs Row $\leftrightarrow$ Weather.
- [ ] Verify test TV connection before running commands: `adb devices`.
- [ ] Record all major architectural modifications in `docs/changelog.md`.
