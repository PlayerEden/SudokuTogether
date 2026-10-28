# Sudoku Together

> **Development context backup:** **[Read the living project handoff](docs/PROJECT_HANDOFF.md)** before editing. See the [roadmap](docs/ROADMAP_V08_TO_LAUNCH.md), [v0.8C QA evidence](docs/V0.8C_DIFFICULTY_SELECTION_HANDOFF.md), and [known-green checkpoint](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8c-qa-green). This full Android source is on `development`; the default `main` is currently a docs/workflow landing branch. New milestone PRs should target the newest verified source, and verified work should be promoted back to `development` with this handoff updated.


> **Current development (v0.8E):** the native Android game now includes an unambiguous Solo timer, separate assisted/unassisted Solo records and per-difficulty best times, clearer instructions for Hint/pencil cleanup and more accessible Hint/Help controls. Previous Easy/Medium/Hard, local multiplayer, Solo hints and notes remain intact. Full Gradle QA passed [run 40612864973](https://github.com/PlayerEden/SudokuTogether/actions/runs/40612864973); **9/9 Android emulator tests passed** [run 40612871881](https://github.com/PlayerEden/SudokuTogether/actions/runs/40612871881). Exact verified source at [`checkpoint/v0.8e-qa-green`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8e-qa-green). **Actual two-phone Wi-Fi and large-font/TalkBack QA are still needed.** Read [the living handoff](docs/PROJECT_HANDOFF.md) and [v0.8E audit](docs/V0.8E_SOLO_FAIRNESS_QA.md); v0.9A physical LAN validation is next. Reusable framework extraction remains postponed until Sudoku is finished.

> **Historical notes below** document the original v0.1–v0.4 development stages, including test-status statements from those stages. They are preserved for traceability, not the current build status.

A native Android Sudoku game built for **solo, local Wi-Fi co-op, and head-to-head racing**. Co-op and versus use two phones: one hosts, the other joins on the same home Wi-Fi. Solo and local personal records work offline. A cloud account, internet rooms, and synced leaderboards are **not** included.

The name is provisional. This project has its own implementation and original generated puzzles; an unrelated app already uses the same public title.

## v0.4 experimental: player badges and earned milestones

The stacked `feat/player-identity-v0.4` branch adds **six earned player emblems**, **nine achievements with progress**, compact opponent identity cards, and a post-game co-op summary showing each partner's *final filled entries*. Achievements derive from locally saved completed games; they are not verified by an online server. Player-selected emblems are now exchanged over protocol **v4** on matching Android `0.4.0-dev` APKs.

Players can enable **Classic number display** to show all entries in a uniform, readable style only on their own device. The cosmetic identity still belongs to the author in the underlying game state. This pass also repairs unread-chat counts while the chat sheet is open and expands local record retention to 10,000 finished games.

**Development status:** source and new test cases committed, with a static source pass that caught and fixed two missing Kotlin parentheses. The app has **not** been compiled, signed, or physically tested on two phones in this environment. [Detailed v0.4 handoff and QA](docs/V0.4_HANDOFF.md).

## v0.3 experimental: player identity and local chat

The stacked `feat/chat-cosmetics-v0.3` branch introduces an optional collapsible room chat for co-op and versus, with quick phrases and a 30-line memory-only chat history. It also adds earned number colors, local board backgrounds and built-in font choices under **My profile & records**.

In co-op, numbers written by each player carry that player's equipped color and number font. The starting clues stay neutral; undo restores the previous contributor's style. In versus, an opponent's equipped cosmetic set is visible as a small identity label, but their private solution entries remain hidden. Profile unlocks are earned from device-local saved achievements, not online accounts or a store.

**Status:** source and tests have been authored; compilation, Android tests, signing and two-phone acceptance are pending. Wi-Fi protocol **v3** and Android `0.3.0-dev` require both players to upgrade together. [Implementation and QA handoff](docs/V0.3_HANDOFF.md).

## v0.2 development branch: solo, versus, and player records

Branch: `feat/solo-profiles-race-v0.2` (based on the existing v0.1 co-op branch).

- **Solo:** Standard 9×9 Sudoku, pencil marks, erase, undo, running active-play timer, resume after leaving, and save-on-edit. Solo progress is stored separately from any hosted multiplayer room.
- **Versus:** host or join a same-Wi-Fi race; each phone gets the same starting clues on its own independent grid; the host accepts the first fully valid solution as the winner. Both players see fill counts, **never the opponent's values or notes**. Games pause if a phone leaves; host can create a fresh race, resume the room, or replace the rival.
- **Personal profile:** solved count, best solo time, accumulated solo solving time, head-to-head W/L/D, co-op completions and recent results. Records and a random local profile ID are persisted atomically on the device and duplicate completions are prevented.
- Existing v0.1 co-op remains the default `Host a puzzle` option, with `Host a versus race` offered separately.

**Development status:** Source and regression/smoke tests are committed, but the new Android build and two-phone v0.2 acceptance tests **have not yet run**. Do not treat this branch as a validated release APK. All v0.2 test phones must install the same updated APK because the LAN wire protocol advances to version 2. The Android versionCode is 2 (`0.2.0-dev`).

**No cloud sync yet:** local stats survive ordinary restarts and upgrades using the same package/signing key, but clearing app storage or uninstalling can erase them. Sync, sign-in, account recovery, and leaderboards require a separate secured service in a later milestone. See [v0.2 handoff](docs/V0.2_HANDOFF.md).

## First test version (v0.1 co-op baseline)

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

The multiplayer modes support a trusted home LAN. The room code controls admission; LAN game traffic is not encrypted. Remote internet rooms, guaranteed background hosting, all hotspot configurations, online accounts, and public-store publication are outside the currently tested v0.1 baseline.

## Build

The v0.2 branch requires a fresh Android compile and physical multiplayer acceptance before sharing a new APK. Do not replace the original signed APK with a debug build if you plan to update in place; keep the same original signing identity.\n
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
