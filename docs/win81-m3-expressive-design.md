# Windows 8.1 + Material 3 Expressive Home Redesign

**Status:** Design approved; first implementation slice complete  
**Date:** 2026-10-26  
**Reference plan:** `/home/zvirox/Downloads/climin_tv_win81_m3_expressive_plan.md`  
**Visual references:** `images/tv/screenshot/tv_win8_mockup_baseline.png` and the two user-supplied home and settings mockups.

## Understanding and scope

- Redesign Climin TV Friend as a remote-first Android TV launcher using the supplied mockups as the visual baseline and the attached plan for behavior and content.
- Use a dedicated top header for the vertical clock/date/greeting and quick-action circles, followed by the horizontally scrolling app/content grid.
- Keep hardware input cards and menu/utility cards in a separate bottom row below the app/content grid. The header's TV, palette, Wi-Fi, Bluetooth, and TV Settings circles remain available as quick actions.
- All installed applications remain directly available on Home as icon-only tiles. Selecting an app launches it; no separate All Apps page or modal is part of the target experience.
- Super Mode adds media recommendation tiles. Minimal Mode hides those tiles while retaining the clock, weather, apps, and system inputs.
- Settings use the mockup's split-pane layout and expose launcher controls and the three supplied color palettes.
- Preserve D-pad navigation, focus visibility, focus restoration, and long-press OK reorder/resize behavior.
- Preserve existing uncommitted project changes and build on them after auditing their implementation.

## Assumptions and non-functional requirements

- Target use is landscape Android TV at the project's supported Android versions and device capabilities; maintain the existing Java/XML stack and avoid new dependencies unless implementation evidence requires one.
- App inventory size varies by device. The app tile area must remain scrollable and every installed app must remain reachable by remote navigation.
- Keep existing weather and TMDB data sources, stored city preference, and privacy/network behavior. No new personal data collection is in scope.
- Network content failures must not block app launch, TV inputs, settings, or the rest of Home. Show a local fallback in the affected content area.
- Keep rendering and remote input responsive on lower-powered TV hardware. Do not refresh invisible or focused recommendation cards; pause periodic motion when Super Mode is inactive or the screen is inactive.
- Expose app names to accessibility services even though visible app tiles remain icon-only.

## Design contract

### Home

- **Header band:** vertical hours/minutes clock without seconds, full date, time-of-day and saved-location greeting on the left; TV input, palette, Wi-Fi, Bluetooth, and TV Settings circles on the right.
- **Main canvas:** a three-row horizontally scrolling grid interleaves icon-only app tiles, in-card media recommendations in Super Mode, and weather in both modes.
- **Separate bottom row:** HDMI 1/2/3, USB, AV, UI Settings, Search, and TV Settings. These cards stay outside the scrolling content grid. Header quick actions remain separate and visible.
- Recommendation metadata (title, year, rating, provider) stays inside each media card. Media cards use the plan's flat rounded tile treatment and requested image refresh flip.
- Super Mode shows recommendations. Minimal Mode hides them and reflows remaining content. Weather and apps remain available in both modes.

### Settings and palette behavior

- Use the supplied split-pane composition: palette/status information in the sidebar and grouped launcher controls in the main pane.
- Make palette selection immediate and persistent using the existing settings model.
- Support Crimson Wine Dark (`#000000`, `#1F1F1F`, `#6E6E6E`, `#6C0010`, `#350008`), Cyber Teal & Oxblood Dark (`#007A7A`, `#004040`, `#000000`, `#3D0000`, `#7A0000`), and Honeydew & Prussian Light (`#E63946`, `#F1FAEE`, `#A8DADC`, `#457B9D`, `#1D3557`).
- Adaptive app color may tint app tiles while the selected palette remains the overall visual foundation.

### Remote interaction and motion

- D-pad focus is visible and operable without relying on color alone. OK activates a focused app or control; Back closes Settings and restores focus to its opener.
- If a mode change removes the focused recommendation, move focus to the nearest remaining visible control.
- Long-press OK enters tile reorder/resize mode with clear feedback and a Back/cancel path.
- App and content tiles use compact one- or two-column widths inside the three-row horizontal grid. The grid stays within its header and bottom-row boundaries. Hardware inputs and menu cards do not consume grid cells.
- Implement the plan's focus scaling and 3D recommendation flip only on visible, unfocused cards. Periodic flips pause when Super Mode or the screen is inactive.
- Respect Android accessibility naming for icon-only app tiles and do not intercept normal text-input behaviors.

