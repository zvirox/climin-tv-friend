# Climin UI — Screen Map

Section-by-section map of every UI surface, with coordinates, states and the code that
draws it. Use the **section IDs** (e.g. `HOME.CARDS.WIDE`, `SHEET.GRID`,
`SETTINGS.PREFS`) to name the exact place you want changed.

> Read `RULES.md` first. A section marked **frozen** in this map cannot change without
> the approval protocol in `RULES.md` §4. A section marked **open** (type, spacing,
> colour, glass tint, a brand-new surface) can be tweaked directly.

Verified 2026-10-27 against source (`HomeView.java`, `HomeApps.java`, `SettingsActivity.java`)
and the running APK `io.climintv.home.debug` v0.0.1 on the Xiaomi TV (1280×720).

---

## 0. Coordinate system

- Everything is drawn in a **1920 × 1080 design space**; there is no XML layout.
- `HomeActivity` calls `setContentView(new HomeView(this))`. `HomeView` is a `ViewGroup`
  holding 8 full-screen `Layer` children; the settings screen is a separate custom view.
- Scale factor: **`k = screenWidth / 1920`** (`HomeView.onSizeChanged`, `SettingsActivity:343`).
  On the current TV (1280 wide) **k = 0.667**, so design px × 0.667 = on-screen px.
- All numbers below are **design px** unless marked "(720p)". `HOME.*` lives in
  `HomeView.java`; `SETTINGS.*` lives in `SettingsActivity.java`.

### Layer stack (z-order, bottom → top)

| z | Layer | Constant | Draws | Moves when |
|---|---|---|---|---|
| 0 | Sky | `SKY` | background photo / gradient, night tint | drawer open (veil) |
| 1 | Hero | `HERO` | title, tabs, cards | drawer open (fades, lifts −560) |
| 2 | Status | `STATUS` | round top-right buttons, sleep chip, quotes | drawer open (fades, lifts) |
| 3 | Platter | `PLATTER` | shelf glass | drawer open (lifts −560) |
| 4 | Tiles | `TILES` | shelf app tiles | drawer open (lifts −560) |
| 5 | Drawer | `DRAWER` | drawer row + weather/quotes cards | drawer open (slides 0), closed (+560) |
| 6 | Sheet | `SHEET` | All Apps scrim + grid | sheet open (alpha 0→1, scale 1.04→1) |
| 7 | Toast | `TOAST` | notification pill | independent |

Focus feedback (`home`/`RULES.md`) is a per-item **spring**, not a layout change:
`topFocus[]`, `cardFocus[]`, `drawerFocus[]`, `gridFocus[]`, `drawer`, `sheet`, `scroll`.
These springs and their timings are **frozen**.

---

## 1. State matrix — what is on canvas

| State | Hero (title/tabs/cards) | Buttons stack | Shelf | Drawer | Sheet | Toast |
|---|---|---|---|---|---|---|
| **Base** (idle) | visible | visible | visible | **off-canvas** (+560 → y 1100–1568), alpha 0 | hidden (alpha 0) | hidden |
| Card focused | card +5 %, rises 6 px, white rim | visible | visible | off-canvas | hidden | hidden |
| Shelf focused | dimmed by `shelfOn` | visible | tile +20 %, shadow | off-canvas | hidden | hidden |
| Shelf reorder | hidden | hidden | tile lifted, others slide | off-canvas | hidden | "← → to move · OK to drop" |
| **Drawer open** | **faded out** (alpha 0 above 32 % progress) | **faded out** | **lifted −560** → y 222–460 | visible at 540–666 + 708–1008 | hidden | hidden |
| **All Apps open** | alpha 0 | alpha 0 | alpha 0 | alpha 0 | scrim + grid, scale 1 | hidden |
| Night (sunset→sunrise) | same geometry | same | same | same | same | same, dark glass |
| **No TMDB key** | replaced by clock/date/weather line | visible | visible | off-canvas | hidden | hidden |

