# Graph Report - climin-tv-friend  (2026-10-26)

## Corpus Check
- 548 files · ~443,664 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 77 file(s) not represented in the graph (top: .xml 67, (none) 3, .ttf 2)

## Summary
- 2473 nodes · 6394 edges · 141 communities (81 shown, 60 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 370 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ee384510`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MainActivity
- Flamechart Performance Profiler
- Device Benchmark & System Telemetry
- Wallpaper & Scene Generators
- check.py
- Architecture & Memory Budgets
- HomeView
- AdbDeviceConnection
- TCL Smart TV Pro G09 (Google TV, Android 12)
- Super General (CVTE TP.SK708D.PC821, Android 9): notes
- GitHub Actions Release Pipeline
- java_legacy_backup/MainActivity.java
- SettingsView
- UI Screenshots & Screen Layouts
- trace.py
- re
- android.view.View
- Privacy & Telemetry Auditing
- tv.py
- sys
- HomeApps
- Prefs
- MediaItem
- Spoken
- android.graphics.Paint
- readme_card.py
- argparse
- Performance Tooling Documentation
- android.view.KeyEvent
- Nokia Streaming Box 8000
- Build & Install Shell Automation
- android.graphics.Canvas
- Theme Push Shell Script
- Android Launcher Mipmap Icon Asset
- OEM Debloat Shell Automation
- AI Agent Rules & Workflows
- Theme Push PowerShell Script
- OEM Restore Shell Script
- Outfit Font Typography License
- Python ADB Dependencies
- TCL 32L5A (Android TV 11)
- Windows 8.1 + Material 3 Expressive Home Redesign
- HomeKeyService
- HomeMode
- AppEntry
- DreamView
- Anker Nebula D2426 (Android TV 14): debloated, with an Apple TV style home
- BYINTEK Love U4, 1080P (Android 9 projector)
- Motorola MT4310 (43", Newsan / Hisense hayward, Android TV 11)
- TCL G03 / BeyondTV4 (Google TV 11): 43C645, 50C635, 60C635
- TCL 65" C7L Google TV
- TCL Q7C (G08, Google TV, Android 14), European firmware
- Anker Nebula Mars 3 Air (Google TV 14): debloated, with an Apple TV style home
- Epson LS800 (Android TV 14 laser projector)
- Freebox Player POP
- Philips Google TV TA7 (Brazil)
- Sber Salute TV 43" (StarOS on Android 13)
- Recs
- XGIMI MoGo 4 (Google TV 14 portable projector)
- PhotoInbox.java
- BenQ GV32 (Google TV 14 portable projector)
- os
- Skyworth SWTV-22AE-FHD (umeda)
- TCL 55C725
- Acer AR55AR2851UDPRO 55" (Android TV 14): debloated, with the launcher on the Home button
- Skyworth 7S60_A5 (Coocaa OS, China market, Android 8)
- Third unit: 55Q6C (Vietnam, Wi-Fi)
- AQUA MatrixTV EE 50" 4K (Google TV 14, omonia)
- Haier MatrixTV GE / HR9216EU (Google TV 14, haeundae)
- Hisense U75 / 75U75N (hengshan), Google TV 12
- Lumio Vision 7 (55" / 139 cm)
- NVIDIA SHIELD Android TV (2019) — codename `sif`
- Implementation Plan: Win 8.1 Start Menu + M3 Expressive (climin_tv_win81_m3_expressive_plan.md)
- Grundig Android UHD TV (Arçelik, `kunyang`)
- Sankey SMART_TV (CVTE, HiSilicon Hi3751V350), Android 12: notes
- Sony BRAVIA KD-55XF7596 (Android TV 9)
- Sony BRAVIA KDL-43W950D
- tcl-43c645/build-and-install.sh
- TCL 43S450F-CA (TCL S4 Fire TV Edition, Fire OS 8.1.8.5)
- TCL 55C745 (2023, Google TV, Android 14)
- TCL Smart TV Pro (G08, Android 12), Australian firmware
- MotionHelper.java
- TCL 75C845X2 (UK/Europe), Google TV 14
- b64push.py
- RCA RC50A21S-4KSM (Android TV 9, Toptech/Zeasn firmware, Realtek RTD2851P)
- anker-nebula-d2426/restore.sh
- epson-ls800/restore.sh
- Sony BRAVIA KD-43X8000G
- TCL 65EP68
- LumiFlow (Mi Box S theme)
- anker-nebula-d2426/build-and-install.sh
- hisense-65d62qh/README.md
- tcl-43c645/restore.sh
- tcl-g08-eu-android12/README.md
- Scene
- TCL 75T7 (G08, Google TV, Android 14), US firmware
- Override
- Design QA: Win 8.1 + M3 Expressive Home
- InputKeyService
- Item
- android.graphics.Bitmap
- android.content.Context
- HomeData
- .resume
- .w
- Override
- Screensaver
- TvInputEntry
- Palette
- StartGridTile
- Ui
- .retarget
- StartGridAdapter
- .refreshDrawerText
- .connect
- Photos
- .schedule
- Place
- .update
- Pick
- .fetchRecommendations
- .onStartJob
- M3Switch
- Frost
- Spring
- SleepOverlay
- RockTek GB1
- Sony BRAVIA XBR-55X850D
- Config

## God Nodes (most connected - your core abstractions)
1. `HomeView` - 176 edges
2. `SettingsView` - 75 edges
3. `HomeApps` - 70 edges
4. `Recs` - 59 edges
5. `HomeMode` - 53 edges
6. `MainActivity` - 51 edges
7. `AppEntry` - 51 edges
8. `DreamView` - 50 edges
9. `Prefs` - 48 edges
10. `HomeData` - 42 edges

## Surprising Connections (you probably didn't know these)
- `Launcher` --references--> `HomeKeyService`  [INFERRED]
  examples/acer-ar55ar2851udpro/README.md → launcher/app/src/main/java_legacy_backup/service/HomeKeyService.java
- `Remote (RC710)` --references--> `HomeKeyService`  [INFERRED]
  examples/acer-ar55ar2851udpro/README.md → launcher/app/src/main/java_legacy_backup/service/HomeKeyService.java
- `The remote` --references--> `HomeKeyService`  [INFERRED]
  examples/hisense-u75-hengshan/README.md → launcher/app/src/main/java_legacy_backup/service/HomeKeyService.java
- `Philips 55OLED854/12: single-session notes` --references--> `HomeKeyService`  [INFERRED]
  examples/philips-tpm191e/README.md → launcher/app/src/main/java_legacy_backup/service/HomeKeyService.java
- `Remote buttons after Google TV Home` --references--> `HomeKeyService`  [INFERRED]
  examples/rocktek-gb1/README.md → launcher/app/src/main/java_legacy_backup/service/HomeKeyService.java

## Import Cycles
- None detected.

## Communities (141 total, 60 thin omitted)

### Community 0 - "MainActivity"
Cohesion: 0.07
Nodes (4): ClockDream, MainActivity, ExpressivePalette, AppPreferences

### Community 2 - "Flamechart Performance Profiler"
Cohesion: 0.07
Nodes (33): colour(), draw(), highlighted(), lane_height(), lanes(), main(), median_run(), parse() (+25 more)

### Community 3 - "Device Benchmark & System Telemetry"
Cohesion: 0.06
Nodes (19): accessibility_services(), android_uptime(), clear_log(), cold_start(), frame_rows(), idle_cpu(), key_event_time(), launcher_pid() (+11 more)

### Community 5 - "Wallpaper & Scene Generators"
Cohesion: 0.10
Nodes (24): above_clouds(), alpenglow(), aurora(), autumn_valley(), balloon(), balloon_sunrise(), city_lights(), dunes() (+16 more)

### Community 6 - "check.py"
Cohesion: 0.10
Nodes (25): asks_for_input_hardware(), expected_launcher(), fix(), handler_apps(), home_activities(), home_of(), inspect(), launcher_to_use() (+17 more)

### Community 7 - "Architecture & Memory Budgets"
Cohesion: 0.06
Nodes (37): Systems & Software Architecture Specification, FocusAnimator Spring Physics, Native High-DPI Sharpness Pipeline, Resource & Memory Budget Constraints, TvScrollView Navigation Guard, Climin UI View Hierarchy & Navigation Graph, Project Changelog, M3Switch Custom Widget (+29 more)

### Community 10 - "TCL Smart TV Pro G09 (Google TV, Android 12)"
Cohesion: 0.18
Nodes (10): Change the dock order, `INPUT_KEY_SERVICE=1` and volume repeat, Install, from scratch or again, Notes for this set, Re-enable one app, Remote app buttons, Stock YouTube RAM reclamation, TCL Smart TV Pro G09 (Google TV, Android 12) (+2 more)

### Community 11 - "Super General (CVTE TP.SK708D.PC821, Android 9): notes"
Cohesion: 0.10
Nodes (16): Philips 50PUS8505: single-session TPM191E notes, Philips 55OLED854/12: single-session notes, What was checked, Examples, 1. Turn on network ADB, 2. Remote access (optional), 3. Debloat, 4. Launcher (+8 more)

### Community 12 - "GitHub Actions Release Pipeline"
Cohesion: 0.07
Nodes (27): name, runs-on, steps, GITHUB_TOKEN, jobs, build-and-release, name, permissions (+19 more)

### Community 15 - "UI Screenshots & Screen Layouts"
Cohesion: 0.10
Nodes (25): All Applications Grid View, Close Back Navigation Button, Hold OK Pin to Home Action, All Applications Screenshot, Application Drawer Menu Grid, Home Shelf Pinned Indicator Badge, App Menu Screenshot, Pinned Streaming Apps Carousel (+17 more)

### Community 16 - "trace.py"
Cohesion: 0.13
Nodes (11): _field(), main(), other_app(), parse(), perfetto_config(), record_one(), recording_ms(), _run() (+3 more)

### Community 17 - "re"
Cohesion: 0.11
Nodes (22): adopt(), between(), brand_lines(), brands(), check_cell(), counts(), examples_rows(), folders() (+14 more)

### Community 18 - "android.view.View"
Cohesion: 0.14
Nodes (4): ViewHolder, Holder, ThemeManager, FocusAnimator

### Community 19 - "Privacy & Telemetry Auditing"
Cohesion: 0.18
Nodes (13): audit(), classify(), connect(), list_lines(), main(), print_report(), probe(), query_packages() (+5 more)

### Community 20 - "tv.py"
Cohesion: 0.14
Nodes (10): check_arity(), connect(), get_signer(), keyevent(), main(), restrict(), screenshot(), screenshot_args() (+2 more)

### Community 21 - "sys"
Cohesion: 0.20
Nodes (22): addons(), cmd_install(), cmd_list(), cmd_remove(), cmd_set(), cmd_show(), cmd_status(), find() (+14 more)

### Community 25 - "MediaItem"
Cohesion: 0.10
Nodes (6): OnMediaClickListener, OnMediaFocusedListener, SuperModeAdapter, ViewHolder, MediaItem, ImageLoader

### Community 26 - "Spoken"
Cohesion: 0.11
Nodes (3): Source, Spoken, TvScrollView

### Community 27 - "android.graphics.Paint"
Cohesion: 0.11
Nodes (3): Glyphs, Quotes, Symbols

### Community 28 - "readme_card.py"
Cohesion: 0.12
Nodes (3): font_and_out(), font_and_out(), font_and_out()

### Community 29 - "argparse"
Cohesion: 0.10
Nodes (13): calendar(), instant(), main(), main(), pipe(), serve(), box(), compare() (+5 more)

### Community 30 - "Performance Tooling Documentation"
Cohesion: 0.24
Nodes (10): Flamechart Tool Guide, flamechart.py Performance Visualization, Performance Tooling Requirements, Perfetto Python Library, Pillow Python Imaging Library, Screendiff Tool Guide, screendiff.py Visual Regression Verifier, Trace & Trace Report Guide (+2 more)

### Community 32 - "Nokia Streaming Box 8000"
Cohesion: 0.14
Nodes (12): Apply, Before you start again, Nokia Streaming Box 8000, Screensaver, Stock launcher, Undo, Why `tools/disable.sh` does not work here, Apply (+4 more)

### Community 33 - "Build & Install Shell Automation"
Cohesion: 0.39
Nodes (7): ANDROID_HOME, MSYS_NO_PATHCONV, PATH, query_home(), rollback_home(), build-and-install.sh script, enable_service()

### Community 35 - "Theme Push Shell Script"
Cohesion: 0.33
Nodes (3): MSYS_NO_PATHCONV, theme.sh script, tv()

### Community 37 - "OEM Debloat Shell Automation"
Cohesion: 0.60
Nodes (4): drop_pkg(), home_launchers(), MSYS_NO_PATHCONV, disable.sh script

### Community 38 - "AI Agent Rules & Workflows"
Cohesion: 0.50
Nodes (4): Agents Rules and Instructions, Graphify Invariants for AI Subagents, GitHub Copilot Instructions, Copilot Graphify Workflow

### Community 49 - "TCL 32L5A (Android TV 11)"
Cohesion: 0.14
Nodes (13): 1. Overview, 2. Debloat Details, 3. How to Re-enable Any Disabled App, 4. How to Re-build and Re-install the Launcher, 5. How to Change Dock Order or App Pinnings, 6. How to Change or Set Wallpaper, 7. Optional Ad-blocking Private DNS Undo, 8. Closing Network ADB and Securing the TV (+5 more)

### Community 50 - "Windows 8.1 + Material 3 Expressive Home Redesign"
Cohesion: 0.15
Nodes (12): Assumptions and non-functional requirements, Current task state, Decision log, Design contract, Home, Implementation slices, Remote interaction and motion, Risks (+4 more)

### Community 51 - "HomeKeyService"
Cohesion: 0.15
Nodes (10): Brazilian unit: the RC902V_M2 remote, Remote buttons: remapped, TCL 65C655 Pro: the BT_RC813_MA remote, Install, Second unit: 50C7K, Poland, Ethernet, TCL C7K (G08, Google TV, Android 14), UK and EU firmware, Additional G08 AU02 unit: native shortcuts and held-volume repeat, Verification and limits (+2 more)

### Community 53 - "AppEntry"
Cohesion: 0.06
Nodes (8): AppGridAdapter, OnAppActionListener, DockAdapter, OnAppClickListener, ViewHolder, AppEntry, AdaptiveColorEngine, FullCardIconRenderer

### Community 54 - "DreamView"
Cohesion: 0.12
Nodes (3): DreamView, Glass, Slide

### Community 55 - "Anker Nebula D2426 (Android TV 14): debloated, with an Apple TV style home"
Cohesion: 0.20
Nodes (9): ADB, Anker Nebula D2426 (Android TV 14): debloated, with an Apple TV style home, Change the dock order or the dock apps, Change the wallpaper, Notes from this projector, Re-enable one app, Rebuild and reinstall the launcher, Undo everything (+1 more)

### Community 56 - "BYINTEK Love U4, 1080P (Android 9 projector)"
Cohesion: 0.20
Nodes (9): BYINTEK Love U4, 1080P (Android 9 projector), Change the dock order, Firmware recovery, Inputs, Install, from scratch or again, Optional, this firmware only: hide the Zeasn ad service, Turning ADB on, Undo everything (+1 more)

### Community 57 - "Motorola MT4310 (43", Newsan / Hisense hayward, Android TV 11)"
Cohesion: 0.20
Nodes (9): Change the dock order, Change the wallpaper, Connecting again, Motorola MT4310 (43", Newsan / Hisense hayward, Android TV 11), Notes from this TV, Re-enable one app, Rebuild / reinstall the launcher, Undo everything (+1 more)

### Community 58 - "TCL G03 / BeyondTV4 (Google TV 11): 43C645, 50C635, 60C635"
Cohesion: 0.20
Nodes (9): Change the dock order or the wallpaper, Quirks on this firmware (60C635), Re-enable one app, Remote buttons, TCL G03 / BeyondTV4 (Google TV 11): 43C645, 50C635, 60C635, Tested models, Verified, What is here (+1 more)

### Community 59 - "TCL 65" C7L Google TV"
Cohesion: 0.20
Nodes (9): After a firmware update, Apply the list and install the launcher, Change the dock order, Change the wallpaper, Connecting to the TV, Re-enable one app, TCL 65" C7L Google TV, Undo everything (+1 more)

### Community 60 - "TCL Q7C (G08, Google TV, Android 14), European firmware"
Cohesion: 0.11
Nodes (18): Connecting: Wireless debugging only, and its port moves, Fifth unit: G08 EU/UK, OTA from Android 12, Fourth unit: 65T8C (Wi-Fi), Identify the firmware, Install, Launcher config used, Notes for this set, Notes for this unit (+10 more)

### Community 61 - "Anker Nebula Mars 3 Air (Google TV 14): debloated, with an Apple TV style home"
Cohesion: 0.22
Nodes (8): ADB, Anker Nebula Mars 3 Air (Google TV 14): debloated, with an Apple TV style home, Notes from this projector, Re-enable one app, The dock, The quirks that cost the most time, Undo everything, What was changed

### Community 62 - "Epson LS800 (Android TV 14 laser projector)"
Cohesion: 0.22
Nodes (8): Change the dock or the wallpaper, Epson LS800 (Android TV 14 laser projector), Extras set on this unit, Install, from scratch or again, Re-enable an app, Remote buttons, Turning ADB on, When you are done

### Community 63 - "Freebox Player POP"
Cohesion: 0.22
Nodes (8): Ad-blocking DNS (off, on purpose), Change the dock or its order, Change the wallpaper, Freebox Player POP, Install, from scratch or again, Re-enable one app, Undo everything, When you're done: close ADB

### Community 64 - "Philips Google TV TA7 (Brazil)"
Cohesion: 0.22
Nodes (8): Change the dock order, Change the wallpaper, Close ADB when you're done, Philips Google TV TA7 (Brazil), Re-enable an app, Reinstall or update the launcher, Undo everything, What changed

### Community 65 - "Sber Salute TV 43" (StarOS on Android 13)"
Cohesion: 0.22
Nodes (8): App "rotation": why apps vanish on their own, Install, Launcher notes, Sber Salute TV 43" (StarOS on Android 13), The Sber platform service, Turning ADB on, Undo everything, What stays

### Community 67 - "XGIMI MoGo 4 (Google TV 14 portable projector)"
Cohesion: 0.22
Nodes (8): Inputs and hardware, Install, Notes on `config.json`, Remote buttons, The helper costs volume-key auto-repeat, The stock launcher does not stay disabled, Turning ADB on, XGIMI MoGo 4 (Google TV 14 portable projector)

### Community 69 - "BenQ GV32 (Google TV 14 portable projector)"
Cohesion: 0.25
Nodes (7): BenQ GV32 (Google TV 14 portable projector), Change the dock or the wallpaper, Install, from scratch or again, Re-enable an app, Remote buttons ("RCI078/079", Bluetooth), Turning ADB on, When you are done

### Community 70 - "os"
Cohesion: 0.09
Nodes (11): main(), split_host_port(), generate_rsa_keypair(), get_default_key_paths(), check_setupwraith_wifi_ownership(), is_core_package(), validate_package_list(), create_ledger() (+3 more)

### Community 71 - "Skyworth SWTV-22AE-FHD (umeda)"
Cohesion: 0.25
Nodes (7): Apply / Restore, Device info, Live TV, Notes on debloat, Skyworth SWTV-22AE-FHD (umeda), Stock launcher, Verified

### Community 72 - "TCL 55C725"
Cohesion: 0.25
Nodes (7): Firmware quirks, Inputs, Install, Resources, TCL 55C725, Undo, What changed

### Community 73 - "Acer AR55AR2851UDPRO 55" (Android TV 14): debloated, with the launcher on the Home button"
Cohesion: 0.29
Nodes (6): Acer AR55AR2851UDPRO 55" (Android TV 14): debloated, with the launcher on the Home button, Connect, Debloat, Launcher, Remote (RC710), Undo

### Community 74 - "Skyworth 7S60_A5 (Coocaa OS, China market, Android 8)"
Cohesion: 0.29
Nodes (6): Inputs, Install, Known issues, Skyworth 7S60_A5 (Coocaa OS, China market, Android 8), Undo, Why Coocaa OS needs more than build-and-install.sh

### Community 75 - "Third unit: 55Q6C (Vietnam, Wi-Fi)"
Cohesion: 0.29
Nodes (7): AirPlay and Cast on this network, Connecting, Home without disabling setupwraith, TclAppBoot after a reboot, Test limits, The list as applied, Third unit: 55Q6C (Vietnam, Wi-Fi)

### Community 76 - "AQUA MatrixTV EE 50" 4K (Google TV 14, omonia)"
Cohesion: 0.33
Nodes (5): AQUA MatrixTV EE 50" 4K (Google TV 14, omonia), Install, from scratch or again, Quirks on this firmware, all checked on the TV, Re-enable one app, Undo everything

### Community 77 - "Haier MatrixTV GE / HR9216EU (Google TV 14, haeundae)"
Cohesion: 0.33
Nodes (5): Haier MatrixTV GE / HR9216EU (Google TV 14, haeundae), Install, from scratch or again, Quirks on this firmware, all checked on the TV, Re-enable one app, Undo everything

### Community 78 - "Hisense U75 / 75U75N (hengshan), Google TV 12"
Cohesion: 0.33
Nodes (5): Display, Hisense U75 / 75U75N (hengshan), Google TV 12, Live TV and inputs, Sound through a soundbar, The remote

### Community 79 - "Lumio Vision 7 (55" / 139 cm)"
Cohesion: 0.33
Nodes (5): config.xml notes, Getting in, Lumio Vision 7 (55" / 139 cm), Other notes, The one thing to know: the launcher needs setupwraith disabled

### Community 80 - "NVIDIA SHIELD Android TV (2019) — codename `sif`"
Cohesion: 0.33
Nodes (5): Notes, NVIDIA SHIELD Android TV (2019) — codename `sif`, Replacing the Home key is one step here, Restore, Tweaks applied

### Community 81 - "Implementation Plan: Win 8.1 Start Menu + M3 Expressive (climin_tv_win81_m3_expressive_plan.md)"
Cohesion: 0.15
Nodes (12): Architecture Decisions, Checkpoint: acceptance checklist §6 walk-through + build, Checkpoint: build passes, logic reviewed, Gaps to close, Implementation Plan: Win 8.1 Start Menu + M3 Expressive (climin_tv_win81_m3_expressive_plan.md), Open Questions, Overview, Phase 1: Grid engine & models (+4 more)

### Community 82 - "Grundig Android UHD TV (Arçelik, `kunyang`)"
Cohesion: 0.40
Nodes (4): Grundig Android UHD TV (Arçelik, `kunyang`), Keep, Notes, What worked

### Community 83 - "Sankey SMART_TV (CVTE, HiSilicon Hi3751V350), Android 12: notes"
Cohesion: 0.40
Nodes (4): Sankey SMART_TV (CVTE, HiSilicon Hi3751V350), Android 12: notes, The reference launcher, Things this firmware does not have, Turn on ADB

### Community 84 - "Sony BRAVIA KD-55XF7596 (Android TV 9)"
Cohesion: 0.40
Nodes (4): Kept on purpose, Notes from this firmware, Sony BRAVIA KD-55XF7596 (Android TV 9), What was tested

### Community 85 - "Sony BRAVIA KDL-43W950D"
Cohesion: 0.40
Nodes (4): Confirmed on this unit, Launcher configuration, Not yet confirmed, Sony BRAVIA KDL-43W950D

### Community 86 - "tcl-43c645/build-and-install.sh"
Cohesion: 0.40
Nodes (4): ANDROID_HOME, JAVA_HOME, PATH, build-and-install.sh script

### Community 87 - "TCL 43S450F-CA (TCL S4 Fire TV Edition, Fire OS 8.1.8.5)"
Cohesion: 0.40
Nodes (4): Apply and undo, Notes, TCL 43S450F-CA (TCL S4 Fire TV Edition, Fire OS 8.1.8.5), What worked and what did not

### Community 88 - "TCL 55C745 (2023, Google TV, Android 14)"
Cohesion: 0.40
Nodes (4): Install, Notes for this set, TCL 55C745 (2023, Google TV, Android 14), Undo everything

### Community 89 - "TCL Smart TV Pro (G08, Android 12), Australian firmware"
Cohesion: 0.40
Nodes (4): Live TV without an aerial, Notes for this set, Remote buttons, TCL Smart TV Pro (G08, Android 12), Australian firmware

### Community 90 - "MotionHelper.java"
Cohesion: 0.07
Nodes (16): TileSpec, Win8DynamicTileEngine, MotionHelper, View.OnFocusChangeListener, ViewOutlineProvider, AnimatorListenerAdapter, Already done (verified in working tree), Checkpoint (+8 more)

### Community 91 - "TCL 75C845X2 (UK/Europe), Google TV 14"
Cohesion: 0.50
Nodes (3): Differences from the C755 list, Known issue: Settings > Accounts & profiles is empty, TCL 75C845X2 (UK/Europe), Google TV 14

### Community 92 - "b64push.py"
Cohesion: 0.39
Nodes (3): main(), q(), safe_remote()

### Community 93 - "RCA RC50A21S-4KSM (Android TV 9, Toptech/Zeasn firmware, Realtek RTD2851P)"
Cohesion: 0.25
Nodes (7): Inputs: use RCA's own panel, Install, Other firmware quirks, RCA RC50A21S-4KSM (Android TV 9, Toptech/Zeasn firmware, Realtek RTD2851P), The remote's Input (Fuente/Source) button: optional, not tested with the merged options, Turning ADB on, Undo everything

### Community 104 - "TCL 75T7 (G08, Google TV, Android 14), US firmware"
Cohesion: 0.33
Nodes (5): Notes for this set, Second unit: TCL 55Q77K, TCL 75T7 (G08, Google TV, Android 14), US firmware, The remote's buttons with launcherx disabled, What is different from the other G08 lists

### Community 105 - "Override"
Cohesion: 0.11
Nodes (3): Node, Pic, Recording

### Community 106 - "Design QA: Win 8.1 + M3 Expressive Home"
Cohesion: 0.40
Nodes (4): Build and code checks, Current implementation, Design QA: Win 8.1 + M3 Expressive Home, Xiaomi TV verification

### Community 107 - "InputKeyService"
Cohesion: 0.18
Nodes (7): Keep / restore the stock Google TV launcher, Notes, Vu VIBE TV, Checks and access to Google TV, Configuration, Xiaomi MiTV-MOOR2: Home redirect with Google TV kept enabled, InputKeyService

### Community 111 - "android.content.Context"
Cohesion: 0.15
Nodes (3): Capabilities, android.content.BroadcastReceiver, Watchdog

### Community 112 - "HomeData"
Cohesion: 0.15
Nodes (3): HomeData, Listener, Weather

### Community 116 - "Screensaver"
Cohesion: 0.15
Nodes (3): Listener, Screensaver, ScreensaverAlone

### Community 117 - "TvInputEntry"
Cohesion: 0.15
Nodes (5): InputChipAdapter, OnInputClickListener, ViewHolder, TvInputEntry, TvInputHelper

### Community 119 - "StartGridTile"
Cohesion: 0.20
Nodes (5): StartGridTile, HourlySlot, WeatherCallback, WeatherData, WeatherRepository

### Community 131 - ".fetchRecommendations"
Cohesion: 0.27
Nodes (3): ProviderInfo, TmdbCallback, TmdbRepository

### Community 138 - "RockTek GB1"
Cohesion: 0.33
Nodes (5): Home handover, Remote buttons after Google TV Home, RockTek GB1, Verification still needed before submitting upstream, What is in this folder

### Community 139 - "Sony BRAVIA XBR-55X850D"
Cohesion: 0.33
Nodes (5): Home and input configuration, Owner-reported startup observations, Recovery and debugging, Sony BRAVIA XBR-55X850D, What was tested

## Knowledge Gaps
- **48 isolated node(s):** `JAVA_HOME`, `ANDROID_HOME`, `PATH`, `MSYS_NO_PATHCONV`, `ANDROID_HOME` (+43 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 695 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **60 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `HomeView` connect `HomeView` to `android.os.Handler`, `Pick`, `.update`, `HomeApps.java`, `Ui.java`, `Spring`, `java_legacy_backup/MainActivity.java`, `android.view.View`, `HomeApps`, `Prefs`, `Recs.java`, `Spoken`, `android.graphics.Paint`, `android.graphics.Canvas`, `DreamView`, `Recs`, `Scene`, `Item`, `HomeView.java`, `android.graphics.Bitmap`, `HomeData`, `.resume`, `Ui`, `.retarget`, `.refreshDrawerText`?**
  _High betweenness centrality (0.096) - this node is a cross-community bridge._
- **What connects `JAVA_HOME`, `ANDROID_HOME`, `PATH` to the rest of the system?**
  _48 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `MainActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.07010402532790593 - nodes in this community are weakly interconnected._
- **Why does `HomeKeyService` connect `HomeKeyService` to `android.os.Handler`, `XGIMI MoGo 4 (Google TV 14 portable projector)`, `Acer AR55AR2851UDPRO 55" (Android TV 14): debloated, with the launcher on the Home button`, `RockTek GB1`, `Super General (CVTE TP.SK708D.PC821, Android 9): notes`, `TCL Smart TV Pro G09 (Google TV, Android 12)`, `Third unit: 55Q6C (Vietnam, Wi-Fi)`, `Hisense U75 / 75U75N (hengshan), Google TV 12`, `TCL Smart TV Pro (G08, Android 12), Australian firmware`, `TCL Q7C (G08, Google TV, Android 14), European firmware`, `android.view.KeyEvent`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Should `Flamechart Performance Profiler` be split into smaller, more focused modules?**
  _Cohesion score 0.06654567453115548 - nodes in this community are weakly interconnected._
- **Why does `Examples` connect `Super General (CVTE TP.SK708D.PC821, Android 9): notes` to `HomeKeyService`, `Wallpaper & Scene Generators`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Should `Device Benchmark & System Telemetry` be split into smaller, more focused modules?**
  _Cohesion score 0.05584415584415584 - nodes in this community are weakly interconnected._