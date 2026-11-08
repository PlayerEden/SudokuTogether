# GridMates delivery plan

November 7 follow-up. The [player presence and menu design](superpowers/specs/2026-11-04-player-presence-and-menu-design.md) and [APK update and save preparation](GRIDMATES_UPDATE_AND_SAVE_PREPARATION.md) build on v0.11C, which remains the latest delivered QA APK.

The [native rollout plan](superpowers/plans/2026-11-07-gridmates-native-rollout.md) tracks the bundled number styles, the [Versus player panel](superpowers/plans/2026-11-07-gridmates-native-rollout.md#stage-2-implementation), and the remaining two-phone and signed upgrade checks.

Updated November4, 2026 (America/Chicago). Current verified QA milestone: v0.11C, `feat/gridmates-nearby-animals` ([draft PR28](https://github.com/PlayerEden/SudokuTogether/pull/28)), stacked on the owner-accepted v0.11B.117 JVM tests and78 accepted native executions; original18→19 update verified. Preserve the earlier drafts/checkpoints and v0.11B rollback. [Current audit and exact source/artifact evidence](V0.11C_NEARBY_WOODLAND_QA.md). The older development app is not the implementation baseline.

The owner reports a successful two-phone test and asks to lock the current game. [Playtest findings and next-pass recommendation](V0.11B_OWNER_PLAYTEST_2026-11-04.md) cover simpler hosting/joining and room header/chat refinement before the next larger product stage. Open nearby joining is now the chosen easiest default, with Require a code optional; it is implemented and verified in v0.11C. Design especially for older/less technically confident players. The Woodland portrait collection is implemented and retains existing emblems/unlocks. Basic physical co-op is now reported successful; the remaining detailed physical checklist is still open.

## Product direction

Keep classic Sudoku, quick offline play, thoughtful nearby multiplayer and a profile worth keeping. Use short visible labels and disclose detail when requested. “Good puzzles. Good company.” is the favored tagline candidate, suitable for share cards/store exploration; final artwork remains open. Preserve the current native palette and icon. Crossword follows Sudoku completion.

## November 5–7 playtest follow-up

The [new design notes on development](https://github.com/PlayerEden/SudokuTogether/tree/1818cd54e2fc5c5eaecf37389295ae3d55b898c9/docs) supplement the active v0.11C source. Keep each feature independently testable.

| Work | Direction | Evidence needed |
| --- | --- | --- |
| QA updates | Use the original QA identity, verify the candidate package and increase the build number. Keep a verified current download once a release asset is approved. | Install the exact original v0.11C APK, create progress, install over it and verify the saved data through the app. |
| Font consistency | Compare the same unlocks, equipped font and Classic number setting on both phones. Replace generic Android family mappings with licensed bundled faces under `clear`, `serif` and `mono` when the design is selected. | The same digit style in preview and gameplay after reopening on both phones. |
| Player interface | Bring both identities and progress above the Versus board. Give achievements a Profile entry and bring every player icon into one Collection browser. Ocean and Bug sets can use its family filters. | Small-screen and large-text layouts, readable progress, and old icon selections retained. |
| Versus results | Start with the result and progress already known to both clients. A later full board comparison requires an authoritatively finished race and consent from both players. | The race still keeps opponent entries private. Declining or disconnecting leaves the results usable. |
| Co-op board themes | Resolve a common board from the players' equipped themes. Keep authored digits in each player's personal ink/font. | Both phones agree on the board and show legible numbers across themes and reconnects. Final ranking, timing and an accessibility override are open choices. |
| Coach | Use the Sudoku research for visible proofs of the four supported techniques and helpful endgame guidance. | Verified examples with candidate eliminations, 1–2 cells left, and an earlier incorrect entry. Preserve existing difficulty grades. |
| Later design work | Choose a small Daily reward model, consider reusable offline gift codes, and evaluate router-free nearby play separately. | Clear unlock and restore behavior for rewards. Physical connectivity checks before changing the multiplayer transport. |

## Stages

| Stage | Deliverable | Acceptance |
| --- | --- | --- |
| 1 — v0.11B, verified QA | Launcher label **Sudoku**; useful Solo/Daily entry checking when an earlier mistake blocks logical hints | Full in-game/app name retained; same icon/package/certificate; explicit solution checks, accurate assisted results, safe Undo/resume, actual v0.11A upgrade test and native UI checks |
| v0.11C — Nearby/collection refinement | Open nearby joining, optional remembered code, stable header/chat,12 earned Woodland portraits |117 JVM +78 native executions, old-APK continuity and installed-byte proof; renewed physical two-phone testing remains open |
| 2 — Coach guidance | Clear proof highlights and progressive explanations for supported logical techniques, followed by an explicitly requested single-entry apply action if useful | Real proof from current board; no guesses or note-dependent mathematics; invalidate stale previews; track assistance without double-counting one deduction; no numeric score yet |
| 3 — Share result | Optional finish-screen button, preview, one polished on-device PNG format and native Android sharing for Solo/Daily | Persisted time/difficulty/assistance; no solution digits, invented score or store link; cancellation/export failure preserves the game; physical share-target acceptance |
| 4 — Player profile | Chosen avatar/style, earned badge showcase, concise honest statistics; profile-card export afterward | Preserve owned cosmetics/history and legacy records; accessible defaults, empty-state and long-name layouts; editable sharing privacy |
| 5 — Scoring | Versioned, calibrated score with a clear optional breakdown | Distinct deduction/tier accounting across Undo/resume; old scores remain unknown; assistance stays unlimited; assisted/unassisted comparisons stay separate; no arbitrary speed bonus |
| Release work alongside stages | Reconcile stacked source, target API36+, production signing/AAB, modern-device/accessibility QA, Play setup/testing and accurate store materials | Follow the launch guide and actual account eligibility. No publication or source promotion is implied. Basic two-phone co-op is owner-reported successful; the remaining physical lifecycle/complete-game/versus checklist is open |
| After a clean beta | Rare between-puzzle ads and optional one-time Remove Ads | The free model is decided; price/frequency remain open. Test consent, purchases/restoration and interruption-free play before live use |

The five new proposal files remain design references. An approved product direction is distinct from implemented, tested or released software. Global leaderboards, remote play, cloud backup, paid randomized cosmetics, Split Clues and framework extraction remain separate future decisions/gates.

## First APK scope

Stage 1 is the first implementation pass. It does not bundle the later stages into the launcher-label request. Entry checks never change a value automatically. The player can explicitly erase the identified entry and use normal Undo. A successful comparison against the original unique puzzle is counted separately from logical hints and marks the finish Assisted. Duplicate-rule feedback and an empty check do not add assistance. Existing records retain their meaning; optional new fields default to zero.

Each later stage gets its own concrete implementation plan and verified checkpoint. Stage1 used the existing native toolchain, min26/target35, schema1/protocol5 and v0.10D/v0.11A QA signing identity. API36 migration belongs to release work rather than this small launcher change.

Stage1 acceptance:103 JVM +46 API26 native cases; original17→18 continuity and installed short launcher/full application labels verified. [Current QA and remaining gates](V0.11B_COACH_LAUNCHER_QA.md). Stage2 remains the next larger product stage; the November4 recommendation is a contained multiplayer usability pass before it. No later-stage implementation is implied.


The contained multiplayer/collection pass before Stage2 is now delivered as v0.11C. Native/source evidence and its limited physical-device coverage are recorded in the current audit; no later-stage implementation or source promotion is implied.

