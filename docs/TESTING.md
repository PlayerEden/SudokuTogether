# Verification

## Automated checks

The implementation includes real behavior tests for puzzle uniqueness, invalid-board completion, immutable clues, notes, erase, own-move undo, concurrent edits, duplicate IDs, old epochs, guest admission, pause/resume, real TCP connections, rejoining after host reload, unconsumed acknowledgment recovery, framed-message bounds, malformed input, save recovery, and manual address validation.

Run:

```bash
./gradlew :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleRelease
```

Release signing requires `SUDOKU_SIGNING_FILE`. Use `:app:assembleDebug` for an ordinary unsigned-development-configuration test build (Android still signs debug APKs with its debug identity).

An Android instrumentation smoke test in `PlaySmokeTest` exercises hosting, entering a number, undo, notes, erase, and visible connection information. Build with `:app:assembleDebug :app:assembleDebugAndroidTest`, install both APKs on a test device, then run:

```bash
adb shell am instrument -w -r com.playereden.sudokutogether.test/androidx.test.runner.AndroidJUnitRunner
```

## Recorded results

- Core behavior and real-socket tests: **32 passed, 0 failed**.
- Android JVM address-validation tests: **2 passed, 0 failed**.
- Extended Android 8/API 26 instrumentation flow: **1 passed**, 73.273 seconds. It covers host creation, number entry, own undo, shared-note state, actual pencil-glyph bounds, activity recreation with retained notes, erase, and room information.
- An earlier basic flow also passed on Android 11/API 30.
- Signed release assembly and debug lint completed successfully. Lint has zero errors; advisory warnings remain for pinned dependency updates, platform backup guidance, and optional SharedPreferences KTX syntax.
- Final Android-8 emulator visual checks: default portrait reviewed; Notes and number controls remain reachable by scrolling at 200% system text. The in-app Share APK action opened Android's sharing chooser successfully. Delivery to another physical phone is part of the remaining home-Wi-Fi check.
- APK package `com.playereden.sudokutogether`, version `0.1.0` / code `1`, minimum API 26, target API 35. Its v2 RSA-3072 signature verifies against the public certificate in DEVELOPMENT.md.
- APK bytes: `7139268`. SHA-256: `29cf0f758b18ac6d3f36ad85580a93fbe28c24c624494b78767ca7fdc4c0cb07`.

The independent read-only review identified two Important issues. Explicit Wi-Fi socket creation and safe same-cell undo lineage were fixed after five new regression tests first failed and then passed. There were no Critical or definite deferred Minor findings. The rendering assertion was refined to check glyph bounds rather than paragraph allocation width; paragraph whitespace had produced a false overflow alarm.

The review did not claim physical-phone execution, target-37 permission behavior, hostile-LAN security, exhaustive fuzz/load testing, power-loss durability beyond the implemented file synchronization/replacement, CI execution, or inspection of private signing custody. This test release retains its approved trusted-LAN/foreground/target-35 scope; these broader cases are not certified by the reported checks.

## Home-Wi-Fi acceptance check

Actual two-physical-phone home-Wi-Fi testing remains a separate check from JVM sockets or emulator UI tests:

1. Install the same APK on both phones and enter different display names.
2. Host a new puzzle on one phone; discover/join it from the other using the room code.
3. Enter a number and a pencil mark from each phone. Confirm both boards agree and each selection marker appears on the other phone.
4. Try the same square at almost the same time. Confirm the accepted change survives and the stale attempt receives a helpful message.
5. Confirm Undo never removes the other person's later work.
6. Background either phone, return, and verify pause/recovery. Turn Wi-Fi off/on and rejoin from the saved invitation.
7. Close/reopen the host, resume the puzzle, and discover/rejoin it from the guest. Confirm saved numbers and notes return.
8. With cellular enabled, also test joining on a Wi-Fi network without internet access. Try manual address joining if discovery fails. Check that the Wi-Fi is not a guest network with client isolation.
9. Finish a puzzle together and confirm both phones receive completion.

No two-phone device-verification claim is made until this checklist or equivalent documented testing is actually completed.
