# GridMates: Sudoku — Puzzle Coach and Post-Game Scoring Proposal

**Date:** 2026-11-03  
**Status:** DESIGN PROPOSAL / discussion, **not yet final owner-approved specification or implemented code**  
**Audience:** Work/implementation chat  
**Repository:** PlayerEden/SudokuTogether; Android source on `development`  
**References:** [Living handoff](PROJECT_HANDOFF.md), [v0.8D assistance audit](V0.8D_SOLO_ASSISTS_AUDIT.md), [GridMates brand](GRIDMATES_BRAND_AND_SERIES.md)

## 1. Owner's goal

The owner wants hints to be **genuinely useful even late in a puzzle** (e.g., two cells remaining, perhaps with an earlier wrong entry), useful to Sudoku newcomers, yet educational rather than mindlessly filling the board. **No per-puzzle hint limits**, paywalls or punishment that prevents finishing. At the same time, assistance should be **visible in a satisfying, transparent after-game score/stat record**, not ignored. Aim for *friendly challenge, optional depth, no shame*. Preserve the nostalgic pocket-puzzle-book feel and substantial modern features.

**Core principle:** Unlimited help; every completed puzzle still feels like an accomplishment. Compare like-with-like and reward unassisted play without erasing assisted progress.

## 2. Existing implementation — verified source read on 2026-11-03

Current `core/SudokuAssistance.kt`:
- `nextHint(puzzle, visible)` builds an assessment from the **current visible numbers** via `HumanSudoku.assess`; it avoids solution-board lookup and doesn't change a cell.
- Returns only the first *placement* step (naked/hidden single), with at most one referenced prior locked-candidate/naked-pair deduction in its message.
- Returns null if givens were changed, the current visible state is unsolvable/ambiguous, or no placement step was found. It does **not** currently diagnose which wrong player entry caused the problem.
- A selected cell + textual explanation is shown; no progressive help levels, highlighted proof trail or optional fill.

Current `HumanSudoku.kt`: naked single, hidden single, locked candidate and naked pair. Generated puzzle ratings use technique ceilings (Easy/Medium/Hard) without a guessing solver.

Current `PersonalPlay.kt`, `DailyChallenge.kt` and `SudokuScreen.kt`:
- `SoloProgress.hintsUsed` and `GameRecord.hintsUsed`, `assisted`, `usedNoteAssist` are already stored.
- A shown hint increments `hintsUsed` and marks the finish assisted; completed Solo/ Daily report hints, moves and time. Assisted/unassisted personal bests are separate; there is **no numerical performance score** in current data.
- All code paths must preserve old saved boards, record decoding and the current assisted/unassisted separation.
- Existing hints are **solo/daily only**. Co-op/versus need separate security/fairness design, particularly Split Clues. Do not silently enable them.

**Hypothesis to test:** If two empty cells remain and the filled digits are globally correct, there should be an easy logical deduction. If a previous entry makes the board unsolvable, the existing hint returns no suggestion: the user may experience "the late-game hint doesn't work." Other issues could involve UI presentation and missing full proof steps.

## 3. Proposed "GridMates Puzzle Coach" — assistance ladder

Build progressive guidance around **one reproducible, verified logical next step**, always from the player's actual state:

| Help tier | Example experience | Suggested *cumulative* assist cost per step (tentative) |
|---|---|---:|
| 0. No help | Solve normally | 0 |
| 1. Nudge | Highlight a row/column/box and say where to look; don't disclose the value | 10 |
| 2. Explain | Highlight the relevant digits and elimination/proof chain in a plain-language, optionally step-by-step breakdown | 25 |
| 3. Reveal | Highlight exact target and needed digit, with causal reasoning | 60 |
| 4. Apply | Only with explicit tap, enter one valid digit and show what was done; normal Undo support | 100 |

**Costs are not final.** They mean **scoring deductions**, not currency, coins, throttling or usage limits. Escalation on the **same deduction** should replace the previous maximum tier cost, not add up all tiers. Merely reopening a previously charged explanation should not count twice. Never charge for unsuccessful hints, system errors or tutorials viewed outside an active puzzle.