Off-canvas is literal: the drawer's layer sits at `translationY = (1 − d) × 560`, so a
closed drawer is drawn at y 1100–1568, below the 1080 canvas. Never edit a rect inside
`layoutDrawer()` assuming it is visible; it is only visible when `drawerOpen`.

---

## 2. HOME — base screen (drawer closed)

```
┌──────────────────────────────────────────────────────────────┐ 1920×1080
│  For You  (HOME.TITLE)           ◯ ◯ ◯ ◯ ◯  (HOME.BUTTONS)  │ ← y 103
│  [Explore] Movies Shows (HOME.TABS)          BTC ETH … QUOTES │ ← y 158–226
│                                                              │
│  ┌──────────────────────┐ ┌───────┐ ┌───────┐                │ ← y 262
│  │  HOME.CARDS.WIDE     │ │ TALL1 │ │ TALL2 │                │
│  │  1040 × 442          │ │320×442│ │320×442│                │
│  └──────────────────────┘ └───────┘ └───────┘                │ ← y 704
│  ▲ title y 750   ▲ meta y 784 (wide only)                    │
│                                                              │
│  ╭──────────────── HOME.SHELF.GLASS ─────────────────╮       │ ← y 782 (7 apps)
│  │  [tile] [tile] [tile] [tile] [tile] [tile] [tile]  │       │ ← tiles 822–940
│  ╰────────────────────────────────────────────────────╯       │ ← y 1020
└──────────────────────────────────────────────────────────────┘
```

### HOME.TITLE — "For You"
- Box: baseline at (80, **130**); family `Ui.displaySemibold`, **76 px**; colour `L_INK` (day) / `WHITE` (night).
- Source: `drawLightTop()` first line; paint `lTitle` in `makePaints()`.
- 720p: baseline ≈ (53, 87), 51 px.
- Not drawn when there is no TMDB key (see `HOME.FALLBACK`).
- **Open** (typography/colour). Text itself is translated via `Ui.tr`.

### HOME.TABS — Explore / Movies / Shows
- Capsule band: **y 158 → 226** (height 68). First pill starts at x 54 (text at 80); each
  pill = text width + 26 px padding each side; 4 px between pills.
- Text: 30 px; selected `lTabOn` semibold `L_INK`, others `lTab` medium `L_DIM`.
- Focus: selected tab gets a **white pill**, +5 %, shadow; text turns `L_INK`.
- Tabs are data: `tabs()` from `HomeApps`/`Ui.tr`; `TAB_KIND = {null, "movie", "tv"}` selects
  which `Recs` list feeds the cards.
- 720p: 105 → 151; text 20 px.
- **Frozen:** focus pill, +5 % grow, shadow, selection behaviour. **Open:** text size/weight, gap, pill height.

### HOME.BUTTONS — round glass buttons (top right)
- Up to 5 circles, diameter **68**, centre y **103**, right edge at **1840**, gap **20**.
  Default order, left → right: Inputs 1420–1488, Wi-Fi 1508–1576, Theme 1596–1664,
  Home Settings 1684–1752, TV Settings 1772–1840.
- Icons: `Symbols` at 27 px; colour `L_INK` day / `0xEBFFFFFF` night. Focused: circle turns
  **white**, icon dark, grows **+10 %**, and its **name** appears below at y ≈ 177 (25 px).
- Source: `HomeApps.topButtons()` / `topButtonKeys()` (user order from Settings › Top Buttons),
  `drawLightButtons()`, rects `topRects[3..7]` in `layoutLight()`.
- Actions:
  | Key | Icon | Focus label | Action |
  |---|---|---|---|
  | `inputs` | `hdmi` | HDMI Inputs | opens the drawer on the first input |
  | `wifi` | `wifi` | Wi-Fi | `Settings.ACTION_WIFI_SETTINGS` (fallback `ACTION_SETTINGS`) |
  | `theme` | `brightness_auto` / `sunny` / `clear_night` | Automatic / Light / Dark | cycles auto → light → dark, `Prefs.setTheme`, no activity |
  | `settings` | `settings` | Home Settings | `SettingsActivity` |
  | `tv` | `tv_settings` | TV Settings | TV's `Settings.ACTION_SETTINGS` |
