# Sudoku Together Local Co-op Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Deliver an installable Android APK in which two phones on home Wi-Fi solve one saved Sudoku puzzle together.

**Architecture:** A pure Kotlin JVM module owns puzzle generation, authoritative edits, persistence, and real TCP host/client sessions. A native Android module adds Compose screens, NSD discovery, lifecycle handling, and APK sharing. The host persists accepted edits before broadcasting snapshots; per-cell revisions prevent silent overwrites.

**Tech Stack:** JDK 17; Gradle 8.11.1; AGP 8.9.2; Kotlin/Compose compiler 2.1.20; kotlinx.serialization 1.8.1; coroutines 1.10.2; Compose BOM 2025.04.01; JUnit 4.13.2. Compile/target SDK 35, minimum SDK 26. This is a sideloaded test app, not a Play submission. Upgrade target SDK with the documented LAN runtime permission path before targeting 37.

**Spec:** `docs/superpowers/specs/2026-10-25-sudoku-together-design.md`

## Global Constraints

- Two connected Android phones contributing to the same board.
- Android 8.0/API 26 or newer.
- Shared pencil marks; immutable starting clues; no compulsory turns or lives.
- Local Wi-Fi with no account or internet service required during play.
- The host saves every accepted board mutation atomically before acknowledging it.
- Every operation carries session, epoch, identity, operation ID, and expected cell revision.
- Same-cell stale changes cannot silently overwrite accepted work.
- Foreground play; reconnect and restore confirmed progress after interruptions.
- Private signing keys never enter source control.
- Do not spend Android build minutes for a documentation-only change.
- Distinguish automated transport/build checks from two-physical-phone Wi-Fi verification.

## Review Focus

- An acknowledgment lost after a saved edit must not cause progress loss or a duplicate edit on rejoin: Task 3 transport recovery test.
- Old or corrupted save data must produce a recoverable result without silently replacing the user's puzzle: Task 2 store recovery tests.
- A partner entering a value while the other changes notes in the same square must cause one stale rejection, not a lost edit: Task 1 reducer test and Task 3 live sockets test.
- Leaving the app, permission denial, or a Wi-Fi network without internet must pause or explain connection state without resetting the board: Task 4 lifecycle and discovery checks.
- Larger text and short portrait screens must keep controls reachable and state intact: Task 5 UI smoke checks and emulator inspection where available.

## File Structure

- Root Gradle configuration and wrapper: reproducible JVM/Android build.
- `core/.../Model.kt`: serializable puzzles, snapshots, operations, undo records, and durable session state.
- `core/.../Sudoku.kt`: original deterministic puzzle generation, solution counting, conflicts, and completion.
- `core/.../GameSession.kt`: authoritative validation, revisions, undo, deduplication, and guest admission.
- `core/.../SessionStore.kt`: atomic private structured saves with backup recovery.
- `core/.../Protocol.kt`: bounded versioned length-framed JSON messages.
- `core/.../HostServer.kt`: server admission, ordered durable edits, presence, heartbeats, and reconnect.
- `core/.../GuestClient.kt`: connection handshake, snapshot reception, input submission, and disconnection.
- `core/src/test/...`: independent rules/store/protocol/real-socket behavior tests.
- `app/.../RoomDiscovery.kt`: Android NSD registration, serialized resolution, cleanup, and local addresses.
- `app/.../GameViewModel.kt`: main-thread state, background engine/transport calls, save recovery, lifecycle.
- `app/.../MainActivity.kt`: theme, system insets, screen-awake handling, APK sharing.
- `app/.../SudokuScreen.kt`: home, joining, board, partner presence, controls, and confirmations.
- Android manifest/resources: networking, launcher icon, app styling, and narrow APK FileProvider.
- `.github/workflows/android.yml`: manually requested verification/APK build, no push-triggered minute consumption.
- `README.md`, `docs/TESTING.md`, `docs/DEVELOPMENT.md`: installation, exact checks, evidence, and remaining device checks.

### Task 1: Testable puzzle engine and authoritative edits

