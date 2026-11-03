# Development checkpoint

## Architecture

`core` is an Android-independent Kotlin/JVM module. `Sudoku` generates deterministic, original, uniquely solvable puzzles. `GameSession` validates and serializes edits. `AtomicSessionStore` saves before acceptance and can recover a prior validated snapshot. `HostServer` and `GuestClient` exchange bounded, length-framed JSON over real TCP sockets. Android adapters add Compose, NSD, private files, lifecycle pause, and APK sharing.

The host's board is authoritative. Each move includes an operation ID, session/epoch, and expected cell revision. The socket's authenticated identity determines its player role. Different-cell changes can succeed concurrently; a stale same-cell change is rejected with refreshed state. Recent operation IDs prevent duplicates. No pending input is automatically replayed after disconnection.

The guest token is random, privately saved, and absent from discovery advertisements. Replacing the partner invalidates it. Moving to a new puzzle changes its epoch. Host migration and concurrent offline merging are not implemented.

## Toolchain and Android behavior

JDK 17; Gradle 8.11.1 with distribution SHA-256 verification; Android Gradle Plugin 8.9.2; Kotlin/Compose compiler 2.1.20; Compose BOM 2025.04.01; serialization 1.8.1. Minimum SDK 26; compile/target SDK 35. This is a sideloaded test release. When raising the target to API 37, implement and verify Android 17's `ACCESS_LOCAL_NETWORK` permission flow before broad LAN use.

Join and rejoin explicitly create sockets through a TRANSPORT_WIFI network socket factory, without requiring INTERNET or VALIDATED capabilities. This keeps LAN connections on Wi-Fi even when cellular remains the default network. Discovery uses `_sudokut._tcp.` and serializes older NSD resolution calls. Manual joining remains available. The screen stays awake only during visible play. Background participation is announced as paused; socket timeouts handle unannounced loss.

## Stable signing identity

Application ID: `com.playereden.sudokutogether`. Initial version: `0.1.0`, code `1`.

Public signing certificate SHA-256:

```text
D362DAB018A255CCB313582DF1E3CF836246E7DA1778A198EB1DA03A7CFE7CD1
```

The private signing material is in the separately saved `SudokuTogether_private_signing_backup.zip`, outside this repository. It includes the keystore, local signing properties, and recovery instructions. Share only the APK with players. Keep this signing identity for updates; changing the app's display name does not require changing its application ID.

Local signing uses the path supplied by `SUDOKU_SIGNING_FILE`. The manual GitHub workflow expects `SUDOKU_KEYSTORE_BASE64` and `SUDOKU_KEYSTORE_PASSWORD`; the key alias is `sudokutogether`. No private signing value is committed, included in the APK, or printed in build logs. The workflow is prepared for future use and must not be described as CI-verified until actually run.

## Next work after device feedback

First validate Host/Join and reconnect on the two actual phones. Then refine readability and joining friction. Additional puzzle ratings, hints, QR pairing, a dedicated one-device mode, competitive races, and internet play can follow without replacing the core reducer/transport boundaries.

## v0.10D private debug QA identity

The presentation milestone keeps the `.qa` package and protocol5/schema1. To build future QA updates, use the separately saved `SudokuTogether-v0.10D-private-QA-signing.zip` debug keystore and verify public certificate SHA-256 `949731a60cb187405282efc8233d6e68ab5b46e9bf6765f70aa2b60dcc543b2c`. Restore its debug.keystore to the build user's Android debug-keystore location before assembly. Do not commit or attach that private backup to a public PR/evidence ZIP. This is separate from production signing and differs from older QA certificates, including v0.10C. A signing conflict must not be bypassed by uninstalling a progress-bearing app.

## v0.11A GridMates QA updates

The GridMates display-name change keeps `com.playereden.sudokutogether.qa` and reuses the v0.10D debug certificate above. The delivered version17 / `0.11.0-dev1-qa` APK passed an actual in-place update from the original version16 APK with saved puzzle/note, identity and preferences retained. Use the existing private v0.10D backup for subsequent QA updates; do not generate a replacement key. No new signing backup is needed for this pass. The public name does not change the application ID, schema1, protocol5 or private-file locations. See [the exact audit and coverage limits](V0.11A_GRIDMATES_QA.md).
