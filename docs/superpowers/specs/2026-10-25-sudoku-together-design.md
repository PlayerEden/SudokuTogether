# Sudoku Together: local co-op Android design

Status: approved by the user on 2026-10-25. This document records the first playable version; implementation and device verification are tracked separately.

## Intent

Two people install the same APK on their own Android phones, connect to their home Wi-Fi, and solve one Sudoku puzzle together. One phone hosts the session. No account, rented server, computer running at home, or internet connection is required during play.

The game should feel like sitting beside someone with a shared puzzle book. Both people can contribute freely, discuss possibilities, and return to an unfinished puzzle. Correct synchronization and an easy connection flow matter more than extra game modes.

## Confirmed choices and proposed defaults

The user chose co-op before competition, separate phones from the first playable version, Android APK distribution, and home Wi-Fi as the initial connection method. The existing repository is `PlayerEden/SudokuTogether`. The working title is **Sudoku Together**.

The following are the builder's proposals, not additional user requirements: two players maximum; simultaneous contributions rather than compulsory turns; shared pencil marks; local automatic saving; no time pressure or lives; native Kotlin and Jetpack Compose; Android 8.0/API 26 or newer; and the networking, conflict, and recovery rules below. Approving this document adopts these defaults unless the user changes them.

## Approaches considered

| Approach | Benefit | Tradeoff | Decision |
| --- | --- | --- | --- |
| One-device pass-and-play first | Fastest route to a playable Sudoku board. | Separate-phone play would arrive later. | The user prefers separate phones immediately. |
| One phone hosts over local Wi-Fi | Each person uses their own phone; no ongoing hosting service is needed. | Requires discovery, synchronization, interruption recovery, and device testing. | Selected direction. |
| Online service from the beginning | Supports players in different locations and sessions independent of a host phone. | Adds internet dependence, service setup, and more operating work. | Outside the first version. |

For the selected approach, native Android is recommended because local listening sockets, network discovery, permissions, and lifecycle handling are central features. A web interface in an Android wrapper is possible, but would still need native hosting/discovery integration. A full game engine adds little to a small touch-based puzzle app. The framework choice remains the builder's recommendation for approval, rather than an earlier user selection.

## First playable scope

- A standard 9 by 9 Sudoku board with immutable starting clues.
- Two connected Android phones contributing to the same board.
- Host Game and Join Game, with nearby-room discovery and a manual address fallback.
- Number entry, erasing, shared pencil marks, and safe undo of your own moves.
- Each player's current selection is visible to the other, with names or symbols as well as color.
- Duplicate-number conflicts are highlighted. Incorrect guesses do not consume lives or end the game.
- Automatic saving, reopening a saved host game, and guest reconnection.
- A shared completion result after the final valid entry.
- A small bundled set of original, uniquely solvable puzzles. Initial puzzles use a neutral Standard label; difficulty ratings are added only after appropriate solving analysis.
- An installable test APK plus installation and play instructions.

Solo use can run through the same local puzzle engine. A dedicated pass-and-play mode, competitive racing, internet matchmaking, chat, accounts, ads, daily challenges, advanced hints, QR pairing, and a public store launch are later work. Avoid presenting inactive buttons for these features.

## Connection and play

1. Both people install the same test build. Android may require each person to permit installation from the app opening the APK.
2. Both phones connect to the same home Wi-Fi. Internet access on that Wi-Fi is unnecessary. Guest networks that isolate devices can prevent play even when the network name matches.
3. One player hosts a new puzzle or resumes a saved one. The app advertises the room through Android Network Service Discovery and supplies a short room code for joining.
4. The other player discovers the room, enters its code, and joins. Successful admission reserves the single guest seat and sends the complete current puzzle state.
5. Both players can enter numbers or notes without waiting for a turn. The host runs the same move-validation path for its own input and the guest's input.
6. Once the puzzle is complete, both phones receive the same completion state. Starting a replacement puzzle is a host action with an explicit confirmation because it replaces the shared board.

If discovery fails, the host supplies its local address and actual listening port for manual entry. The room code still applies. A short code by itself cannot locate a phone without working discovery or an online rendezvous service. Permission denial, incompatible builds, a full room, a wrong code, and unreachable networks each need a clear, recoverable result.

## Puzzle and editing rules

The engine validates that each bundled puzzle has 81 cells, legal starting clues, and exactly one solution. Puzzle data is generated for this project rather than copied from another publisher's puzzle collection. Puzzle identity includes a stable identifier and content hash; both clients receive identical clues from the host.

Players may enter any digit from 1 to 9 in an editable cell, including a wrong guess. Duplicate values in a row, column, or box are highlighted on both devices. The first version does not silently compare every entry with the solution or automatically reject wrong guesses.

