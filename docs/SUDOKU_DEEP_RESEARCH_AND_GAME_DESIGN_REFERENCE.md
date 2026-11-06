# Sudoku — Deep Research, Solving Techniques & GridMates Development Reference

**Research date:** November 6, 2026
**Audience:** GridMates: Sudoku game design, human-difficulty calibration, generator/solver engineering, Puzzle Coach, QA, onboarding and accessible player education
**Status:** Source-backed reference and recommendations. **Not a gameplay implementation, not a new release commitment and not a player-tested calibration study.**
**Game:** GridMates: Sudoku, repository PlayerEden/SudokuTogether.
**Code inspected:** latest available Android source branch **feat/gridmates-nearby-animals** / draft PR #28, revision 975e31a1e8cbfebad1fbbc7602b3340c357da7ef, with the v0.11C known-green checkpoint. The development branch holds earlier game source plus design notes. Do not overwrite or restart from it alone.
**Companions:** [Brand and vision](GRIDMATES_BRAND_AND_SERIES.md), [Puzzle Coach and scoring](GRIDMATES_PUZZLE_COACH_AND_SCORING_PROPOSAL.md), [Release handoff](PROJECT_HANDOFF.md), [v0.8B calibration](V0.8B_HUMAN_DIFFICULTY_CALIBRATION.md).

---

## Research brief: five findings worth using immediately

1. **Sudoku difficulty is not clue count.** A low-clue board is not necessarily hard. Empirical work by Pelánek emphasizes both individual deduction difficulty **and the dependency structure connecting deductions**. A fixed human-solver technique ladder is a useful start, but it is not the same as human calibration. [R1][R2][R3]
2. **A computer solving a grid and teaching a person to solve it are different jobs.** An exact constraint solver validates unique solutions. A human-oriented solver should produce a transparent series of lawful placements and candidate eliminations. [R4][R5]
3. **Candidate eliminations matter.** Singles make placements; locked candidates, naked pairs, hidden pairs, fish and chains often remove *possibilities* first. A strong hint engine must show why a candidate is excluded rather than jumping mysteriously to a correct number. [R5][R7–R11]
4. **One bad entry can make normal hints appear to fail.** A duplicate or logically incorrect player entry must be distinguished from a merely difficult puzzle. “Check my work” should be explicit and respectful, and solution comparison must be disclosed as assistance.
5. **Every player should be able to finish.** Unlimited educational hints are compatible with challenge if the game tracks assistance transparently, protects fair records and keeps daily participation fun. This is GridMates product design, not a theorem from the literature.

### Evidence labels used below

- **Established fact:** basic rules, cited mathematics, published studies and verified current source.
- **Community technique convention:** recognized Sudoku strategy descriptions; classification as Easy/Medium/Hard varies by solver.
- **Proposed for GridMates:** a design or algorithm to implement/test, **not** something already shipping.
- **Open question:** requires real player testing or additional technical research.

---

## 1. What Sudoku is

Classic Sudoku is a **constraint-based number-placement puzzle**. Fill an **81-cell, 9×9** board with digits **1–9** so that:

- Each of the **9 rows** contains every digit exactly once.
- Each of the **9 columns** contains every digit exactly once.
- Each of the **9 non-overlapping 3×3 boxes** contains every digit exactly once.
- Original **givens/clues** are fixed; players fill only the initially empty cells.

There are **27 houses/units** (9 rows + 9 columns + 9 boxes). Each cell has **20 distinct peers**: eight in its row, eight in its column and four additional peers in its box not already counted. A legal completed 9×9 Sudoku has each digit exactly nine times.

**No arithmetic is necessary.** Symbols 1–9 are labels, not numbers to add or multiply. A completed row happens to sum to 45 because 1+…+9=45, but **“sum to 45” is not sufficient to validate a Sudoku row**; repeated digits could have the same sum. The actual rule is uniqueness in each unit. [R4][R12]

**A well-formed classic puzzle is normally designed with exactly one completion.** A partially filled grid can have zero, one or multiple solutions; it is not a fair standard puzzle merely because the givens do not immediately conflict. The app should validate uniqueness, not just absence of duplicates.

### Terminology glossary

| Term | Meaning | Beginner-friendly wording |
|---|---|---|
| Grid/board | Entire 9×9 puzzle | Puzzle board |
| Cell/square | One position | Square |
| Given/clue | Starting digit; should not change | Starting number |
| House/unit | Row, column or 3×3 box | Row, column or box |
| Peer | Cell sharing a row, column or box | Related square |
| Candidate | Digit that is still *possible* | Pencil mark/possible number |
| Placement | Proven digit written into a cell | Fill a number |
| Elimination | Proven rejection of a candidate | Cross out a possibility |
| Naked | Candidate pattern visible from the listed values of certain cells | Visible option pattern |
| Hidden | Digit(s) restricted to particular cells inside a unit | Only spots for a number |
| Contradiction | Partial state cannot lead to a valid completion | Something doesn't fit |
| Unique solution | Exactly one full valid grid matches the clues | Only one answer board |
| Human logical trace | Sequence of explainable deductions | How you solve it |
| Exact solver | Program that finds/counts solutions, may search | Puzzle validator |

**Coordinate conventions:** Use **R1C1** for row 1, column 1 through **R9C9**; many sites write r1c1. Consistently label the nine boxes from left to right, top to bottom. Avoid showing internal “unit 17” indices to a player.

### First-time learning example

If a row contains 1, 2, 3, 4, 5, 6, 7 and 8, its remaining cell **must be 9**. This is often called **Full House** or **Last Digit**. It is the clearest first coaching demonstration. [R7]

---

## 2. History and mathematical facts

### Brief history

- The structural ancestor is a **Latin square**: each symbol appears once per row and column; Sudoku adds the 3×3 box constraint. Do **not** confuse it with a magic square, which is defined by sums.
- The modern 9×9 puzzle appeared in Dell puzzle publications as **Number Place** in **1979**, attributed to **Howard Garns**.
- The Japanese publisher **Nikoli** popularized it in Japan in the **1980s**, under the Sudoku name.
- Sudoku reached wider international newspaper popularity around **2004–2005**. [R12][R13]

