# GridMates APK updates and progress preservation

The merged v0.11D baseline has a verified in-place upgrade from v0.11C. The v0.11E candidate adds portable backup and restore alongside the signed QA update. [v0.11D audit](V0.11D_INTEGRATION_AND_QA.md) · [v0.11E candidate](https://github.com/PlayerEden/SudokuTogether/pull/33)

## What the owner asked for

Continue the player-presence/menu design and learn from Operation PHOSPHOR and Sector Rebound's APK delivery so future GridMates updates retain people's progress. Players should install a newer compatible APK over Sudoku, then find their puzzles, notes, records, cosmetics and preferences intact. A signing failure must never be answered with an instruction to uninstall a valuable save.

## Comparison with the other repositories

Inspected public source only; neither other repository was modified. File blob SHAs were checked against these immutable source trees.

| Project and inspected source | What actually exists | What GridMates should borrow |
| --- | --- | --- |
| [Operation PHOSPHOR, `a0ae74d`](https://github.com/PlayerEden/Operation-Phosphor/tree/a0ae74dbdc3938ee8f326a55f355948a3fb94324) | `android/app/build.gradle.kts` pins a test-only `.preview` signer; the build workflow supplies its run number, checks the APK package/version/certificate and uploads an artifact. `android/README.md` describes an emulator old/new install-over test using real game progress. Its cloud snapshot adapters are staged, explicitly not wired into the Activity. No GitHub releases were returned by the release listing during this inspection. | A stable identity, increasing build number, real APK inspection, and an actual saved-game upgrade rehearsal. Do not claim its cloud code is already an active service or treat the public preview key as a production credential. |
| [Sector Rebound / Ricochet-Rumble, `b114dfa`](https://github.com/PlayerEden/Ricochet-Rumble/tree/b114dfa37af2f132e4d3c08d503afcb15c5dff19) | Preview build properties and dedicated signing paths, a disposable preview workflow with real certificate checks, permanent versioned GitHub prereleases, tested WebView/localStorage install-over evidence, and an offline backup flow with preview/confirmation/recovery. M4 adds explicit Android sharing of backup text. | Separate update compatibility from portable backup; validate before restore and retain rollback. Use immutable release assets plus a deliberate current-download pointer when publication is ready. |
| GridMates v0.11C | Native Compose app; local JSON progress and Android preferences, isolated `.qa` package, an existing real 18→19 upgrade rehearsal. The old `qa-debug.yml` did not restore a durable QA key, increase the build code automatically or inspect the final APK certificate. | Keep existing saves; repair the distributable QA build path rather than replace the save system. |

Sector Rebound is a reference, not blanket evidence of safety: its older `publish-preview-apk.yml` builds a generic debug APK and even warns that its certificate may differ. That older publication path is not the pattern to copy. An APK having a permanent URL does not make it a compatible update. The Sector Rebound document `ANDROID_WEBVIEW_PERSISTENCE_2026-11-01.md` also distinguishes a successful controlled emulator case from physical-device acceptance and abrupt termination durability.

Key source references: PHOSPHOR's [Android readme](https://github.com/PlayerEden/Operation-Phosphor/blob/a0ae74dbdc3938ee8f326a55f355948a3fb94324/android/README.md) and [build workflow](https://github.com/PlayerEden/Operation-Phosphor/blob/a0ae74dbdc3938ee8f326a55f355948a3fb94324/.github/workflows/android-debug-apk.yml); Sector Rebound's [persistence rehearsal](https://github.com/PlayerEden/Ricochet-Rumble/blob/b114dfa37af2f132e4d3c08d503afcb15c5dff19/docs/ANDROID_WEBVIEW_PERSISTENCE_2026-11-01.md), [backup sharing](https://github.com/PlayerEden/Ricochet-Rumble/blob/b114dfa37af2f132e4d3c08d503afcb15c5dff19/docs/M4_OFFLINE_BACKUP_SHARE_2026-11-03.md) and [preview signing](https://github.com/PlayerEden/Ricochet-Rumble/blob/b114dfa37af2f132e4d3c08d503afcb15c5dff19/android/app/build.gradle).

## What already saves in GridMates

| Location | Data | Existing protection |
| --- | --- | --- |
| `files/local-player.json` | Local player ID, active Solo board, Daily boards, entries, notes, Undo history, timers and completed game records; schema 1 | `LocalPlayerStore` validates content, uses Android `AtomicFile`, verifies written bytes and keeps a separate last-good recovery snapshot. Unreadable files are archived before repair, or saving is paused if preservation fails. |
| `shared_prefs/settings.xml` | Name, equipped portrait/emblem/ink/board/font, vibration, number display, note assistance, difficulty, optional room-code preference and recent puzzle IDs | Android preferences in the existing app sandbox; appearance is checked against earned access when loaded. |
| `host-session.json`, `host-race-session.json`, `guest-session.json`, and their recovery files | Resumable multiplayer rooms and admission/rejoin material | `AtomicSessionStore` and session validation. Keep these in place for normal upgrades; do not put them in a portable progress export. |

Unlocks/achievement progress are derived from real saved records, so preserving records matters as much as preserving the equipped icon. Exporting only `local-player.json` would miss the name, appearance and preferences. Renaming the launcher to Sudoku does not change the app's package identity or storage.

`android:allowBackup="false"` is currently intentional. Local recovery lives inside the app and is not an external/device-replacement backup. We have not enabled Google backup, Play Games, accounts or a developer upload service. Do not promise that uninstalling or clearing app data preserves progress.

## QA update packaging

- [QA baseline policy](../distribution/qa-update-baseline.json) records the accepted APK identity, version, checksum and exact source. The previous build has its own [v0.11C receipt](V0.11C_DELIVERY_RECEIPT.md).
- The manual QA workflow restores **the original** private debug keystore from `SUDOKU_QA_KEYSTORE_BASE64`. No key material is stored in source. Missing/wrong material stops the build before it can generate a substitute downloadable APK. The expected original alias/password are Android's debug defaults; a differently configured key must not silently replace the baseline.
- Paired Gradle properties `gridmatesQaUpdate=true` and `gridmatesQaVersionCode` select that QA signer. Unconfigured local developer builds retain their usual behavior. Production signing remains separate; QA builds are not Play releases.
- A new workflow run reserves code `1000 + github.run_number`; reruns are refused so a rebuilt candidate does not pretend to be a newly numbered release. Only this workflow owns this numbering stream. A future higher manually numbered channel must reconcile the accepted baseline before resuming it.
- `scripts/apk_update_preflight.py` invokes real Android `apksigner verify` and `aapt dump badging`, compares the package, signer, requested increasing version and minimum SDK, and hashes the unchanged final APK. It never installs, edits or clears app data.
- The download includes `GridMates-Sudoku-QA.apk`, its SHA-256, and a receipt with the exact source SHA. A receipt explicitly says install-over/save-retention have **not** been tested by the identity check.
- The workflow is manual and builds the selected revision.

This guards the designated QA download path. A local developer debug APK can still have a different signer and must not be advertised as an owner update. The release workflow targets the separate production package and is not a QA migration mechanism.

## Downloads: current candidate versus accepted release

For now, workflow APK artifacts expire after 14 days. A chat file and an Actions artifact are delivery locations; neither is an in-app auto-update service. Downloading a new file also does not update the installed app until Android installs it.

When the next APK has passed native acceptance, retain its original bytes as a uniquely tagged GitHub **prerelease**, with its receipt and checksum. Then update one checked-in current-QA index/readme entry to point to that exact immutable asset. Do not replace older release assets or point people to an unverified newest workflow run. Advance the accepted baseline in the same reviewed delivery record. The future current index must include package, signer, version, source, checksum, exact asset URL, and native retention evidence.

v0.11D is an owner sideload delivery. Open the accepted APK with Android's standard installer and choose **Update** while Sudoku is installed.

## Portable backup and restore

In v0.11E, open **Settings → Progress → Back up progress** and choose where to save the file. On the receiving installation, open **Restore backup**, choose the file, and review its saved date, Daily puzzles and game records. Choose **Restore** to replace that installation's offline progress and settings.

The versioned file carries saved Solo and Daily boards, completed records from all modes, and the allowlisted player preferences. Styles are checked against the imported record history. Restore uses the receiving installation's identity. Room saves and their admission details live in separate device-local files.

The app validates the file before showing a preview. It checks size, schema, types, playable Solo boards and Daily date consistency. Before replacement it commits a private recovery journal for both local stores. An interrupted or failed restore recovers the prior profile and preferences or pauses further writes until recovery can complete.

The Play variant uses a separate Android package. Export the file from the updated QA app before moving progress to that variant. Routine QA updates use Android's in-place update and existing saves.

The Android 8 rehearsal used the system file picker to export from QA and restore into the split production package. The resumed Solo board matched the seeded entry and pencil note. [v0.11E QA evidence](V0.11E_PROFILE_PROGRESS_QA.md)

## Verification and remaining gates

The original v0.11B **18 → v0.11C 19** check preserved the complete profile, an active answer and note, preferences, and a protected legacy room. [v0.11C QA](V0.11C_NEARBY_WOODLAND_QA.md)

The original v0.11C **19 → v0.11D 20** rehearsal used the exact delivered APKs and verified the saved profile and reopened Solo, Daily, and host sessions. `UpgradeContinuityTest` accepts explicit baseline and candidate versions. Its two phases run on either side of `adb install -r`. [v0.11D QA](V0.11D_INTEGRATION_AND_QA.md)

For each new APK, use the previously delivered bytes as the emulator baseline, seed actual progress, and repeat the package, signer, version, native build and install-over checks before advancing the baseline policy.

Official Android references: [app updates](https://developer.android.com/google/play/app-updates), [app signing](https://developer.android.com/studio/publish/app-signing), [Auto Backup](https://developer.android.com/identity/data/autobackup). Compatible application ID and signing certificate, plus a suitable version code, permit an in-place update; retention still depends on preserving and reading the application's data correctly.
