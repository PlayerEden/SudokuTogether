# GridMates: Sudoku

The full Android source, tests and design history live on `main`. [Start here](CONTINUE_HERE.md) · [Project handoff](docs/PROJECT_HANDOFF.md) · [Delivery plan](docs/GRIDMATES_DELIVERY_PLAN.md)

**v0.11D QA** adds both players' identities and progress to the Versus screen, reconnect feedback, and consistent bundled number styles. The signed update uses the installed Sudoku QA app's identity. [Repository audit and upgrade evidence](docs/V0.11D_INTEGRATION_AND_QA.md)

**v0.11F test candidate** adds shared Co-op themes, Versus finish summaries and a same-room rematch. [QA evidence](docs/V0.11F_LOCAL_MULTIPLAYER_QA.md) · [Delivery plan](docs/GRIDMATES_DELIVERY_PLAN.md)

**v0.11E** introduced Profile and Collection, portable progress backup, and the Android 16 build and signing path. [Candidate PR #33](https://github.com/PlayerEden/SudokuTogether/pull/33) · [QA evidence](docs/V0.11E_PROFILE_PROGRESS_QA.md)

## Earlier QA milestones

> **v0.11C rollback QA** introduced open nearby joining, optional remembered code protection and 12 earned Woodland portraits. [Native QA audit](docs/V0.11C_NEARBY_WOODLAND_QA.md) · [APK receipt](docs/V0.11C_DELIVERY_RECEIPT.md)

> **v0.11B rollback QA** on [feat/gridmates-coach-launcher](https://github.com/PlayerEden/SudokuTogether/tree/feat/gridmates-coach-launcher), stacked on v0.11A. Launcher **Sudoku**, full **GridMates: Sudoku** brand retained; explicit Solo/Daily entry checks with saved assistance. **103 JVM +46 API26 native cases passed**, lint0 errors/14 warnings, exact17→18 update verified. [QA audit](docs/V0.11B_COACH_LAUNCHER_QA.md) · [Delivery plan](docs/GRIDMATES_DELIVERY_PLAN.md) · [Recovery log](docs/V0.11B_RECOVERY_LOG.md). Main/development and earlier checkpoints remain unchanged.

[v0.11A GridMates identity and concise UI — draft PR26](https://github.com/PlayerEden/SudokuTogether/pull/26)

> **Earlier identity milestone: GridMates v0.11A**, on [`feat/gridmates-brand-copy`](https://github.com/PlayerEden/SudokuTogether/tree/feat/gridmates-brand-copy). [Brand/copy audit](docs/V0.11A_GRIDMATES_QA.md). The approved public name is GridMates: Sudoku; logo concepts remain provisional. The earlier v0.10D source is preserved below.

> **v0.11A QA:** version17 / `0.11.0-dev1-qa`; 87 JVM tests and 31 accepted API26 native cases passed, lint0 errors/20 warnings. The exact APK updated v0.10D while preserving the seeded profile, active puzzle/note, identity and preferences. [QA checkpoint](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.11a-gridmates-qa-green). Two-phone/device release gates remain open.

> **Earlier v0.10D Visual QA**, stacked on the verified v0.10C source. [Visual QA audit](docs/V0.10D_VISUAL_QA.md)

> **Changes:** a cohesive native game theme and icon; immediate Solo Resume and Daily access; Play/Records/Collection/Settings navigation; cosmetic previews; framed grid, clearer controls, adaptive landscape/large-text layouts, improved results and Join keyboard flow. QA `0.10.0-dev4-qa`  / code16. The v0.10C save integrity work remains intact. 87 JVM tests and 42 API26 native cases passed; lint 0 errors/18 warnings. Both independent-review regressions are GREEN after observed RED fixtures. Physical two-phone testing remains deferred; this is not a Play release.

> **Historical notes below** document the original v0.1–v0.4 development stages, including test-status statements from those stages. They are preserved for traceability, not the current build status.

A native Android Sudoku game built for **solo, local Wi-Fi co-op, and head-to-head racing**. Co-op and versus use two phones: one hosts, the other joins on the same home Wi-Fi. Solo and local personal records work offline. A cloud account, internet rooms, and synced leaderboards are **not** included.

GridMates: Sudoku is the approved public name. The logo is provisional; see the [identity brief](docs/GRIDMATES_VISUAL_IDENTITY_BRIEF.md). The game uses its own implementation and original generated puzzles.

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

1. Install the same accepted QA APK on both Android phones (Android 8 or newer). If Sudoku is already installed, open the APK and choose **Update** to keep your progress.
2. Connect both phones to the same home Wi-Fi. Guest networks with device isolation can block connections.
3. Host a co-op puzzle or Versus race on one phone. On the other, choose **Join a game** and select the nearby room. Enter the displayed code if the host enabled **Require a code**.
4. If the room is missing from the list, enter the complete host address, including its port. Supply the code for a protected room.
5. Both people can enter numbers and shared notes freely. Starting clues stay fixed. Duplicate numbers are highlighted; wrong guesses do not end the game.
6. Keep both apps visible while playing. If either phone leaves the game, entries pause. Return to the apps, or use **Reconnect** and select the host's room again. The host can reopen **Resume your puzzle** after a restart.

Play multiplayer on a trusted home network. Optional room codes control who can join.

## Build

Install JDK 17, Android SDK Platform 36 and Build Tools 35.0.0. Set `ANDROID_HOME` to the SDK directory (or use an untracked `local.properties`).

```bash
./gradlew :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

For an update to the installed Sudoku QA app, follow the original-signer and increasing-version checks in the [QA update guide](docs/GRIDMATES_UPDATE_AND_SAVE_PREPARATION.md). The ordinary developer build above uses the local debug signer. The manual QA workflow builds the selected revision and verifies APK identity. The production signing workflow serves the separate production app. [Actions](https://github.com/PlayerEden/SudokuTogether/actions) · [Development notes](docs/DEVELOPMENT.md)

## Project records

- [Approved design](docs/superpowers/specs/2026-10-25-sudoku-together-design.md)
- [Implementation plan](docs/superpowers/plans/2026-10-25-local-coop-implementation.md)
- [Verification and device checklist](docs/TESTING.md)
- [Development and signing](docs/DEVELOPMENT.md)