Dates and the precise word-origin nuance vary across popular retellings; these milestones are the dependable high-level story.

### Surprising numbers — correctly interpreted

- **6,670,903,752,021,072,936,960**: total distinct **completed valid 9×9 solution grids** when all digit labels and rearrangements are counted. This is about **6.67 sextillion** (short scale). [R14]
- **5,472,730,538**: completed solution grids essentially different when common Sudoku symmetries/relabelings are treated as equivalent. [R14]
- **17 clues**: minimum number of initially filled cells that **can** produce a standard 9×9 puzzle with a unique solution. The exhaustive no-16-clue result was reported in 2012 and published in 2014 by McGuire, Tugemann and Civario. [R15]
- **17 does NOT mean “hard”**. A sparse valid puzzle may be approachable, while a puzzle with more clues might need subtler deductions.
- **“Number of completed solution grids” is NOT “number of playable clue patterns”**. A single completed board can underpin numerous valid puzzles.

### Symmetry: aesthetics versus logic

Rotational symmetry of the **given positions** can make a printed puzzle look deliberate and attractive. It does not, by itself, guarantee uniqueness, ease, fairness or interesting deductions. Puzzle generation can vary the clue pattern while independently validating exact solution count and human solve route.

### What Sudoku does not prove

Sudoku provides logical practice and an enjoyable mental activity. Avoid marketing claims that playing this specific game **prevents dementia, raises IQ or guarantees cognitive improvement** without dedicated supporting clinical evidence. Enjoyment and accessible thinking are sufficient reasons to play.

---

## 3. The logic underneath every strategy

Think of each empty cell as holding a set of currently possible digits.

**Base candidate rule:** a digit is eligible for a cell if it does **not** already appear in that cell's row, column or box. If a cell has no candidates, the current board is contradictory or an earlier logical elimination is wrong.

A cell's candidate list can be reduced beyond this base rule by **inferred eliminations** from strategies such as locked candidates and naked pairs. In a teaching engine, both *why* the elimination was legal and *which* squares it affects should be retained.

### Two foundational modes of reasoning

1. **Cell-first:** “Which numbers can go here?” If exactly one remains, that is a **Naked Single**.
2. **Digit-first in a unit:** “Where can this 7 go in row 4?” If one square in that row remains possible, that is a **Hidden Single**.

These can lead to the same placement, but the educational explanations differ. [R4][R7]

### Candidates and pencil marks are not identical

- **Legal candidates** are computed from puzzle state and logically proved eliminations.
- **Player notes** are a personal, potentially incomplete or wrong representation.
- Never consider a number impossible *just because the player forgot to pencil it in*.
- If “Auto Notes” computes legal candidates, explain what it does and make it optional.
- If “Auto Clean Notes” erases candidates made impossible by a new placed number, Undo should restore them correctly.

This distinction is critical for a trustworthy hint engine.

### Placement vs elimination

A direct placement can tell a player “R6C4 = 7.” An elimination might only say “R6C4 cannot be 7,” which is still meaningful progress. **An engine that only presents the final number misses the actual learning moment for many intermediate and advanced strategies.**

---

## 4. Solving techniques: practical reference ladder

**Difficulty assignments below are advisory, not universal Sudoku standards.** A given pattern can be visually difficult to spot even when the underlying deduction is logically elementary. HoDoKu explicitly makes ordering and level settings configurable. [R5][R6]

### 4A. Fundamentals — first lesson to learn

| Technique | Core rule | When to look | Suggested GridMates explanation |
|---|---|---|---|
| Full House / Last Digit | One empty cell in a row/column/box has the only missing digit | A house nearly full | “Only 6 is missing from this row.” |
| Naked Single | One cell has exactly one logically possible candidate | A cell heavily constrained by its peers | “Every other number is blocked by this row, column or box.” |
| Hidden Single | In a row/column/box, a digit has only one candidate cell | Scan *one digit* through a house | “In this box, only this square can hold 4.” |
| Cross-hatching / scanning | Visually eliminate candidate places based on fixed numbers in intersecting houses | Start with repeated/highly placed digits | “The 8s in these rows rule out most of this box.” |

**Example — Naked Single:** a target empty square has possible values **{4}** once its row, column and box are considered. The square must contain 4.

**Example — Hidden Single:** an empty square has candidates **{2, 4, 7}**, but every *other* square in its row excludes 7. The 7 must be placed there even though the square still has other apparent options.

**Beginner lesson order:** learn Full House, scanning and Hidden Single before insisting on filling in every pencil mark. Keep the board readable.

### 4B. Intersections / locked candidates — next step

| Strategy | What it proves | Effect |
|---|---|---|
| **Pointing pair/triple** (locked candidates type 1) | All places for digit D inside one 3×3 box lie in the same row/column | Remove D from that row/column **outside the box** |
| **Claiming / box-line reduction** (type 2) | All places for D in a row/column lie inside one 3×3 box | Remove D from the **other** cells of that box |

**Example — pointing:** the only potential 5s in box 1 are R3C1 and R3C2. Since one must be 5, no other square of **row 3 outside box 1** can be 5. This doesn't tell us whether R3C1 or R3C2 is the 5.

**Coach must highlight:** the source candidates inside the box, the intersecting line, and the crossed-out candidates. Merely saying “locked candidate” is not enough for someone learning it. [R8]

### 4C. Subsets — pairs, triples and beyond

| Technique | Pattern | Consequence |
|---|---|---|
| **Naked Pair** | Two cells in the same house each have exactly the same two candidates {A,B} | Other cells in that house cannot contain A or B |
| **Hidden Pair** | Two particular digits have no possible cells outside the same two cells of one house | Remove other candidates from the pair cells |
| **Naked Triple** | Three cells of one house collectively have exactly three possible digits | Remove those digits from other cells in the house |
| **Hidden Triple** | Three digits are confined to three cells of one house | Remove unrelated candidates from those three cells |
| **Naked/Hidden Quadruple** | Four-cell/four-digit generalizations | More complex analogous eliminations |

