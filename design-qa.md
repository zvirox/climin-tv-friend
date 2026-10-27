# Design QA: Win 8.1 + M3 Expressive Home

## Current implementation

The revised home uses a dedicated clock/action header, a three-row horizontal app/content/weather canvas, and an independent bottom row. The bottom row holds HDMI 1/2/3, USB, AV, UI Settings, Search, and TV Settings. Header quick-action circles remain visible. The user’s separation correction takes precedence over the attached plan’s all-in-one-grid input suggestion.

## Xiaomi TV verification

The release APK was installed with `adb install -r` on the paired **MiTV-AXSO0 (dangal)** at **1280×720 px, 213 dpi**. App data was retained and no other TV apps were removed.

| Check | Result | Evidence |
|---|---|---|
| Home hierarchy | Passed: clock/date/greeting and header controls stay above the grid; input/utility cards are in a separate bottom strip. | `/tmp/climin-final-home.xml`, `/tmp/climin-final-home.png` |
| Horizontal grid | Passed: apps, weather and Super Mode recommendations render across three rows; D-pad Right moved from YouTube to Prime Video. | `/tmp/climin-focus-right.xml`, `/tmp/climin-super-latest.png` |
| Bottom actions | Passed: HDMI 1/2/3, USB, AV, UI Settings, Search and TV Settings are visible below the grid. UI Settings opens the split-pane settings screen. | `/tmp/climin-final-home.png`, `/tmp/climin-settings-grid.png` |
| Super Mode | Passed: enabling it displays interleaved recommendation cards with title/year/rating/provider; live artwork depends on network/cache availability. | `/tmp/climin-super-latest.png` |
| Minimal Mode | Passed: recommendations disappear while app tiles and weather remain. The TV was returned to Minimal Mode. | `/tmp/climin-minimal-latest.png` |
| Settings dismissal | Passed in the latest capture: header, tile grid, weather and bottom labels remain rendered after closing settings. | `/tmp/climin-settings-dismiss-final.png` |

The TV accessibility bridge intermittently returned an empty root during repeated dumps; screenshots and successful dumps were used where available. Some remote artwork URLs were unavailable, so those recommendation cards use a flat tile fallback.

## Build and code checks

- `assembleRelease` succeeded after the layout and focus changes.
- `git diff --check` is run after the final edits.
- No tests were added or run. The requested manual device checks were performed.
- Existing working-tree changes were preserved; no commit was created.
