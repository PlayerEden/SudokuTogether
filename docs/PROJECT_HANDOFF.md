# Sudoku Together — CURRENT PROJECT HANDOFF

> **Read this first in a new ChatGPT conversation or after a context reset.**
>
> Last reconciled: **2026-10-27 US Eastern / 2026-10-28 UTC**.
> Game priority: **finish Sudoku Together before building any other game**.
> This file is the living development handoff. Update it after every meaningful milestone, QA run, architectural decision, bug discovery, or shipped build.

## 1. Where the real project lives

- Repository: https://github.com/PlayerEden/SudokuTogether
- **Development/source branch:** [`development`](https://github.com/PlayerEden/SudokuTogether/tree/development) — full native Android source, game rules, tests, Gradle wrapper, previous docs. Use this as the starting point for the **next** development slice, and update it when a new slice is verified.
- **Latest milestone feature branch:** [`feat/v0.10a-offline-daily`](https://github.com/PlayerEden/SudokuTogether/tree/feat/v0.10a-offline-daily) — offline Daily Challenge, independent progress and local records, [PR #21](https://github.com/PlayerEden/SudokuTogether/pull/21). Previous v0.8E [PR #20](https://github.com/PlayerEden/SudokuTogether/pull/20).
- **Latest known-green source checkpoint:** [`checkpoint/v0.10a-qa-green`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.10a-qa-green) at commit `147835bd7752ae390d6f0a0bd7857d05eb45f7f0`. Previous checkpoints: [`v0.8E`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8e-qa-green), [`v0.8D`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8d-qa-green) and [`v0.8C`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8c-qa-green). Do not overwrite.
- **IMPORTANT:** default [`main`](https://github.com/PlayerEden/SudokuTogether/tree/main) is still a **small documentation/workflow skeleton**, not the full working Android project. It links to this handoff. **Do not conclude the game is missing because you checked only `main`.**
- GitHub PRs #1, #2, #3, #4, #5, #6, #7, #8, #9, #15, #17, #19, #20 and #21 are a stacked *draft history*. Do not independently merge them out of order or delete their branches without first planning and verifying consolidation. The eventual `main` promotion is tracked in [repository issue #18](https://github.com/PlayerEden/SudokuTogether/issues/18); it must not delay the next playable feature.

## 2. Current playable product — v0.10A (development QA)

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
- LAN wire protocol remains **v5** (two-phone testing deferred). Debug QA app uses application ID suffix `.qa`; version `0.10.0-dev1`, versionCode `13`.

- **Offline Daily Challenge (v0.10A):** a new home card offers one uniquely solvable, logically Easy puzzle per device-local calendar date. Date-salted versioned SHA-256 selection ensures the same dated puzzle across restarts, without internet. Daily sessions use separately persisted progress/history and records (up to 31 saved daily date slots). Normal Solo progress, records and multiplayer sessions remain untouched. Daily hints, notes, Undo and elapsed time reuse tested Solo behavior. Daily completion is idempotent, date-tagged and assisted-aware; daily records do **not** count as regular Solo wins/bests. The user can review a finished day's board but cannot create another new board for the same date.
- **Limits to avoid overstating:** difficulty labels are human-technique tiers, not calibrated difficulty ratings; Daily is Easy-only in this first slice. There is no internet lobby, online synced daily contest, cloud save, global rankings, Split Clues, co-op/versus hints, final store identity or Google Play production release.

## 3. Verified QA evidence, not assumptions

- **v0.10A full Android build:** [run 40616087461](https://github.com/PlayerEden/SudokuTogether/actions/runs/40616087461) **SUCCESS**, core tests (including DailyChallengeTest), Android unit tests, lint, instrumentation compilation and isolated QA APK assembly.
- **v0.10A Android emulator:** initial [run 40616092688](https://github.com/PlayerEden/SudokuTogether/actions/runs/40616092688) **failed 2 legacy profile-navigation tests**, because the new Daily card moved the profile button below the visible fold and those tests clicked without scrolling. The new daily/regular Solo coexistence test passed even in that run. Corrected tests using `performScrollTo()`.
- **Corrected Android emulator:** [run 40616779175](https://github.com/PlayerEden/SudokuTogether/actions/runs/40616779175) **SUCCESS, 10/10 tests, 0 failures/errors/skips**, confirmed from Android JUnit XML in artifact. Covers Daily + Solo persistence, hint, profile, solo/resume, host, race and notes.
- **Exact tested code checkpoint:** `147835bd7752ae390d6f0a0bd7857d05eb45f7f0` on [`checkpoint/v0.10a-qa-green`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.10a-qa-green). Later commits change audit/handoff documents only.
- **QA APK:** `SudokuTogether-QA-40616087461`, size **10,729,452 bytes**, SHA-256 `7a6f108b8e4630b99c95a787cdebb12d5d161e0ebba109ee16df57f52a2bda2a` verified. The build precedes only a smoke-test scroll correction; app code is identical. GitHub Actions artifacts expire (~14 days), so committed source and checkpoint are the durable fallback.
- **Visual evidence:** actual Android emulator screenshot `screenshots/01-home.png` shows Daily card and familiar mode chooser. The player-profile card now requires scrolling on compact screens; emulator interaction explicitly tests that behavior. **No gameplay/profile screenshot or 200%-font/TalkBack physical certification** is claimed.
- **Still unverified physically:** real-phone daily restart, local-midnight date rollover, small-font/large-font readability, signed APK upgrade path and user's Solo saves. Physical two-phone Wi-Fi co-op/versus testing is **explicitly deferred until user is ready**, not waived or passed.
- Previous v0.8E green tests: [build 40612864973](https://github.com/PlayerEden/SudokuTogether/actions/runs/40612864973), [emulator 40612871881](https://github.com/PlayerEden/SudokuTogether/actions/runs/40612871881), 9/9 cases. Detailed daily QA: [`V0.10A_DAILY_CHALLENGE_QA.md`](V0.10A_DAILY_CHALLENGE_QA.md).

## 4. Exact next task — user-approved development order

**Next: v0.10B — richer replayability and post-game summaries (single-device work).**

1. Improve the completion summary for standard Solo and Daily Challenge with elapsed time, difficulty, assist status, moves, and clear return/replay actions. Never mislabel daily as normal Solo or compare assisted and unassisted scores as equivalent.
2. Expand local daily challenge history/records presentation, with transparent offline-only counts, optional non-coercive progression. Avoid online/global rankings or fabricated worldwide statistics.
3. Polish the home-screen scrolling and native layouts at large Android font sizes, test daily/solo completion pathways and save/restart behavior.
4. Add regression and Android UI tests, capture relevant screenshots, build a verified APK. Record exact SHA/QA evidence and update this living handoff and roadmap.
5. Continue toward v0.11 release readiness when single-phone quality is stable, but preserve the physical multiplayer gate.

**User's explicit deferral stays in effect:** do **not** push for a two-phone playtest; v0.9A is still **pending / unverified**, and private Split Clues/QR-backed multiplayer release acceptance must wait for actual LAN evidence. The user will indicate when they are ready.

Roadmap: [v0.10 issue #13](https://github.com/PlayerEden/SudokuTogether/issues/13) active; [v0.9 LAN issue #12](https://github.com/PlayerEden/SudokuTogether/issues/12) deferred; [v0.11 #14](https://github.com/PlayerEden/SudokuTogether/issues/14) later.

## 5. Remaining release roadmap

- **v0.8E:** fair assisted/unassisted records, duplicate-timer cleanup, explanatory help and touch-target improvements **implemented and Android-tested**. Physical accessibility/font and Solo assist playtests are still pending.
- **v0.9A/B:** two-phone LAN playtest and invite/reconnect validation **deferred until user is ready**; still a required gate before multiplayer release claims. Can work on non-device-dependent robustness/tests separately.
- **v0.9C:** **Split Clues** remains blocked behind actual LAN gate. Optional collaborative mode. Each phone sees a different subset of givens; cannot leak through guest network state, notes, hints, previews, screen-reader semantics, or saved mirror. This depends on v0.9A.
- **v0.10A implemented/Android-verified:** daily puzzle with local calendar date, independent saves and assisted-aware results. **v0.10B next:** stronger completion summary, daily history and native usability.
- **v0.11:** distinctive legal/brand review, app icon and store visuals, Android/Play target API validation at submission, signing/AAB, privacy/data safety, closed testing eligibility, accessibility, performance/stability and launch gates.
- **Later only:** reusable native Android puzzle-game template so future crossword/Minesweeper-type games can reuse infrastructure. Concept preserved in [`REUSABLE_ANDROID_PUZZLE_FOUNDATION.md`](REUSABLE_ANDROID_PUZZLE_FOUNDATION.md) and [issue #16](https://github.com/PlayerEden/SudokuTogether/issues/16). **Paused at user request.** Do not begin extracting it while Sudoku unfinished.

Full acceptance matrix: [`ROADMAP_V08_TO_LAUNCH.md`](ROADMAP_V08_TO_LAUNCH.md).
Milestone QA: [`V0.10A_DAILY_CHALLENGE_QA.md`](V0.10A_DAILY_CHALLENGE_QA.md), [`V0.8E_SOLO_FAIRNESS_QA.md`](V0.8E_SOLO_FAIRNESS_QA.md), [`V0.8D_SOLO_ASSISTS_AUDIT.md`](V0.8D_SOLO_ASSISTS_AUDIT.md), [`V0.8C_DIFFICULTY_SELECTION_HANDOFF.md`](V0.8C_DIFFICULTY_SELECTION_HANDOFF.md).
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
3. Consult [roadmap](ROADMAP_V08_TO_LAUNCH.md), [current milestone audit](V0.10A_DAILY_CHALLENGE_QA.md), and relevant tests/design before editing.
4. For any new feature, create a small branch off the newest verified source and note which checkpoint it descends from. Preserve previous milestone source in Git history and checkpoint refs.
5. Implement, review, run core/Android tests and emulator QA, note manual gaps. Deliver APK when verified. Update the **living handoff**, root pointer if branch changes, and `development` **only after the milestone meets its stated acceptance gate**.
6. If something fails, recover from the checkpoint branch or last known-green commit, not from a guess about what a previous chat accomplished.

## 8. Source-of-truth precedence

Actual committed source code + test results > verified CI runs > this living handoff > draft design/plans > chat discussion. A design proposal is **not** an implemented feature. Never infer a Play release or two-phone test from a passing emulator build.