**Example — Naked Pair:** in row 4, R4C2 = **{2,7}** and R4C7 = **{2,7}**. Those digits must occupy those two cells in some order. Any other empty cell in row 4 can have candidate 2 and 7 crossed out.

**Example — Hidden Pair:** suppose digits 3 and 8 can occur only in two squares of a box, even though those squares currently also list {1,5} as candidates. The two squares must hold 3 and 8, so remove their other candidates.

**Subtle but important:** a Naked Triple does **not** require each of the three cells to have all three candidates; their **union** is the three-digit set. A common implementation mistake is only matching identical candidate masks.

**Coach design:** explain *which cells reserve which digits*, then offer the elimination; do not immediately fill a later cell without showing the pair/triple deduction. [R9][R10]

### 4D. Advanced pattern-based eliminations

| Technique | Basic concept | Typical role |
|---|---|---|
| **X-Wing** | Two rows restrict a digit to the same two columns (or reverse) | Exclude that digit from other cells of those columns |
| **Swordfish** | Three base rows and three cover columns (or vice versa) constrain a digit | Broader fish elimination |
| **Jellyfish** | Four-by-four fish generalization | Specialized advanced technique |
| **XY-Wing** | Three cells with pairs {X,Y}, {X,Z}, {Y,Z} arranged as pivot and wings | Exclude Z from cells seeing both wings |
| **XYZ-Wing** | Pivot {X,Y,Z} with two supporting bivalue cells | Exclude shared Z from cells seeing pivot and both wings |
| **Simple Coloring** | Color a chain of linked candidates for one digit and derive a contradiction | Rule out candidates via link logic |
| **Skyscraper / 2-String Kite** | Single-digit strong-link arrangements | Other ways to eliminate candidates |
| **Alternating inference chains** | Link candidate truths/falsities to establish a deduction | Expert solving and explanation |
| **Unique Rectangle / BUG+1** | Assumes the published puzzle has exactly one solution | Specialized conditional inference; teach assumption explicitly |

**X-Wing conceptual pattern:** Suppose in rows 2 and 8, digit 4 appears as a candidate **only in columns 3 and 7**. The pair of rows must place a 4 in each of those columns in some order, so other cells of columns 3 and 7 cannot contain 4. This is not “four 4s”; it's a structured restriction of possible placements. [R11]

**XY-Wing conceptual pattern:** a pivot {2,5} sees a wing {2,9} and a second wing {5,9}. One wing must be 9 whichever value the pivot takes. A square seeing **both wings** cannot be 9. [R16]

**Unique rectangle warning:** uniqueness-based techniques rely on the puzzle's **promised unique solution**, rather than only the three placement rules. They are legitimate in a deliberately unique puzzle, but not every player wants to use uniqueness assumptions. Mark as advanced and optional, not “Sudoku basics.” [R17]

**Recommendation:** GridMates does **not** need all of these techniques immediately. This is a future reference for richer Hard/Expert puzzles, training tools and solver coverage. Better to teach the existing four accurately before expanding the technique catalog.

---

## 5. What actually makes a Sudoku Easy, Medium or Hard?

### Clue count: a trap

A 24-clue puzzle isn't necessarily harder than a 30-clue puzzle, and two puzzles with identical clue counts can feel radically different. Human problem-solving research explicitly warns against equating number of blanks with difficulty. [R1][R2]

### Four meaningful difficulty dimensions

**A. Hardest *used or required* deduction.** Singles-only versus needing locked candidates, pairs, fish or a chain. A useful first approximation, but the chosen solver's path is not necessarily the only human solution path.

**B. Number of deductions.** Many easy steps can be exhausting, while one recognizable advanced step could unlock a simple finish.

**C. Dependency structure and bottlenecks.** Does the player have several available useful choices, or must they identify one hard-to-notice move to progress? Pelánek's findings show the structure of dependencies matters along with step complexity. [R1]

**D. Visual/cognitive burden.** How easy is it for humans to notice the pattern? Many candidate marks, long-distance comparisons, similar digits, small screens and necessary note cleanup affect real experience. This dimension needs GridMates player testing rather than assumptions.

### Formal difficulty vs perceived difficulty

The puzzle can be **logically** solvable using only basic moves yet feel difficult to a novice because spotting the move is hard. Conversely, an experienced player can recognize a Naked Pair instantly. An automated difficulty label should be understood as **technique-based and calibrated**, not a universal judgment of the player's intelligence.

### Working difficulty taxonomy for GridMates — **proposal, not shipped rating rules**

| Tier | Intended player experience | Candidate permitted tools for a future expanded solver |
|---|---|---|
| **Gentle / Beginner** (optional future) | Very visible placements, lots of choices, teaching prompts | Full House, obvious Naked/Hidden Singles |
| **Easy** | Smooth scanning and basic deduction | Singles, with generous logical opportunities |
| **Medium** | Some candidate management and cross-house reasoning | Singles + pointing/claiming + selected easy pairs |
| **Hard** | Sustained candidate work, denser dependencies | Intersections + multiple subset families, possibly X-Wing |
| **Expert** (future only) | Players deliberately seek advanced patterns | Fish, wings, coloring, chains and other verified methods |

**Do not silently change the meaning of existing “Hard.”** GridMates currently maps Hard to the presence of a Naked Pair on its deterministic trace. If the tier system is expanded, do an explicit **rating-version migration** and preserve the labels stored on old records.

### Composite rubric to investigate

Separate two outputs:

1. **Technique band** = highest class in the deterministic supported explanation trace (already present).
2. **Within-band challenge score** = an experimental *puzzle rating* constructed from:
   - weighted frequency of deductions by technique;
   - number of elimination-only steps before a placement;
   - length of longest reasoning dependency sequence;
   - number of states with only one useful next move (bottlenecks);
   - candidate density / likely visual search work;
   - after actual tests, median completion time and hint/stuck rates within each tier.

