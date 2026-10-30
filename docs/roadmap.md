# Your TV Friend: Product Roadmap & Future Milestones

This document tracks upcoming features, engineering milestones, and optimization targets for **Your TV Friend** and **Your UI**.

---

## 1. Near-Term Goals

- [ ] **More For You tabs**:
  - The row already has Explore / Movies / Shows; add genre chips (Anime, Documentaries, …) and let
    the genres be chosen in Settings instead of being fixed in `Recs`.
- [ ] **Per-App Custom Banner Art**:
  - Allow users to override low-res or portrait banners for sideloaded mobile APKs using local storage images or custom TMDB/TVDB banner URLs.
- [ ] **HDMI Input Renaming & Custom Icons**:
  - Allow renaming "HDMI 1" to "PlayStation 5", "HDMI 2" to "Apple TV", "HDMI 3" to "Nintendo Switch", etc.
- [ ] **Ambient Mode beyond the screensaver**:
  - Let the screensaver stay up as an ambient display (dimmed clock and photos) instead of exiting on
    the first key, where the TV's dream settings allow it.

---

## 2. Mid-Term Goals

- [x] **Audio Feedback** — shipped: focus and open clicks, synthesized at runtime (`Click`), toggleable
  in Settings › Home Settings › Navigation sounds.
- [ ] **More sound choices**: a second click set, and a distinct sound for reorder drop.
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