## Implementation slices

1. Audit the existing working tree and map already completed requirements to the acceptance criteria. Preserve all existing uncommitted files and changes.
2. Reconcile Home composition, app visibility, input placement, clock, weather, and focus navigation with the design contract.
3. Reconcile the settings split pane, palette persistence, adaptive coloring, and toggle behavior.
4. Reconcile recommendation metadata, artwork quality, refresh behavior, and motion constraints.
5. Build and review the complete flow; capture Home and Settings at a TV viewport and compare each with its corresponding supplied mockup. Fix actionable visual and interaction gaps before handoff.

## Verification and acceptance

- Verify all ten acceptance criteria in the reference plan, including no separate app page, icon-only apps, in-card metadata, tile geometry/spans, card flips, vertical clock/date/greeting, all three palettes, settings behavior, crisp assets, and D-pad navigation.
- Inspect both rendered screens against the mockups at matched viewport and state. Normalize crops and image density, use a combined visual comparison input, and record screenshot paths and dimensions in `design-qa.md`.
- Review code changes for correctness, readability, architecture, security, performance, and accessibility. Report any remaining visual differences as accepted, expected, or actionable.

## Risks

- The app inventory may exceed the visible right-side grid and make D-pad focus traversal cumbersome; scrolling must keep every tile reachable without trapping focus.
- Three columns plus a prominent input row may exceed available height on lower-resolution TV displays; scrolling must preserve access to persistent actions.
- Periodic 3D motion and spring focus treatment can compete with remote navigation or consume resources; motion must pause under the conditions defined above.
- The existing working tree already contains substantial changes in the same feature areas. Their ownership and completeness must be reviewed before further code edits.

## Decision log

| Decision | Alternatives considered | Reason |
|---|---|---|
| Use a clear header, interleaved three-row content grid, and separate bottom input/utility row. | Put inputs or menu cards among apps and recommendations. | Applies the user's correction while preserving the plan's horizontal Start screen. |
| Keep all installed apps on Home as icon-only tiles in the three-row horizontal grid. | Keep an All Apps page/modal or limit the Home set. | Keeps every installed app directly reachable on the Start screen. |
| Show recommendations only in Super Mode; keep apps, clock, and weather in both modes. | Remove more content in Minimal Mode or retain recommendations. | Matches the plan's mode behavior while preserving core launcher utility. |
| Treat the chosen palette as the overall foundation; adaptive colors may tint app tiles. | Let extracted app colors replace the chosen palette. | Preserves user control of the visual theme while retaining adaptive tile color. |
| Reuse existing Java/XML, adapters, repositories, preferences, and image loading. | Introduce a new state framework or media dependency. | Fits the current project architecture and avoids unnecessary dependencies. |
| Preserve existing uncommitted changes and audit before editing overlapping files. | Reset or replace the working tree. | The repository contains user work that must not be discarded or silently overwritten. |
| Limit requested animation to visible, unfocused recommendation tiles and pause when inactive. | Animate all tiles continuously. | Protects remote usability and performance on TV hardware. |

## Current task state

The `main` branch was already dirty when this task began, with changes across `MainActivity`, adapters, theme and preference utilities, image loading, layouts, Gradle properties, and new assets. Those changes were retained; no commit was created.

This task added the corrected header/grid/bottom-row structure, a three-row interleaved app/content/weather feed, D-pad grid stepping and focus restoration, app tile size persistence, reorder/resize feedback, and the split-pane settings UI. The unused All Apps dialog layout was removed to match the Home-only app access decision. Build and visual QA results are recorded in `design-qa.md`.

The current implementation follows the corrected macro layout. Xiaomi TV testing verified the separate bottom row, Settings entry and dismissal, a horizontal D-pad move, and both Super and Minimal Mode. Live recommendation artwork depends on the configured TMDB/network source. Captures were made on the Xiaomi TV at 1280×720 (213 dpi).
