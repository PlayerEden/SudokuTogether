# GridMates delivery plan

**Current milestone · v0.11D QA.** Start from `main`. The signed update adds Versus player presence and bundled number styles. The [integration audit](V0.11D_INTEGRATION_AND_QA.md) records the exact upgrade evidence and remaining gates.

The owner reported a successful basic two-phone test on v0.11B. [Playtest findings](V0.11B_OWNER_PLAYTEST_2026-11-04.md) guided v0.11C's open nearby joining, optional room codes, clearer chat and Woodland portraits. The current build still needs its own physical co-op and Versus pass.

The [native rollout plan](superpowers/plans/2026-11-07-gridmates-native-rollout.md) tracks the delivered font and Versus stages and the planned Profile and Collection improvements.

## Product direction

Keep classic Sudoku, quick offline play, thoughtful nearby multiplayer and a profile worth keeping. Use short visible labels and disclose detail when requested. “Good puzzles. Good company.” is the favored tagline candidate, suitable for share cards/store exploration; final artwork remains open. Preserve the current native palette and icon. Crossword follows Sudoku completion.

## November 5–7 playtest follow-up

The design notes and implementation now share the same source branch. Keep each feature independently testable.

| Work | Direction | Evidence needed |
| --- | --- | --- |
| QA updates | Preserve the QA identity and increase the build number for each update. | [v0.11D upgrade rehearsal](V0.11D_INTEGRATION_AND_QA.md) |
| Font consistency | Bundled faces use the existing `clear`, `serif` and `mono` IDs. Preview follows Classic number display. | Native preview check passed. Compare the same equipped style on two physical phones. |
| Player interface | Versus identities and progress are in v0.11D. The unified Collection and Profile organization are the next stages in the native rollout. | Validate physical Versus reconnect and compact layouts. Keep older icon selections usable. |
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