Pencil marks are shared state, not private notes. They can be changed only in empty editable cells. Entering a number clears notes in that cell. The first version does not automatically prune notes elsewhere, keeping notes predictable and undo simple. Erase clears the selected editable cell's number and notes. Starting clues cannot be edited, erased, or restored through undo.

Completion requires a full, valid Sudoku grid that preserves all starting clues. A merely full board with conflicts remains unfinished. Completion and its shared result are authoritative host events, not independent client guesses.

## Synchronization and simultaneous input

Use one authoritative game state on the host. A client submits an operation and shows it as pending until the host accepts it; it does not treat an optimistic local edit as confirmed progress.

Every operation carries the session identifier, game epoch, authenticated player identity, unique operation identifier, cell index, intended change, and expected revision for that cell. Note operations set a note on or off explicitly instead of toggling it blindly. The host processes operations serially, verifies them, saves accepted durable changes, and then broadcasts the new authoritative state and revision.

The initial implementation sends full board snapshots after accepted moves. An 81-cell puzzle is small, and snapshots make recovery easier than a complex delta protocol. Each cell has its own revision in addition to the board's monotonically increasing revision. Changes to different cells can therefore both succeed even if they were submitted against the same older board revision.

If both players edit the same cell revision, the first accepted operation wins. The second is rejected as stale with the latest cell state; it never silently overwrites the first. Duplicate requests are deduplicated by operation identifier and return their original outcome. A bounded, persisted recent-operation ledger is sufficient because pending requests are not automatically replayed after disconnection; the client obtains a fresh snapshot first.

Undo reverses the player's own most recent eligible accepted edit. It is allowed only if its affected cell still has the revision produced by that edit, so it cannot erase a partner's later work. Undo restores the previous number and notes as a new authoritative operation. Clear, note, value, and undo operations use the same validation and sequencing rules.

Selection markers are temporary presence messages. They do not reserve or lock cells, count as moves, or need durable saving. Both people can select the same cell. Completed puzzles are read-only until the host starts another puzzle.

## Local transport and room admission

Use native TCP sockets on the Wi-Fi LAN, with a small versioned, length-framed UTF-8 JSON protocol. Use an available port rather than assuming a fixed port is free. Apply strict message-size limits, input validation, connection timeouts, and serialized writes. Network work and saving run off the UI thread.

Advertise only a room label, protocol compatibility information, and connection details via NSD. Do not advertise the room code or saved rejoin credential. Require a randomly generated six-digit code for new admission, rate-limit incorrect attempts, and enforce the two-player limit. The admitted guest receives a random rejoin token that the host and guest save locally. A disconnected guest's seat remains reserved until the host explicitly replaces that partner, which invalidates the old token.

The room code is a local admission mechanism, not transport encryption. Version one is scoped to a trusted home LAN and does not advertise internet access, open router ports, or claim protection from a hostile LAN observer. Transport security can be extended independently later.

The exact framing layout, service identifier, timeout constants, serialization library, and retry backoff are implementation choices. Protocol version, game epoch, revision checks, input limits, and a compatibility handshake are required. Full protocol mismatches must be explained rather than accepted optimistically.

Use Android's supported LAN discovery and networking permissions for the chosen target SDK. Current Android documentation requires `ACCESS_LOCAL_NETWORK` for broad LAN access in apps targeting Android 17/API 37 or higher. Older supported versions need their documented compatible path. Permission denial must leave the app usable for local solo play and offer a later retry. Choose and pin the exact supported SDK/toolchain versions when writing the implementation plan, against current primary documentation.

## Saving and interruption behavior

The host saves every accepted board mutation atomically before acknowledging it. Save format includes schema version, session and puzzle identity, game epoch, clues, entered values, notes, revisions, eligible undo history, completion state, and partner identity/credential data needed for rejoining. Session credentials remain in private app storage and are excluded from diagnostics or normal exports.

The guest keeps a mirror of the most recent confirmed snapshot plus its rejoin identity. That mirror is a read-only recovery copy, not an independent multiplayer authority. Host migration and merging two independently edited offline boards are out of scope.

When either player disconnects, co-op editing pauses while the app reconnects or the host chooses to end co-op. Unacknowledged input is visibly unconfirmed, discarded after reconnect, and reconciled from the host's fresh snapshot; confirmed input is not submitted twice. If a durable change reached the host before a disconnect but its acknowledgment did not reach the guest, the fresh snapshot reveals the accepted change.

Reopening the host app can resume its saved puzzle and advertise a room again, even if its network address or port changed. The previous guest can discover that room and rejoin using the saved credential. Starting a different puzzle changes the game epoch, so stale requests from an earlier puzzle cannot modify it. Replacing a partner rotates the credential and requires new admission.

