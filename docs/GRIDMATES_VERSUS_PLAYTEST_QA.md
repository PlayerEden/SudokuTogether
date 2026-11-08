# GridMates Versus playtest APK

November 7, 2026. This playtest combines the bundled number fonts and the Versus player panel from [PR #31](https://github.com/PlayerEden/SudokuTogether/pull/31). It installs as **Sudoku Test** alongside the delivered v0.11C **Sudoku** app. Each app uses its own local profile. Use the same test APK on both phones for the physical Versus check.

## Artifact

| Field | Value |
| --- | --- |
| File | `GridMates-Sudoku-Versus-Test.apk` |
| Package | `com.playereden.sudokutogether.versustest` |
| Version | `0.11.0-dev3-versus-test`, code 19 |
| SDK | Minimum 26, target 35 |
| Source commit | `605a3ba968c8cfd36c04eef3b85267c324a70789` |
| APK SHA-256 | `3bc50bb77e7faf8d9f41b7ff7719e2934968d49266861606038f6ab6864ed412` |
| Certificate SHA-256 | `62de3f5f44b1488aa2239ff0a1c666b2cead4cc384287bedc463794e38496e6e` |

Use JDK 17 and Android SDK 35. The opt-in package and label are selected by `./gradlew -PgridmatesVersusPlaytest=true :app:assembleDebug :app:assembleDebugAndroidTest`. A manifest build without the flag resolved to the usual `.qa` package. The Gradle guard rejects using the playtest and signed-update flags together. The same-identity preflight rejected the playtest certificate against v0.11C.

## Verification

- The app and instrumentation APKs built. All 124 JVM tests and 13 APK policy checks passed. Android lint reported zero errors and 16 warnings.
- Android build tools verified the final signature, package and launcher label. The four bundled fonts and license notice matched their source bytes.
- All seven API 26 device tests passed on the exact playtest APK. They covered Solo resume, a real Versus host waiting for a player, the two player tracks, reconnect after a result, narrow portrait and compact landscape at 200% text, and the number font preview.
- For the side-by-side rehearsal, the original v0.11C APK saved a Solo entry and pencil note through its UI. After installation and testing of the playtest, both installed APKs matched their delivered bytes. The original profile and preferences matched their saved bytes before relaunch. Continue reopened the original entry and note, and the playtest had a separate player ID.

An in-place update for the delivered v0.11C APK requires its original private QA signer and a separate install-over rehearsal. The delivery baseline is recorded in the [v0.11C receipt](V0.11C_DELIVERY_RECEIPT.md).
