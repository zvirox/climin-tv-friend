# Graph Report - tools  (2026-10-27)

## Corpus Check
- Corpus is ~36,046 words - fits in a single context window. You may not need a graph.

## Summary
- 497 nodes · 995 edges · 18 communities (14 shown, 4 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 34 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Flamechart Drawing & Rendering
- Benchmarking & System Uptime
- Wallpapers & Scene Math
- Package Management & Addons
- Base64 Push & Payload Transfer
- TV Environment Sanity Checks
- ADB Connection State & Fallback
- Table Generation & Adoption Metrics
- Perfetto & Trace Tooling Guides
- ADB Device Client Connection
- ADB Shell & Python RSA Auth
- Privacy Audit & Data Sanitization
- Bash Theme Deployment
- Package Disabling Shell Helpers
- PowerShell Theme Staging
- Restore Script MSYS Handlers

## God Nodes (most connected - your core abstractions)
1. `grad()` - 17 edges
2. `svg()` - 16 edges
3. `main()` - 15 edges
4. `TV` - 14 edges
5. `radial()` - 13 edges
6. `save()` - 12 edges
7. `hills()` - 12 edges
8. `render()` - 11 edges
9. `ridge()` - 11 edges
10. `main()` - 10 edges

## Surprising Connections (you probably didn't know these)
- `pick()` --indirect_call--> `ordered()`  [INFERRED]
  bench.py → gen_tables.py
- `screendiff.py Visual Invariance Verifier` --semantically_similar_to--> `flamechart.py Performance Visualization`  [INFERRED] [semantically similar]
  screendiff.md → flamechart.md
- `relay.py ADB Port Relay` --conceptually_related_to--> `adb-shell Library (adb-shell==0.4.4)`  [INFERRED]
  relay.md → requirements.txt
- `Perfetto Python Library (perfetto==0.58.2)` --references--> `trace_report.py Latency Reporter`  [EXTRACTED]
  requirements-perf.txt → trace.md
- `home_activities()` --calls--> `shell()`  [EXTRACTED]
  check.py → recovery.py

## Import Cycles
- None detected.

## Communities (18 total, 4 thin omitted)

### Community 0 - "Flamechart Drawing & Rendering"
Cohesion: 0.07
Nodes (33): colour(), draw(), highlighted(), lane_height(), lanes(), main(), median_run(), parse() (+25 more)

### Community 1 - "Benchmarking & System Uptime"
Cohesion: 0.06
Nodes (19): accessibility_services(), android_uptime(), clear_log(), cold_start(), frame_rows(), idle_cpu(), key_event_time(), launcher_pid() (+11 more)

### Community 2 - "Wallpapers & Scene Math"
Cohesion: 0.10
Nodes (23): above_clouds(), alpenglow(), aurora(), autumn_valley(), balloon(), balloon_sunrise(), city_lights(), dunes() (+15 more)

### Community 3 - "Package Management & Addons"
Cohesion: 0.09
Nodes (26): addons(), cmd_install(), cmd_list(), cmd_remove(), cmd_set(), cmd_show(), cmd_status(), find() (+18 more)

### Community 4 - "Base64 Push & Payload Transfer"
Cohesion: 0.08
Nodes (18): main(), q(), safe_remote(), create_ledger(), ensure_state_dir(), list_ledgers(), load_ledger(), _field() (+10 more)

### Community 5 - "TV Environment Sanity Checks"
Cohesion: 0.10
Nodes (25): asks_for_input_hardware(), expected_launcher(), fix(), handler_apps(), home_activities(), home_of(), inspect(), launcher_to_use() (+17 more)

### Community 6 - "ADB Connection State & Fallback"
Cohesion: 0.08
Nodes (15): main(), split_host_port(), calendar(), instant(), main(), main(), pipe(), serve() (+7 more)

### Community 7 - "Table Generation & Adoption Metrics"
Cohesion: 0.11
Nodes (22): adopt(), between(), brand_lines(), brands(), check_cell(), counts(), examples_rows(), folders() (+14 more)

### Community 8 - "Perfetto & Trace Tooling Guides"
Cohesion: 0.07
Nodes (21): Flamechart Tool Guide, flamechart.py Performance Visualization, Two-Chart Pattern for Home Changes, Relay Tool Guide, relay.py ADB Port Relay, adb-shell Library (adb-shell==0.4.4), Cryptography Library (cryptography>=46.0.7), Core ADB Python Requirements (+13 more)

### Community 9 - "ADB Device Client Connection"
Cohesion: 0.08
Nodes (6): AdbDeviceConnection, generate_rsa_keypair(), get_default_key_paths(), check_setupwraith_wifi_ownership(), is_core_package(), validate_package_list()

### Community 10 - "ADB Shell & Python RSA Auth"
Cohesion: 0.14
Nodes (10): check_arity(), connect(), get_signer(), keyevent(), main(), restrict(), screenshot(), screenshot_args() (+2 more)

### Community 11 - "Privacy Audit & Data Sanitization"
Cohesion: 0.18
Nodes (13): audit(), classify(), connect(), list_lines(), main(), print_report(), probe(), query_packages() (+5 more)

### Community 12 - "Bash Theme Deployment"
Cohesion: 0.33
Nodes (3): MSYS_NO_PATHCONV, theme.sh script, tv()

### Community 13 - "Package Disabling Shell Helpers"
Cohesion: 0.60
Nodes (4): drop_pkg(), home_launchers(), MSYS_NO_PATHCONV, disable.sh script

## Knowledge Gaps
- **9 isolated node(s):** `MSYS_NO_PATHCONV`, `MSYS_NO_PATHCONV`, `MSYS_NO_PATHCONV`, `Two-Chart Pattern for Home Changes`, `Relay Tool Guide` (+4 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 201 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `MSYS_NO_PATHCONV`, `MSYS_NO_PATHCONV`, `MSYS_NO_PATHCONV` to the rest of the system?**
  _9 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Flamechart Drawing & Rendering` be split into smaller, more focused modules?**
  _Cohesion score 0.06654567453115548 - nodes in this community are weakly interconnected._
- **Should `Benchmarking & System Uptime` be split into smaller, more focused modules?**
  _Cohesion score 0.05584415584415584 - nodes in this community are weakly interconnected._
- **Should `Wallpapers & Scene Math` be split into smaller, more focused modules?**
  _Cohesion score 0.10431372549019607 - nodes in this community are weakly interconnected._
- **Should `Package Management & Addons` be split into smaller, more focused modules?**
  _Cohesion score 0.0935374149659864 - nodes in this community are weakly interconnected._
- **Should `Base64 Push & Payload Transfer` be split into smaller, more focused modules?**
  _Cohesion score 0.08013937282229965 - nodes in this community are weakly interconnected._
- **Should `TV Environment Sanity Checks` be split into smaller, more focused modules?**
  _Cohesion score 0.10256410256410256 - nodes in this community are weakly interconnected._