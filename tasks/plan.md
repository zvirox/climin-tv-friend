# Implementation Plan: Win 8.1 Start Menu + M3 Expressive (climin_tv_win81_m3_expressive_plan.md)

## Overview
Finish the in-progress redesign of the Climin TV launcher into a Windows 8.1-style
horizontally scrolling Start screen: dedicated header band, 3-row spanned grid with
interleaved app / content / weather / input tiles, full-card icons, 3D flip content
refresh, and the three attached palettes.

The repo already contains (uncommitted) work toward this plan. This plan covers the
**remaining gaps**, verified against `docs` plan §6 acceptance checklist.

## Already done (verified in working tree)
- Header band with vertical clock (no seconds), date, greeting, 5 circular buttons.
- 3-row horizontal `GridLayoutManager` Start grid + `StartGridAdapter`.
- Three palettes + Metro swatches in `ThemeManager`.
- Settings dialog redesign; `dialog_m3_all_apps` removed.
- Reorder/resize flow (hold OK).
- `MotionHelper.flipLiveTile` 3D flip helper (camera distance, midpoint swap).

## Gaps to close
1. Tile engine deviates from plan §5.2: no 2x2/1x2 content shapes, no minimal-mode
   reflow semantics, no TV-input tiles, weather not icon-first.
2. TV inputs (HDMI 1/2/3, USB, AV) still live in a bottom `HorizontalScrollView`
   bar — plan requires them as tiles inside the 3-row grid.
3. MainActivity carries ~500 lines of dead wiring (columns, featured/center cards,
   weather widget, forecast chips, bottom menu) and never re-renders the grid when
   Super Mode toggles; the periodic 3D flip animates a now-removed view.
4. Media tiles use flat text strips instead of gradient scrim + provider pill.
5. Header/grid metrics don't match plan §2 (≈210dp header, 52sp clock, 48dp insets,
   96dp grid end padding with peek).

## Architecture Decisions
- Keep `GridLayoutManager(horizontal, spanCount=3)` + per-tile row spans via
  `SpanSizeLookup`; column widths via adapter `LayoutParams` (columnSpan).
  A custom LayoutManager adds risk with no acceptance benefit.
- Use the framework's default RecyclerView arrow-key focus search (remove custom
  ±spanCount navigation, which breaks on spanned tiles).
- Inputs are 1x1 tiles with icon + small bottom label (labels allowed for inputs;
  app cards stay text-free per plan).
- App tile sizes come from persisted `AppEntry.tileSize` (defaults already follow
  the plan's minimal-mode pattern: 2x2, 2x1, 1x1…), preserving user reorder/resize.

## Task List

### Phase 1: Grid engine & models
- [ ] Task 1: Tile engine — media shape cycle 1x2 tall / 2x2 square / 2x1 wide in
      Super Mode; minimal mode reflow semantics; input tiles; weather 2x1; cache the
      packed list in MainActivity (stop re-packing in SpanSizeLookup).
- [ ] Task 2: `StartGridTile` INPUT type + input tile list in MainActivity.

### Phase 2: Rendering
- [ ] Task 3: Adapter — input tile binding (icon + label), gradient scrim +
      provider pill on media tiles, weather tile with icon, palette-driven focus
      stroke; framework default focus navigation.

### Checkpoint: build passes, logic reviewed

### Phase 3: Wiring & layout
- [ ] Task 4: MainActivity — remove dead code, fold inputs into grid, re-render on
      Super Mode toggle, retarget periodic 3D flip to a grid media tile, wire
      btn_circle_inputs to first input tile.
- [ ] Task 5: activity_main.xml — header ≈210dp with 52sp clock, plan §2 paddings,
      remove bottom menu row; prune dead styles.

### Checkpoint: acceptance checklist §6 walk-through + build

## Risks
| Risk | Impact | Mitigation |
|------|--------|------------|
| No device attached for visual verification | Med | Keep changes mechanical; compile + review; user installs APK |
| Focus regression from removing custom nav | Med | Framework GridLayoutManager focus search handles spans natively |

## Open Questions
- Settings dialog still hardcodes dark colors in light palette (pre-existing);
  left as-is unless requested.
