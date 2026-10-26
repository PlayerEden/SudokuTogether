# Sudoku Together

Two phones, one puzzle. An Android app for relaxed Sudoku co-op on home Wi-Fi. One phone hosts the game and the other joins. Playing does not require an account, internet connection, or a separate server.

The name is provisional. This project has its own implementation and original generated puzzles; an unrelated app already uses the same public title.

## First test version

- Simultaneous shared entries and pencil marks.
- Nearby-room discovery, a six-digit room code, and manual IP/port fallback.
- Partner selection markers and shared conflict highlighting.
- Own-move undo that preserves a partner's subsequent edits.
- Automatic host saves, backup recovery, and credential-backed guest rejoining.
- Eight original Standard puzzles generated locally from bundled seeds.
- An in-app **Share APK** action to send the installed app using Android's sharing menu.

## Play on two phones

1. Install the same APK on both Android phones (Android 8 or newer). If prompted, allow installation from the app opening the APK. You can share the APK file directly; your partner does not need access to this private repository.
2. Connect both phones to the same home Wi-Fi. Guest networks with device isolation can block connections.
3. Choose **Host a puzzle** on one phone. On the other, choose **Join a puzzle**, select the nearby room, and enter the code displayed by the host.
4. If the room is not discovered, enter the complete address displayed by the host, including its port, then enter the room code. A code alone does not locate a phone.
5. Both people can enter numbers and shared notes freely. Starting clues stay fixed. Duplicate numbers are highlighted; wrong guesses do not end the game.
6. Keep both apps visible while playing. If either phone leaves the game, entries pause. Return to the apps, or use **Reconnect** and select the host's room again. The host can reopen **Resume your puzzle** after a restart.

This version supports a trusted home LAN. The room code controls admission; LAN game traffic is not encrypted. Remote internet rooms, guaranteed background hosting, all hotspot configurations, competitive races, and public-store publication are outside v0.1.

## Build

Install JDK 17 and Android SDK platform 35/build-tools 35.0.0. Set `ANDROID_HOME` to the SDK directory (or use an untracked `local.properties`).

```bash
./gradlew :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

To build a signed update using the existing private signing backup, extract the backup outside the repository, edit its `storeFile` path, set `SUDOKU_SIGNING_FILE` to its `signing.properties`, increment `versionCode`, and run `./gradlew :app:assembleRelease`. The keystore and signing properties must stay private and untracked. See [development notes](docs/DEVELOPMENT.md).

The GitHub build workflow runs only when manually requested. It requires the existing signing identity in repository secrets; source/documentation changes do not trigger Android builds. Local testing and building can continue without using Actions minutes.

## Project records

- [Approved design](docs/superpowers/specs/2026-10-25-sudoku-together-design.md)
- [Implementation plan](docs/superpowers/plans/2026-10-25-local-coop-implementation.md)
- [Verification and device checklist](docs/TESTING.md)
- [Development and signing](docs/DEVELOPMENT.md)
