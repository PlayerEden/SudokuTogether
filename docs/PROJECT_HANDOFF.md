# Sudoku Together — CURRENT PROJECT HANDOFF

> **Read this first in a new ChatGPT conversation or after a context reset.**
>
> Last reconciled: **2026-10-27 US Eastern / 2026-10-28 UTC**.
> Game priority: **finish Sudoku Together before building any other game**.
> This file is the living development handoff. Update it after every meaningful milestone, QA run, architectural decision, bug discovery, or shipped build.

## 1. Where the real project lives

- Repository: https://github.com/PlayerEden/SudokuTogether
- **Development/source branch:** [`development`](https://github.com/PlayerEden/SudokuTogether/tree/development) — full native Android source, game rules, tests, Gradle wrapper, previous docs. Use this as the starting point for the **next** development slice, and update it when a new slice is verified.
- **Latest milestone feature branch:** [`feat/v0.8d-solo-assists`](https://github.com/PlayerEden/SudokuTogether/tree/feat/v0.8d-solo-assists) — new optional Solo Hint and pencil-cleanup features, draft [PR #19](https://github.com/PlayerEden/SudokuTogether/pull/19). Previous v0.8C implementation: [PR #17](https://github.com/PlayerEden/SudokuTogether/pull/17).
- **Latest known-green source checkpoint:** [`checkpoint/v0.8d-qa-green`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8d-qa-green) at commit `89fdd86163ff4c414c96a03689b4246c21b396e2`. Previous v0.8C checkpoint: [`checkpoint/v0.8c-qa-green`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8c-qa-green). Preserve both unchanged for rollback.
- **IMPORTANT:** default [`main`](https://github.com/PlayerEden/SudokuTogether/tree/main) is still a **small documentation/workflow skeleton**, not the full working Android project. It links to this handoff. **Do not conclude the game is missing because you checked only `main`.**
- GitHub PRs #1, #2, #3, #4, #5, #6, #7, #8, #9, #15, #17 and #19 are a stacked *draft history*. Do not independently merge them out of order or delete their branches without first planning and verifying consolidation. The eventual `main` promotion is tracked in [repository issue #18](https://github.com/PlayerEden/SudokuTogether/issues/18); it must not delay the next playable feature.

## 2. Current playable product — v0.8D (development QA)

**Native Android** (Kotlin / Jetpack Compose, not HTML/WebView), 9×9 classic Sudoku. Modes:
- Solo/offline with local persistence, Undo, Notes, erase, completion and records.
- Two Android phones on **the same Wi-Fi**: host and join a shared co-op board, guest reconnect, room code/copy, chat, player styling.
- Local head-to-head versus with independent boards and completion checks.
- Player identity, local achievements, earned cosmetics, profile, haptic preference, accessible selection/row-column-box highlights, fixed bottom number dock, per-digit remaining counts.
- Fresh generated uniquely solvable puzzles with 64 recent-puzzle ID avoidance.
- **Easy, Medium, Hard** selection before a new game. Difficulty derives from a deduction-only technique grader (singles; locked candidates; naked pairs), not clue count alone. Tier selector refuses silent downgrades; generated puzzles must match the requested technique grade.
- Difficulty travels inside the puzzle/host-authoritative snapshot and is retained for rematches; legacy records and old saves without grade load as **Legacy** instead of being falsely relabeled.
- **Optional Solo Hint:** opens a logical deduction based on the player's visible grid; shows a location, number and reasoning but never fills it. If user-entered numbers make the grid impossible, no speculative hint is given. Hint use is persisted before display.
- **Optional Solo pencil-mark cleanup:** disabled by default, enabled in profile. A new placed digit removes that candidate from related empty cells; Undo restores both the number and automatically removed notes.
- **Assisted result tracking:** saved Solo progress remembers hint usage and note-assist usage; completed Solo records display `Assisted`. Older saved games and history remain readable using defaults.
- Assistance is **Solo-only** pending a fair, host-authoritative policy for competitive/co-op rooms.
- LAN wire protocol remains **v5**, meaning **both phones must use a matching QA version**. Debug QA app uses application ID suffix `.qa`; version `0.8.0-dev4`, versionCode `11`.

**Limits to avoid overstating:** difficulty labels are technique tiers, not professionally calibrated difficulty; a rare Hard candidate may require generation retries. There is no internet lobby, cross-device cloud save, global rankings, Split Clues, co-op/versus hints, daily puzzle, final store identity or Google Play production release.

## 3. Verified QA evidence, not assumptions

- **v0.8D QA build:** green [run 40610640428](https://github.com/PlayerEden/SudokuTogether/actions/runs/40610640428): core tests, Android unit tests, lint, instrumentation Kotlin compilation and APK assembly.
- **v0.8D Android emulator:** green [run 40610910679](https://github.com/PlayerEden/SudokuTogether/actions/runs/40610910679): **9 of 9 instrumented tests passed**, no failures, errors or skips. The Solo Hint dialog was exercised in an actual Android emulator.
- **Exact verified source commit:** `89fdd86163ff4c414c96a03689b4246c21b396e2`, held in `checkpoint/v0.8d-qa-green`. The later commits add audit/handoff documentation, not untested game code.
- **APK artifact:** `SudokuTogether-QA-40610640428` on GitHub Actions, checksum `c1ef3be6c12b3e39774f1055be6ebf2db13e30b59454c3e98894a2d17bf12a4a`. GitHub test artifacts expire in 14 days. The source commit, Gradle project, tests, docs and checkpoint are the durable backups.
- Previous v0.8C build and emulator were green: [40607157498](https://github.com/PlayerEden/SudokuTogether/actions/runs/40607157498), [40607235385](https://github.com/PlayerEden/SudokuTogether/actions/runs/40607235385) (8/8).
- Difficulty calibration report [v0.8B](V0.8B_HUMAN_DIFFICULTY_CALIBRATION.md) remains the source for human-technique grading constraints.
- **Outstanding real-device work:** a Solo Hint and optional cleanup playthrough, upgrade/save compatibility with the user's actual Android APK, accessibility/small-screen layout, and especially the **two-physical-phone** Wi-Fi co-op/versus reliability gate. Do not claim these were physically approved because emulator tests passed.
- **Known UI gaps:** Solo time shown in two places; assisted and classic best-time leaderboards may benefit from clearer separation; selected-cell screenshot capture is not as reliable as instrumentation interaction evidence.

## 4. Exact next task

**v0.8E — focused final Sudoku UI/fairness polish**, then **v0.9A two-real-phone Wi-Fi acceptance**:

1. On a phone, confirm Solo Hint is accessible, really leaves the grid unchanged, and selects a useful square; optional pencil cleanup/Undo behaves reliably after restart.
2. Improve first-time help for the optional assists; remove duplicate Solo timer and check 200% system font, narrow/landscape layouts, TalkBack and touch targets.
3. Distinguish assisted vs unassisted best times accurately, preserve historical records and define fair achievements without retroactive score rewrites.
4. Do not add co-op/versus hints until the rules are symmetric, host-validated and cannot leak private information.
5. Verify new APK with core and Android tests, capture screenshots if meaningful, and record exact SHA/run.
6. **Next blocking gate:** two physical phones on the same Wi-Fi for hosting, discovering, joining, notes, conflicts, chat, completion, background/force-stop, reconnect and versus. No Split Clues until this passes.

Planning issues: [v0.8 assist #11](https://github.com/PlayerEden/SudokuTogether/issues/11), [v0.9 LAN #12](https://github.com/PlayerEden/SudokuTogether/issues/12), [v0.10 #13](https://github.com/PlayerEden/SudokuTogether/issues/13), [v0.11 #14](https://github.com/PlayerEden/SudokuTogether/issues/14).
Latest audit: [`V0.8D_SOLO_ASSISTS_AUDIT.md`](V0.8D_SOLO_ASSISTS_AUDIT.md).

## 5. Remaining release roadmap

- **v0.8E:** assist-result fairness, duplicate-timer cleanup, native accessibility/font sizing, real-phone Solo assist check; v0.8D Solo hints and note assistance are implemented.
- **v0.9A/B:** same-Wi-Fi two-phone host/discovery/manual join, rejoin after app background/kill, saved state, chat, racing, edit conflicts, error feedback, QR invitation. Log physical device evidence.
- **v0.9C:** **Split Clues** optional collaborative mode. Each phone sees a different subset of givens; cannot leak through guest network state, notes, hints, previews, screen-reader semantics, or saved mirror. This depends on v0.9A.
- **v0.10:** daily puzzle with offline-date handling, fair records and achievement progression, more replayability, useful end-of-match stats.
- **v0.11:** distinctive legal/brand review, app icon and store visuals, Android/Play target API validation at submission, signing/AAB, privacy/data safety, closed testing eligibility, accessibility, performance/stability and launch gates.
- **Later only:** reusable native Android puzzle-game template so future crossword/Minesweeper-type games can reuse infrastructure. Concept preserved in [`REUSABLE_ANDROID_PUZZLE_FOUNDATION.md`](REUSABLE_ANDROID_PUZZLE_FOUNDATION.md) and [issue #16](https://github.com/PlayerEden/SudokuTogether/issues/16). **Paused at user request.** Do not begin extracting it while Sudoku unfinished.

Full acceptance matrix: [`ROADMAP_V08_TO_LAUNCH.md`](ROADMAP_V08_TO_LAUNCH.md).
Milestone QA: [`V0.8D_SOLO_ASSISTS_AUDIT.md`](V0.8D_SOLO_ASSISTS_AUDIT.md), [`V0.8C_DIFFICULTY_SELECTION_HANDOFF.md`](V0.8C_DIFFICULTY_SELECTION_HANDOFF.md).
Game specs: [original design](superpowers/specs/2026-10-25-sudoku-together-design.md) and [co-op extras](superpowers/specs/2026-10-25-co-op-extras-design.md).
Device checklist: [`TESTING.md`](TESTING.md).

## 6. Project decisions and development standards

- User wants a **genuine, intuitive native mobile game** rather than AI-generated-feeling panels, unnecessary copy, or a browser-like UI. Keep Sudoku central; use thumb-friendly dock, calm cream/forest palette, native Material/Compose controls and deliberate feedback.
- Preserve familiar Sudoku rules and optional assistance, while adding differentiated teamwork, meaningful replayability and natural onboarding.
- Play from separate phones on same Wi-Fi (no remote multiplayer promise at launch); no account required.
- Host-authoritative state with appropriate per-player projections; protect answers/clues when Split Clues is introduced.
- Do not wipe player saves; treat serialization schema defaults, Android signing and package identifiers seriously.
- **GitHub is development memory**: after a substantive pass, update this handoff, roadmap statuses and testing evidence, link from default README, note known issues/next tests and preserve a known-green checkpoint. Keep the source and docs in GitHub even if chats fill up.
- The user typically wants actual changes, verified APKs **downloadable directly in chat**, and meaningful before/after emulator screenshots where possible. Do not claim a tool build succeeded without confirming a completed green CI run.
- GitHub Actions public QA workflows are opt-in/manual; avoid unplanned automatic runs and resource waste.
- The `main` and `development` copies of QA/emulator workflows now both dispatch from the real `development` source by default. **Do not treat a successful workflow from a stale branch as evidence for untested current changes.** GitHub stores history remotely; for disaster resilience, periodically clone or export the repository to a separate device/drive as a truly independent backup.

## 7. New-chat recovery procedure

1. Open [repo README on `main`](https://github.com/PlayerEden/SudokuTogether/blob/main/README.md), which points here.
2. Fetch the **latest** [`development`](https://github.com/PlayerEden/SudokuTogether/tree/development) and this file. **Also check the newest branch, draft PR, issues and actual GitHub Actions run**, since this static handoff may lag behind live work.
3. Consult [roadmap](ROADMAP_V08_TO_LAUNCH.md), [current milestone audit](V0.8C_DIFFICULTY_SELECTION_HANDOFF.md), and relevant tests/design before editing.
4. For any new feature, create a small branch off the newest verified source and note which checkpoint it descends from. Preserve previous milestone source in Git history and checkpoint refs.
5. Implement, review, run core/Android tests and emulator QA, note manual gaps. Deliver APK when verified. Update the **living handoff**, root pointer if branch changes, and `development` **only after the milestone meets its stated acceptance gate**.
6. If something fails, recover from the checkpoint branch or last known-green commit, not from a guess about what a previous chat accomplished.

## 8. Source-of-truth precedence

Actual committed source code + test results > verified CI runs > this living handoff > draft design/plans > chat discussion. A design proposal is **not** an implemented feature. Never infer a Play release or two-phone test from a passing emulator build.