**Files:** root Gradle configuration; `core/build.gradle.kts`; `core/src/main/kotlin/com/playereden/sudokutogether/core/{Model,Sudoku,GameSession}.kt`; corresponding `SudokuTest.kt` and `GameSessionTest.kt`.

**Interfaces:** `Sudoku.generate(seed: Long): Puzzle`; `Sudoku.solutionCount(clues: List<Int>, limit: Int = 2): Int`; `Sudoku.conflicts(values: List<Int>): Set<Int>`; `Sudoku.complete(puzzle: Puzzle, values: List<Int>): Boolean`; `GameSession.submit(player: String, operation: Operation): Outcome`; `GameSession.snapshot(): Snapshot`; `GameSession.saved(): SavedSession`.

- [x] Write tests asserting `solutionCount(generate(seed).clues) == 1` for multiple seeds, invalid clues are rejected, and invalid full boards never complete.
- [x] Write tests asserting immutable clues, shared notes, enter-clears-own-notes, erase, exact-revision own-move undo, different-cell concurrency, stale same-cell value/note rejection, duplicate IDs, and old epochs.
- [x] Run `./gradlew :core:test`; confirm the new tests fail because implementation is absent.
- [x] Implement the interfaces and immutable serializable models. Use a shuffled valid grid and uniqueness-preserving clue removal; use a serial reducer and a bounded recent-operation ledger.
- [x] Run `./gradlew :core:test`; require zero failing tests. Commit the engine task.

### Task 2: Durable sessions and recoverable saves

**Files:** `core/.../SessionStore.kt`, `core/src/test/.../SessionStoreTest.kt`, persistence integration in `GameSession.kt`.

**Interfaces:** `SessionStore.load(): LoadResult`; `SessionStore.save(state: SavedSession)`; `AtomicSessionStore(file: File)`; `LoadResult.Missing`, `Loaded`, `Recovered`, `Invalid`. `GameSession` accepts a saving callback and updates live state only after successful saving.

- [x] Write tests for round-trip values/notes/revisions/history/token, truncated primary with valid backup, invalid primary and backup, unsupported schema, and simulated save failure.
- [x] Run the targeted tests and confirm failure.
- [x] Implement temp-file flush/fsync, atomic replacement, one validated backup, strict schema/data checks, and recoverable loading. Save accepted actions before returning success; failed writes keep the prior state.
- [x] Run `./gradlew :core:test`; require all tests to pass. Commit persistence.

### Task 3: Real local host/client play and recovery

**Files:** `core/.../{Protocol,HostServer,GuestClient}.kt`; `ProtocolTest.kt`; `TransportTest.kt`.

**Interfaces:** `HostServer(session: GameSession, listener: (HostEvent) -> Unit)` with `start(): Int`, `submitLocal(operation: Operation): Outcome`, `select(cell: Int)`, `setForeground(active: Boolean)`, `replaceGuest()`, `newPuzzle(seed: Long)`, `close()`; `GuestClient(socketFactory: () -> Socket = { Socket() }, listener: (ClientEvent) -> Unit)` with `connect(host: String, port: Int, code: String, name: String, resumeToken: String? = null)`, `submit(operation: Operation)`, `select(cell: Int)`, `setForeground(active: Boolean)`, `close()`.

- [x] Write real-loopback socket tests for code rejection, guest seat limits, compatible handshake, full snapshots, edits in both directions, same-cell contention, invalid frames and protocol versions, partner background pause, and duplicate requests.
- [x] Write recovery tests: save/receive a move, close the socket before the sender handles acknowledgment, reconnect by token, and assert the authoritative value appears once; reject an old token after partner replacement and old operations after new-puzzle epoch change.
- [x] Run `./gradlew :core:test`; confirm new tests fail before implementing transport.
- [x] Implement four-byte length framing with a 128 KiB maximum, bounded admissions, a six-digit code, per-peer serialized output, heartbeats/timeouts, and snapshot-first reconnection. Bind authenticated socket identity to every guest action rather than trusting a claimed player field.
- [x] Run `./gradlew :core:test`; require all live-socket tests to pass with resources closed. Commit transport.

### Task 4: Android adapters, discovery, permissions, and lifecycle