Example **illustrative mathematical structure**:

~~~text
DifficultyIndex =
  base(weight_of_hardest_step)
+ sum(weight_of_each_step × frequency)
+ bottleneck_penalty
+ dependency_depth_adjustment
+ visual_search_adjustment
~~~

**These weights do not exist yet.** They must be fitted and tested, not treated as a production formula.

**Crucial caveat:** “hardest technique in one chosen solver path” is **not proof that every possible valid solving path needs that technique**. Avoid claims such as “this puzzle requires Naked Pair” unless that has been independently established under a stated solving model.

### Real-world calibration suggestion

Have novice, casual and experienced testers solve a **stratified set of puzzles** at each purported difficulty. An ethically minimal, opt-in QA method can record:
- starting puzzle identifier and versioned rated technique trace;
- completed / abandoned;
- active time (paused/background excluded);
- number and depth of hints; solution checks;
- explicit subjective rating, e.g. “Too easy / About right / Too hard”;
- whether a player got stuck with few cells left;
- where players repeatedly needed explanations.

A few friends' observations are excellent *diagnostics*, but don't claim statistically representative calibration until sample sizes grow. Existing offline-first privacy promises still apply; no background telemetry should be added without informed consent and disclosure.

---

## 6. Our current implementation: exact strengths and boundaries

### Verified from v0.11C source

**Game core — Sudoku.kt**
- Generates a valid completed grid from a patterned base plus randomized digit, row and column permutations, then removes clues one at a time while preserving exactly one solution.
- Validates puzzle states by counting solutions with backtracking/constraint pruning. A limit of 2 is enough to differentiate 0, 1 and at least 2 completions.
- Detects duplicated digits in rows/columns/boxes and validates completed boards.
- **A useful research observation:** the generator's initial complete boards come from **permututations of one patterned construction**, not a fully general solved-board sampler. This is a subset of all completed Sudoku layouts. It doesn't prove players are receiving duplicates, but future variety research could examine **structural familiarity** and independent full-board generation.

**Rated supply — PuzzleSupply.kt**
- Avoids exact repeats from the most recent **64 puzzle IDs**.
- Attempts up to **200** candidates for a requested grade and refuses to downgrade when none match.
- Uses starting clue removal floors as **generation controls** (Easy 36, Medium 29, Hard 24), **not as the final difficulty grade**.
- Requires the human solver's exact matching grade after unique-solution validation.

**Human logic — HumanSudoku.kt**
- Enumerates four techniques: **Naked Single, Hidden Single, Locked Candidate (pointing and claiming), Naked Pair**.
- Applies one deterministic logical deduction at a time; records placement or candidate-elimination steps, then restarts scanning.
- Grading: **Easy** if solved through singles; **Medium** if locked candidates appear but no Naked Pair; **Hard** if a Naked Pair is used; **Beyond Current Logic** for a uniquely solvable board it cannot finish with supported techniques; **Invalid** for non-unique/contradictory givens.
- **Beyond Current Logic is not the same as an unsolvable board.** This distinction should appear in QA logs and internal diagnostics.

**Current hint system — SudokuAssistance.kt**
- Recomputes a human-readable trace from the player's **visible grid**, not from a guessed candidate list in user notes.
- Returns the first placement step, with at most one preceding elimination step summarized in explanatory text.
- Will not manufacture a step when wrong entries break the puzzle's consistency.
- Provides a separately requested **Check my work** action that identifies obvious duplicate conflicts or compares a suspected entry against the original unique solution.
- The UI explicitly labels solution comparison as assistance; explanation is currently limited compared with the proposed progressive Puzzle Coach.

**Current Android UI — PuzzleCoachDialog.kt**
- Distinguishes an ordinary logical hint, conflicting repeat, incorrect entry, correct-so-far and no-entry state.
- Does **not** yet contain a fully interactive diagram of every proof cell, an advanced “hint only for selected cell” logical derivation, or a complete tiered coaching ladder.

### Small historical generator data — not population calibration

The existing v0.8B audit sampled **48 deterministic seeds** per clue-removal floor and found that the same low clue count can lead to several solver grades. It also recorded that some generated unique puzzles fall **Beyond Current Logic**. That is a *small development benchmark*, not empirical proof that the resulting Hard tier feels challenging to casual users. [Internal: v0.8B audit]

### Main gaps worth researching / prototyping

1. **Human experience within a grade**: Easy can still vary a great deal by number and positioning of singles. Track complexity of *the route*, not just the hardest step.
2. **Hint proof data**: current step has target cells/digits and a reason string, but does not always preserve all supporting witnesses, affected candidate lists and proof dependency chain. Structured steps would support visual teaching.
3. **Hard/Expert variety**: current technique set excludes Hidden Pair/Triple, Naked Triple, X-Wing and beyond. Expanding it affects rating semantics, generator yield and QA.
4. **Generator source diversity**: randomizing a patterned solved grid is efficient but limited to a family of layouts; measure structural repetitions before modifying.
5. **Check my work wording**: contradiction due to duplicate versus violation of the puzzle's actual unique completion are different. Never fabricate row/column “proofs” where only full solution comparison is available.
6. **Difficult late-game hints**: if one or two empties remain but a previous player entry is wrong, the ordinary logical hint can be unavailable. Offer explicit correction review and then resume normal coaching.
7. **Mobile readability**: strategy diagrams, candidate highlights, 200% text and legible numerals matter just as much as correctness.

---

## 7. Generator and validator: recommended engineering standard

### Core acceptance pipeline

~~~text
Generate candidate solved grid (or seeded canonical template)
→ Remove proposed givens
→ Count full solutions (stop after 2)
   0: invalid / reject
   1: uniquely solvable
   2+: ambiguous / reject
