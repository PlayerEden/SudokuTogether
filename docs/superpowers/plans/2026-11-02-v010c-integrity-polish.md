# v0.10C Save Integrity and Native Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Protect local player progress and deliver visually verified Daily/history/finish screens.

**Architecture:** Keep Android AtomicFile and schema 1, add verified last-good recovery and write blocking, and load the profile on Dispatchers.IO. Separate the accepted local edit from archive retention through `LocalPlayUpdate`. Adapt existing Compose rows and expand real screenshot/instrumentation coverage.

**Tech Stack:** Existing Kotlin/Compose, serialization 1.8.1, JDK 17, Gradle 8.11.1, Android SDK 35; no new app dependency.

**Spec:** `docs/superpowers/specs/2026-11-02-v010c-integrity-polish-design.md`

## Global Constraints

- Source baseline a4448bf on development; separate feature branch, original schema 1/profile ID, Solo/Daily records and LAN protocol 5 remain compatible.
- Android minimum 26, compile/target 35; QA app `.qa`, version 0.10.0-dev3, code 15. API/Play production work remains the next milestone.
- Preserve all unfinished Daily saves within 128 total slots; retain at most 31 completed review boards and separate completion records.
- Block writing over unreadable progress if recovery cannot be proven safe. No automatic reset, new cloud/uninstall promise, or private data in public artifacts.
- Two-phone acceptance remains deferred. No unrelated game modes or framework extraction.

## Review Focus

1. Corrupt profile, failed quarantine/backup, AtomicFile rename failure: source bytes and previous in-memory state stay intact; no false successful save (Task 1).
2. Completing an older Daily evicts it immediately: completion screen still has exact accepted progress and record is idempotent (Task 2).
3. An invalid undo cell or old JSON turns into a broken board/reset: validate writes and all undo payloads while reading compatible defaults (Task 1).
4. Large fonts/landscape squeeze date/action/result text: actual Compose captures and action/glyph reachability (Task 3).
5. Test fixtures, failed asynchronous loads or late callbacks corrupt player data: off-main load guards, generation checks, isolated fixture backup/restore (Tasks 1–3).

---

### Task 1: Verified local recovery and off-main initialization

**Files:** `app/.../LocalPlayerStore.kt`, `GameViewModel.kt`, `SudokuScreen.kt`; new `app/src/androidTest/.../LocalPlayerStoreTest.kt`.

**Interfaces:** LocalPlayerStore retains `current()` and `update(transform)` and adds read-only `canSave`. A valid last-good backup is stored privately beside the primary as `.recovery`. Validation is shared by load and update. GameUiState gains `localSaveBlocked`; initialization defers all profile file I/O to Dispatchers.IO.

- [x] Run the unchanged unit/build baseline; record counts and exact result.
- [x] Write Android tests for corrupt primary/no backup refusing writes; successful backup recovery retaining notes/profile ID; failed backup/commit preserving old memory and files; unreadable-byte preservation; malformed undo rejection; legacy JSON compatibility.
- [x] Build/install tests and observe the new tests failing for the identified missing behavior.
- [x] Implement guarded recovery, verified AtomicFile commits, validation, off-main initialization and clear blocked-state UI. Keep loading/profile actions guarded.
- [x] Run the new tests and existing unit suite green; commit `fix: preserve local progress through profile recovery failures`.

### Task 2: Safe immediate Daily pruning and retained finish state

**Files:** core `DailyChallenge.kt`, `PersonalPlay.kt`; Android `GameViewModel.kt`, `SudokuScreen.kt`; tests `DailyChallengeTest.kt`, new `LocalPlayUpdateTest.kt`.

**Interfaces:** `LocalPlayUpdate(data: LocalPlayerData, progress: SoloProgress?)`; `PersonalPlay.applyEdit(data, dailyDate: String?, operation: Operation, elapsedMs: Long, nowMs: Long, autoCleanNotes: Boolean = false): LocalPlayUpdate`. GameUiState retains `finishedSolo` for the displayed completed session. Complete is idempotent after archive eviction.

- [x] Add failing tests for oldest and newest Daily completion with 31 completed reviews, unfinished retention, separate permanent records, repeat completion after eviction, and exact returned finish metadata; cover ordinary Solo and stale operations.
- [x] Run focused tests RED. Implement a shared pruning rule and atomic accepted-progress result; use it in the ViewModel and finish details.
- [x] Run full core/Android JVM tests GREEN; commit `fix: retain daily completion results while pruning review history`.

### Task 3: Responsive history/results and native screenshots

**Files:** `SudokuScreen.kt`, focused `DailyHistoryCard.kt` if useful; `NativeScreenshotTest.kt`, new `DailyFinishUiTest.kt`, `app/build.gradle.kts`.

**Interfaces:** Existing GameActions are preserved. History row/action tags remain date-addressed; completion metadata gets a stable tag. Test fixtures seed and restore controlled QA profile files around manually launched ActivityScenario instances.

- [x] Add failing UI coverage for finishing an older saved Daily, result metadata after eviction, date-neutral text, reachable history actions at 200% font and after activity recreation.
- [x] Inspect actual captures; fix date wording, adaptive history actions, recent-record wrapping and any observed compact/landscape clipping. Keep 48dp controls.
- [x] Capture Home, Daily/history and both finish screens via Compose at normal and 200% text, including landscape; restore test profile/configuration afterward.
- [x] Increment QA version. Run full unit/lint/debug/instrumentation build and emulator tests; inspect images; commit `fix: polish daily history and finish screens across text sizes`.

### Task 4: Independent review, verified QA handoff and durable source

**Files:** QA notes, living handoff/roadmap, plan, README as appropriate; APK/screenshots outside git.

- [x] Obtain one fresh whole-branch review against the spec and the five Review Focus cases. Reproduce and fix material findings with RED→GREEN regressions and a green suite.
- [x] Verify app package/version/signature and checksum; record test counts, actual emulator versions, screenshot coverage, and physical/signing limits.
- [x] Save and verify the feature source in GitHub, open a draft PR into development, and preserve the old green checkpoint. Do not promote to main or publish to Play.
- [x] Save the verified QA APK and useful screenshot comparison for delivery. Give a concise outcome and material remaining gates. No repeated execution-approval question.

## Self-review and execution

Task 1 owns storage and loading guards. Task 2 consumes that unchanged store interface and supplies `finishedSolo` to Task 3. All Review Focus conditions have owning tests. The user accepted the proposed integrity/polish direction and delegated routine overhaul decisions; execute natively in this session with a fresh final review.
