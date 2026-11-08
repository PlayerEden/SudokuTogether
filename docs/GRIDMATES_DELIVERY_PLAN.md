# GridMates delivery plan

**Current candidate · v0.11G.** Solo and Daily have visual Coach proofs, and Collection has six Ocean portraits. v0.11F introduced shared Co-op themes and the Versus finish and rematch flow. [v0.11G QA](V0.11G_COACH_OCEAN_QA.md) · [v0.11F QA](V0.11F_LOCAL_MULTIPLAYER_QA.md)

[Draft PR #33](https://github.com/PlayerEden/SudokuTogether/pull/33) contains v0.11E's Profile, Collection, portable progress transfer and API 36 release preparation. The merged v0.11D QA checkpoint introduced Versus presence and bundled number styles. [v0.11D audit](V0.11D_INTEGRATION_AND_QA.md)

The owner reported a successful basic two-phone test on v0.11B. [Playtest findings](V0.11B_OWNER_PLAYTEST_2026-11-04.md) guided v0.11C's open nearby joining, optional room codes, clearer chat and Woodland portraits. The current build still needs its own physical co-op and Versus pass.

The [native rollout plan](superpowers/plans/2026-11-07-gridmates-native-rollout.md) tracks the delivered font, Versus and Profile and Collection stages.

## First release checklist

| Gate | Status |
| --- | --- |
| Player profile and collection | v0.11E added Profile and Collection. v0.11G adds six Ocean portraits that use the existing completion history. [Ocean milestones](V0.11G_COACH_OCEAN_QA.md#ocean) |
| Owner updates and transfer | The signed v0.11F to v0.11G install-over passed. The v0.11E split release build restored an exported QA file and reopened the same saved entry and pencil note. [Progress transfer guide](GRIDMATES_UPDATE_AND_SAVE_PREPARATION.md) |
| Android build and signing | The build targets API 36 and supports devices from API 26. The original production certificate was verified and the App Bundle passed local validation. |
| Device acceptance | Finish current-build co-op and Versus on two physical phones, plus modern Android orientation, TalkBack, and Daily rollover. [LAN gate](https://github.com/PlayerEden/SudokuTogether/issues/12) |
| Play Console | Confirm signing enrollment, listing and declarations, and run the required testing track. [Launch guide](GOOGLE_PLAY_FROM_SCRATCH_LAUNCH_GUIDE.md) |

## Product direction

Keep classic Sudoku, quick offline play, thoughtful nearby multiplayer and a profile worth keeping. Use short visible labels and disclose detail when requested. “Good puzzles. Good company.” is the favored tagline candidate, suitable for share cards/store exploration; final artwork remains open. Preserve the current native palette and icon. Crossword follows Sudoku completion.

## November 5–7 playtest follow-up

The design notes and implementation now share the same source branch. Keep each feature independently testable.

| Work | Direction | Evidence needed |
| --- | --- | --- |
| QA updates | Preserve the QA identity and increase the build number for each update. | [v0.11F upgrade rehearsal](V0.11F_LOCAL_MULTIPLAYER_QA.md) |
| Font consistency | Bundled faces use the existing `clear`, `serif` and `mono` IDs. Preview follows Classic number display. | Native preview check passed. Compare the same equipped style on two physical phones. |
| Player interface | Versus identities and progress are in v0.11D. The unified Collection and Profile are in v0.11E. | Validate physical Versus reconnect and compact layouts, including large text. |
| Versus results | v0.11F shows the winner, both filled counts and the local grid at the finish. The host can start a fresh race once both players are present. | Test a complete race and rematch on two phones, including a rival leaving and rejoining at the result. A future two-board comparison requires consent from both players. |
| Co-op board themes | v0.11F selects from Classic, Linen, Midnight and Circuit in that order of priority. Authored entries keep their personal ink and font, with the existing uniform-number option. | Test both phones across themes, saved-room resume and partner rejoin. Use the device playtest to evaluate a local board override. |
| Coach | v0.11G opens with a target square and reveals the supporting grid and candidate removals through **Show why**. **Check my work** handles an earlier incorrect entry. | Check small-phone readability, large text, and play near the end of a puzzle. [Coach QA](V0.11G_COACH_OCEAN_QA.md#coach) |
| Later design work | Choose a small Daily reward model, consider reusable offline gift codes, and evaluate router-free nearby play separately. | Clear unlock and restore behavior for rewards. Physical connectivity checks before changing the multiplayer transport. |

## Stages

| Stage | Deliverable | Acceptance |
| --- | --- | --- |
| 1 — v0.11B, verified QA | Launcher label **Sudoku**; useful Solo/Daily entry checking when an earlier mistake blocks logical hints | Full in-game/app name retained; same icon/package/certificate; explicit solution checks, accurate assisted results, safe Undo/resume, actual v0.11A upgrade test and native UI checks |
| v0.11C — Nearby/collection refinement | Open nearby joining, optional remembered code, stable header/chat,12 earned Woodland portraits |117 JVM +78 native executions, old-APK continuity and installed-byte proof; renewed physical two-phone testing remains open |
| 2 — Coach guidance | v0.11G shows a read-only proof preview and progressive explanations for the four supported techniques. | The player places each digit. Verify candidate removal and late puzzles on a phone. An optional apply action is a later design choice. |
| 3 — Share result | Optional finish-screen button, preview, one polished on-device PNG format and native Android sharing for Solo/Daily | Persisted time/difficulty/assistance; no solution digits, invented score or store link; cancellation/export failure preserves the game; physical share-target acceptance |
| 4 — Profile and card | v0.11E delivers the profile, achievements, history and Collection. Profile-card export follows the Solo and Daily result card. | Preserve owned cosmetics and legacy records. Check empty states, long names, large text and editable sharing privacy. |
| 5 — Scoring | Versioned, calibrated score with a clear optional breakdown | Distinct deduction/tier accounting across Undo/resume; old scores remain unknown; assistance stays unlimited; assisted/unassisted comparisons stay separate; no arbitrary speed bonus |
| Release work alongside stages | Verify current-build physical co-op and Versus, modern-device accessibility, and Play setup and testing | Use the API 36 and signed App Bundle evidence above. The full physical lifecycle, complete-game and Versus checklist is open. |
| After a clean beta | Rare between-puzzle ads and optional one-time Remove Ads | The free model is decided; price/frequency remain open. Test consent, purchases/restoration and interruption-free play before live use |

The proposals cover additional work beyond the features in the current QA candidate. Global leaderboards, remote play, cloud backup, paid randomized cosmetics, Split Clues and framework extraction remain future decisions.

## November 8 proposal review

The Home screen still places the difficulty picker directly below **Continue** when a Solo puzzle is saved. A short first-use test can check whether players understand that the picker applies to **New puzzle**. The saved-puzzle caption counts givens. If players read it as their own progress, show the number of their entered squares instead. Keep the current mode tabs and Daily card during that comparison. [First-impression proposal](https://github.com/PlayerEden/SudokuTogether/blob/main/docs/GRIDMATES_HOME_FIRST_IMPRESSION_USABILITY_CONSIDERATIONS.md)

For board and number style pilots, compare distinct light, marine and dark palettes on the same real 9×9 sample. Compare candidate font faces using every digit and pencil marks at phone size. v0.11F already shares an equipped Co-op board while rendering each player's own entries, so new themes need an explicit display priority and contrast checks. Review a durable record of earned unlocks before relying on a larger milestone catalog. [Board and font expansion proposal](https://github.com/PlayerEden/SudokuTogether/blob/main/docs/GRIDMATES_BOARD_THEMES_AND_NUMBER_FONTS_EXPANSION.md)

## Online expansion after the offline launch

The November 8 owner decision puts the complete offline game and a clean Google Play release first. Solo, Daily, nearby Co-op and Versus work without an account. The game will be free to install. Rare between-puzzle ads and a one-time Remove Ads purchase are the approved monetization directions, subject to testing and store requirements.

A later online phase requires its own approval. Evaluate player demand, retention, net earnings and ongoing service costs after launch. Set a budget ceiling before prototyping. If the phase is sustainable, start with invite-only asynchronous friend challenges, then consider remote Versus and Co-op. Public matchmaking depends on an active player base. Offline play remains available during service outages. [Online feasibility and funding gates](https://github.com/PlayerEden/SudokuTogether/blob/development/docs/GRIDMATES_ONLINE_FRIEND_CHALLENGES_AND_MATCHMAKING_FEASIBILITY.md)

## First APK scope

Stage 1 is the first implementation pass. It does not bundle the later stages into the launcher-label request. Entry checks never change a value automatically. The player can explicitly erase the identified entry and use normal Undo. A successful comparison against the original unique puzzle is counted separately from logical hints and marks the finish Assisted. Duplicate-rule feedback and an empty check do not add assistance. Existing records retain their meaning; optional new fields default to zero.

Each later stage gets its own concrete implementation plan and verified checkpoint. Stage1 used the existing native toolchain, min26/target35, schema1/protocol5 and v0.10D/v0.11A QA signing identity. API36 migration belongs to release work rather than this small launcher change.

Stage1 acceptance:103 JVM +46 API26 native cases; original17→18 continuity and installed short launcher/full application labels verified. [Current QA and remaining gates](V0.11B_COACH_LAUNCHER_QA.md). Stage2 remains the next larger product stage; the November4 recommendation is a contained multiplayer usability pass before it. No later-stage implementation is implied.


The contained multiplayer/collection pass before Stage2 is now delivered as v0.11C. Native/source evidence and its limited physical-device coverage are recorded in the current audit; no later-stage implementation or source promotion is implied.

