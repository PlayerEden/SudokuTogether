# GridMates native rollout implementation plan

> For implementation, work through the stages below. Keep each stage small enough to verify on its own and update the delivery plan with the evidence.

**Goal** — Turn the November playtest feedback into native improvements and keep existing Sudoku progress across updates.

**Architecture** — Keep native Compose, the offline save model and the same Wi-Fi match flow. Land small changes on top of the current draft. Give any later multiplayer or save-format change its own compatibility and recovery tests.

**Tech stack** — Kotlin, Jetpack Compose, Gradle, Android SDK and JUnit.

**Design references** — [Delivery plan](../../GRIDMATES_DELIVERY_PLAN.md), [player presence and menus](../specs/2026-11-04-player-presence-and-menu-design.md), and [APK update and save preparation](../../GRIDMATES_UPDATE_AND_SAVE_PREPARATION.md).

## Baseline and review gates

- Start from draft [PR #29](https://github.com/PlayerEden/SudokuTogether/pull/29) at `9f6111fbf564eac35eb39197a547e985b813e3a8`. v0.11C is the reference build for install-over acceptance.
- Keep the existing font, ink, theme, emblem and portrait IDs. Keep save files and unlock rules readable across updates.
- Preserve Android min SDK 26 and the current SDK, dependency and LAN protocol choices in the font stage. Keep room codes optional in the usual host flow.
- Keep each stage in a reviewable draft. A local compile and APK are development evidence. Testers receive an update only after the original signing identity, increasing version code and an install-over rehearsal are verified.
- Use the current grid and keypad as the space budget. Verify small screens, large text, readable contrast, focus order and accessibility labels as screens move into the Android app.

## Delivery order

| Stage | Implement | Acceptance |
| --- | --- | --- |
| 1. Consistent number styles | Bundle the three existing font choices, fix the equipped preview to follow Classic number display, and retain saved selections. | Each ID resolves to its own bundled face. Turning Classic off restores the equipped look. The same font and settings produce the same digit shapes on both phones. |
| 2. Versus presence | Put both players and their progress above the live board. Show searching, connected, reconnecting and finished states. Use the real local and remote filled counts. | Count player-filled squares separately from givens. Keep the board and keypad accessible on narrow screens. Check guest and host views, an interrupted match and a completed result. |
| 3. Profile and Collection | Put records and earned milestones in Profile. Use one Collection with portrait, emblem, theme, ink and font filters. Keep Play a direct path to a puzzle. | Existing Woodland portraits and legacy emblems still equip. Locked items explain how to earn them. Validate keyboard and TalkBack navigation and long names. |
| 4. Co-op board themes | Resolve one shared board theme from the two equipped choices. Define an explicit theme order and a stable tie result. Keep each player's entry ink and typeface. | Both phones agree on the theme across reconnects and handle an unknown ID safely. Check both authors' numbers on light and dark themes. |
| 5. Versus results | Add a clear winner/result summary and a replay path. Offer full board comparison after completion when both people opt in. | Keep the opponent's entries private during play. Declining or losing a connection leaves a usable results screen. A rematch starts a fresh comparison decision. |
| 6. Coach and scoring | Surface candidate eliminations and why they work. Test the last few cells and an earlier wrong entry. Show assistance clearly in records and results. | Compare deductions to a valid solution and preserve the agreed difficulty meanings. Players can keep asking for help. Score changes and result sharing get their own review. |
| 7. Collections and Daily rewards | Use generated art for achievement badges and earned profile frames, then add Ocean and Bug portraits through the unified catalog. Choose the Daily reward and reusable offline gift rules before wiring them into progression. | Every asset has a stable ID and unlock route. Record the sources for generated art and check small-size readability. Preview, equip, save and restore every new item. |
| 8. Save and release work | Rehearse an in-place upgrade before distributing the first new candidate. Plan optional offline export and preview/restore separately. Run modern Android and two-phone release checks before Play testing. | Retain puzzles, notes, undo, times, records, unlocks, selected cosmetics, settings and saved rooms. Validate a backup before replacing progress. Release signing and store requirements receive a separate check. |

Router-free nearby play and optional cloud saves are later projects. They need their own discovery, identity and recovery tests. The everyday experience stays focused on Solo and local Wi-Fi play.

## Stage 1 implementation

**Baseline behavior** — `fontFor` mapped `clear`, `serif` and `mono` to generic Android families. With `uniformNumbers` on, the board switched to the default font and ink while Collection showed the equipped style. The two-phone check controls for the setting and equipped ID.

**Typeface choice** — Use unmodified static Noto Sans Regular and Bold for Clear, Noto Serif Regular for Bookish, and Liberation Mono Regular for Terminal. Fixed clues use Clear Bold. Styled chat still follows the equipped typeface, with device fallback for characters outside the bundled fonts.

**Files**

- `app/src/main/res/font/` contains the four licensed assets. `app/src/main/assets/font_licenses.txt` carries their notices and license texts. `docs/GRIDMATES_FONT_ASSETS.md` records their source and hashes.
- `NumberAppearance.kt` owns `fontFor(id)` and `Appearance.withNumberDisplay(uniformNumbers)`. Unknown IDs render as Clear. Classic display uses the default ink and font while preserving the rest of the appearance.
- `PlayScreen.kt`, `CosmeticUi.kt` and `JournalScreen.kt` use that policy for authored board digits, notes, fixed clues and the main Style Preview. Individual style swatches continue showing the style they represent.
- `NumberAppearanceTest.kt` checks resource families, the fallback and the reversible Classic override. `NumberFontPreviewTest.kt` checks the rendered digit styles and label in Collection.
- The QA workflow resolves its keystore path within the restore step and passes it to Gradle after certificate verification.

### Review focus

- A saved Bookish or Terminal choice survives loading and applies again after Classic display is turned off.
- Classic display leaves board, portrait and emblem selections in place and explains the override in Collection.
- An unknown font ID safely renders Clear. A host and guest keep their own font when Classic display is off.
- Every bundled asset provides all digits, with bold clues and compact notes checked on Android.
- A compile or an unsigned release candidate cannot stand in for a verified install-over with the original key.

### Tasks

- [x] Reproduce the generic-family failure against the original resolver. A focused JUnit run compiled the core and app unit tests with Kotlin 2.1.20, the app's Compose 1.8.0 artifacts and generated Android resource IDs. All 112 core and eight app tests passed after the fix.
- [x] Add the assets, licenses and shared display policy. Route the main preview and board through it while retaining the equipped choice.
- [x] Compile and link the four font resources with Android's resource tools. Verify all digit outlines are present and compare the packaged font and license bytes to the source files.
- [x] Run workflow lint and the APK preflight tests after repairing the signing-path reference. Actionlint and all 13 Python tests passed.
- [x] Run `:core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest` on the combined Versus branch. The four fonts and license notice in the APK match the source bytes.
- [x] Run the Collection preview instrumentation test on the API 26 emulator.
- [ ] Exercise the three fonts on both phones using the same setting. Include a dark board and pencil notes at large text size.
- [ ] Use the original v0.11C signing key and a newer version code for the install-over rehearsal. Record actual saved data and visual results before offering a download.

## Stage 2 implementation

Stack the Versus pass on [PR #30](https://github.com/PlayerEden/SudokuTogether/pull/30). Read both identities and aggregate fill counts from the existing race snapshot. Each track has the same target, the number of cells that started empty. Clearing an entry can reduce progress. Pencil marks do not fill a cell.

- [x] Test the presentation mapping with real host and guest snapshots. Cover an open seat, a live race, a player stepping away, a dropped guest connection, and an authoritative finish.
- [x] Put both players and the race state above the grid in portrait and above the keypad in the landscape side rail. Keep the known rival and their last count visible while paused. The open seat has no score.
- [x] Check long names and keypad access at 320dp with 200% text, rematch progress reset, and the finished view. Keep Reconnect available to a disconnected guest alongside the finished result so they can rejoin for the next race.
- [ ] Play a race on two physical phones. Verify join, erase, pause, reconnect, finish, and the next race from each seat.

The combined branch built both Android packages and passed 112 core tests, 12 app unit tests, and 13 APK update preflight tests. The fresh Android lint run reported zero errors and 17 warnings in existing files. Device regressions reproduced a missing Reconnect action after a finish and truncated names at 200% text before the fixes.

The first API 26 emulator run passed 12 of 13 checks, including all Versus cases, compact Solo layout, host smoke tests, and the font preview. The standalone chat test missed the “Rematch?” quick reply and failed again when the chat test class was rerun.

On a fresh API 26 emulator, the parent build at `290ad06` passed all four chat and room UI checks. The rebuilt Versus branch passed all eight checks on the same emulator, including those four, the three Versus UI cases, and compact Versus landscape at 200% text.

## Stage 3 implementation

Start from the integrated v0.11D checkpoint. Use the native navigation and reuse the existing record and unlock models.

- [x] Route the bottom Profile tab and home avatar to the same profile. Make records, Daily history and achievements reachable from there.
- [x] Group Collection into Player icons, Boards and Numbers. Keep both basic emblems and Woodland portraits in the icon browser and retain their existing eligibility rules.
- [x] Verify that tapping a locked icon shows its real progress. Confirm that a separate equip action selects an available icon and that saved styles and records survive the upgrade.

## First release preparation alongside Stage 3

- [x] Use Android SDK 36 for builds and API 36 for the hosted visual suite. Verify the supported SDK floor and both signing paths.
- [x] Produce the signed App Bundle alongside the existing production APK in the manual workflow. Fail before upload if either artifact cannot be verified. Local signing and Bundletool validation passed.
- [x] Add portable progress export and restore to Settings. Validate the file before preview, require confirmation before replacing progress, and recover the destination profile and settings if a restore is interrupted.
- [x] Rehearse a signed v0.11D update with real saved games. Exercise backup transfer between independent local profiles and verify old room saves and destination identity.
- [ ] Complete current-build physical Wi-Fi, TalkBack and Play Console acceptance.