Version one promises foreground play. During an active visible game the app keeps the screen awake; leaving the app or locking the phone pauses the session when detected. A heartbeat handles abrupt process death or lost connectivity. Returning players reconnect and restore confirmed progress. Rotation must preserve session ownership without creating duplicate servers. An always-running background host is not required and no background-hosting claim should appear in the app or release notes.

## Android implementation structure

Proposed stack: Kotlin, Jetpack Compose, Android lifecycle/ViewModel integration, coroutines, platform NSD and TCP sockets, and private local structured saves. No browser-only mockup is a substitute for the native multiplayer transport.

Keep the puzzle rules and operation reducer independent of Android APIs. Put the authoritative session coordinator and transport behind interfaces so real host/client integration tests can run on the JVM. Android adapters own discovery, permissions, lifecycle, and UI state. Use explicit navigation states for hosting, joining, connected play, paused/reconnecting, and completion.

The interface should prioritize legible digits and touch targets, work in portrait on both phones, respect system insets and larger text, and avoid scrolling the board and number controls during ordinary portrait play where screen size permits. Appearance, palette, icons, exact typography, and final product branding are left to the builder and subsequent visual review; no mockup has been approved yet.

## APK distribution and updates

Provide the same APK to both players with a recognizable version label and installation instructions. Distribution is by file sharing or a repository download; Google Play enrollment is not a prerequisite for the test version. Receiving the APK from the other player must not require access to the private repository.

Android requires APK signing. Ongoing builds must preserve the application identifier and signing identity while increasing the version code to support in-place updates. Establish a persistent private signing key before promising a repeatable update channel; do not commit the private signing key into source. Secure key provisioning is an implementation setup task, and any test-only signing limitation must be stated in the corresponding release notes.

Use manually triggered APK build workflows initially to control GitHub Actions usage. Cache normal build dependencies, run meaningful checks, and retain build identification and checksums with the downloadable APK. Do not spend Android build minutes for a documentation-only change. Store download/install instructions and a short development checkpoint in the repository.

## Validation and evidence

Automated verification must cover:

- Bundled puzzle legality and unique solutions; immutable starting clues; incomplete and invalid-full board detection.
- Notes, erase, own-move undo, and refusal to undo over a later partner edit.
- Real host/client transport connections using separate clients: simultaneous different-cell and same-cell edits, duplicate operation identifiers, invalid payloads, full rooms, and incompatible protocol versions.
- Disconnect during an in-flight operation, reconnect from a snapshot, token validation, game-epoch rejection, and host save/reload without losing acknowledged moves.
- Atomic save recovery from a truncated or invalid save, with a clear recoverable result rather than a crash or silent progress reset.
- Successful Android compilation and appropriate UI/permission/lifecycle checks where the available environment supports them.

Before claiming that separate-phone home-Wi-Fi play is device-verified, test the actual APK on two Android phones or an equivalent explicitly documented device setup. JVM sockets, emulators, and a successful APK build provide useful evidence but cannot certify a particular home's discovery, router settings, battery handling, and OEM behavior. The initial handoff must distinguish automated checks from the remaining home-Wi-Fi check, and the app must include useful connection diagnostics without exposing credentials.

The user acceptance scenario is: install the shared APK on both phones; host and discover/join a room on the same Wi-Fi; make entries and notes from each phone; verify simultaneous edits remain consistent; interrupt a phone and restore the shared board; complete a valid puzzle together.

## Naming and later work

Sudoku Together remains the working title and repository name. A current Google Play listing already uses that exact app title, including co-op Sudoku. This check does not establish trademark clearance. Choose and check a more distinctive public title before a public launch; renaming the display title later should not force an application-identifier change.

Once local co-op is proven, consider improved difficulty selection and hints, QR joining, a dedicated one-phone pass-and-play mode, competitive races on identical separate boards, and internet rooms. Hotspot behavior is a later device-tested capability, not a promise that every hotspot configuration works like the first home-Wi-Fi setup.

## Primary references checked

- Android Network Service Discovery: https://developer.android.com/develop/connectivity/wifi/use-nsd
- Android local network permissions: https://developer.android.com/privacy-and-security/local-network-permission
- Android background restrictions: https://developer.android.com/develop/background-work/background-tasks/bg-work-restrictions
- Android signing and update identity: https://developer.android.com/studio/publish/app-signing
- Existing same-title Play listing: https://play.google.com/store/apps/details?id=com.sudoku2gether.app
- USPTO explanation of distinctive, descriptive, and generic marks: https://www.uspto.gov/trademarks/basics/strong-trademarks

These references inform the design; they are not a substitute for implementation checks or a full public-name clearance search.
