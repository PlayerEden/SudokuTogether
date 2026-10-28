# Sudoku Together — CURRENT PROJECT HANDOFF

> **Read this first in a new ChatGPT conversation or after a context reset.**
>
> Last reconciled: **2026-10-27 US Eastern / 2026-10-28 UTC**.
> Game priority: **finish Sudoku Together before building any other game**.
> This file is the living development handoff. Update it after every meaningful milestone, QA run, architectural decision, bug discovery, or shipped build.

## 1. Where the real project lives

- Repository: https://github.com/PlayerEden/SudokuTogether
- **Development/source branch:** [`development`](https://github.com/PlayerEden/SudokuTogether/tree/development) — full native Android source, game rules, tests, Gradle wrapper, previous docs. Use this as the starting point for the **next** development slice, and update it when a new slice is verified.
- **Latest milestone feature branch:** [`polish/v0.8e-solo-fairness`](https://github.com/PlayerEden/SudokuTogether/tree/polish/v0.8e-solo-fairness) — duplicate Solo timer removed, separate assisted/unassisted records and improved help/accessibility labels, [PR #20](https://github.com/PlayerEden/SudokuTogether/pull/20). Previous v0.8D: [PR #19](https://github.com/PlayerEden/SudokuTogether/pull/19).
- **Latest known-green source checkpoint:** [`checkpoint/v0.8e-qa-green`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8e-qa-green) at commit `8634920c9a67630c177a96d471aeb983a854ad30`. Previous verified checkpoints: [`v0.8D`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8d-qa-green) and [`v0.8C`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8c-qa-green). Never overwrite these branches.
- **IMPORTANT:** default [`main`](https://github.com/PlayerEden/SudokuTogether/tree/main) is still a **small documentation/workflow skeleton**, not the full working Android project. It links to this handoff. **Do not conclude the game is missing because you checked only `main`.**
- GitHub PRs #1, #2, #3, #4, #5, #6, #7, #8, #9, #15, #17, #19 and #20 are a stacked *draft history*. Do not independently merge them out of order or delete their branches without first planning and verifying consolidation. The eventual `main` promotion is tracked in [repository issue #18](https://github.com/PlayerEden/SudokuTogether/issues/18); it must not delay the next playable feature.

## 2. Current playable product — v0.8E (development QA)

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
- **v0.8E polish:** Solo timer is displayed once above the board; hints and Help have 48dp minimum touch targets and explicit accessibility descriptions; Help explains the opt-in controls.
- **Fair records:** Solo shows separate assisted and unassisted bests, overall and per difficulty. The old records stay intact; older runs without assist metadata are correctly treated as unassisted before the assist feature existed. Legacy difficulty remains unassigned. Speed achievement criteria remain unchanged to avoid revoking historical earned rewards.
- LAN wire protocol remains **v5**, meaning **both phones must use a matching QA version**. Debug QA app uses application ID suffix `.qa`; version `0.8.0-dev5`, versionCode `12`.

**Limits to avoid overstating:** difficulty labels are technique tiers, not professionally calibrated difficulty; a rare Hard candidate may require generation retries. There is no internet lobby, cross-device cloud save, global rankings, Split Clues, co-op/versus hints, daily puzzle, final store identity or Google Play production release.

## 3. Verified QA evidence, not assumptions

- **v0.8E full build green:** [run 40612864973](https://github.com/PlayerEden/SudokuTogether/actions/runs/40612864973) — unit tests (including SoloRecordSummaryTest), lint, instrumented Kotlin compilation and isolated Android APK assembly passed.
- **v0.8E real Android emulator green:** [run 40612871881](https://github.com/PlayerEden/SudokuTogether/actions/runs/40612871881) — **9/9 instrumented Android tests passed**, zero failures, errors or skipped tests. Includes Help, Hint, profile records, Solo/resume and multiplayer host/race regression checks.
- **Exact verified code checkpoint:** `8634920c9a67630c177a96d471aeb983a854ad30`, saved as `checkpoint/v0.8e-qa-green`. Later commits update documentation only.
- **QA APK:** artifact `SudokuTogether-QA-40612864973`, 10,713,064 bytes; SHA-256 `c64e776985aa56f39b3a63e56a865c4473c41275826af5f678730724041ddeb9`, matching GitHub checksum. GitHub Actions artifacts expire after ~14 days; durable backups are source/tests/commit/tag references.
- **Visual coverage:** the actual emulator home screenshot was inspected and is readable; **no exported gameplay/profile screenshot** for this pass. Do not claim 200% font, TalkBack or records screen appearance was visually audited merely from automated tests.
- Previous v0.8D passes: build [40610640428](https://github.com/PlayerEden/SudokuTogether/actions/runs/40610640428) and emulator [40610910679](https://github.com/PlayerEden/SudokuTogether/actions/runs/40610910679), 9/9 tests.
- **Still unverified physically:** install/update persistence on user's phone; Solo assistance and Undo after restart; readability at high font scaling; **two separate Android phones** sharing same-Wi-Fi co-op and versus reliably. No Split Clues until real-device LAN acceptance. The rest of the game and its future Play Store readiness remain separate milestones.
- Difficulty calibration details remain in [v0.8B report](V0.8B_HUMAN_DIFFICULTY_CALIBRATION.md).

## 4. Exact next task — temporary user-approved reorder

**User decision (2026-10-27 local): defer the real two-phone multiplayer playtest until the user is ready.** This is a scheduling decision, **not** a pass, waiver, or validation of LAN reliability. Do not request or schedule the physical test unprompted. Continue Sudoku Together work that does not rely on two phones.

**Next development target: v0.10A, offline Daily Challenge / replayability foundation.**

1. Plan one stable, uniquely solvable 9×9 daily puzzle, with a clearly documented local date/time-zone rule, no internet requirement, and a difficulty policy that does not invent grades.
2. Ensure daily puzzle selection is deterministic across restarts, does not overwrite or corrupt an existing active Solo puzzle, and uses separately persisted daily progress and completion records if needed.
3. Add useful result summaries, but distinguish daily/standard Solo, assisted/unassisted, and each difficulty; never present offline local records as global ranks.
4. Add JVM tests for same-day reproducibility, local-date rollover, old-save migration, timer integrity, uniqueness and score separation. Then Android UI tests, screenshots and a verified QA APK.
5. Keep improving native screen layouts, accessibility and reliability where this does not require two players. Maintain GitHub source, handoff, QA evidence and the known-good checkpoint.

**Deferred, still required before shipping multiplayer claims or introducing Split Clues: v0.9A physical same-Wi-Fi testing.** When the user is ready, check co-op host/join/discovery/manual IP, both-way edits/notes, conflict and Undo safety, pause/background/force-stop, reconnect, chat and versus results on two separate Android phones. Record observed evidence and defects. QR invite and private Split Clues should not be described as validated while this gate is open.

Roadmap: [v0.9 issue #12](https://github.com/PlayerEden/SudokuTogether/issues/12) remains **pending by user choice**; [v0.10 issue #13](https://github.com/PlayerEden/SudokuTogether/issues/13) becomes the active development track; [v0.11 #14](https://github.com/PlayerEden/SudokuTogether/issues/14) is later. Latest verified audit: [`V0.8E_SOLO_FAIRNESS_QA.md`](V0.8E_SOLO_FAIRNESS_QA.md).

## 5. Remaining release roadmap

- **v0.8E:** fair assisted/unassisted records, duplicate-timer cleanup, explanatory help and touch-target improvements **implemented and Android-tested**. Physical accessibility/font and Solo assist playtests are still pending.
- **v0.9A/B:** two-phone LAN playtest and invite/reconnect validation **deferred until user is ready**; still a required gate before multiplayer release claims. Can work on non-device-dependent robustness/tests separately.
- **v0.9C:** **Split Clues** remains blocked behind actual LAN gate. Optional collaborative mode. Each phone sees a different subset of givens; cannot leak through guest network state, notes, hints, previews, screen-reader semantics, or saved mirror. This depends on v0.9A.
- **v0.10:** **next active development track**: daily puzzle with offline-date handling, fair records and achievement progression, more replayability, useful end-of-match stats.
- **v0.11:** distinctive legal/brand review, app icon and store visuals, Android/Play target API validation at submission, signing/AAB, privacy/data safety, closed testing eligibility, accessibility, performance/stability and launch gates.
- **Later only:** reusable native Android puzzle-game template so future crossword/Minesweeper-type games can reuse infrastructure. Concept preserved in [`REUSABLE_ANDROID_PUZZLE_FOUNDATION.md`](REUSABLE_ANDROID_PUZZLE_FOUNDATION.md) and [issue #16](https://github.com/PlayerEden/SudokuTogether/issues/16). **Paused at user request.** Do not begin extracting it while Sudoku unfinished.

Full acceptance matrix: [`ROADMAP_V08_TO_LAUNCH.md`](ROADMAP_V08_TO_LAUNCH.md).
Milestone QA: [`V0.8E_SOLO_FAIRNESS_QA.md`](V0.8E_SOLO_FAIRNESS_QA.md), [`V0.8D_SOLO_ASSISTS_AUDIT.md`](V0.8D_SOLO_ASSISTS_AUDIT.md), [`V0.8C_DIFFICULTY_SELECTION_HANDOFF.md`](V0.8C_DIFFICULTY_SELECTION_HANDOFF.md).
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
