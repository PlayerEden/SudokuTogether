# GridMates Entry Checking Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans task by task. Steps use checkbox syntax. The owner authorized planning and execution in this session; implement directly, with one fresh whole-branch review at the end.

**Goal:** Ship the short Sudoku launcher label and explicit, useful Solo/Daily entry checks while preserving identity, saves and fairness.

**Architecture:** Give the existing launcher Activity a separate label resource. Extend the pure assistance engine and backward-compatible local progress metadata, wire a guarded ViewModel action through a defaulted GameActions callback, and keep coach dialogs in a focused Compose file. Existing edit, persistence, solver and transport paths remain authoritative.

**Tech Stack:** Existing Kotlin2.1.20 / Compose2025.04.01 / Gradle8.11.1 / JDK17; no new dependency.

**Spec:** `docs/superpowers/specs/2026-11-03-coach-entry-check-design.md`

## Global Constraints

- Version18 / `0.11.0-dev2-qa`; min26/compile35/target35, schema1/protocol5.
- Preserve `com.playereden.sudokutogether.qa`, launcher component/icon and v0.10D/v0.11A QA certificate.
- Launcher/activity label Sudoku; full application/in-game brand GridMates: Sudoku.
- Unlimited help, explicit solution checks only in Solo/Daily, no automatic cell mutation.
- Optional checksUsed fields default0; do not reinterpret historical hint counts or records.
- Concise visible copy and scrollable help; no later-stage feature or release promotion.

## Review Focus

- An earlier wrong entry with no local duplicate must be identified without fabricating logical reasoning or exposing its replacement digit.
- Saving a check fails, or navigation supersedes asynchronous work: do not reveal a result with unrecorded assistance or mutate another session.
- Undo/resume, Daily completion/pruning and legacy data must preserve fair assistance metadata.
- Room transitions, busy/repeated requests and already-finished boards must reject local checking and stale feedback.
- The launcher rename must not change the installed app's package, certificate, icon, stored identity/preferences or nonempty history.

### Task 1: Reconcile plans and shorten the launcher label

**Files:** Create GRIDMATES_DELIVERY_PLAN.md; carry the five new proposal documents and updated brand vision; modify app/src/main/AndroidManifest.xml, app/src/main/res/values/strings.xml, app/build.gradle.kts. Update living docs at final acceptance.

**Interfaces:** `launcher_name` is consumed only by MainActivity's manifest label. Existing app_name stays intact in main/debug resources.

- [x] Verify clean source at92ebc3b and run `:core:test :app:testDebugUnitTest`; Expected:87 passes.
- [x] Carry planning content without replacing the newer application source, and commit the staged design/plan.
- [x] Add launcher_name=Sudoku and Activity label; increment code18/dev2. This resource/configuration edit is verified through the compiled/installed artifact rather than a string-mirroring unit test.
- [x] Inspect APK badging/ActivityInfo label, installed launcher text and full in-game/app branding; Expected: launcher Sudoku, full brand retained, same icon/component/package/certificate.

### Task 2: Pure entry checking and fair saved metadata

**Files:** Modify core/SudokuAssistance.kt, PersonalPlay.kt, DailyChallenge.kt; add core tests EntryCheckTest.kt and CheckUsageTest.kt. Modify Android LocalPlayerStore.kt validation.

**Interfaces:** SudokuAssistance gains `enum class CheckKind { CONFLICT, INCORRECT_ENTRY, CORRECT_SO_FAR, NO_ENTRIES }`, `data class EntryCheck(val kind: CheckKind, val cell: Int = -1, val conflicts: Set<Int> = emptySet())`, computed `usesSolution: Boolean`, and `fun checkEntries(puzzle: Puzzle, visible: List<Int>): EntryCheck?`. SoloProgress/GameRecord append checksUsed default0; SoloProgress exposes computed assisted. `PersonalPlay.recordCheck(s: SoloProgress): SoloProgress` increments only unfinished progress.