- `theme` shows the mode now in use: icon `brightness_auto` (auto), `sunny` (light),
  `clear_night` (dark); the focus label is the mode name, not the word "Theme". Cycling calls
  `checkNight(true)` so the day/night palette follows at once and persists across restarts.
- Live TV is no longer a top button (it stays in the drawer's Inputs group). A saved list equal
  to the old default `live,inputs,settings` migrates to the new default (`TOP_LEGACY`).
- 720p: diameter 45, centre y 69.
- **Frozen:** focus animation, white fill, icon, label behaviour, order semantics. **Open:** glass tint, icon size, spacing.

### HOME.SLEEPTIMER — "Off in 45 min" chip
- Appears **left of the first button** at centre y 103, right-aligned to `topRects[3].left − 28`;
  `lQuote` 21 px + `bedtime` symbol.
- Source: tail of `drawLightButtons()`, text from `updateSleep()`.
- Only when `Prefs.sleepShown()` and the overlay cannot be drawn over other apps.
- **Open** (typography). **Frozen:** when it appears, what it says.

### HOME.QUOTES — crypto prices row
- Right-aligned to **1840**, vertically centred on the tab bar (**y ≈ 192**), 21 px, coin
  glyphs 6 px gap / 24 px item spacing.
- Fades out while any of the top-right buttons has focus (they share the row).
- Only when `HomeData.coinCount() > 0` and `Prefs.quotes()`.
- Source: `drawQuotes()`, `Quotes`; colours `lQuote`/`staleQuote`.
- **Open** (spacing/size). **Frozen:** data, which coins, fade rule.

### HOME.CARDS — the three recommendation cards
Row: top **262**, height **442** (bottom 704), gap **40**, left margin 80, right edge 1840.

| Part | Box (design px) | 720p | Source |
|---|---|---|---|
| `HOME.CARDS.WIDE` | x 80 → 1120 (1040 × 442) | 53→747 × 175→469 | `cardRects[0]` |
| `HOME.CARDS.TALL1` | x 1160 → 1480 (320 × 442) | 773→987 | `cardRects[1]` |
| `HOME.CARDS.TALL2` | x 1520 → 1840 (320 × 442) | 1013→1227 | `cardRects[2]` |
| `HOME.CARDS.TITLE` | baseline y **750**, 31 px semibold | y 500 | `lCardTitle` |
| `HOME.CARDS.META` | baseline y **784**, 25 px, **wide card only** | y 523 | `lCardMeta` |

- Images: `Recs.Pick` → `Backdrop.wide` (card 0) / `Backdrop.tall` (cards 1–2), rounded 18 px;
  focused card +5 %, rises 6 px, deep shadow + white gradient rim.
- Empty/no-key slot: flat `0xDADADF` (day) / `0x2C2C2E` (night).
- Source: `drawLightCard(i)`, `layoutLight()`, paints `lCardTitle`, `lCardMeta`; data `updateCards()`.
- ⚠ Clearance note: with 7 shelf apps the shelf glass starts at y **782**, 2 px above the meta
  baseline (784). Flagged, not changed — say the word if you want more breathing room here.
- **Frozen:** card geometry, +5 % grow, rim, 3D flip, D-pad order, TMDB pipeline.
  **Open:** radii, title/meta size, spacing, shadow strength, empty-slot colour.

### HOME.SHELF — app canvas
- Glass platter: `platterRect`, padding **40** on all sides, corner radius `TILE_RADIUS + PAD = 62`.
  Bottom edge is **fixed at y 1020**; top = `900 − tileHeight`.
- Tiles: 16:9, height ≤ **126**; width = `(1760 − (n−1)·48) / n`, capped at 224; gap **48**;
  row centred horizontally. With **7 apps**: 210 × 118, tiles 822 → 940, glass 782 → 1020.
- Up to **8** tiles visible (`HomeApps.shelfVisible`); more than that the row scrolls one tile at a time.
- Tile content: brand-colour gradient + logo (tvOS-style, `HomeApps.SHELF`), focused tile
  **+20 %** with shadow, neighbours dimmed by `shelfOn`.
- Source: `layoutShelf()`, `drawPlatter()`, `drawTiles()`, `drawShelfTile()`, `tileRects[]`.
- Glass: `scene.liquid(...)` — white day / `SMOKE` night.
- Editing (all **frozen**): hold OK → reorder (`startReorder`, ← → move, OK drop, Back cancel);
  hold OK in All Apps → add/remove (`toggleShelf`, `Prefs.setShelf`).
- 720p: tiles 140×79 at y 548; glass 521 → 680.
- **Open:** tile radius, logo scale, gap, glass tint. **Frozen:** reorder, add/remove, scroll, focus grow.

### HOME.FALLBACK — no TMDB key
- `drawLightTop()` returns early after `drawClock()`: date (34 px, baseline 330),
  clock (200 px light, baseline 540), weather symbol + line (40 px, baseline 626) at x 80,
  quotes below at baseline 694.
- Tabs, title and cards are **not drawn**; D-pad skips `Z_CARDS`.
- **Freeze as is** — this is the offline/keys-missing state.

---

## 3. HOME.DRAWER — open with ↓ from the shelf

Everything lifts by **`SHIFT = 560`**: hero/status fade out, shelf moves to y 222–460.
Drawer contents are laid out in **open** coordinates (`layoutDrawer()`).

```
┌──────────────────────────────────────────────────────────────┐
│   [ shelf, lifted: 222–460 ]                                 │
│                                                              │
│  Inputs                    Shortcuts        (HOME.DRAWER.LABELS y 518)
│  ┌────┐┌────┐┌────┐┌────┐   ┌────┐┌────┐┌────┐                │ ← y 540
│  │Live││Jio ││Fire││Game│   │All ││Home││ TV │  126 high      │
│  └────┘└────┘└────┘└────┘   └────┘└────┘└────┘                │ ← y 666
│                                                              │
│  ╭────────────── HOME.DRAWER.WEATHER ──────────────╮          │ ← y 708
│  │ 14°  Clear · H L  │ 11AM 12PM 1PM 2PM 3PM 4PM   │  300 high│
│  ╰─────────────────────────────────────────────────╯          │ ← y 1008
└──────────────────────────────────────────────────────────────┘
```

### HOME.DRAWER.LABELS — group headings
- Baselines at **y 518**, 24 px, `rowTitle`. "Inputs" at the first tile's left + 4;
  "Shortcuts" at the first shortcut's left + 4. A group with no items draws no label.

### HOME.DRAWER.INPUTS — TV inputs (first group)
- Tiles: top **540**, height **126**, width `min(230, fit)`, gap **18** inside a group,
  **56** between groups. With 4 inputs + 3 shortcuts: inputs at x 80, 328, 576, 824,
  then the group break → shortcuts at 1110, 1358, 1606.
- Content: `Symbols` 34 px at 40 % height + label 23 px at 80 % (auto-shrinks to 18 before ellipsis).
- Focused tile: **white**, dark ink, **+7 %**, shadow; unfocused: glass at 0.7 tint, ink `L_INK`.
- Unavailable input: icon/label dimmed (`0x6BFFFFFF`), still focusable.
- Source: `HomeApps.allInputs()` → `drawerList()` → `drawerRects[]`, `drawDrawerTile()`.
- Actions: `TvInputHelper`/`inputIntent` launch; a `LIVE_TV` kind starts Live TV.
- **Frozen:** which inputs appear, dim-when-disconnected, launch behaviour, D-pad.

### HOME.DRAWER.SHORTCUTS — second group
- Same tile geometry; starts after the 56 px group gap.
- Items from `HomeApps.drawerShortcuts()`: **All Apps**, **Home Settings**, **TV Settings**
  (plus any the user adds — the list is data-driven).
- Actions: All Apps → `SHEET`; Home Settings → `SettingsActivity`; TV Settings → system Settings.
- **Frozen:** item set, ordering, actions.

### HOME.DRAWER.WEATHER — big horizontal card
- Box: x 80 → **1840** (full) when quotes are off; split at **58.5 %** (`x ≈ 1110`) when both
  cards are on (weather left, 16 px gutter). Height **300** (708 → 1008), radius 30, glass 0.72.
- Internal layout (offsets from card top/left):
  - Title (city or "Weather") baseline **+54**, 30 px;
  - weather glyph 52 px at **+132**, big temp baseline **+170** (numbers, large);
  - condition **+220**, high/low **+258**, both at x = left + 34;
  - divider at **left + 392**;
  - hourly region: 6 columns (or 5 days when `Prefs.forecast() == "days"`), starting at
    `left + 422`; labels **+96**, glyphs 30 px at **+142**, values **+196**;
  - sunrise/sunset row: symbol 22 px + label, baseline **+258**.
- Source: `drawWeatherCard()` → `drawWeather()`, `refreshDrawerText()`, `drawerCard()`.
- 720p: card 472 → 672, height 200.
- **Frozen:** data, hourly/day switch, sunrise/sunset, glass call. **Open:** type scale, column widths, internal spacing, tint.
- Not keyboard-focusable without TalkBack (`WIDGETS` zone is TalkBack-only).

### HOME.DRAWER.QUOTES — second card (optional)
- Right side of the drawer (from 58.5 % + 16 to 1840) when both cards are on; full width when
  weather is off. Same 300 height, same glass.
- Coin logo + price 36 px, one row per configured coin.
- Only when `Prefs.quotes()` and prices exist. **Frozen:** rows, format, data.

### Drawer navigation (frozen)
- ↓ from shelf → opens; drawer focus starts on the input nearest the shelf cursor.
- ← → moves inside the row; ↑ on the first row closes the drawer and restores the shelf cursor.
- Selecting an input launches it; All Apps opens `SHEET`; Settings items launch settings.

---

## 4. SHEET — "All Apps" overlay

Opens from `Shortcuts › All Apps` (or a remote's All Apps button, `EXTRA_ALL_APPS`).

```
┌──────────────────────────────────────────────────────────────┐
│                       All Apps   (baseline 124)              │
│    ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐         │ ← grid top 196
│    │ 300×169│ │      │ │      │ │      │ │      │           │
│    └───────┘ └───────┘ └───────┘ └───────┘ └───────┘         │
│      label    label    label    label    label               │
│    … 3 rows visible, scrolls a row at a time …               │
└──────────────────────────────────────────────────────────────┘
```

| Part | Geometry | Source |
|---|---|---|
| `SHEET.SCRIM` | full screen, `0xA8000000` | first line of `drawSheet()` |
| `SHEET.TITLE` | centred, baseline **124**, display semibold | `drawSheet()` |
| `SHEET.GRID` | 5 columns × 300 wide, gap 40 → total 1660, left **130**; top **196**; row pitch **239**; card 300 × 169; 3 rows visible | `COLUMNS`, `GRID_W/H/GAP/ROW/TOP`, `drawSheet()` |
| `SHEET.LABELS` | centred under each card, baseline card.bottom + 40 (**+46** when focused), 23 px | `gridLabels`, `gridLabel` |
| `SHEET.FOCUS` | focused card **+8 %** with shadow, sheen, white rim | `drawCard(..., 0.08f)` |

- Contents: `others` = installed, launchable apps **not on the shelf** (`setOthers`).
- 720p: card 200 × 113, grid left 87, top 131, row pitch 159, viewport 131 → 609.
- Actions: OK launches the app; hold OK adds/removes from the shelf (`toggleShelf`, toast
  "Added to the shelf"/"Removed from the shelf"); Back closes; leaving to an app and returning
  keeps it open (`allAppsOnResume`).
- **Frozen:** column count, navigation, add/remove, scroll, scale-in. **Open:** card radius,
  gaps, label size, scrim opacity.

---

## 5. HOME.TOAST — notification pill
- Centred horizontally, top **64**, slides down 16 px while fading in; 6 s total.
- Glass pill (`SMOKE`, 0.55), `bell` symbol 26 px, message up to 2 lines, 720 px max width.
- Source: `drawToast()`, `showToast()`; messages come from shelf edits, mode switches, notices.

---

## 6. SETTINGS — Home Settings app (`SettingsActivity`)

Same coordinate system (`k = w / 1920`). Two fixed regions:

| Region | Box | Contents |
|---|---|---|
| `SETTINGS.SHELL.LEFT` | centre x **500** | `sparkles` 150 px at y 400; "Climin UI" 50 px baseline **612**; subtitle 26 px **660**; version 21 px **704** |
| `SETTINGS.SHELL.LIST` | x **1000 → 1824**, top **186** | screen-specific rows, focus box spans the full row width |

Background: the live scene shader (`scene.drawBackground`), not the home screen's blur.

### SETTINGS.MAIN — root list
- 8 rows, `MAIN_TOP = 140`, row height **53.75**, gap **4** (pitch 57.75) → y 140 → 598.
- Rows: Home Screen, Screensaver (preview), Home Settings, Shelf, Top Buttons, Remote Buttons,
  Sleep Timer, TV Settings.
- Below the rows (`y ≈ 622`): sections **RECOMMENDATIONS** (TMDB status, link to Watchmode),
  **COMPARISON**, **NOTICES**, plus any error text.
- 720p: rows start y 93, pitch 38.

### SETTINGS.HOME_CHOICE — Home Screen picker
- Two 104-high cards from `LIST_TOP + 20` (206): "Climin UI" / "Original", each with a
  2-line description; hint under them. Picking starts the mode switch (screen blinks).

### SETTINGS.PREFS — Home Settings
- Heading "Home Settings" baseline **162** (LIST_TOP − 24), 34 px.
- Rows: top **206**, height **68**, gap **6** (pitch 74), **10 visible** → 206 → 940.
- Order: Language, Temperature, Theme, My Photos, Navigation sounds, Weather widget, City,
  Forecast, Quotes widget, Bitcoin, Ethereum, Solana, Dollar (BRL only).
- Right-aligned value, 30 px inset; locked rows render "Unavailable" at 45 % opacity;
  a hint line sits below the last row (`LIST_TOP + 20 + rows·74 + 36`).
- **Frozen:** row set/order, semantics, persistence. Note: this list is the *only* supported way
  to change home appearance — do not add hidden defaults elsewhere.

### SETTINGS.SHELF — edit the shelf
- Heading "Shelf" baseline 162. Rows 68 high, gap 6, **9 visible**.
- Each row: app name (520 px max), right value = `#place` when on the shelf (with a check) or
  "Off", "Unavailable" for uninstalled apps.
- OK toggles membership, ← → moves one place. Hint: "OK adds or removes (up to N). ← → change the order."
- `SETTINGS.TOP_BUTTONS` reuses the same screen (`editingTop`): heading "Top Buttons", the
  five candidates (Inputs, Wi-Fi, Theme, Home Settings, TV Settings), same OK/← → semantics,
  hint "OK adds or removes a button."

### SETTINGS.PICKER — long-list picker
- Heading = context: **City**, **Language**, **Sleep Timer**, **Position**, or the remote-button name.
- Rows 68 high, gap 6, **9 visible**; current value gets a check on the right; unavailable rows
  greyed; footer shows "n / total · OK picks, Back cancels."

### SETTINGS.REMOTE_KEYS — remote buttons
- Heading "Remote Buttons"; row set depends on the TV brand (`RemoteKeys`, `keyLabels()`), e.g.
  Xiaomi: Mi, Okko, VK, Rutube, KION, Wink, IVI. Rows 68 high, all visible; a brand-specific
  hint is printed under the list.

### SETTINGS.POWER — Sleep Timer
- Heading "Sleep Timer"; 2–3 rows: Sleep Timer (duration), Show Time Left, Position (when shown).
- Values change with ← → or OK (opens `PICKER` for duration / placement).

### SETTINGS.TV_SETTINGS
- Leaves the app: `Settings.ACTION_SETTINGS` (the TV's own settings). No UI of ours.

---

## 7. Cross-cutting tokens

| Concern | Where | Day / Night |
|---|---|---|
| Ink | `L_INK` `0xFF1D1D1F` / `WHITE` `0xFFF5F5F7` | `tone(light, night)` swaps |
| Secondary | `L_DIM` `0xFF6E6E73`, `L_FAINT` `0xFF8E8E93` | lighter at night |
| Smoked glass | `SMOKE` `0xFF111113` | drawer/platter/buttons |
| Glass | `scene.liquid(...)` (platter, buttons), `drawerGlass()` (drawer/quotes/weather) | tint + blur, `HomeView` |
| Background | `drawLightSky()`: card 0's photo blurred, or a gradient | night image/gradient |
| Focus springs | `Spring(...)` per item | frozen |
| Shadows | `Shadows` + `SHADOW_BLUR 26`, `DEEP_BLUR 30` | frozen |

---

## 8. Where to change what (open surfaces)

| Want to change | Touch |
|---|---|
| "For You" size/weight/colour | `lTitle` (makePaints), `L_TITLE_BASE` |
| Tab text/size, pill width | `lTab`, `lTabOn`, padding `26`, `L_TABBAR_*` |
| Round button size/gap/glass | `L_BUTTON`, `L_BUTTON_GAP`, `L_BUTTON_CY`, `drawLightButtons()` |
| Card radii / shadow | `18 * k` in `drawLightCard`, `DEEP_BLUR` |
| Card title/meta | `lCardTitle`, `lCardMeta`, offsets 46 / 80 |
| Card sizes/gap | `L_TALL_W`, `L_CARD_GAP`, `L_CARDS_TOP`, `L_CARD_H` (changes layout, not behaviour) |
| Shelf tile radius/logo/gap | `TILE_RADIUS`, `logoScale` in `HomeApps.SHELF`, `TILE_GAP`, `SHELF_PAD` |
| Shelf glass tint | `drawPlatter()` → `scene.liquid(...)` |
| Drawer tile size/gap | `layoutDrawer()` (`230`, `18`, `56`, `ROW_H`) |
| Drawer label size | `drawerLabel` text size (23 / min 18) |
| Weather internals | `drawWeather()` offsets, `bigTemp`/`hourPaint`/`hourTemp`/`dayLow` |
| Sheet grid | `COLUMNS`, `GRID_W/H/GAP/ROW/TOP`, `GRID_RADIUS` |
| Scrim opacity | `drawSheet()` first line `0xA8000000` |
| Toast look | `drawToast()` |
| Settings type/layout | `SettingsActivity` paints + `LIST_X/LIST_R/LIST_TOP/ROW_H/…` |

Anything **not** in this table is either data (preferences, app list, TMDB) or frozen motion.

---

## 9. Re-verifying on the TV

```bash
adb connect 192.168.31.247:5555
adb -s 192.168.31.247:5555 shell input keyevent KEYCODE_HOME
adb -s 192.168.31.247:5555 exec-out screencap -p > /tmp/opencode/tvtest/home.png
# drawer:
adb -s 192.168.31.247:5555 shell input keyevent KEYCODE_DPAD_DOWN
# All Apps: Shortcuts › All Apps, or open Settings and pick Shelf / Top Buttons
adb -s 192.168.31.247:5555 logcat -d -b crash | grep -c FATAL
```

Reference captures: `/tmp/opencode/tvtest/live_home_1.png`, `live_drawer.png`,
`live_movies.png`, `live_shows.png`.

---

## 10. Amending this map

When a change lands, update the affected section (geometry, state, code pointer) in the
same commit. Section IDs are the vocabulary for change requests — keep them stable; add new
IDs for new surfaces rather than renaming existing ones.
