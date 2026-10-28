# Sudoku Together — CURRENT PROJECT HANDOFF

> **Read this first in a new ChatGPT conversation or after a context reset.**
>
> Last reconciled: **2026-10-27 US Eastern / 2026-10-28 UTC**.
> Game priority: **finish Sudoku Together before building any other game**.
> This file is the living development handoff. Update it after every meaningful milestone, QA run, architectural decision, bug discovery, or shipped build.

## 1. Where the real project lives

- Repository: https://github.com/PlayerEden/SudokuTogether
- **Development/source branch:** [`development`](https://github.com/PlayerEden/SudokuTogether/tree/development) — full native Android source, game rules, tests, Gradle wrapper, previous docs. Use this as the starting point for the **next** development slice, and update it when a new slice is verified.
- **Historical feature branch:** [`feat/v0.8c-difficulty-selection`](https://github.com/PlayerEden/SudokuTogether/tree/feat/v0.8c-difficulty-selection) — v0.8C implementation and draft [PR #17](https://github.com/PlayerEden/SudokuTogether/pull/17).
- **Known-green code checkpoint:** [`checkpoint/v0.8c-qa-green`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8c-qa-green) at exact commit `fa08a6e4ad17fa082fc134d2c18332ecc5fba38f`. Preserve this branch unchanged as a rollback/reference point.
- **IMPORTANT:** default [`main`](https://github.com/PlayerEden/SudokuTogether/tree/main) is still a **small documentation/workflow skeleton**, not the full working Android project. It links to this handoff. **Do not conclude the game is missing because you checked only `main`.**
- GitHub PRs #1, #2, #3, #4, #5, #6, #7, #8, #9, #15 and #17 are a stacked *draft history*. Do not independently merge them out of order or delete their branches without first planning and verifying consolidation. The eventual `main` promotion is tracked in [repository issue #18](https://github.com/PlayerEden/SudokuTogether/issues/18); it must not delay the next playable feature.

## 2. Current playable product — v0.8C (development QA)

**Native Android** (Kotlin / Jetpack Compose, not HTML/WebView), 9×9 classic Sudoku. Modes:
- Solo/offline with local persistence, Undo, Notes, erase, completion and records.
- Two Android phones on **the same Wi-Fi**: host and join a shared co-op board, guest reconnect, room code/copy, chat, player styling.
- Local head-to-head versus with independent boards and completion checks.
- Player identity, local achievements, earned cosmetics, profile, haptic preference, accessible selection/row-column-box highlights, fixed bottom number dock, per-digit remaining counts.
- Fresh generated uniquely solvable puzzles with 64 recent-puzzle ID avoidance.
- **Easy, Medium, Hard** selection before a new game. Difficulty derives from a deduction-only technique grader (singles; locked candidates; naked pairs), not clue count alone. Tier selector refuses silent downgrades; generated puzzles must match the requested technique grade.
- Difficulty travels inside the puzzle/host-authoritative snapshot and is retained for rematches; legacy records and old saves without grade load as **Legacy** instead of being falsely relabeled.
- LAN wire protocol now **v5**, meaning **both phones must use a matching QA version**. Debug QA app uses application ID suffix `.qa`; version `0.8.0-dev3`, versionCode `10`.

**Limits to avoid overstating:** difficulty labels are technique tiers, not professionally calibrated difficulty; a rare Hard candidate may require generation retries. There is no internet lobby, cross-device cloud save, global rankings, Split Clues, explainable hint UI, daily puzzle, final store identity or Google Play production release.

## 3. Verified QA evidence, not assumptions

- Full build / core and Android JVM tests / lint / compile instrumentation tests / APK assembly: **PASS** [workflow run 40607157498](https://github.com/PlayerEden/SudokuTogether/actions/runs/40607157498).
- Actual Android emulator instrumentation: **PASS, 8 of 8 cases** [workflow run 40607235385](https://github.com/PlayerEden/SudokuTogether/actions/runs/40607235385). Includes new difficulty picker, game mode selection, solo resume, hosting/chat, race waiting, pencil marks, erase, Undo.
- **Working code checkpoint:** `fa08a6e4ad17fa082fc134d2c18332ecc5fba38f` as above. Later changes on development as of this handoff are documentation only.
- APK artifact: `SudokuTogether-QA-40607157498` under the build's **Artifacts**; GitHub Actions artifacts expire after **14 days**. Do not treat temporary artifacts or chat sandbox links as durable backup. **Source, tests, docs, workflow config and immutable commit history are the durable backups.**
- Prior calibration: [v0.8B report](V0.8B_HUMAN_DIFFICULTY_CALIBRATION.md), including 48 puzzles × five clue floors. At floor 36, 48/48 sampled puzzles graded Easy. At floor 24, sampled 30 Easy, 3 Medium, 3 Hard, 12 beyond current logical solver. The sampler uses an exact unique-solution check and a separate human-technique trace.
- No two-physical-phone multiplayer test evidence yet. Do not claim LAN reliability is real-device-certified solely because emulator automation passed.
- One small known UX issue: Solo gameplay displays timer twice. Fix during a polish pass.

## 4. Exact next task

**v0.8D — Optional, explainable Sudoku assistance**, no major refactoring:
1. Plan hint semantics that reveal **one valid human deduction** using current *visible* state; never fill the entire solution or reveal remote private information.
2. Distinguish hint explanation from auto-play. Player can opt in or decline; Classic play continues without hints.
3. Optional note clean-up after placement, configurable conflict/mistake visibility. Preserve undo, completion and accessibility.
4. Track whether any assistance was used for records/achievements so assisted runs aren't misrepresented as fully unassisted.
5. Add core regression tests + Android UI tests; build a verified QA APK; capture before/after screenshots and record the exact commit/run.
6. **After v0.8D:** prioritize v0.9A physical two-phone LAN acceptance *before* Split Clues.

Open planned work: [v0.8C/D issue #11](https://github.com/PlayerEden/SudokuTogether/issues/11); [v0.9 issue #12](https://github.com/PlayerEden/SudokuTogether/issues/12); [v0.10 issue #13](https://github.com/PlayerEden/SudokuTogether/issues/13); [v0.11 issue #14](https://github.com/PlayerEden/SudokuTogether/issues/14). Verify issue titles/numbers if modifying them.

## 5. Remaining release roadmap

- **v0.8D/E:** explainable hints, optional notes assistance, conflict readability, no skill-spoiling/competitive leakage, first-play instruction and device UI polish.
- **v0.9A/B:** same-Wi-Fi two-phone host/discovery/manual join, rejoin after app background/kill, saved state, chat, racing, edit conflicts, error feedback, QR invitation. Log physical device evidence.
- **v0.9C:** **Split Clues** optional collaborative mode. Each phone sees a different subset of givens; cannot leak through guest network state, notes, hints, previews, screen-reader semantics, or saved mirror. This depends on v0.9A.
- **v0.10:** daily puzzle with offline-date handling, fair records and achievement progression, more replayability, useful end-of-match stats.
- **v0.11:** distinctive legal/brand review, app icon and store visuals, Android/Play target API validation at submission, signing/AAB, privacy/data safety, closed testing eligibility, accessibility, performance/stability and launch gates.
- **Later only:** reusable native Android puzzle-game template so future crossword/Minesweeper-type games can reuse infrastructure. Concept preserved in [`REUSABLE_ANDROID_PUZZLE_FOUNDATION.md`](REUSABLE_ANDROID_PUZZLE_FOUNDATION.md) and [issue #16](https://github.com/PlayerEden/SudokuTogether/issues/16). **Paused at user request.** Do not begin extracting it while Sudoku unfinished.

Full acceptance matrix: [`ROADMAP_V08_TO_LAUNCH.md`](ROADMAP_V08_TO_LAUNCH.md).
Previous feature QA: [`V0.8C_DIFFICULTY_SELECTION_HANDOFF.md`](V0.8C_DIFFICULTY_SELECTION_HANDOFF.md).
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
