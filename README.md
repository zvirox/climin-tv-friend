# CliminTV: Android TV Debloater & Material 3 Expressive Launcher

A lightweight, non-destructive Android TV debloating system and custom native launcher designed with **Google's Material 3 Expressive UI**.

Based on the systems architecture of [CleanYourTV](https://github.com/Gathin23/cleanyourtv), CliminTV provides the **exact same robust functionality, strict non-destructive safety invariants, and security protocol**, while replacing the traditional glassmorphic/Apple TV UI with a **Material 3 Expressive TV design system**.

---

## 1. Architectural Highlights

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CLIMINTV SYSTEM TOPOLOGY                        │
└────────────────────────────────────────────────────────────────────────┘
                                     │
            ┌────────────────────────┴────────────────────────┐
            ▼                                                 ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────────────┐
│           DEBLOAT SUBSYSTEM          │  │       MATERIAL 3 EXPRESSIVE HOME     │
│               (tools/)               │  │              (launcher/)             │
├──────────────────────────────────────┤  ├──────────────────────────────────────┤
│ • Pure-Python ADB Client (tv.py)     │  │ • Package: io.climintv.home          │
│ • State Snapshot & Recovery Ledger   │  │ • M3 Expressive Tonal Hierarchy      │
│ • Home Guard & Wi-Fi Protection Gate │  │ • Spring Physics Focus Animation     │
│ • Core Android Invariant Enforcement │  │ • Native TV Input Framework (TIF)    │
│ • 48 KB Base64 Binary Stream Pipeline│  │ • Remote Key Interception (284 Key)  │
│ • Zero-Port Security Hardening Audit │  │ • Zero Network Permission (Offline)  │
└──────────────────────────────────────┘  └──────────────────────────────────────┘
```

---

## 2. Material 3 Expressive Design System

CliminTV Home departs from translucent glass blurs in favor of Material 3 Expressive's bold, tactile visual language:

| Design Dimension | Traditional Glass UI (CleanYourTV) | CliminTV Material 3 Expressive UI |
| :--- | :--- | :--- |
| **Containers** | Translucent frosted glass rects, blur shaders | **Tactile Tonal Containers** (`surfaceContainerLowest` to `surfaceContainerHighest`) |
| **Geometry** | Rigid rounded rectangles | **Dynamic Radii & Pill Geometry** (24dp squircle Hero cards, 18dp grid tiles, 999dp pill chips) |
| **Focus Physics** | Subtle border glow | **Dynamic Spring Overshoot** (`FocusAnimator` 1.08x–1.1x scaling + tactile elevation + vivid accent outline) |
| **Color Theming** | Static dark glass tint | **Dynamic Expressive Palettes** (Coral, Electric Indigo, Cyber Emerald, Vivid Ocean, Radiant Amber, Cosmic Violet) |
| **Header Bar** | Minimal monochrome clock | **Expressive Status Hub** (Greeting chip, Big Display clock with AM/PM pill, live TIF HDMI chips, Wi-Fi pill) |
| **App Dock** | Standard horizontal row | **Expressive Hero Dock** with elevated icon containers and dual-line typography |

---

## 3. Core Safety & Security Invariants

CliminTV strictly executes non-destructive user-space package manager primitives (`pm disable-user --user 0`). System packages on `/system` or `/vendor` are never deleted, ensuring **100% reversible recovery** via `tools/restore.sh`.

### 3.1 Immutable Core Safety Gate (`CORE_RE`)
Disabling low-level system services bricks Android TV into an unrecoverable bootloop. `tools/disable.sh` enforces an immutable regular expression barrier:
```bash
CORE_RE='^(android|com\.android\.systemui|com\.android\.shell|com\.android\.phone|com\.android\.settings|com\.android\.tv\.settings|com\.(google\.)?android\.(permissioncontroller|packageinstaller|ext\.services|ext\.shared|modulemetadata)|com\.google\.android\.overlay\.modules.*|com\.android\.providers\..*)$'
CORE_OK='^com\.android\.providers\.(calendar|contacts|userdictionary|blockednumber)$'
```

### 3.2 Setupwraith Wi-Fi Drop Trap
On Google TV devices, the initial setup wizard (`com.google.android.tungsten.setupwraith`) owns the Wi-Fi credentials saved during device provisioning. Disabling `setupwraith` flushes the credentials, immediately killing Wi-Fi and severing remote ADB TCP access. CliminTV inspects `dumpsys wifi` for `cname` ownership and safely skips `setupwraith` unless explicitly overridden.

### 3.3 The Home Guard Mechanism
Before touching stock OEM launchers (`tvlauncher`, `launcherx`, `mitv.tvhome`), `tools/disable.sh` verifies that a functional launcher (`io.climintv.home`) is already compiled, installed, and answering the `android.intent.category.HOME` intent query.

### 3.4 Zero-Network & Low Memory Constraint
- **No `android.permission.INTERNET`**: CliminTV Home has no network stacks or telemetry; it cannot send or receive remote data.
- **Resource Budget**: Runs with a memory footprint under **35 MB RAM** (ideal for budget 1 GB RAM sticks/boxes).

---

## 4. Hardware Integrations

### 4.1 Native TV Input Framework (TIF) Passthrough
Bypasses vendor-bloated input apps:
- Enumerates hardware inputs via `TvInputManager.getTvInputList()`.
- Identifies HDMI 1, HDMI 2, HDMI 3, AV, and system Tuner.
- Generates channel URIs via `TvContract.buildChannelUriForPassthroughInput(inputId)`.
- Dispatches `ACTION_VIEW` directly to the display engine surface.

### 4.2 Remote Key Interception (`HomeKeyService`)
On devices like Xiaomi Mi TV Stick / Mi Box, the remote Home button emits `KEYCODE_ALL_APPS` ($284$), which Android TV 9 routes exclusively to PatchWall. CliminTV's accessibility service intercepts `KEYCODE_ALL_APPS` and instantly routes the user back to CliminTV Home.

---

## 5. Quick Start & Usage

### 5.1 Prerequisites
- Python 3.8+ (with `cryptography` installed)
- Android TV / Google TV on the same Wi-Fi or Ethernet network
- Developer Options & USB Debugging enabled on TV

### 5.2 Build & Install CliminTV Home
```bash
# Build release APK and install over ADB
TV_HOST=192.168.1.50 ./launcher/build-and-install.sh
```

### 5.3 Non-Destructive Debloating
Choose your device profile from `examples/` (e.g. Xiaomi, TCL, Sony, Onn):
```bash
# Verify debloat list against safety rules
python3 tools/check.py examples/xiaomi-mitv-aetp0-dofus/debloat.txt

# Run debloater
TV_HOST=192.168.1.50 ./tools/disable.sh examples/xiaomi-mitv-aetp0-dofus/debloat.txt
```

### 5.4 Instant Rollback & Recovery
```bash
# Re-enable all packages back to their original state
TV_HOST=192.168.1.50 ./tools/restore.sh examples/xiaomi-mitv-aetp0-dofus/debloat.txt
```

### 5.5 Post-Install Security Hardening
After completing installation:
1. On the TV, navigate to **Settings → Device Preferences → Developer options**.
2. Toggle **USB debugging** to **OFF**.
3. Select **Revoke USB debugging authorizations**.
4. Run the security verification script:
```bash
python3 tools/adb-closed.py 192.168.1.50
# Returns exit code 0 when port 5555 is closed and secured.
```

---

## 6. Project Structure

```
climin-tv-friend/
├── Makefile                     # Operational CLI automation
├── README.md                    # Project documentation
├── examples/                    # 290+ Curated OEM TV device trees (debloat.txt, tv.json)
├── tools/                       # Pure-Python ADB debloat subsystem
│   ├── tv.py                    # TCP socket & ADB protocol client
│   ├── disable.sh               # Non-destructive debloat executor
│   ├── restore.sh               # Reversible state rollback executor
│   ├── check.py                 # Safety invariant auditor
│   ├── b64push.py               # 48 KB chunked binary sideload pipeline
│   ├── recovery.py              # Snapshot transaction ledger manager
│   ├── adb-closed.py            # Port 5555 security verifier
│   └── lib/
│       ├── adb_client.py        # Protocol transport implementation
│       ├── invariants.py        # CORE_RE & setupwraith safety rules
│       └── ledger.py            # Transaction history logger
└── launcher/                    # Native Android TV Launcher (Material 3 Expressive)
    ├── build-and-install.sh     # Automated build & TV deployment script
    └── app/
        ├── build.gradle         # R8 Proguard stripping & Leanback config
        └── src/main/
            ├── AndroidManifest.xml # Zero-network & package visibility
            ├── res/             # M3 Expressive colors, layouts, drawables
            └── java/io/climintv/home/
                ├── MainActivity.java
                ├── ClockDream.java
                ├── adapter/     # Dock, App Grid & Input Chip adapters
                ├── model/       # App & TV Input models
                ├── service/     # HomeKeyService & InputKeyService
                ├── theme/       # Dynamic M3 Expressive color palettes
                ├── tif/         # Native TvInputManager helper
                └── util/        # FocusAnimator (Spring physics) & Prefs
```

---

## 7. License
Licensed under the Apache License 2.0. Compatible with open-source Android TV projects.