- [x] Write tests from a hand-checked standard puzzle/solution: non-duplicate wrong1 atR1C3 returns INCORRECT_ENTRY/cell2 without editing input; duplicate5 returns CONFLICT including0/2; correct4 returns CORRECT_SO_FAR; untouched board NO_ENTRIES; malformed/changed-given/ambiguous originals return null. Add late one/two-cell logical-hint characterizations.
- [x] Run `:core:test --tests '*EntryCheckTest'`; Expected: missing contract/then assertion RED against minimal null-return API shapes, not an SDK error.
- [x] Implement checkEntries using existing conflicts and solutionCount, checking a player's entry against original clues for explicit solution comparisons. Run core tests; Expected: all GREEN.
- [x] Write record tests: a checked completed Solo and Daily retain checksUsed=1 and assisted=true; logical hints stay0; Undo retains count; old JSON defaults0; completed progress does not acquire another check. Observe RED before record implementation.
- [x] Implement metadata/record propagation and store bounds/assisted consistency validation. Run `:core:test :app:testDebugUnitTest`; Expected: full suite GREEN, no legacy record changes.
- [x] Commit the core deliverable and record test results.

### Task 3: Guarded coach UI and native regression coverage

**Files:** Modify GameViewModel.kt, MainActivity.kt, SudokuScreen.kt, PlayScreen.kt, DailyHistoryCard.kt and JournalScreen.kt; create PuzzleCoachDialog.kt and Android EntryCheckUiTest.kt; extend LocalPlayerStoreTest.kt and upgrade fixture.

**Interfaces:** GameUiState adds hintUnavailable=false and entryCheck=null. `GameViewModel.checkWork()` computes/persists before publishing, honors generation/session/revision and rejects rooms, completed/busy/blocked saves. GameActions appends default `checkWork: () -> Unit = {}`; MainActivity wires model::checkWork. `PuzzleCoachDialog(state: GameUiState, actions: GameActions)` replaces only the existing hint dialog; chat remains separate.

- [x] Add a native case: wrong non-duplicate entry → Hint unavailable without count → explicit Check my work → count1, unchanged value, selected suspect → Erase entry → Undo restores it and count remains1. Observe RED before wiring.
- [x] Implement normal/unavailable/result dialogs with the explicit disclosure and existing erase/Undo callbacks; clear feedback on edits, selection and room/navigation transitions. Use bounded scroll content at200%.
- [x] Add/run real ViewModel/store cases for a positive check, persistence/recreation, save failure with no result/count, stale navigation and rejected room/completed requests; Expected: RED→GREEN for missing behavior, full native set GREEN.
- [x] Extend the original-APK upgrade seed with an entered answer and nonempty history alongside its note/preferences. Verify complete saved bytes and resumed values before/after17→18.
- [x] Run relevant gameplay/convenience/Daily regressions and normal/large coach screenshots; Expected: accepted input/Undo/history unchanged, no stale/check information in rooms, all dialog actions reachable.
- [x] Commit the UI deliverable and record evidence.

### Task 4: Reviewed QA APK and durable handoff

**Files:** Update README, PROJECT_HANDOFF, roadmap, delivery plan and new V0.11B QA/work log. No merge or promotion of previous drafts.

- [x] Run `:core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest`; Expected: build/tests pass, report actual lint counts.
- [x] Obtain one fresh whole-branch review against92ebc3b and the spec/Review Focus. Resolve material findings once with observed RED→GREEN and a green suite; document deferred minors and scope limits.
- [x] Freeze final APK; verify signature/icon/package/label, exact17→18 update and final native checks against those bytes. Inspect actual screenshots and package public evidence without keys or profile payloads.
- [ ] Preserve exact source/tree in a draft stacked on feat/gridmates-brand-copy and a QA checkpoint; save APK/evidence/preview, then complete the plan and handoff.

Pre-flight: Task2's EntryCheck is consumed by Task3's state/dialog; checksUsed and assisted feed both Solo/Daily records and their presentation. Task1 label resource has no dependency on assistance. Appending defaulted fields/callbacks preserves existing positional constructor call sites. No interface conflict found.