**Files:** Android Gradle configuration; manifest/resources; `RoomDiscovery.kt`; `GameViewModel.kt`; `MainActivity.kt`.

**Interfaces:** `RoomDiscovery.startHosting(name: String, port: Int)`, `discover(onRoom: (NearbyRoom) -> Unit)`, `stop()`; `GameViewModel` exposes `StateFlow<GameUiState>` and host/join/edit/select/undo/foreground/replace/new-puzzle actions. Android private files back host and guest saves.

- [x] Add Android unit checks for address parsing; exercise visible state transitions through real transport and Android UI tests; retain core tests for active/inactive participant authorization.
- [x] Implement NSD with `_sudokut._tcp.`, available ports, queued resolution, appropriate multicast cleanup, Wi-Fi address preference without requiring internet validation, and manual address fallback. Do not include room codes/tokens in advertisements or logs.
- [x] Implement main-thread state updates, IO-dispatched saves/network operations, single-pending edit handling, credential-backed reconnect, and host background pause with rotation preservation.
- [x] Configure target-35 compatible networking permissions, no broad file storage permission, Android insets, and screen awake only during visible play.
- [x] Run `./gradlew :core:test :app:testDebugUnitTest :app:assembleDebug`; require success before claiming an APK build. Commit adapters.

### Task 5: Playable mobile interface and APK sharing

**Files:** `SudokuScreen.kt`; UI theme/resources; APK sharing action in `MainActivity.kt`; narrow FileProvider configuration.

**Interfaces:** `SudokuApp(state: GameUiState, actions: GameActions)` renders home/join/play states; board cells use stable `cell_<index>` tags and row/column content descriptions.

- [x] Add UI smoke tests for hosting, selecting a cell, entering a number, toggling notes, undo/erase, visible connection state, and reachable controls at larger text settings where a device/emulator is available.
- [x] Implement a calm cream/teal interface, distinct player selection cues, legible 3 by 3 note placement, two-row number controls, immutable clue styling, conflict highlights, completion, safe new-puzzle confirmation, and paused/reconnect messaging.
- [x] Provide APK sharing through Android's chooser by copying the installed single APK into a dedicated share cache directory; grant only that file URI.
- [x] Build, run available UI smoke checks, and inspect actual emulator screenshots if supported. Confirm short-screen fallback can scroll without losing puzzle state. Commit the interface.

### Task 6: Signed APK, evidence, repository checkpoint, and handoff

**Files:** manual Android workflow; `README.md`; `docs/{TESTING,DEVELOPMENT}.md`; git-ignored signing configuration; final APK artifact.

- [x] Generate a persistent private app-signing key outside the repository and durably save its backup; record only its public certificate fingerprint in project documentation. Use the same application ID `com.playereden.sudokutogether` and version code 1 for the initial APK.
- [x] Run `./gradlew :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleRelease`; verify APK signature, package ID, version, minimum API, and permissions with Android tools. Include the signed APK checksum in the handoff.
- [x] Review the complete implementation with a fresh reviewer as required by the execution/review skill, repair meaningful findings, and rerun affected checks.
- [x] Save source, approved spec, implementation plan, and evidence to the supplied GitHub repository. Verify remote contents against the local snapshot; never upload keystores, passwords, or local SDKs.
- [x] Make the APK available as a durable download. Give short installation/Host/Join instructions and explicitly identify two-physical-phone home-Wi-Fi testing as outstanding unless actually performed.

## Plan self-review

All spec sections map to Tasks 1–6. Interface names and values are consistent across tasks. Each Review Focus condition has an owning test/check. Appearance is an implementation choice and can be refined after a playable APK. Execution continues in this session following the user's explicit approval to begin implementation; no additional design or execution-method approval is needed.

## Completion checkpoint

The user approved implementation. The signed 0.1.0 APK and its private signing backup have been saved as separate durable files. The full unit suite has 34 passing tests; the extended API-26 Android UI test passed with rotation and glyph bounds. Physical two-phone Wi-Fi validation remains the documented acceptance handoff. Final source is retained in the supplied GitHub repository on the local-coop feature branch.
