# GridMates delivery plan

Updated November 4, 2026 (America/Chicago). The owner authorized planning and starting this work after reviewing the new proposals. Latest verified and owner-accepted baseline: v0.11B, `feat/gridmates-coach-launcher` at `77f900f73f21dcb5a02cdae01fab71f00514b898`, stacked on v0.11A. Continue from that source and preserve the existing drafts/checkpoints. The older development app is not the implementation baseline.

The owner reports a successful two-phone test and asks to lock the current game. [Playtest findings and next-pass recommendation](V0.11B_OWNER_PLAYTEST_2026-11-04.md) propose simpler hosting/joining and room header/chat refinement before the next larger product stage. Optional room-code policy is a proposal, not implemented behavior. Basic physical co-op is now reported successful; the remaining detailed physical checklist is still open.

## Product direction

Keep classic Sudoku, quick offline play, thoughtful nearby multiplayer and a profile worth keeping. Use short visible labels and disclose detail when requested. “Good puzzles. Good company.” is the favored tagline candidate, suitable for share cards/store exploration; final artwork remains open. Preserve the current native palette and icon. Crossword follows Sudoku completion.

## Stages

| Stage | Deliverable | Acceptance |
| --- | --- | --- |
| 1 — v0.11B, verified QA | Launcher label **Sudoku**; useful Solo/Daily entry checking when an earlier mistake blocks logical hints | Full in-game/app name retained; same icon/package/certificate; explicit solution checks, accurate assisted results, safe Undo/resume, actual v0.11A upgrade test and native UI checks |
| 2 — Coach guidance | Clear proof highlights and progressive explanations for supported logical techniques, followed by an explicitly requested single-entry apply action if useful | Real proof from current board; no guesses or note-dependent mathematics; invalidate stale previews; track assistance without double-counting one deduction; no numeric score yet |
| 3 — Share result | Optional finish-screen button, preview, one polished on-device PNG format and native Android sharing for Solo/Daily | Persisted time/difficulty/assistance; no solution digits, invented score or store link; cancellation/export failure preserves the game; physical share-target acceptance |
| 4 — Player profile | Chosen avatar/style, earned badge showcase, concise honest statistics; profile-card export afterward | Preserve owned cosmetics/history and legacy records; accessible defaults, empty-state and long-name layouts; editable sharing privacy |
| 5 — Scoring | Versioned, calibrated score with a clear optional breakdown | Distinct deduction/tier accounting across Undo/resume; old scores remain unknown; assistance stays unlimited; assisted/unassisted comparisons stay separate; no arbitrary speed bonus |
| Release work alongside stages | Reconcile stacked source, target API36+, production signing/AAB, modern-device/accessibility QA, Play setup/testing and accurate store materials | Follow the launch guide and actual account eligibility. No publication or source promotion is implied. Basic two-phone co-op is owner-reported successful; the remaining physical lifecycle/complete-game/versus checklist is open |
| After a clean beta | Rare between-puzzle ads and optional one-time Remove Ads | The free model is decided; price/frequency remain open. Test consent, purchases/restoration and interruption-free play before live use |

The five new proposal files remain design references. An approved product direction is distinct from implemented, tested or released software. Global leaderboards, remote play, cloud backup, paid randomized cosmetics, Split Clues and framework extraction remain separate future decisions/gates.

## First APK scope

Stage 1 is the first implementation pass. It does not bundle the later stages into the launcher-label request. Entry checks never change a value automatically. The player can explicitly erase the identified entry and use normal Undo. A successful comparison against the original unique puzzle is counted separately from logical hints and marks the finish Assisted. Duplicate-rule feedback and an empty check do not add assistance. Existing records retain their meaning; optional new fields default to zero.

Each later stage gets its own concrete implementation plan and verified checkpoint. The current pass uses the existing native toolchain, min26/target35, schema1/protocol5 and v0.10D/v0.11A QA signing identity. API36 migration belongs to release work rather than this small launcher change.

Stage1 acceptance:103 JVM +46 API26 native cases; original17→18 continuity and installed short launcher/full application labels verified. [Current QA and remaining gates](V0.11B_COACH_LAUNCHER_QA.md). Stage2 remains the next larger product stage; the November4 recommendation is a contained multiplayer usability pass before it. No later-stage implementation is implied.
