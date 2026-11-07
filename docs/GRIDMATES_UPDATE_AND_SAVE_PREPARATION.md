# GridMates APK updates and progress preservation

November 4, 2026 (America/Chicago). Prepared from the verified v0.11C branch at `975e31a1e8cbfebad1fbbc7602b3340c357da7ef`. This is preparation and a packaging repair, not a new delivered APK, online updater, cloud save, or public release.

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

## Packaging repair prepared in this branch

- `distribution/qa-update-baseline.json` records the exact verified v0.11C QA APK: package `com.playereden.sudokutogether.qa`, code **19**, min SDK **26**, certificate SHA-256 `949731a60cb187405282efc8233d6e68ab5b46e9bf6765f70aa2b60dcc543b2c`, APK SHA-256 `4ca53d52b24d537372c407d17a31a8c293c27bc0dd060a1fd8b97b2e43f260f4`.
- The manual QA workflow restores **the original** private debug keystore from `SUDOKU_QA_KEYSTORE_BASE64`. No key material is stored in source. Missing/wrong material stops the build before it can generate a substitute downloadable APK. The expected original alias/password are Android's debug defaults; a differently configured key must not silently replace the baseline.
- Paired Gradle properties `gridmatesQaUpdate=true` and `gridmatesQaVersionCode` select that QA signer. Unconfigured local developer builds retain their usual behavior. Production signing remains separate; QA builds are not Play releases.
- A new workflow run reserves code `1000 + github.run_number`; reruns are refused so a rebuilt candidate does not pretend to be a newly numbered release. Only this workflow owns this numbering stream. A future higher manually numbered channel must reconcile the accepted baseline before resuming it.
- `scripts/apk_update_preflight.py` invokes real Android `apksigner verify` and `aapt dump badging`, compares the package, signer, requested increasing version and minimum SDK, and hashes the unchanged final APK. It never installs, edits or clears app data.
- The download includes `GridMates-Sudoku-QA.apk`, its SHA-256, and a receipt with the exact source SHA. A receipt explicitly says install-over/save-retention have **not** been tested by the identity check.
- The selected source ref is built as selected, without silently substituting the old `development` branch. The workflow remains manual-only; this preparation does not consume an Android CI run.

This guards the designated QA download path. A local developer debug APK can still have a different signer and must not be advertised as an owner update. The release workflow targets the separate production package and is not a QA migration mechanism.

## Downloads: current candidate versus accepted release

For now, workflow APK artifacts expire after 14 days. A chat file and an Actions artifact are delivery locations; neither is an in-app auto-update service. Downloading a new file also does not update the installed app until Android installs it.

When the next APK has passed native acceptance, retain its original bytes as a uniquely tagged GitHub **prerelease**, with its receipt and checksum. Then update one checked-in current-QA index/readme entry to point to that exact immutable asset. Do not replace older release assets or point people to an unverified newest workflow run. Advance the accepted baseline in the same reviewed delivery record. The future current index must include package, signer, version, source, checksum, exact asset URL, and native retention evidence.

No release/tag/current-download pointer has been published or advanced in this pass. An automatic downloader or installer is a separate feature and unnecessary for this foundation. Android's standard installer remains responsible for installing an explicitly downloaded APK.

## Portable backup follow-up — proposed, not implemented

Add **Settings → Progress → Back up progress / Restore backup** after the UI layout review. Keep ordinary autosaving automatic; players do not need to manually export before every normal same-identity update.

The export should be one versioned offline file, containing allowlisted Solo/Daily progress and records, plus the known preference fields above. Include format/schema/export-time/app-version metadata. Do not export whole application directories, room codes, network addresses, guest tokens, live room state or signing material. Leave the destination installation's `playerId` intact; portable progress is not an online identity/account transfer. Recalculate unlock access from imported records before equipping a requested style.

Restore sequence:

1. Read with a size limit and strictly validate format/schema/types, boards, history and records using the existing validators; reject unknown future schemas without touching current files.
2. Show a preview of the incoming progress and explain that restore replaces the current offline progress. Permit cancellation without writes. Do not restore during an active room/session.
3. Preserve and verify a complete recovery snapshot of both existing progress and allowlisted preferences before replacement.
4. Apply the imported records/progress with the current installation ID, write preferences, reload through the real app and verify the resulting state. Roll back both stores on failure; never report success based only on a write call.
5. Use Android's explicit document picker/share actions to save outside the app; no broad storage permission or automatic upload. Treat this as a private backup, distinct from a public spoiler-free Share result image.

Do not mark this portable backup as shipped, and do not add a nonfunctional backup button to the Android app. A future `.qa` → production/Play transition changes package identity and needs an explicit transfer path; it is not automatically covered by QA install-over continuity.

## Verification and remaining gates

Existing historical evidence remains: original v0.11B **18 → v0.11C 19** update preserved the complete profile hash, a genuine completed record, active answer/note, player ID/preferences/equipped emblem and a protected legacy room. See [v0.11C QA](V0.11C_NEARBY_WOODLAND_QA.md) and [receipt](V0.11C_DELIVERY_RECEIPT.md). That is prior evidence, not a fresh test of this branch.

This branch adds executable release-decision tests using temporary command doubles for Android tools. They cover acceptance/hash receipt and rejection of wrong package/certificate/version/minimum SDK, failed tools and malformed metadata/policy. Actual APK signature verification, Gradle compilation and a new native update rehearsal are separate gates. This workspace currently has a JRE but no Android SDK/full JDK or original private QA keystore, and those native checks have not run.

November 7 verification passed all 13 release-decision tests. The QA workflow YAML parsed, and Bash syntax checks passed for all nine shell blocks. Independent review found no blocking issue in the inspected packaging logic. Native build and install-over validation remain open.

Before recommending a candidate: restore the exact original private QA key, run the native build/test/lint checks, seed the **original delivered v0.11C APK**, install the new APK with `adb install -r`, then verify actual Solo/Daily boards, notes/Undo/timers, records/unlocks, selected Woodland portrait and legacy emblem, identity/preferences and saved rooms. Test relaunch as a baseline control. The existing `UpgradeContinuityTest` was written around v0.11B and needs a separate current-baseline fixture; rerunning it unmodified is not evidence for a v0.11C→new update.

Official Android references: [app updates](https://developer.android.com/google/play/app-updates), [app signing](https://developer.android.com/studio/publish/app-signing), [Auto Backup](https://developer.android.com/identity/data/autobackup). Compatible application ID and signing certificate, plus a suitable version code, permit an in-place update; retention still depends on preserving and reading the application's data correctly.
