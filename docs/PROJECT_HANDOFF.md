# Sudoku Together — CURRENT PROJECT HANDOFF

> **Read this first in a new ChatGPT conversation or after a context reset.**
>
> Last reconciled: **2026-10-27 US Eastern / 2026-10-28 UTC**.
> Game priority: **finish Sudoku Together before building any other game**.
> This file is the living development handoff. Update it after every meaningful milestone, QA run, architectural decision, bug discovery, or shipped build.

## 1. Where the real project lives

- Repository: https://github.com/PlayerEden/SudokuTogether
- **Development/source branch:** [`development`](https://github.com/PlayerEden/SudokuTogether/tree/development) — full native Android source, game rules, tests, Gradle wrapper, previous docs. Use this as the starting point for the **next** development slice, and update it when a new slice is verified.
- **Latest milestone feature branch:** [`feat/v0.10b-daily-history-results`](https://github.com/PlayerEden/SudokuTogether/tree/feat/v0.10b-daily-history-results) — Daily History, real completion summaries, safe unfinished-puzzle retention, draft [PR #22](https://github.com/PlayerEden/SudokuTogether/pull/22). Previous v0.10A implementation [PR #21](https://github.com/PlayerEden/SudokuTogether/pull/21).
- **Latest known-green source checkpoint:** [`checkpoint/v0.10b-qa-green`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.10b-qa-green) at exact code commit `83df54621bb2713917b518ee8ccfed4c25511a80`. Earlier verified checkpoints: [`v0.10A`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.10a-qa-green), [`v0.8E`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8e-qa-green), [`v0.8D`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8d-qa-green) and [`v0.8C`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8c-qa-green). Never force-push/delete these.
- **IMPORTANT:** default [`main`](https://github.com/PlayerEden/SudokuTogether/tree/main) is still a **small documentation/workflow skeleton**, not the full working Android project. It links to this handoff. **Do not conclude the game is missing because you checked only `main`.**
- GitHub PRs #1, #2, #3, #4, #5, #6, #7, #8, #9, #15, #17, #19, #20, #21 and #22 are a stacked *draft history*. Do not independently merge them out of order or delete their branches without first planning and verifying consolidation. The eventual `main` promotion is tracked in [repository issue #18](https://github.com/PlayerEden/SudokuTogether/issues/18); it must not delay the next playable feature.

## 2. Current playable product — v0.10B (development QA)

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
- LAN wire protocol remains **v5** (two-phone testing deferred). Debug QA app uses application ID suffix `.qa`; version `0.10.0-dev2`, versionCode `14`.

- **Offline Daily Challenge (v0.10A):** a new home card offers one uniquely solvable, logically Easy puzzle per device-local calendar date. Date-salted versioned SHA-256 selection ensures the same dated puzzle across restarts, without internet. Daily sessions use separately persisted progress/history and records, retaining up to 128 daily board slots (up to 31 completed review boards plus unfinished puzzles, bounded by 128). Normal Solo progress, records and multiplayer sessions remain untouched. Daily hints, notes, Undo and elapsed time reuse tested Solo behavior. Daily completion is idempotent, date-tagged and assisted-aware; daily records do **not** count as regular Solo wins/bests. The user can review a finished day's board but cannot create another new board for the same date.
- **v0.10B Daily History:** offline profile lists saved daily dates with completion state and elapsed time. The user can reopen any older **already saved** daily board to continue or review, but cannot generate extra backdated puzzles. Six most recent rows display first, with Show all for the full archive.
- **v0.10B safe retention:** never silently discard unfinished daily progress when 31 days pass. Preserve up to 128 unfinished slots; completed boards are pruned to the latest 31 when appropriate. If all 128 slots are unfinished, creating another challenge shows an explicit message and preserves existing data. Completed daily result records persist independently of reviewable boards.
- **v0.10B finish summary:** Solo and Daily completed games show saved elapsed time, moves, grade, assisted/unassisted status and hints used. No fabricated global times, ranks or competitor statistics.
- **Limits to avoid overstating:** difficulty labels are human-technique tiers, not calibrated difficulty ratings; Daily is Easy-only in this first slice. There is no internet lobby, online synced daily contest, cloud save, global rankings, Split Clues, co-op/versus hints, final store identity or Google Play production release.

## 3. Verified QA evidence, not assumptions

- **v0.10B Kotlin/Android full build:** [run 40621938766](https://github.com/PlayerEden/SudokuTogether/actions/runs/40621938766) **SUCCESS**: core tests, Android JVM unit tests, lint, instrumentation Kotlin compilation and isolated Android APK assembly.
- **v0.10B Android emulator:** [run 40622291879](https://github.com/PlayerEden/SudokuTogether/actions/runs/40622291879) **SUCCESS, 10/10 tests, 0 failures/errors/skips** (confirmed from uploaded Android JUnit XML). Includes Daily History → reopen daily board, Solo persistence, Hint, profile, co-op/versus regressions.
- **Second v0.10B Android emulator:** [run 40623046297](https://github.com/PlayerEden/SudokuTogether/actions/runs/40623046297) **SUCCESS, 10/10 cases, no failures/errors/skips**. The screenshot artifact only contains Home: an intermittent emulator system Wait overlay/ADB accessibility crawler prevented reliable Daily/Profile image capture. **UI interaction confirms navigation, but no visual proof of the new Daily History/profile/finish card is claimed.** Next visual pass should use Compose-owned screenshot captures.
- **One failed, corrected first regression:** [build 40621625278](https://github.com/PlayerEden/SudokuTogether/actions/runs/40621625278) had one assertion expecting 32 retained slots; the correct count was 33 (two unfinished plus 31 completed). Test corrected and full build subsequently passed.
- **v0.10B QA APK:** `SudokuTogether-QA-40621938766`, **10,745,836 bytes**, SHA-256 `c931401634c25403301141256a97f4165f2d2310bb38886b55bf9d1b964eb917`, verified against GitHub's checksum file. Actions artifacts expire (14 days); committed source, tests, handoff and a pinned checkpoint are durable.
- **Previous v0.10A green:** [build 40616087461](https://github.com/PlayerEden/SudokuTogether/actions/runs/40616087461), [Android emulator 40616779175](https://github.com/PlayerEden/SudokuTogether/actions/runs/40616779175) (10/10).
- **Still unverified physically:** user's actual phone Daily History/complete-game results, save upgrade, local midnight transition, narrow screen, TalkBack and large fonts. Two-phone Wi-Fi testing is **explicitly deferred by user**, not passed or waived. Never declare Split Clues/network release ready before real-device LAN acceptance.
- Specific audit: [`V0.10B_DAILY_HISTORY_QA.md`](V0.10B_DAILY_HISTORY_QA.md).

## 4. Exact next task — user-approved development order

**Next: v0.10C / v0.11 — restrained, single-phone improvements toward launch readiness**, and preserve physical multiplayer gate:

1. Review actual Daily History and Solo/Daily completed-game screens, including large-font/landscape accessibility and sensible touch targets. Capture actual on-device screenshots where possible.
2. Assess measured offline replayability: meaningful daily achievements or streak presentation that never pressures users, misreports missed days, or pretends online/global competition. Protect existing saves and avoid pointless gamification.
3. Polish Android release requirements: app identity distinctiveness, privacy/data safety inventory, target API, app signing/AAB, store listing/testing eligibility and update path. **Do not publish** until verified.
4. Add core regression + emulator tests, produce QA APK and verify checksum. Maintain GitHub docs, milestone branch and known-green checkpoint.
5. When the user volunteers to test with two phones, run v0.9A real-device co-op/versus checklist; do not prompt or assume readiness. Keep QR/privacy-sensitive Split Clues blocked until this passes.

Roadmap: [v0.10 issue #13](https://github.com/PlayerEden/SudokuTogether/issues/13) active; [v0.9 LAN issue #12](https://github.com/PlayerEden/SudokuTogether/issues/12) deferred; [v0.11 issue #14](https://github.com/PlayerEden/SudokuTogether/issues/14) later.

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
