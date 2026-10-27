# Graph Report - climin-tv-friend  (2026-10-27)

## Corpus Check
- 526 files · ~435,680 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 77 file(s) not represented in the graph (top: .xml 67, (none) 3, .ttf 2)

## Summary
- 2206 nodes · 5375 edges · 139 communities (84 shown, 55 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 283 edges (avg confidence: 0.87)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d8e999b9`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- trace_report.py
- bench.py
- scenes_wallpapers.py
- sys
- os
- HomeApps
- race_calendar.py
- gen_tables.py
- Perfetto & Trace Tooling Guides
- adb_client.py
- tv.py
- HomeView
- android.graphics.Canvas
- Windows 8.1 + Material 3 Expressive Home Redesign
- Prefs
- HomeMode
- recovery.py
- android.content.Context
- .SettingsView
- Super General (CVTE TP.SK708D.PC821, Android 9): notes
- Pick
- 6. SETTINGS — Home Settings app (`SettingsActivity`)
- Recs
- Spoken.java
- TCL Q7C (G08, Google TV, Android 14), European firmware
- with
- Override
- SleepOverlay.java
- Screensaver
- flamechart.py
- argparse
- HomeActivity
- Ui
- privacy.py
- Photos
- SettingsView
- SettingsActivity
- readme_card.py
- RemoteKeyService
- PhotoInbox
- Scene
- .onSizeChanged
- TCL 32L5A (Android TV 11)
- HomeData
- Implementation Plan: Win 8.1 Start Menu + M3 Expressive (climin_tv_win81_m3_expressive_plan.md)
- AdbDeviceConnection
- Place
- Adb
- Features & Highlights
- TCL 55C725
- TCL Smart TV Pro G09 (Google TV, Android 12)
- Climin TV — Project Rule Book
- .drawContent
- Anker Nebula D2426 (Android TV 14): debloated, with an Apple TV style home
- BYINTEK Love U4, 1080P (Android 9 projector)
- Motorola MT4310 (43", Newsan / Hisense hayward, Android TV 11)
- TCL G03 / BeyondTV4 (Google TV 11): 43C645, 50C635, 60C635
- TCL 65" C7L Google TV
- Recording
- Climin TV Friend: Android TV Debloater & Climin UI Launcher
- TODO: Win 8.1 Start Menu + M3 Expressive completion
- Palette
- Anker Nebula Mars 3 Air (Google TV 14): debloated, with an Apple TV style home
- Epson LS800 (Android TV 14 laser projector)
- Freebox Player POP
- Philips Google TV TA7 (Brazil)
- Sber Salute TV 43" (StarOS on Android 13)
- XGIMI MoGo 4 (Google TV 14 portable projector)
- .onStartJob
- README.md
- Climin TV Friend: AI Agent Prompting Directives & System Prompts
- BenQ GV32 (Google TV 14 portable projector)
- RCA RC50A21S-4KSM (Android TV 9, Toptech/Zeasn firmware, Realtek RTD2851P)
- Skyworth SWTV-22AE-FHD (umeda)
- launcher/build-and-install.sh
- DreamView
- Acer AR55AR2851UDPRO 55" (Android TV 14): debloated, with the launcher on the Home button
- Skyworth 7S60_A5 (Coocaa OS, China market, Android 8)
- 4. Installation Guide
- theme.sh
- AQUA MatrixTV EE 50" 4K (Google TV 14, omonia)
- Haier MatrixTV GE / HR9216EU (Google TV 14, haeundae)
- Hisense U75 / 75U75N (hengshan), Google TV 12
- Lumio Vision 7 (55" / 139 cm)
- NVIDIA SHIELD Android TV (2019) — codename `sif`
- TCL 75T7 (G08, Google TV, Android 14), US firmware
- Design QA: Win 8.1 + M3 Expressive Home
- Nokia Streaming Box 8000
- Grundig Android UHD TV (Arçelik, `kunyang`)
- Sankey SMART_TV (CVTE, HiSilicon Hi3751V350), Android 12: notes
- Sony BRAVIA KD-55XF7596 (Android TV 9)
- Sony BRAVIA KDL-43W950D
- tcl-43c645/build-and-install.sh
- TCL 43S450F-CA (TCL S4 Fire TV Edition, Fire OS 8.1.8.5)
- TCL 55C745 (2023, Google TV, Android 14)
- TCL Smart TV Pro (G08, Android 12), Australian firmware
- disable.sh
- TCL C7K (G08, Google TV, Android 14), UK and EU firmware
- TCL 75C845X2 (UK/Europe), Google TV 14
- Vu VIBE TV
- Xiaomi MiTV-MOOR2: Home redirect with Google TV kept enabled
- theme.ps1
- Project rules — read first
- anker-nebula-d2426/restore.sh
- epson-ls800/restore.sh
- Sony BRAVIA KD-43X8000G
- TCL 65EP68
- LumiFlow (Mi Box S theme)
- Config
- tools/restore.sh
- anker-nebula-d2426/build-and-install.sh
- hisense-65d62qh/README.md
- tcl-43c645/restore.sh
- tcl-g08-eu-android12/README.md
- copilot-instructions.md
- Climin TV Friend: Systems & Software Architecture
- TiVo Stream 4K
- android.content.Context
- AppLog
- Frost

## God Nodes (most connected - your core abstractions)
1. `HomeView` - 181 edges
2. `SettingsView` - 101 edges
3. `HomeApps` - 70 edges
4. `Recs` - 59 edges
5. `HomeMode` - 53 edges
6. `DreamView` - 50 edges
7. `Prefs` - 50 edges
8. `HomeData` - 42 edges
9. `Scene` - 40 edges
10. `Adb` - 38 edges

## Surprising Connections (you probably didn't know these)
- `2.1 View Hierarchy & Navigation Graph` --references--> `MainActivity`  [INFERRED]
  docs/architecture.md → launcher/app/src/main/java/io/climintv/home/MainActivity.java
- `Examples` --references--> `aurora()`  [INFERRED]
  examples/README.md → tools/scenes_wallpapers.py
- `0. Coordinate system` --references--> `HomeActivity`  [INFERRED]
  UI_MAP.md → launcher/app/src/main/java/io/climintv/home/HomeActivity.java
- `Current task state` --references--> `MainActivity`  [INFERRED]
  docs/win81-m3-expressive-design.md → launcher/app/src/main/java/io/climintv/home/MainActivity.java
- `The launcher` --references--> `MainActivity`  [INFERRED]
  examples/tivo-stream-4k/README.md → launcher/app/src/main/java/io/climintv/home/MainActivity.java

## Import Cycles
- None detected.

## Communities (139 total, 55 thin omitted)

### Community 0 - "trace_report.py"
Cohesion: 0.09
Nodes (27): lanes(), main(), median_run(), window(), compare(), expand(), fmt(), _has_outlier() (+19 more)

### Community 1 - "bench.py"
Cohesion: 0.06
Nodes (19): accessibility_services(), android_uptime(), clear_log(), cold_start(), frame_rows(), idle_cpu(), key_event_time(), launcher_pid() (+11 more)

### Community 2 - "scenes_wallpapers.py"
Cohesion: 0.10
Nodes (24): above_clouds(), alpenglow(), aurora(), autumn_valley(), balloon(), balloon_sunrise(), city_lights(), dunes() (+16 more)

### Community 3 - "sys"
Cohesion: 0.20
Nodes (22): addons(), cmd_install(), cmd_list(), cmd_remove(), cmd_set(), cmd_show(), cmd_status(), find() (+14 more)

### Community 4 - "os"
Cohesion: 0.13
Nodes (11): _field(), main(), other_app(), parse(), perfetto_config(), record_one(), recording_ms(), _run() (+3 more)

### Community 5 - "HomeApps"
Cohesion: 0.05
Nodes (11): Backdrop, HomeApps, Item, RemoteKeys, 3. HOME.DRAWER — open with ↓ from the shelf, Drawer navigation (frozen), HOME.BUTTONS — round glass buttons (top right), HOME.DRAWER.INPUTS — TV inputs (first group) (+3 more)

### Community 6 - "race_calendar.py"
Cohesion: 0.31
Nodes (3): calendar(), instant(), main()

### Community 7 - "gen_tables.py"
Cohesion: 0.11
Nodes (22): adopt(), between(), brand_lines(), brands(), check_cell(), counts(), examples_rows(), folders() (+14 more)

### Community 8 - "Perfetto & Trace Tooling Guides"
Cohesion: 0.07
Nodes (21): Flamechart Tool Guide, flamechart.py Performance Visualization, Two-Chart Pattern for Home Changes, Relay Tool Guide, relay.py ADB Port Relay, adb-shell Library (adb-shell==0.4.4), Cryptography Library (cryptography>=46.0.7), Core ADB Python Requirements (+13 more)

### Community 9 - "adb_client.py"
Cohesion: 0.09
Nodes (11): main(), split_host_port(), generate_rsa_keypair(), get_default_key_paths(), check_setupwraith_wifi_ownership(), is_core_package(), validate_package_list(), create_ledger() (+3 more)

### Community 10 - "tv.py"
Cohesion: 0.14
Nodes (10): check_arity(), connect(), get_signer(), keyevent(), main(), restrict(), screenshot(), screenshot_args() (+2 more)

### Community 11 - "HomeView"
Cohesion: 0.06
Nodes (5): Hero, HomeView, Layer, Snapshot, Spring

### Community 12 - "android.graphics.Canvas"
Cohesion: 0.12
Nodes (6): Shadows, Surfaces, 7. Cross-cutting tokens, 8. Where to change what (open surfaces), HOME.DRAWER.WEATHER — big horizontal card, HOME.SHELF — app canvas

### Community 13 - "Windows 8.1 + Material 3 Expressive Home Redesign"
Cohesion: 0.17
Nodes (11): Assumptions and non-functional requirements, Decision log, Design contract, Home, Implementation slices, Remote interaction and motion, Risks, Settings and palette behavior (+3 more)

### Community 14 - "Prefs"
Cohesion: 0.11
Nodes (3): Prefs, HOME.SLEEPTIMER — "Off in 45 min" chip, SETTINGS.THEME — mode and look

### Community 18 - "recovery.py"
Cohesion: 0.10
Nodes (25): asks_for_input_hardware(), expected_launcher(), fix(), handler_apps(), home_activities(), home_of(), inspect(), launcher_to_use() (+17 more)

### Community 20 - "android.content.Context"
Cohesion: 0.12
Nodes (3): SleepTimer, android.content.BroadcastReceiver, Watchdog

### Community 22 - "Super General (CVTE TP.SK708D.PC821, Android 9): notes"
Cohesion: 0.06
Nodes (26): Philips 50PUS8505: single-session TPM191E notes, Philips 55OLED854/12: single-session notes, What was checked, Examples, Home handover, Remote buttons after Google TV Home, RockTek GB1, Verification still needed before submitting upstream (+18 more)

### Community 25 - "6. SETTINGS — Home Settings app (`SettingsActivity`)"
Cohesion: 0.17
Nodes (12): 6. SETTINGS — Home Settings app (`SettingsActivity`), SETTINGS.ABOUT — about Climin UI, SETTINGS.BACKDROP — behind the panels, SETTINGS.GREETINGS — placeholder, SETTINGS.HOME_SETTINGS — Home Settings panel, SETTINGS.PANE — shared layout, SETTINGS.PICKER — long-list panel, SETTINGS.POWER — Sleep Timer (+4 more)

### Community 28 - "TCL Q7C (G08, Google TV, Android 14), European firmware"
Cohesion: 0.07
Nodes (28): Additional G08 AU02 unit: native shortcuts and held-volume repeat, AirPlay and Cast on this network, Connecting, Connecting: Wireless debugging only, and its port moves, Fifth unit: G08 EU/UK, OTA from Android 12, Fourth unit: 65T8C (Wi-Fi), Home without disabling setupwraith, Identify the firmware (+20 more)

### Community 29 - "with"
Cohesion: 0.07
Nodes (27): name, runs-on, steps, GITHUB_TOKEN, jobs, build-and-release, name, permissions (+19 more)

### Community 33 - "Screensaver"
Cohesion: 0.16
Nodes (3): Listener, Screensaver, ScreensaverAlone

### Community 34 - "flamechart.py"
Cohesion: 0.13
Nodes (9): main(), q(), safe_remote(), colour(), draw(), highlighted(), lane_height(), parse() (+1 more)

### Community 35 - "argparse"
Cohesion: 0.14
Nodes (10): main(), pipe(), serve(), box(), compare(), _compare(), main(), pair() (+2 more)

### Community 36 - "HomeActivity"
Cohesion: 0.13
Nodes (4): Click, FireHomeActivity, HomeActivity, Listener

### Community 39 - "privacy.py"
Cohesion: 0.18
Nodes (13): audit(), classify(), connect(), list_lines(), main(), print_report(), probe(), query_packages() (+5 more)

### Community 46 - "readme_card.py"
Cohesion: 0.12
Nodes (3): font_and_out(), font_and_out(), font_and_out()

### Community 51 - ".onSizeChanged"
Cohesion: 0.11
Nodes (13): 0. Coordinate system, 10. Amending this map, 1. State matrix — what is on canvas, 2. HOME — base screen (drawer closed), 4. SHEET — "All Apps" overlay, 5. HOME.TOAST — notification pill, 9. Re-verifying on the TV, Climin UI — Screen Map (+5 more)

### Community 52 - "TCL 32L5A (Android TV 11)"
Cohesion: 0.14
Nodes (13): 1. Overview, 2. Debloat Details, 3. How to Re-enable Any Disabled App, 4. How to Re-build and Re-install the Launcher, 5. How to Change Dock Order or App Pinnings, 6. How to Change or Set Wallpaper, 7. Optional Ad-blocking Private DNS Undo, 8. Closing Network ADB and Securing the TV (+5 more)

### Community 55 - "Implementation Plan: Win 8.1 Start Menu + M3 Expressive (climin_tv_win81_m3_expressive_plan.md)"
Cohesion: 0.14
Nodes (13): Already done (verified in working tree), Architecture Decisions, Checkpoint: acceptance checklist §6 walk-through + build, Checkpoint: build passes, logic reviewed, Gaps to close, Implementation Plan: Win 8.1 Start Menu + M3 Expressive (climin_tv_win81_m3_expressive_plan.md), Open Questions, Overview (+5 more)

### Community 58 - "Adb"
Cohesion: 0.14
Nodes (3): Adb, Approval, Message

### Community 59 - "Features & Highlights"
Cohesion: 0.18
Nodes (10): [0.0.1] - 2026-10-25 (Initial Stable Release), 1. Material 3 Expressive UI & Tactile Motion, 2. TMDB Super Mode (Streaming Content Recommendations), 3. Widescreen App Carousel & Full-Screen All Apps, 4. Zero-Key Live Weather Hub, 5. Tactile Material 3 Settings Menu, 6. Hardware Integrations & Safe Debloating, 7. Ultra-Lean Footprint (+2 more)

### Community 60 - "TCL 55C725"
Cohesion: 0.18
Nodes (10): Brazilian unit: the RC902V_M2 remote, Firmware quirks, Inputs, Install, Remote buttons: remapped, Resources, TCL 55C725, TCL 65C655 Pro: the BT_RC813_MA remote (+2 more)

### Community 61 - "TCL Smart TV Pro G09 (Google TV, Android 12)"
Cohesion: 0.18
Nodes (10): Change the dock order, `INPUT_KEY_SERVICE=1` and volume repeat, Install, from scratch or again, Notes for this set, Re-enable one app, Remote app buttons, Stock YouTube RAM reclamation, TCL Smart TV Pro G09 (Google TV, Android 12) (+2 more)

### Community 62 - "Climin TV — Project Rule Book"
Cohesion: 0.18
Nodes (10): 1. Prime directive, 2. Scope of a change, 3. Protected core (do not modify without approval), 4. Approval protocol, 5. Anti-drift rules, 6. Standard workflow for every change, 7. Definition of done (UI / feature work), 8. Rollback (+2 more)

### Community 63 - ".drawContent"
Cohesion: 0.13
Nodes (3): Glyphs, Quotes, Symbols

### Community 64 - "Anker Nebula D2426 (Android TV 14): debloated, with an Apple TV style home"
Cohesion: 0.20
Nodes (9): ADB, Anker Nebula D2426 (Android TV 14): debloated, with an Apple TV style home, Change the dock order or the dock apps, Change the wallpaper, Notes from this projector, Re-enable one app, Rebuild and reinstall the launcher, Undo everything (+1 more)

### Community 65 - "BYINTEK Love U4, 1080P (Android 9 projector)"
Cohesion: 0.20
Nodes (9): BYINTEK Love U4, 1080P (Android 9 projector), Change the dock order, Firmware recovery, Inputs, Install, from scratch or again, Optional, this firmware only: hide the Zeasn ad service, Turning ADB on, Undo everything (+1 more)

### Community 66 - "Motorola MT4310 (43", Newsan / Hisense hayward, Android TV 11)"
Cohesion: 0.20
Nodes (9): Change the dock order, Change the wallpaper, Connecting again, Motorola MT4310 (43", Newsan / Hisense hayward, Android TV 11), Notes from this TV, Re-enable one app, Rebuild / reinstall the launcher, Undo everything (+1 more)

### Community 67 - "TCL G03 / BeyondTV4 (Google TV 11): 43C645, 50C635, 60C635"
Cohesion: 0.20
Nodes (9): Change the dock order or the wallpaper, Quirks on this firmware (60C635), Re-enable one app, Remote buttons, TCL G03 / BeyondTV4 (Google TV 11): 43C645, 50C635, 60C635, Tested models, Verified, What is here (+1 more)

### Community 68 - "TCL 65" C7L Google TV"
Cohesion: 0.20
Nodes (9): After a firmware update, Apply the list and install the launcher, Change the dock order, Change the wallpaper, Connecting to the TV, Re-enable one app, TCL 65" C7L Google TV, Undo everything (+1 more)

### Community 70 - "Climin TV Friend: Android TV Debloater & Climin UI Launcher"
Cohesion: 0.20
Nodes (10): 1. System Topology, 2. Key Features, 3. Minimum Requirements, 5. TV Remote Controls Reference, 6. Testing & Quality Verification, 7. Documentation Directory, 8. License, Climin TV Friend: Android TV Debloater & Climin UI Launcher (+2 more)

### Community 71 - "TODO: Win 8.1 Start Menu + M3 Expressive completion"
Cohesion: 0.20
Nodes (9): Checkpoint, Final checkpoint, Follow-up 2: equal gaps all sides + flashing fix (user request), Follow-up: 4-row grid + reference spacing (user request), Parking lot (not required by plan §6), Phase 1: Grid engine & models, Phase 2: Rendering, Phase 3: Wiring & layout (+1 more)

### Community 72 - "Palette"
Cohesion: 0.21
Nodes (3): Look, Pair, Palette

### Community 73 - "Anker Nebula Mars 3 Air (Google TV 14): debloated, with an Apple TV style home"
Cohesion: 0.22
Nodes (8): ADB, Anker Nebula Mars 3 Air (Google TV 14): debloated, with an Apple TV style home, Notes from this projector, Re-enable one app, The dock, The quirks that cost the most time, Undo everything, What was changed

### Community 74 - "Epson LS800 (Android TV 14 laser projector)"
Cohesion: 0.22
Nodes (8): Change the dock or the wallpaper, Epson LS800 (Android TV 14 laser projector), Extras set on this unit, Install, from scratch or again, Re-enable an app, Remote buttons, Turning ADB on, When you are done

### Community 75 - "Freebox Player POP"
Cohesion: 0.22
Nodes (8): Ad-blocking DNS (off, on purpose), Change the dock or its order, Change the wallpaper, Freebox Player POP, Install, from scratch or again, Re-enable one app, Undo everything, When you're done: close ADB

### Community 76 - "Philips Google TV TA7 (Brazil)"
Cohesion: 0.22
Nodes (8): Change the dock order, Change the wallpaper, Close ADB when you're done, Philips Google TV TA7 (Brazil), Re-enable an app, Reinstall or update the launcher, Undo everything, What changed

### Community 77 - "Sber Salute TV 43" (StarOS on Android 13)"
Cohesion: 0.22
Nodes (8): App "rotation": why apps vanish on their own, Install, Launcher notes, Sber Salute TV 43" (StarOS on Android 13), The Sber platform service, Turning ADB on, Undo everything, What stays

### Community 78 - "XGIMI MoGo 4 (Google TV 14 portable projector)"
Cohesion: 0.22
Nodes (8): Inputs and hardware, Install, Notes on `config.json`, Remote buttons, The helper costs volume-key auto-repeat, The stock launcher does not stay disabled, Turning ADB on, XGIMI MoGo 4 (Google TV 14 portable projector)

### Community 81 - "README.md"
Cohesion: 0.15
Nodes (9): 1. Near-Term Goals (v1.3.0), 2. Mid-Term Goals (v1.4.0 - v1.5.0), 3. Long-Term Architectural Goals, Climin TV Friend: Product Roadmap & Future Milestones, 1. Quick Test Matrix, 2. Remote Navigation Keycode Automated Test Script, 3. Visual Quality & Screenshot Verification, 4. Debloater Safety Verification (+1 more)

### Community 82 - "Climin TV Friend: AI Agent Prompting Directives & System Prompts"
Cohesion: 0.15
Nodes (12): 1. Project Identity & Invariants, 2. Core Subsystems, 3. Agent Task Execution Workflows, 4. Prompt Log & Task History, 5. Maintenance Checklist for Agents, Climin TV Friend: AI Agent Prompting Directives & System Prompts, Non-Negotiable Engineering Invariants, Pattern 1: Implementing a New UI Feature (+4 more)

### Community 83 - "BenQ GV32 (Google TV 14 portable projector)"
Cohesion: 0.25
Nodes (7): BenQ GV32 (Google TV 14 portable projector), Change the dock or the wallpaper, Install, from scratch or again, Re-enable an app, Remote buttons ("RCI078/079", Bluetooth), Turning ADB on, When you are done

### Community 84 - "RCA RC50A21S-4KSM (Android TV 9, Toptech/Zeasn firmware, Realtek RTD2851P)"
Cohesion: 0.25
Nodes (7): Inputs: use RCA's own panel, Install, Other firmware quirks, RCA RC50A21S-4KSM (Android TV 9, Toptech/Zeasn firmware, Realtek RTD2851P), The remote's Input (Fuente/Source) button: optional, not tested with the merged options, Turning ADB on, Undo everything

### Community 85 - "Skyworth SWTV-22AE-FHD (umeda)"
Cohesion: 0.25
Nodes (7): Apply / Restore, Device info, Live TV, Notes on debloat, Skyworth SWTV-22AE-FHD (umeda), Stock launcher, Verified

### Community 86 - "launcher/build-and-install.sh"
Cohesion: 0.39
Nodes (7): ANDROID_HOME, MSYS_NO_PATHCONV, PATH, query_home(), rollback_home(), build-and-install.sh script, enable_service()

### Community 87 - "DreamView"
Cohesion: 0.13
Nodes (4): DreamView, Glass, Slide, Weather

### Community 88 - "Acer AR55AR2851UDPRO 55" (Android TV 14): debloated, with the launcher on the Home button"
Cohesion: 0.29
Nodes (6): Acer AR55AR2851UDPRO 55" (Android TV 14): debloated, with the launcher on the Home button, Connect, Debloat, Launcher, Remote (RC710), Undo

### Community 89 - "Skyworth 7S60_A5 (Coocaa OS, China market, Android 8)"
Cohesion: 0.29
Nodes (6): Inputs, Install, Known issues, Skyworth 7S60_A5 (Coocaa OS, China market, Android 8), Undo, Why Coocaa OS needs more than build-and-install.sh

### Community 90 - "4. Installation Guide"
Cohesion: 0.29
Nodes (7): 4. Installation Guide, Method 1: Automated Installation via AI Agent (Recommended), Method 2: Manual Installation via ADB (Normal App Mode for Testing), Method 3: Full Debloat & Default Home Launcher Installation, Method 4: Sideloading via USB Drive / File Transfer, Step 1: Enable Developer Options & USB Debugging on TV, Step 2: Build & Install Climin UI

### Community 91 - "theme.sh"
Cohesion: 0.33
Nodes (3): MSYS_NO_PATHCONV, theme.sh script, tv()

### Community 92 - "AQUA MatrixTV EE 50" 4K (Google TV 14, omonia)"
Cohesion: 0.33
Nodes (5): AQUA MatrixTV EE 50" 4K (Google TV 14, omonia), Install, from scratch or again, Quirks on this firmware, all checked on the TV, Re-enable one app, Undo everything

### Community 93 - "Haier MatrixTV GE / HR9216EU (Google TV 14, haeundae)"
Cohesion: 0.33
Nodes (5): Haier MatrixTV GE / HR9216EU (Google TV 14, haeundae), Install, from scratch or again, Quirks on this firmware, all checked on the TV, Re-enable one app, Undo everything

### Community 94 - "Hisense U75 / 75U75N (hengshan), Google TV 12"
Cohesion: 0.33
Nodes (5): Display, Hisense U75 / 75U75N (hengshan), Google TV 12, Live TV and inputs, Sound through a soundbar, The remote

### Community 95 - "Lumio Vision 7 (55" / 139 cm)"
Cohesion: 0.33
Nodes (5): config.xml notes, Getting in, Lumio Vision 7 (55" / 139 cm), Other notes, The one thing to know: the launcher needs setupwraith disabled

### Community 96 - "NVIDIA SHIELD Android TV (2019) — codename `sif`"
Cohesion: 0.33
Nodes (5): Notes, NVIDIA SHIELD Android TV (2019) — codename `sif`, Replacing the Home key is one step here, Restore, Tweaks applied

### Community 97 - "TCL 75T7 (G08, Google TV, Android 14), US firmware"
Cohesion: 0.33
Nodes (5): Notes for this set, Second unit: TCL 55Q77K, TCL 75T7 (G08, Google TV, Android 14), US firmware, The remote's buttons with launcherx disabled, What is different from the other G08 lists

### Community 99 - "Design QA: Win 8.1 + M3 Expressive Home"
Cohesion: 0.40
Nodes (4): Build and code checks, Current implementation, Design QA: Win 8.1 + M3 Expressive Home, Xiaomi TV verification

### Community 100 - "Nokia Streaming Box 8000"
Cohesion: 0.22
Nodes (7): Apply, Before you start again, Nokia Streaming Box 8000, Screensaver, Stock launcher, Undo, Why `tools/disable.sh` does not work here

### Community 101 - "Grundig Android UHD TV (Arçelik, `kunyang`)"
Cohesion: 0.40
Nodes (4): Grundig Android UHD TV (Arçelik, `kunyang`), Keep, Notes, What worked

### Community 102 - "Sankey SMART_TV (CVTE, HiSilicon Hi3751V350), Android 12: notes"
Cohesion: 0.40
Nodes (4): Sankey SMART_TV (CVTE, HiSilicon Hi3751V350), Android 12: notes, The reference launcher, Things this firmware does not have, Turn on ADB

### Community 103 - "Sony BRAVIA KD-55XF7596 (Android TV 9)"
Cohesion: 0.40
Nodes (4): Kept on purpose, Notes from this firmware, Sony BRAVIA KD-55XF7596 (Android TV 9), What was tested

### Community 104 - "Sony BRAVIA KDL-43W950D"
Cohesion: 0.40
Nodes (4): Confirmed on this unit, Launcher configuration, Not yet confirmed, Sony BRAVIA KDL-43W950D

### Community 105 - "tcl-43c645/build-and-install.sh"
Cohesion: 0.40
Nodes (4): ANDROID_HOME, JAVA_HOME, PATH, build-and-install.sh script

### Community 106 - "TCL 43S450F-CA (TCL S4 Fire TV Edition, Fire OS 8.1.8.5)"
Cohesion: 0.40
Nodes (4): Apply and undo, Notes, TCL 43S450F-CA (TCL S4 Fire TV Edition, Fire OS 8.1.8.5), What worked and what did not

### Community 107 - "TCL 55C745 (2023, Google TV, Android 14)"
Cohesion: 0.40
Nodes (4): Install, Notes for this set, TCL 55C745 (2023, Google TV, Android 14), Undo everything

### Community 108 - "TCL Smart TV Pro (G08, Android 12), Australian firmware"
Cohesion: 0.40
Nodes (4): Live TV without an aerial, Notes for this set, Remote buttons, TCL Smart TV Pro (G08, Android 12), Australian firmware

### Community 109 - "disable.sh"
Cohesion: 0.60
Nodes (4): drop_pkg(), home_launchers(), MSYS_NO_PATHCONV, disable.sh script

### Community 111 - "TCL C7K (G08, Google TV, Android 14), UK and EU firmware"
Cohesion: 0.50
Nodes (3): Install, Second unit: 50C7K, Poland, Ethernet, TCL C7K (G08, Google TV, Android 14), UK and EU firmware

### Community 112 - "TCL 75C845X2 (UK/Europe), Google TV 14"
Cohesion: 0.50
Nodes (3): Differences from the C755 list, Known issue: Settings > Accounts & profiles is empty, TCL 75C845X2 (UK/Europe), Google TV 14

### Community 113 - "Vu VIBE TV"
Cohesion: 0.50
Nodes (3): Keep / restore the stock Google TV launcher, Notes, Vu VIBE TV

### Community 114 - "Xiaomi MiTV-MOOR2: Home redirect with Google TV kept enabled"
Cohesion: 0.50
Nodes (3): Checks and access to Google TV, Configuration, Xiaomi MiTV-MOOR2: Home redirect with Google TV kept enabled

### Community 135 - "Climin TV Friend: Systems & Software Architecture"
Cohesion: 0.25
Nodes (8): 1. High-Level System Architecture, 2.1 View Hierarchy & Navigation Graph, 2.2 Navigation Guard (`TvScrollView`), 2.3 Motion & Tactile Physics (`FocusAnimator`), 2. Component Design: Climin UI, 3. High-DPI Sharpness & Scaling Pipeline, 4. Resource & Memory Budget, Climin TV Friend: Systems & Software Architecture

### Community 136 - "TiVo Stream 4K"
Cohesion: 0.25
Nodes (7): Current task state, Apply, The launcher, TiVo Stream 4K, Undo, Why `tools/disable.sh` does not work here, MainActivity

## Knowledge Gaps
- **15 isolated node(s):** `JAVA_HOME`, `ANDROID_HOME`, `PATH`, `MSYS_NO_PATHCONV`, `ANDROID_HOME` (+10 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 740 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **55 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `HOME.BUTTONS — round glass buttons (top right)` connect `HomeApps` to `sys`, `android.graphics.Canvas`, `SettingsActivity`, `Prefs`, `.onSizeChanged`?**
  _High betweenness centrality (0.158) - this node is a cross-community bridge._
- **What connects `JAVA_HOME`, `ANDROID_HOME`, `PATH` to the rest of the system?**
  _15 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `trace_report.py` be split into smaller, more focused modules?**
  _Cohesion score 0.08985200845665962 - nodes in this community are weakly interconnected._
- **Why does `tv()` connect `sys` to `HomeApps`?**
  _High betweenness centrality (0.158) - this node is a cross-community bridge._
- **Should `bench.py` be split into smaller, more focused modules?**
  _Cohesion score 0.05584415584415584 - nodes in this community are weakly interconnected._
- **Why does `HomeView` connect `HomeView` to `HomeApps`, `android.graphics.Canvas`, `Prefs`, `Recs.java`, `Pick`, `HomeView.java`, `Recs`, `Spoken.java`, `HomeApps.java`, `SleepOverlay.java`, `HomeActivity`, `Ui`, `.nightChanged`, `Scene`, `.retarget`, `.onSizeChanged`, `HomeData`, `.drawContent`, `Palette`, `DreamView`?**
  _High betweenness centrality (0.157) - this node is a cross-community bridge._
- **Should `scenes_wallpapers.py` be split into smaller, more focused modules?**
  _Cohesion score 0.09796806966618288 - nodes in this community are weakly interconnected._