→ Run deterministic human deduction model
   Solved with supported techniques → candidate grade
   Cannot finish with supported techniques → Beyond Current Logic
→ Validate exact requested grade, no silent downgrade
→ Reject recent exact puzzle IDs; optionally check structural repetition
→ Save immutable puzzle ID, clues, rating version and valid metadata
~~~

**Two independent engines help:** exact search for correctness/uniqueness; human-step engine for *teachability and difficulty*. A fast exact solver doesn't automatically provide understandable hints. [R4][R5]

### Useful puzzle properties to log during offline development

- Seed, ID, versioned generator, given count and clue-position hash.
- Solution count (0/1/at least 2); conflict status.
- Human-solver grade, highest technique, placements, elimination steps.
- Step counts by technique, total logical steps, longest distance between placements.
- Bottlenecks, number of currently available alternative legal deduction steps (if available).
- Whether **each supported hint strategy** can produce an honest next step in all generated puzzles.
- Generation and grading latency on supported hardware: median/P95/P99, search attempts and failure rates.
- Distribution of rotated/reflected/isomorphic patterns and near-similar structures.

Keep QA data private/local and versioned. Do not leak hidden full solutions into co-op guest state or public Daily shares.

### Technical QA threats

- **Nonunique puzzles** masquerading as valid because the check stops after the first solution.
- **False deductions** from candidate masks polluted by a prior elimination error.
- **Incorrect player notes** trusted as solver truth.
- **Out-of-range digits**, corrupt/legacy save states, changed givens, bad Undo state.
- **Duplicate puzzle supply**, generator exhaustion or UI freezing during expensive generation.
- **Difficulty drifts** after changing technique ordering or weights.
- **Hint proof stale** after Undo or a subsequent move.
- **Host/guest desynchronization** on puzzles/rating across LAN versions.
- **Right answer, wrong explanation**: an engine might accidentally reveal a numerically correct digit for an invalid reason. Test explanations, not just finished boards.

### Test corpus recommendation

Preserve a set of small, source-provenanced and license-safe fixtures:
- exact one-solution board; invalid given; ambiguous board;
- each technique's positive pattern and **near miss** (proof must not trigger);
- elimination that produces a placement several steps later;
- puzzle with 1–2 blanks left, all entries correct;
- puzzle with 1–2 blanks left but earlier hidden mistake;
- duplicate mistake and a non-duplicate wrong entry;
- very full/sparse puzzles, varied clue patterns;
- intentionally unsupported but unique expert puzzle: classification should be Beyond Current Logic;
- historical stored ratings after algorithm updates;
- inputs from app-generated grids, not just hand-crafted sample arrays.

Aim for property-based or repeated randomized tests for invariants, plus separately curated known technique examples. Do not reuse a copyrighted third-party full puzzle dataset unless redistribution rights are clear.

---

## 8. Puzzle Coach: how a genuinely helpful hint should work

**Goal:** “Help me learn the next deduction,” not just “tell me the secret number.”

### Suggested assistance ladder (proposed, not shipped)

1. **Nudge:** identify a row/column/box and the kind of pattern to look for, without the answer.
2. **Show evidence:** highlight the relevant cells, candidates and why other positions are ruled out.
3. **Show the conclusion:** either reveal the justified placement **or** show candidate elimination; distinguish these actions.
4. **Apply step (optional):** with explicit consent, enter the digit or update the appropriate managed notes, with Undo support.

Unlimited access. If the player asks again for the same proof, don't repeatedly penalize them. The *degree* of assistance can be summarized honestly in results, without making people feel bad for seeking help.

### Essential late-game logic

- **Correct last one/two cells:** prefer plain direct Full House/Naked/Hidden Single explanation with highlighted row/column/box. Don't launch a sophisticated chain explanation for the last cell.
- **Duplicate:** “This 5 appears twice in row 8” with both cells highlighted.
- **No duplicate but wrong entry:** disclose that **Check my work** compares entries against the puzzle's unique solution and identifies a suspect entry. A phrase like “This doesn't match the solution” is honest; an invented peer-based explanation isn't.
- **No placement currently available:** if a verified elimination step exists, show the actual elimination and why; do not claim the hint engine is useless just because it cannot directly place a digit.
- **Solver cannot explain a unique board:** admit the limit of the current technique model; optionally offer a clearly labeled direct-reveal fallback only with an explicit player request.

### Structured proof model worth considering

~~~text
LogicStep {
  technique_id
  board_revision
  target_cell / target_cells
  candidate_digit(s)
  witness_cells / witness_units
  eliminated_candidates[(cell,digit)]
  placed_value?(cell,digit)
  prerequisites / proof_steps
  plain_language_explanation
  technical_name
}
~~~

Separate the **reasoning proof** from the **UI prose** and render both from the same validated state. A step might eliminate four candidates but not fill any square. The app shouldn't forge a placement just to satisfy a rigid hint-dialog format.

### Beginner-friendly words before jargon

- First: “The 7 can only fit in one square of this box.”
- Optional detail: “This is called a Hidden Single.”
- After the player understands: “Want to try another Hidden Single?”
- For pairs: “These two squares must be 2 and 8 in some order, so no other square in this row can be 2 or 8.”

Keep a glossary behind a lightweight **Learn why** or **Sudoku Guide** action. Avoid overwhelming first-time players with a dozen technique names.

### Visual instruction design

- Use shapes, borders, icons or pattern overlays **plus** color; don't make red/green the only information.
- Highlight relevant **givens**, witness cells, candidate eliminations and final target differently.
- Keep digit contrast and tap targets accessible at various font sizes.
- The same puzzle step should be explainable in ordinary text for TalkBack.
- Always give an unobstructed **Back to puzzle** / Undo route.
- Never reveal an opponent's private board or Split Clues ownership through assistance.

---

## 9. A candidate in-game “Sudoku Guide” from this study

This research document is **not** appropriate to dump wholesale into a game menu. It is long, technical and partly intended for developers. A distilled guide could be short, modular, and searchable.

