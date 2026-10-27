# TODO: Win 8.1 Start Menu + M3 Expressive completion

Source plan: `tasks/plan.md` (from `climin_tv_win81_m3_expressive_plan.md`)

## Phase 1: Grid engine & models
- [x] Task 1: Rework `Win8DynamicTileEngine.pack` (media 1x2/2x2/2x1 cycle, minimal reflow, inputs, weather) + cache packed tiles in MainActivity
- [x] Task 2: `StartGridTile` INPUT type + input list (`loadTvInputs`, real TIF inputs with static fallback)

## Phase 2: Rendering
- [x] Task 3: `StartGridAdapter` — input tiles (icon + label), gradient scrim + provider pill, weather icon, palette-driven focus stroke + tile colors

## Checkpoint
- [x] `./gradlew compileDebugJavaWithJavac` passes

## Phase 3: Wiring & layout
- [x] Task 4: MainActivity dead-code removal (columns / featured & center cards / weather widget / forecast chips / bottom-menu chips), inputs-in-grid, Super Mode toggle re-render, 3D flip retargeted to grid media tile, `btn_circle_inputs` focuses first input tile
- [x] Task 5: `activity_main.xml` — 210dp header, 52sp clock, 48dp header insets, grid end-padding 96dp with peek, bottom menu removed; dead `BottomMenu*` styles pruned

## Final checkpoint
- [x] Build passes (`assembleDebug` → `app-debug.apk`)
- [x] Acceptance checklist (plan §6) walk-through — see `tasks/plan.md` risks for the two judgment calls
- [x] `graphify update .`

## Follow-up: 4-row grid + reference spacing (user request)
- [x] `ROW_COUNT` 3 → 4 (`Win8DynamicTileEngine`)
- [x] Tile margins 5dp → 8dp per side (clear uniform gaps, no overlap); focus scale 1.06 → 1.04 so scaled tiles never touch
- [x] Input tiles: Minimal Mode first two inputs render 2x1 wide (reference HDMI 1/2 treatment), Super Mode all 1x1; icon+label centered as a group; swatch-rotation colors (`getTileColor(position + 3)`)
- [x] Grid `paddingTop=8dp` / `paddingBottom=24dp` (`activity_main.xml`)
- [x] Built `assembleRelease`, installed via `TV_HOST=192.168.31.247 STOCK_LAUNCHER=none ./build-and-install.sh`, verified on TV: 4-row Minimal + Super Mode, wide inputs, no overlap, crash buffer empty; Super Mode restored off

## Follow-up 2: equal gaps all sides + flashing fix (user request)
- [x] Vertical gap missing: tile lp.height filled the whole row band so top/bottom margins collapsed. Fix: root view fills the band (focus/click target, span sizing), visible card is an inner `FrameLayout` with equal 8dp margins on all four sides — spacing now uniform in both directions; focus scale animates the card only
- [x] Content-card flashing: `renderStartGrid` was hooked to every RecyclerView layout pass; `notifyDataSetChanged` → layout → re-render loop re-cleared and reloaded every image. Fix: layout listener fires only on size change; `setTiles` skips rebinding when the packed grid + cell metrics are identical
- [x] Verified on TV: equal gaps in Minimal + Super Mode, consecutive screenshots byte-identical (no flicker), 9s flip swaps content without touching other tiles, crash buffer empty

## Parking lot (not required by plan §6)
- Unused legacy adapters (`DockAdapter`, `AppGridAdapter`, `InputChipAdapter`, `SuperModeAdapter`) and their item layouts could be deleted in a follow-up.
- Settings dialog still hardcodes dark colors in light palette (pre-existing).