A more direct alternate design is two main buttons: **"Show me why"** and **"Fill this square"**, with progressive hints as an optional coaching layer. Select based on usability testing rather than gratuitous UI complexity.

### Good hint qualities

- The engine chooses the **easiest *valid* useful next step** at the current state, not simply any answer from an offline solved grid.
- Explain in terms of the actual proof cells and affected candidates. For elimination techniques, show **eliminations as first-class steps**, not only the last resulting placement.
- Let the user choose **"Hint for selected square"** when a locally valid path to that square exists, or **"Find a step"** to find an easier logical move elsewhere. If target isn't yet inferable, explain that a different step is needed.
- Clearly mark puzzle givens, player entries, candidate marks and proof cells with shapes/labels, not color alone.
- For a last-cell/last-two-cells situation, prefer simple contextual "Only one number fits" coaching without overwhelming the screen.
- Provide brief and expanded text; novice-friendly language first, formal technique name optional.
- After every changed entry, invalidate the prior preview; don't use stale suggestions. Sync with persistence, pause, Undo and app restart.
- A hint **never guesses** and cannot introduce a value inconsistent with the uniquely solved original puzzle.
- Do not let pencil notes incorrectly constrain the mathematics: hand-entered notes can be absent, incomplete or wrong.

### When a wrong earlier digit blocks hints: "Check my work"

This needs to be an **explicit, user-requested mode**, particularly if it compares player entries with the original puzzle solution. Do not silently expose wrong moves or act as if any unsolvable state is a hint-engine failure.

Suggested sequence:
1. Check obvious row/column/box duplicates and highlight the conflict **without solving the whole board**.
2. If no duplicates but the filled state contradicts the unique original solution, offer **"Check my work"**; if the user consents, highlight one suspect entry and why the current board can't be completed. Distinguish obvious duplicate/conflict vs comparison against the solution.
3. Optionally **"Reveal one incorrect entry"** and **"Correct this entry"** with clearly disclosed assistance tiers. Never erase/change user entries without a direct action.
4. Then return to normal logical coaching after a valid state is restored. An opt-in correctness check should not leak other unsolved cells; keep this solo/daily only.

**Important engineering nuance:** `Sudoku.solutionCount(visible)==0` can be true even with no obvious local duplicate; if every original clue admits one solution and the current entries violate it, a solver can detect conflict, but accurately explaining *why* a particular player number is wrong may need proof, or a transparent "This entry doesn't match the puzzle's solution" label rather than fabricated row/column logic.

## 4. Post-game results: meaningful but gentle

### Always lead with completion

Results should celebrate **"Puzzle solved!"** with visible time, difficulty, moves if useful, hint breakdown, and assisted/unassisted status. Never label assisted solvers failures or withhold a valid completion.

### Proposed scoring rubric (illustrative, NOT balanced or approved)

```
Score = max(FLOOR, DifficultyBase + PacingBonus - AssistancePenalty)
DifficultyBase: Easy 1000, Medium 1400, Hard 1800
PacingBonus:   0..200 based on calibrated per-difficulty expectations (later, not arbitrary targets)
AssistancePenalty: sum of distinct logical-step highest revealed tier
  Nudge 10 / Explain 25 / Reveal 60 / Apply 100
Optional check/correction assistance: charge at a clearly presented equivalent tier
Auto pencil cleanup: either disclose a fixed small assist adjustment or keep it
  in Assisted categorization and **exclude from numerical score until tested**
FLOOR: small positive completion value; e.g. 100
```

**Never charge the same step four times merely because the user tapped Nudge → Explain → Reveal → Apply.** Example: a Hard puzzle with a 120 pacing bonus, one explanation (-25), and one revealed digit (-60) = **1,835 points**, still a satisfying completion. Example is illustrative only.