### Suggested lightweight sections

**Getting started:** What is Sudoku? Rows, columns, boxes, givens, pencil marks. A two-minute interactive tutorial that solves one easy move. No timer or scoring pressure.

**Basic tips:** Scan by number, look for missing digits, focus on near-complete houses, use notes when stuck, don't guess unless deliberately experimenting, return to a different part of the board if one area is blocked.

**Techniques:** Full House → Naked Single → Hidden Single → Locked Candidates → Naked Pair. Each lesson shows (a) what to notice, (b) why it works, (c) a visual candidate example, (d) a practice move.

**Getting unstuck:** Check obvious conflicts, request a logical hint, optionally run Check my work, and explain the difference. Unlimited hints available.

**Beyond basics:** Later lessons for Hidden Pair, triples, X-Wing and XY-Wing **only when the explanation engine and fixtures genuinely support them**.

**About difficulty:** Short honest explanation: “Difficulty is based mainly on the logic needed, not the number of clues.” Avoid percentages or pseudo-scientific claims.

### Starter lessons — proposal

| Lesson | Example interaction | Player takeaway |
|---|---|---|
| 1. Each number once | Tap a row and find the missing 9 | Rules make possibilities disappear |
| 2. Scan a box | Highlight digits blocking 6 | See row/column/box intersections |
| 3. Pencil marks | List candidates of a chosen empty cell | Notes are possibilities, not guesses |
| 4. Hidden Single | Show the only place for 4 in a box | Digit-first reasoning |
| 5. Pair discovery | Select two {2,7} cells in a row | Some steps eliminate, not fill |
| 6. Getting unstuck | Present a deliberately wrong cell with no duplicate | Solution-check is explicit assistance |
| 7. Reading results | Assisted vs unassisted record, time and hints | Help remains welcome and transparent |

**Recommendation:** Give guide entry points in a Help/Guide area, on the Puzzle Coach “Why?” action, and maybe after an authentic first encounter with a technique. Do not force a full training course before someone's first game.

### A player tip library worth seeding

- Look for a row, column or box with many numbers already filled.
- Scan a single digit across nearby rows and boxes.
- If nothing is obvious, switch from cell-first to digit-first thinking.
- A pencil mark says “possible,” not “correct.”
- A Naked Pair tells you what *other* cells cannot contain.
- A hint should teach *why*, not just *what*.
- A mistake is a chance to review—don't keep randomly entering numbers.
- Hard puzzles sometimes depend on a candidate elimination before another number can be placed.
- Sudoku does not require math calculations.
- Completion with hints is still a completion.

### More advanced FAQ candidates

- “Why does an empty square still have several candidates?”
- “What is the difference between Hidden Single and Naked Single?”
- “Why did the coach remove a candidate instead of entering a number?”
- “Why does Check my work mark a puzzle Assisted?”
- “Why can two puzzles with the same number of blanks be different difficulties?”
- “What happens when the coach doesn't know a logical step?”
- “Are my pencil notes automatically correct?”
- “Do I have to play fast?” **No.** Time can be an optional stat.

---

## 10. Scoring, records and hints: separate three kinds of progress

### Puzzle difficulty rating ≠ player performance score

A puzzle's **grade** says something about its expected deduction complexity. A player's **result score** says something about how they completed it. Keep the two models independent.

**Existing records:** completion time, moves, difficulty label, hint/check usage, Assisted vs Unassisted in available Solo/Daily pathways. A full numerical score/puzzle-coach tier ledger is a separate proposal.

**Recommended design values:**
- Celebrate every valid completion regardless of hints.
- Track **assistance depth and count** transparently, but do not charge players for tutorial reading, failed hints or app errors.
- Keep Assisted and Unassisted records/bests separate; compare within a consistent puzzle/difficulty and rating version.
- Avoid severe time/move penalties. Some players enjoy calm reasoning, accessibility accommodations or extensive notes.
- Make any achievement that demands Unassisted explicitly say so; don't revoke past legitimate unlocks after a rating algorithm revision.
- Keep Daily cosmetics/rewards accessible to beginners even if they use hints, unless the owner separately approves a reasonable skill-specific challenge.
- Build rewards from validated durable completion records, not optimistic UI events, to avoid double grants.
- If a public leaderboard is ever pursued, recognize that current offline device data and unauthenticated scoring do **not** guarantee anti-cheat-grade fairness.

See the companion [Puzzle Coach and Scoring Proposal](GRIDMATES_PUZZLE_COACH_AND_SCORING_PROPOSAL.md) for a draft penalty rubric and implementation notes. **No provisional score weights should be treated as scientifically validated.**

---

## 11. Game experience: what Sudoku players may actually enjoy

These are design hypotheses to test, not universal truths.

**Clarity:** A readable 9×9 board, obvious selected cell, row/column/box emphasis, clear fixed versus entered digits and calm number entry can matter more than flashy effects.

**Flow:** The next useful deduction should be discoverable. If several minutes pass with no progress, a friendly clue should invite learning rather than make the player feel defeated.

**Control:** Notes, Undo, checking mistakes, pausing, a suitable time display, accessible fonts/contrast and no mandatory ad interruptions should keep players comfortable.

**Skill growth:** Recognizing a technique is itself a satisfying progression moment. Optional “You learned Hidden Single” summaries could be more meaningful than artificial leveling alone—only record that technique if genuinely demonstrated.

**A gentle difficulty ramp:** A tutorial or Beginner preset could be valuable, but avoid constantly changing the existing difficulty definitions or generating empty-seeming trivial puzzles for skilled players.

**Variety:** Repeated puzzle templates, repeated clue locations and lack of changing logical patterns can make play stale, even when puzzle hashes differ.

**User autonomy:** Some people want no hints, some want transparent explanations, some want to relax with unlimited help. The same game can accommodate them if results accurately reflect the differences.

**Good company:** Nearby LAN co-op can encourage in-person conversation and showing each other a deduction. This is GridMates' intentionally local-first identity; online features remain possible in the future, not obligatory.

