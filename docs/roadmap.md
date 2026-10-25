# Climin TV Friend: Product Roadmap & Future Milestones

This document tracks upcoming features, engineering milestones, and optimization targets for **Climin TV Friend** and **Climin UI**.

---

## 1. Near-Term Goals (v1.3.0)

- [ ] **Multi-Category Super Mode Tabs**:
  - Add sub-category chips (Trending, Movies, TV Shows, Anime, Documentaries) with fast D-pad tab switching.
  - User-configurable TMDB genres in Settings.
- [ ] **Per-App Custom Banner Art**:
  - Allow users to override low-res or portrait banners for sideloaded mobile APKs using local storage images or custom TMDB/TVDB banner URLs.
- [ ] **HDMI Input Renaming & Custom Icons**:
  - Allow renaming "HDMI 1" to "PlayStation 5", "HDMI 2" to "Apple TV", "HDMI 3" to "Nintendo Switch", etc.
- [ ] **Screensaver / Ambient Mode**:
  - Material 3 ambient clock screensaver with uncompressed 4K photography backgrounds (`ClockDream.java`).

---

## 2. Mid-Term Goals (v1.4.0 - v1.5.0)

- [ ] **Adaptive Audio Feedback**:
  - Subtle TV sound effects for D-pad navigation clicks, card focus spring pops, and reorder drop feedback (toggleable in Settings).
- [ ] **Parental Control / App Lock**:
  - Numeric PIN lock for selected apps or Settings access.
- [ ] **Integrated System Performance Monitor**:
  - Lean overlay showing live CPU temperature, free RAM, and storage health.
- [ ] **Backup & Sync Profile**:
  - Export launcher configurations (pinned apps, custom order, TMDB API key, weather city) to a single JSON file on local flash drive or USB storage.

---

## 3. Long-Term Architectural Goals

- [ ] **Zero-Dependency Rust ADB Engine**:
  - Optional standalone single-binary debloater written in Rust for zero-Python systems.
- [ ] **Wide Color Gamut (DCI-P3 / BT.2020) & HDR Support**:
  - Native 10-bit color profile support on HDR10/Dolby Vision TV panels.
- [ ] **Multi-Profile Support**:
  - Fast profile switching for kids and adults with separate home shelves and recommendation feeds.