**Scoring risks/guardrails:**
- Do not give a large speed bonus that lets a mass-revealed puzzle beat an equivalent clean result. Unassisted and assisted **leaderboards/personal bests stay separate**, and compare **within the same difficulty**, not across different difficulty bases.
- Time bonus targets need calibration, pause/background handling and comparable puzzle samples; ship score **without speed bonus** if targets aren't defensible yet, showing completion time separately.
- Avoid raw move-count penalty: pencil notes, accessibility needs, Undo and different solving styles make move totals noisy and easy to game. Use moves as a stat, not automatic punishment.
- A mistake is not an automatic deduction just because a player erased/changed a tentative number. Explicit solution checks or correction services are assistance, charged if disclosed; do not punish learning or experimentation.
- Cap deductions; score cannot go negative. Unlimited hints remain available after reaching the score floor.
- Optional automatic notes cleanup already marks Assisted; decide with the owner whether it receives a small flat scoring adjustment and explicitly label it in results.
- No global ranking or online legitimacy claim before backed by actual server/Play integration and anti-cheat/fairness policy.
- Keep existing historical records intact. New score fields must default to unknown/null for legacy records; **do not retroactively fabricate scores** when data about tier escalation doesn't exist.

### What a result card might show

**Puzzle solved!** — Hard · 12:43

- **Score:** 1,835 (illustrative) — Difficulty 1,800 + Pacing 120 - Hints 85
- **Hints:** 2 guided steps (1 explanation, 1 reveal); **Assisted**.
- **Personal record:** best assisted score for Hard, if applicable.
- **"What I learned":** Naked Pair / Hidden Single (optional actionable tip from actual used hints).
- **"Play another"**, **"Review board"**.

Show scoring details on tap to keep the surface tidy. Make scoring entirely optional to care about: people who only want a nice Sudoku should not need to manage ratings.

## 5. Data model and QA expectations

**Design suggestion, not authorization:** Add a versioned `HintUsage` or `AssistLedger` for a session with per-step ID, max tier, technique, timestamp, and any explicit correction applied. Derive the number/penalty from immutable saved history, not multiple volatile UI taps. Save atomically before displaying counted information, with no double counting on interrupted sessions.

- Persist score, score-version, breakdown, and assist ledger in backward-compatible fields. Legacy records should have `score=null`, not invented zero.
- Keep existing `hintsUsed` for legacy compatibility but define whether it means *distinct logical hints* or *actions*, and avoid changing its meaning silently mid-history.
- Test 1 and 2 cells remaining: both correct late-game and earlier undetected incorrect entry; mistake with duplicate; no duplicate but unsolvable.
- Test a complex elimination-only interim move and exact highlights of implicated candidates; explanation should be human-readable and logically justified by real board state.
- Test repeated reveal requests, escalation, returning to the same hint, undoing a hinted cell, leaving/resuming mid-hint and process death; no exploitable resets/double charges.
- Test Easy/Medium/Hard puzzle generation and the hint engine together: all currently issued puzzles should have valid coach paths, or a clearly defined safe fallback.
- Test old saves/records, daily records, assisted/unassisted highs, score idempotency, profile stats and cross-version migrations.
- Test narrow screens, 200% font, landscape, TalkBack, color-blind distinctions, optional animations, haptics.
- No extra hints in versus or clue-private co-op unless designed and reviewed for host authority, fairness and information leakage.

## 6. Staged development idea

1. **A. Diagnose existing late-game failures** with reproducible boards and tests; first fix "no hint" when the true issue is a mistaken entry. Preserve existing behavior if uncertain.
2. **B. Coaching proof highlights and progressive help** for current techniques (naked/hidden singles, locked candidates, naked pairs); evaluate how many buttons/stages actually improve mobile UX.
3. **C. Wrong-entry review** (explicit opt-in), plus simple per-step assistance tracking and save migration.
4. **D. Score engine & finish-screen UI**: pure deterministic tested function, score-versioned records, clear breakdown and profile separation.
5. **E. Playtest balance** with novices and experienced solvers; calibrate penalty weights/bonus pacing, polish explanation clarity, add tutorials as needed.

**Coordination note:** The Work chat may currently be implementing brand/profile designs. This doc is **a new design proposal** and must **not interrupt or replace in-progress work without reprioritization by the owner**. Keep existing stable releases and rollback references; no code or APK was changed by this planning document.