**An ad policy compatible with Sudoku:** Only rare interstitials at natural transitions **after** a finished puzzle/result, never while solving or studying a hint. A pleasant puzzle-book feeling is easy to damage with interruptions.

### Useful early player-test questions

1. Can someone who has never played Sudoku understand the board in 60 seconds?
2. Does Easy *feel* welcoming, or merely get solved quickly by the app's exact solver?
3. When a player is stuck, can a hint explain the logic clearly?
4. Do notes and font cosmetics actually look different on two phones?
5. Can a player finish a Daily puzzle and feel satisfied without chasing a streak?
6. Do two players naturally talk or coordinate while solving together?
7. Are there distinct reasons to return beyond a checklist of cosmetics?
8. Does a wrong entry lead to a helpful explanation rather than a dead-end dialog?

---

## 12. Research-backed development priorities

### P0 — Don't break the trusted rules

- Preserve valid single-solution puzzle generation and deterministic saved identities.
- Preserve original givens, no contradictory placements in final state, saved progress and Undo.
- Protect the owner-tested nearby LAN flow, private peer identities and app signing.
- Continue exact-solution and human-logic checks as **different** operations.

### P1 — Improve “the game teaches Sudoku”

- Upgrade **witness-rich explanation data** for our current four techniques.
- Show pointing/claiming and naked-pair eliminations explicitly.
- Add examples with 1–2 cells remaining and wrong earlier entries.
- Turn existing Check my work into a coherent assistance path with clear disclosure.
- Build a small in-game Guide around these verified techniques.

### P2 — Calibrate what Easy / Medium / Hard means

- Calculate within-tier step counts and bottlenecks for many generated examples.
- Gather opt-in novice/casual/experienced playtest feedback.
- Distinguish “solver used” from “necessarily required.”
- Version rating changes and compare historical records honestly.

### P3 — Broaden puzzle depth selectively

- Add **Hidden Pair** and **Naked Triple** only after positive and negative technique fixtures pass.
- Then consider X-Wing and perhaps XY-Wing for a clearly separate future Expert tier.
- Resist adding every technique to a huge solver merely because it exists; explanation and QA overhead grow quickly.

### P4 — Long-term generator diversity

- Audit seed/pattern equivalence and visible replayability.
- If needed, explore alternative complete-board sampling, alternative removal patterns, and curated technique-focused puzzle sets.
- Benchmark generation times on real phones, not just emulator/JVM.

### P5 — Inform future GridMates puzzle games

The separation between **exact validity**, **human-explainable next steps**, and **player-perceived challenge** is reusable conceptually for GridMates: Crossword and later puzzle games. Do not force Sudoku's specific solver or 9×9 candidate model into a generalized framework prematurely.

---

## 13. Suggested engineering experiments for the Work chat

| Experiment | Question | Acceptance evidence |
|---|---|---|
| 1000+ seeded puzzle audit | How often does our generator produce each grade, with what step distribution? | Reproducible seed/rating CSV, timings and source commit |
| Technique reference suite | Are elimination proofs always valid, not merely coincidentally correct? | Verified positive, negative and near-miss fixtures |
| Endgame recovery suite | Can a player with two cells left get genuinely helpful guidance? | Correct-state and mistaken-entry phone tests |
| Coach proof visualization | Does highlighting sources improve understanding? | Two-phone screenshots and novice feedback |
| Clue-count comparison | Are similar clue counts spread across different human grades? | Data table rather than blanket claims |
| Alternate solving paths | Can a different easy technique avoid the solver's “hardest step”? | Compare multiple valid proof paths / conservative wording |
| Rating model trial | Does a composite rating order puzzles better than highest-technique alone? | Player-time/hint correlation with privacy-safe method |
| Generator diversity review | Are many “new” seeds structurally very similar? | Clue-pattern similarity and isomorphism sampling |
| Readability trial | Can new/casual players easily read notes and numbers on their devices? | Compact phone, 200% font, TalkBack and real-device QA |

These are proposals for staged work, not instructions to interrupt current app development or use extensive GitHub Actions minutes without benefit.

---

## 14. Editorial safety checklist if converting this into a public in-game guide

- [ ] Convert research jargon to ordinary, encouraging language.
- [ ] Every illustrated example is self-created and checked against the actual Sudoku engine.
- [ ] Don't reproduce third-party strategy illustrations without rights.
- [ ] Cite/reference external strategy sources in developer docs; in-game text should be original and concise.
- [ ] Never claim clue count alone determines difficulty.
- [ ] Do not conflate guaranteed uniqueness with the base rules.
- [ ] Explain that some deductions **remove candidates** rather than immediately fill cells.
- [ ] Don't label a solution-comparison “Check my work” as a purely logical hint.
- [ ] A tip cannot reveal private opponent clues or an unplayed Daily solution in a share card.
- [ ] QA all instructional text with large fonts, voice accessibility and multiple devices.
- [ ] Keep hints unlimited and player autonomy intact.
- [ ] If the technique/rating model changes, version stored metadata and retain old records accurately.

---

## 15. Sources and further study

**Selection criteria:** preferentially original mathematical research, empirical studies of human difficulty, established detailed technique references, and verified source files. Sources support facts or technique definitions; proposals for GridMates are explicitly labeled and require product/QA validation. Links verified or located November 6, 2026. Links may change; maintain source names and IDs.

### Research and mathematics

**[R1]** Pelánek, R. (2014). *Difficulty Rating of Sudoku Puzzles: An Overview and Evaluation*. arXiv:1403.7373. Human-solver dataset and difficulty factors: individual logic steps and dependency structure. https://arxiv.org/abs/1403.7373

**[R2]** Pelánek, R. (2011). *Human Problem Solving: Sudoku Case Study* (Masaryk University technical report FIMU-RS-2011-01). Human difficulty modeling, thousands of solving hours. Abstract and bibliographic detail: https://www.fi.muni.cz/reports/2011/abstract/FIMU-RS-2011-01.shtml

