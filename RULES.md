# Your TV — Project Rule Book

**Status: active from 2026-10-27.**
This rule book overrides convenience. Read it before touching code. When in doubt, stop and ask.

---

## 1. Prime directive

> **The launcher already works. From this point on we only do UI work and new
> features. Nothing that currently works may change behaviour.**

Allowed by default:
- Visual/UI polish: spacing, color, typography, layout of new surfaces, new screens.
- **New** features that add something without altering existing flows.

Requires explicit approval **before** implementation:
- Anything that changes behaviour, timing, motion, or a protected component (§3).
- Anything that could be perceived differently by the user (even a “small” fix).

Forbidden without a new, explicit instruction from the user:
- Rewriting or refactoring existing working code.
- Replacing libraries, animators, or architectural patterns.
- Deleting features, screens, preferences, or shortcuts.
- “Cleaning up” code around a UI change.

## 2. Scope of a change

A change is in scope only if it is **purely additive** (new screen / new UI element /
new optional feature) or **purely cosmetic** on a surface we were asked to tweak.

If a UI tweak cannot be delivered without touching a protected component, the default
answer is: **do not deliver it yet** — propose it and wait for a decision.

## 3. Protected core (do not modify without approval)

These work today and are treated as frozen:

| Area | Examples |
|---|---|
| **Animations & motion** | springs, fades, scales, focus transitions, 3D flip, intro motion, screensaver motion, all durations/easings |
| **App icons** | icon assets, icon sizing inside cards, icon loading/caching |
| **App cards** | existing card geometry, focus ring, hover/scale behaviour, label treatment |
| **Launcher navigation** | D-pad rules, focus targets, Home/Back handling, row/column traversal, edge behaviour |
| **Launch flows** | how apps launch, passthrough/TIF inputs, deep links, intents, defaults |
| **System integration** | Home key service, remote key handling, watchdogs, sleep/screensaver, boot behaviour |
| **Data & state** | `Prefs` keys/shape, persisted settings, saved order/sizes, mode switching (Minimal/Super) |
| **Content pipelines** | TMDB recommendations, weather fetch/slots, image loader, network use, caching |
| **Settings** | existing settings entries, their semantics, and what they toggle |

Also frozen: anything not visibly part of the requested change.

## 4. Approval protocol

Before implementing anything that is not purely cosmetic/additive:

1. **Stop.**
2. State: what is protected, why it must change, and the smallest possible change.
3. Offer 2–3 options with impact (visuals only vs. behaviour).
4. Ask for an explicit **“go”**. Silence or a related question is not approval.

Rules:
- One decision at a time; do not bundle approvals.
- If the user approves option A, implement **only** A — no adjacent “while I'm here” edits.
- If implementation reveals a bigger problem than described, stop again.

Template:

```
APPROVAL NEEDED
Area:        <protected component>
Why:         <what the UI/feature needs>
Smallest fix:<one sentence>
Options:     A) ... B) ... C) leave as-is for now
Impact:      visuals only / changes behaviour: <what>
→ Waiting for your go-ahead.
```

## 5. Anti-drift rules

- Do not reformat, rename, reorder imports, or alter comments outside the edited lines.
- Do not upgrade dependencies, Gradle, SDK, or tooling as part of a UI task.
- Do not delete “unused” code — report it and let the user decide.
- Do not change default palette, default mode, or default focus on startup.
- Do not add analytics, telemetry, network calls, or permissions.
- Do not “fix” a protected behaviour you notice while working elsewhere — note it.
- Keep diffs minimal and reviewable; one concern per commit.

## 6. Standard workflow for every change

1. `git status` — start from a clean or explained state.
2. `graphify query "<question>"` before reading source (project graph rules in `AGENTS.md`).
3. Read only the files/lines the change requires.
4. Classify: **cosmetic / additive** (proceed) or **protected** (ask first).
5. Implement the smallest diff that satisfies the request.
6. Build: `cd launcher && ./gradlew assembleRelease`.
7. Install & verify on the TV via `build-and-install.sh` (`TV_HOST=… STOCK_LAUNCHER=none`) — release build only; debug builds install as a separate app.
8. Check `adb logcat -d -b crash` is empty and the requested screen works with a screenshot.
9. `graphify update .`
10. Commit with a scoped message.

## 7. Definition of done (UI / feature work)

- [ ] Requested visual/feature change is visible and matches the reference.
- [ ] No existing behaviour, motion, navigation, or icon rendering changed.
- [ ] No unrelated files touched in the diff.
- [ ] Release build succeeds and installs on the TV.
- [ ] Crash buffer clean; settings/state preserved after the change.
- [ ] Graph updated.

## 8. Rollback

Known-good references (2026-10-27):
- branch `backup/pre-ui-tweaks-20261027`, tag `backup-pre-ui-tweaks-20261027`
- archive `../climin-tv-friend-backups/climin-tv-friend-pre-ui-tweaks-20261027.tar.gz` (made before the rename)
- verified APK `../climin-tv-friend-backups/app-release-verified-20261026.apk`

Work happens on `ui-tweaks`. Never rewrite or delete a backup branch/tag.
Commit each accepted change so any single tweak can be reverted alone.

## 9. Amending this rule book

Only the user amends these rules. To change a rule, propose the edit and wait for
approval — same protocol as §4. When a new decision becomes a standing rule, record
it here so the next session inherits it.
