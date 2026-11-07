# QA update continuity implementation plan

> **For agentic workers:** Use superpowers:executing-plans to implement this contained packaging repair in the current session. Steps use checkboxes for tracking.

**Goal:** Prevent a GridMates QA download from being presented as an update unless its real APK identity can update the established QA install.

**Architecture:** Keep the native application and schema 1 persistence unchanged. A checked-in baseline pins the existing QA package, signing certificate and last verified build. A read-only Python command verifies the candidate with Android's actual `apksigner` and `aapt` before any installable artifact is uploaded. The manual workflow restores the existing private QA key and uses a strictly higher build number.

**Tech stack:** Python 3 standard library, Android build-tools 35.0.0, Gradle Kotlin DSL, GitHub Actions.

**Spec:** [Update and save preparation](../../GRIDMATES_UPDATE_AND_SAVE_PREPARATION.md). The separate UI prototype is for review, not an Android implementation.

## Global constraints

- QA package: `com.playereden.sudokutogether.qa`; production package remains `com.playereden.sudokutogether`.
- QA certificate SHA-256: `949731a60cb187405282efc8233d6e68ab5b46e9bf6765f70aa2b60dcc543b2c`.
- Verified baseline: version code 19, minimum SDK 26; normal local debug builds and production signing remain separate.
- Never commit private keys or replace an installed signer with a newly generated one.
- No uninstall, data clear, save-schema change, cloud backup, in-app updater, release publication, automatic workflow runs, or source promotion.

## Review focus

- An APK with valid-looking certificate text but a failing verification command must be rejected.
- A candidate with a different package, signer, unsupported minimum SDK, or non-increasing version must never produce an acceptance receipt.
- Missing/invalid signing configuration must stop a distributable build, rather than fall back to another debug key.
- Reruns and independently chosen version numbers must not quietly overwrite an accepted download.
- An expiring workflow artifact must not be advertised as a permanent current-release link.

### Task 1: Verify the actual candidate APK

**Files:** `distribution/qa-update-baseline.json`, `scripts/apk_update_preflight.py`, `scripts/tests/test_apk_update_preflight.py`.

**Interface:** CLI consumes `--apk`, `--policy`, `--aapt`, `--apksigner`, `--expected-version-code`, and `--source-commit`. It exits zero and prints one JSON receipt only after all checks pass; rejection exits nonzero without acceptance JSON.

- [x] Write CLI tests using temporary executable doubles for Android build tools. Assert the command's decision, real input file digest and unchanged bytes.
- [x] Run the tests and observe refusal of the missing implementation.
- [x] Implement policy, signer, package, minimum SDK and increasing-version checks that inspect the APK.
- [x] Run the tests and exercise external-tool failure and malformed-output boundaries. All 13 checks passed on November 7.

### Task 2: Make downloadable QA builds use the existing identity

**Files:** `app/build.gradle.kts`, `.github/workflows/qa-debug.yml`.

**Interface:** Paired `gridmatesQaUpdate=true` and `gridmatesQaVersionCode` properties require `SUDOKU_QA_KEYSTORE_FILE`. The QA keystore retains the established Android debug alias/password. The workflow consumes the original key from `SUDOKU_QA_KEYSTORE_BASE64`, without printing it.

- [x] Keep the ordinary developer debug path and reject incomplete update configuration.
- [x] Define the workflow steps to restore the private QA key outside the checkout and inspect its public certificate before building.
- [x] Configure the exact selected ref and code `1000 + github.run_number`, rejecting reruns and versions at or below the baseline.
- [x] Wire preflight before upload, with checksum, receipt and temporary signing file cleanup.
- [x] Parse YAML and check nine shell blocks. Native compilation and install-over rehearsal remain pending.

### Task 3: Record save protection and review the interface

**Files:** `docs/GRIDMATES_UPDATE_AND_SAVE_PREPARATION.md`, `docs/superpowers/specs/2026-11-04-player-presence-and-menu-design.md`, delivery plan and handoff.

- [x] Document the PHOSPHOR and Sector Rebound approaches, including preview identity and publishing gaps.
- [x] Specify an optional export, preview and restore follow-up that excludes room credentials.
- [x] Provide a clickable prototype using the existing art and sample player data.
- [x] Exercise 12 prototype interaction checks in a JavaScript harness.
- [ ] Inspect narrow layouts in a browser. The local file preview was blocked by browser policy.
- [x] Review packaging independently and address the prototype's long-name wrapping issue. Record verification limits in the preparation and prototype documents.

## Native acceptance before distributing the next APK

Restore the original private QA keystore; compile with the declared toolchain; run existing JVM/native checks. Install the exact original v0.11C APK on an emulator, seed genuine Solo/Daily progress, notes, history, preferences, equipped portrait and a saved room, then install the candidate with `adb install -r` and verify those values through the real app. Do not replace the real old APK with a rebuilt baseline. The current `UpgradeContinuityTest` has v0.11B-specific setup/assertions and must receive a separate v0.11C fixture case before claiming that future rehearsal passed.