**[R3]** Ercsey-Ravasz, M., & Toroczkai, Z. (2012). *The Chaos Within Sudoku*. *Scientific Reports*, 2, 725. Alternative computational hardness metric; valuable research, not a substitute for GridMates human calibration. https://www.nature.com/articles/srep00725

**[R4]** Norvig, P. (2006; online essay). *Solving Every Sudoku Puzzle*. Clear constraint-propagation and backtracking explanation. https://norvig.com/sudoku.html ; updated notebook: https://colab.research.google.com/github/norvig/pytudes/blob/main/ipynb/Sudoku.ipynb

**[R12]** Wolfram MathWorld. *Sudoku*. Formal rules, Latin-square context and history. https://mathworld.wolfram.com/Sudoku.html

**[R13]** Block, S. S., & Tavares, S. A. (2009). *Before Sudoku: The World of Magic Squares*, chapter *History of Sudoku*. Oxford Academic. Abstract: https://academic.oup.com/book/53801/chapter-abstract/422180647

**[R14]** Jarvis, F. *Sudoku Enumeration Problems*. Exact enumeration and symmetry-equivalent completed grids. https://www.afjarvis.org.uk/sudoku/

**[R15]** McGuire, G., Tugemann, B., & Civario, G. (2014). *There Is No 16-Clue Sudoku: Solving the Sudoku Minimum Number of Clues Problem via Hitting Set Enumeration*. *Experimental Mathematics*, 23(2), 190–217. https://doi.org/10.1080/10586458.2013.870056 ; public arXiv abstract https://arxiv.org/abs/1201.0749

### Solving technique references and grading

**[R5]** HoDoKu. *Configuring the Solver*. Easy-first strategy, configurable technique order and difficulty. https://hodoku.sourceforge.net/en/docs_solv.php

**[R6]** HoDoKu. *Creating Sudokus*. Describes configurable difficulty level and step-score system. https://hodoku.sourceforge.net/en/docs_cre.php

**[R7]** HoDoKu. *Singles: Full House, Hidden Single, Naked Single*. https://hodoku.sourceforge.net/en/tech_singles.php

**[R8]** HoDoKu. *Intersections: Pointing and Claiming Locked Candidates*. https://hodoku.sourceforge.net/en/tech_intersections.php

**[R9]** HoDoKu. *Naked Subsets*. https://hodoku.sourceforge.net/en/tech_naked.php

**[R10]** HoDoKu. *Hidden Subsets*. https://hodoku.sourceforge.net/en/tech_hidden.php

**[R11]** HoDoKu. *Basic Fish: X-Wing, Swordfish, Jellyfish*. https://hodoku.sourceforge.net/en/tech_fishb.php

**[R16]** HoDoKu. *Wings: XY-Wing, XYZ-Wing, W-Wing*. https://hodoku.sourceforge.net/en/tech_wings.php

**[R17]** HoDoKu. *Uniqueness: Unique Rectangles, BUG*. Notes the extra uniqueness assumption and controversy. https://hodoku.sourceforge.net/en/tech_ur.php

**Additional reference index:** https://hodoku.sourceforge.net/en/techniques.php (coloring, chains, fish variants, reference patterns).

**License caution:** The HoDoKu site identifies content under GNU Free Documentation License in some pages. This study **paraphrases** brief principles; **do not copy complete page explanations or diagrams wholesale into our commercial game**. Recreate self-authored training examples from verified game puzzles and check applicable licenses before any reuse.

### GridMates source evidence (living code, not research literature)

- Latest inspected source branch: https://github.com/PlayerEden/SudokuTogether/tree/feat/gridmates-nearby-animals
- Sudoku rules/generation: https://github.com/PlayerEden/SudokuTogether/blob/feat/gridmates-nearby-animals/core/src/main/kotlin/com/playereden/sudokutogether/core/Sudoku.kt
- Human deduction/grading: https://github.com/PlayerEden/SudokuTogether/blob/feat/gridmates-nearby-animals/core/src/main/kotlin/com/playereden/sudokutogether/core/HumanSudoku.kt
- Tiered puzzle supply: https://github.com/PlayerEden/SudokuTogether/blob/feat/gridmates-nearby-animals/core/src/main/kotlin/com/playereden/sudokutogether/core/PuzzleSupply.kt
- Logical hints/checking: https://github.com/PlayerEden/SudokuTogether/blob/feat/gridmates-nearby-animals/core/src/main/kotlin/com/playereden/sudokutogether/core/SudokuAssistance.kt
- Android coach explanation: https://github.com/PlayerEden/SudokuTogether/blob/feat/gridmates-nearby-animals/app/src/main/java/com/playereden/sudokutogether/PuzzleCoachDialog.kt
- Existing source-backed technique calibration: https://github.com/PlayerEden/SudokuTogether/blob/feat/gridmates-nearby-animals/docs/V0.8B_HUMAN_DIFFICULTY_CALIBRATION.md

---

## 16. Work-chat handoff summary

**Immediate request from owner:** Develop a trustworthy, broad Sudoku research reference to guide game design and potentially support a future **in-game Sudoku strategy/teaching guide**. The document is already created. Do **not** convert it automatically into source or a giant Help screen.

**Recommended next engineering investigation (when Work capacity resumes):** Review our current Easy/Medium/Hard human-solver trace, compare it against published human-difficulty findings, then propose an **incremental** improvement to hint explanations and optional in-game lessons using the strategies we already support.

**Preserve:** existing save history, QA signing, current stored difficulty labels, offline functionality, same-Wi-Fi / nearby LAN transport, hints/check fairness, Daily history, versioned release checkpoints and active stacked PR workflow.

**Before writing a feature:** Scope the smallest educational user benefit, confirm owner approval, build + QA isolated changes, document exact passing evidence, and preserve rollback options.

**Bottom line:** A reliable Sudoku game should not merely know the correct answer; it should understand—and be able to explain—**the next honest step a human can take**.
