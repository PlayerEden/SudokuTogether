# Game visuals and convenience implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Give Sudoku Together a cohesive game presentation and faster everyday navigation without changing its Sudoku, saves or LAN rules.

**Architecture:** Retain the current native Compose application and GameActions/ViewModel contracts. Split the large screen file by player-facing destination, share visual primitives and theme, and hold journal navigation state in the UI.

**Tech Stack:** Kotlin 2.1.20, Compose BOM 2025.04.01, Material3, Gradle 8.11.1/JDK17.

**Spec:** `docs/superpowers/specs/2026-11-03-game-visuals-design.md`

## Global constraints

- Keep minSDK26 / compile and target35, QA package, schema1 and protocol5; no new external UI dependency.
- Preserve all save/recovery, difficulty, result, multiplayer-privacy and optional-assistance semantics.
- Native controls with meaningful accessibility labels and primary actions at least48dp; support large text/short screens with wrapping/scrolling.
- Work from v0.10C commit `ddf661e5e612d763e538ad97b47197d8771f2989` on `feat/v0.10d-game-visuals`; preserve earlier branches.
- No physical two-phone certification, release signing/Play promotion, new rules or generic-framework work.

## Review focus

- Saved Solo + compact viewport: Resume must be visible without scrolling and must resume rather than silently replace.
- Switching journal destinations and returning Home must preserve name/preferences/progress and provide a visible selected destination.
- Short landscape or 200% text: every board row and editing action must remain reachable without overlap; decorative text must not squeeze actionable labels.
- Daily completion reopened after pruning must retain the correct accepted result and its recorded assistance/time.
- Co-op/Versus pause, chat, room code and opponent privacy must survive the presentation refactor.

### Task 1: Theme, Home and navigation

**Files:** Create `GameTheme.kt`, `GameWidgets.kt`, `HomeScreen.kt`; modify `SudokuScreen.kt`, `MainActivity.kt`, `app/build.gradle.kts`; create Android test `GameConvenienceTest.kt`.

**Interfaces:** Consumes existing `GameUiState`, `GameActions`. Produces `JournalTab { RECORDS, COLLECTION, SETTINGS }`, `SudokuTogetherTheme(content)`, original vector `GameIcon`, shared `GamePanel`/action/header components, `HomeScreen(state, actions, share, fresh, freshSolo, freshRace, help, journal)` and `GameNavigation(page, tab, play, journal)`.

- [x] Write `resumeIsVisibleWithoutScrolling` and `journalDestinationsAreOneTapAway`; run on the baseline and observe missing convenience assertions fail.
- [x] Implement theme and destination navigation, prioritize Resume and move identity editing to Settings. Retain mode/difficulty and replacement actions; bump QA version to code16 / `0.10.0-dev4`.
- [x] Run core/app JVM tests, assembly, and both targeted native cases. Expected: all pass.
- [x] Capture/inspect Home and modes, then commit this coherent presentation slice.

### Task 2: Play and pairing presentation

**Files:** Extract/refine `PlayScreen.kt` and `JoinScreen.kt`; update root wiring and `NativeScreenshotTest.kt`/`PlaySmokeTest.kt` selectors only where navigation intentionally changed.

**Interfaces:** Consumes Task1 theme/widgets; preserves `PlayScreen(state, actions, leave, new, replace, help)` and `JoinScreen(state, actions)`. Board and input remain projections of existing state; all mutations still use existing GameActions.

- [x] Write `compactBoardAndKeypadAreVisibleTogether`; verify the baseline layout overlap fails before replacing play layout.
- [x] Implement compact/landscape board sizing from the actual remaining area, explicit selected-cell context, icon-led controls and trophy completion panel. Keep hidden-information/pause conditions and saved results unchanged.
- [x] Style joining and add appropriate IME Next/Done focus behavior; keep address/code validation.
- [x] Run full native smoke flows and targeted compact layout case. Expected: all pass, including real entry/Undo/notes/resume, hint explanation, Daily separation, co-op chat and race pause.
- [x] Inspect native board/selection/notes/Join/completion captures, then commit.

### Task 3: Journal, collection and acceptance

**Files:** Extract/refine `JournalScreen.kt`; update `CosmeticUi.kt`, `DailyHistoryCard.kt` if needed; focused screenshot tests; update README/handoff/QA/work log.

**Interfaces:** Consumes `JournalTab`; produces `ProfileScreen(state, actions, tab, help)` with focused Records/Collection/Settings. Uses existing cosmetic unlock and persistent preference actions unchanged.

- [x] Verify journal navigation contract: each destination contains its own content; Settings editing survives a return to Home; Records can reopen Daily History.
- [x] Implement readable record panels, live cosmetic previews and lock requirements, and accessible settings rows.
- [x] Run full core/app JVM suite + lint + APK/test assembly, native smoke/convenience and relevant large-text Daily/result cases. Expected: green tests; lint findings documented honestly.
- [x] Capture and visually inspect compact portrait/landscape/200% text for Home, board, Records, Collection, Settings and results; fix material visual problems. Pure cosmetic changes are assessed visually, not by implementation-mirroring tests.
- [x] Obtain one fresh whole-branch read-only review, address material findings with appropriate regressions, and preserve final verification and limitations.
- [x] Commit, save the branch/draft PR without merging, and deliver verified APK/visual evidence with hashes and update-signing caution.

## Preflight interface scan

Task1→Task2: shared colors/widgets live in the app package; public action/ViewModel contracts do not change. Task1→Task3: root owns the journal destination; ProfileScreen consumes it, so switching tabs cannot invent a new saved schema. Task2→Task3: in-game profile links explicitly select Records. Tests that used the former long Profile page will navigate to Collection/Settings rather than search hidden destinations. No interface conflicts found.
