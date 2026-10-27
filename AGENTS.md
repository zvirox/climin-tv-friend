# Project rules — read first

`RULES.md` in the repository root is the binding rule book for this project.
It freezes the launcher's working behaviour (animations, icons, app cards,
navigation, launch flows, system integration, settings/state) and allows only
UI work and new features without explicit per-change approval.

**Before any edit: read `RULES.md`.** If a requested UI change needs a protected
component, stop and use the approval template in `RULES.md` §4.

`UI_MAP.md` is the section-by-section map of every screen (home, drawer, All Apps,
Home Settings) with coordinates, states and the code that draws each part. Use its
section IDs (`HOME.CARDS.WIDE`, `HOME.DRAWER.WEATHER`, `SHEET.GRID`,
`SETTINGS.PREFS`, …) to locate anything the user names; update the map whenever a
change lands.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

When the user types `/graphify`, use the installed graphify skill or instructions before doing anything else.

Rules:
- When graphify-out/graph.json exists, run `graphify query "<question>"` before your first grep, search or source-file read for a codebase question, and put this rule in every subagent prompt that explores code. Read raw files only for lines the graph or an earlier read already located. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- Dirty graphify-out/ files are expected after hooks or incremental updates; dirty graph files are not a reason to skip graphify. Only skip graphify if the task is about stale or incorrect graph output, or the user explicitly says not to use it.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
- A committed graphify-out/graph.json is rewritten whole on every build, so a large diff to it in a pull request is expected. Do not flag it as churn or suggest skipping the graph rebuild or its hooks.
