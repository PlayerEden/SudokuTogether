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

The final execution evidence will be recorded here after checks finish. A test definition or a successful compile is not itself evidence that the test ran.

## Home-Wi-Fi acceptance check

Actual two-physical-phone home-Wi-Fi testing remains a separate check from JVM sockets or emulator UI tests:

1. Install the same APK on both phones and enter different display names.
2. Host a new puzzle on one phone; discover/join it from the other using the room code.
3. Enter a number and a pencil mark from each phone. Confirm both boards agree and each selection marker appears on the other phone.
4. Try the same square at almost the same time. Confirm the accepted change survives and the stale attempt receives a helpful message.
5. Confirm Undo never removes the other person's later work.
6. Background either phone, return, and verify pause/recovery. Turn Wi-Fi off/on and rejoin from the saved invitation.
7. Close/reopen the host, resume the puzzle, and discover/rejoin it from the guest. Confirm saved numbers and notes return.
8. Try manual address joining if discovery fails. Check that the Wi-Fi is not a guest network with client isolation.
9. Finish a puzzle together and confirm both phones receive completion.

No two-phone device-verification claim is made until this checklist or equivalent documented testing is actually completed.
