# GridMates: Sudoku — Home-Screen First Impression & Beginner Accessibility

**Date:** November 8, 2026  
**Status:** **Optional UX considerations only — NOT an approved redesign, implementation requirement, or launch blocker.** The owner explicitly asked for this to be recorded so the Work chat can consider it, modify it, or simply discard/delete it if testing shows the current home works better.  
**Evidence:** owner-provided Android home-screen screenshot dated 2026-11-08; current main-branch `app/src/main/java/com/playereden/sudokutogether/HomeScreen.kt`.  
**Audience to test:** people who simply want to play Sudoku, especially older adults or people who do not frequently use mobile games. Avoid assuming older people are inherently bad at technology; test clarity with real users rather than stereotypes.  
**Source of truth:** canonical `main`; consult [project handoff](PROJECT_HANDOFF.md) and [delivery plan](GRIDMATES_DELIVERY_PLAN.md) before any implementation.

## 1. Current impression — keep what already works

The captured screen looks warm, finished, and calmly recognizable as a puzzle app:
- Nostalgic cream/paper palette with dark green and subtle gold.
- Large, visibly tappable mode controls and a clearly emphasized Continue button.
- Friendly small animal profile portrait, helpful tutorial icon, a prominent Daily puzzle, and stable bottom navigation.
- Clear division of Solo / Co-op / Versus, without forcing a login, game server or noisy promotions.
- Minimal text and no visual advertising clutter.

**Recommendation: polish wording and action hierarchy if tests justify it; do not redesign the overall visual identity.** The phone screenshot itself is not an empirical usability study or evidence of a defect.

## 2. Findings grounded in current HomeScreen.kt

### A. Continue vs New Puzzle vs difficulty

Current main-branch code:
- Mode selectors: `Solo`, `Co-op`, `Versus`. Solo is the default selected mode via `rememberSaveable`.
- When a Solo puzzle exists and is unfinished, the panel title is `Your puzzle`, a primary button says `Continue`, three difficulty buttons appear, and a separate `New puzzle` action sits below.
- The difficulty buttons call `actions.difficulty(id)`; accessibility description says **“difficulty for new games”**. **The selection is for new games**, not for retroactively changing the current saved puzzle.
- Without an unfinished Solo puzzle, the panel instead says `Classic 9×9` with `New puzzle` as the primary action. **Do not assert that brand-new users see Continue.** The supplied screenshot represents an existing saved game.

**Potential confusion:** visually placing Easy/Medium/Hard directly under Continue makes it possible to think those buttons change the currently active puzzle, even though the accessible description already clarifies they're for new games.

**Low-risk suggestions to A/B playtest, not mandatory:**
1. Change **Continue** to **Continue puzzle** when a saved game is present.
2. Add a short label **Start a new puzzle** above the Easy/Medium/Hard selector, or visually group picker with New puzzle.
3. Check whether a new user's primary button should be **Play Sudoku** or **New puzzle**; preserve consistency with existing labels and automated tests.
4. Test whether a brief warning is needed before replacing an unfinished saved puzzle; don't add unnecessary confirmation clicks if the current flow already handles persistence safely.

### B. Progress line includes givens

The screenshot says **Easy · 36/81 · 00:00:04**. HomeScreen.kt derives the 36/81 using `active.cells.count { it.value != 0 }`, which **counts the starting clues**, not just numbers the player has entered. The 36/81 value at four seconds is therefore easy to misread as “I solved 36 cells.”

**Consider clearer language**, e.g.:
- `Easy · 0 numbers entered` for a new puzzle.
- `Easy · 12 numbers entered · Saved` for an in-progress puzzle.
- Or `Easy · 36 of 81 filled (including clues)` if full-board count is intentionally preferred.

Compute a player's actual entered total using cells whose corresponding puzzle clue was initially empty and whose current value is nonzero. This is **filled by player**, not necessarily correct, and notes alone should not count as completed digits. Do not introduce new gameplay state merely to change the copy.

### C. Beginner understanding of multiplayer modes

`Solo`, `Co-op` and `Versus` are short and consistent but not universally understandable without game familiarity. The current mode-specific descriptions are useful: **Same Wi-Fi** and **Private boards · Same Wi-Fi** appear after the player selects a multiplayer mode.

**Test**, don't automatically replace:
- `Co-op` → `Together` or `Play together`.
- `Versus` → `Race` or `Compete`.
- An optional one-line description after selecting the mode: “Solve one board together” / “Race to solve your own board.”
- Keep the visible big mode targets and avoid a wall of explanatory text.

Note that local/Wi-Fi connectivity is not the same as internet multiplayer; don't promise remote or Bluetooth play that has not shipped.

### D. Daily puzzle card

Screenshot shows **Daily puzzle · Ready · 2026-11-08 · Easy**. Current code uses `Ready`, `In progress`, and `Completed`. The card is discoverable and visually friendly.

Possible plain-language copy to test: **Today's puzzle**, **Play today's Sudoku**, or **Continue today's puzzle** rather than only Ready. This is **not** an established issue; no need to change unless observed users hesitate.

### E. Timer and comfort

The timer is visible in the progress caption; some players find time tracking motivating, while others prefer relaxed untimed play. **Consider** whether an existing preference already allows hiding it before proposing any new option. A hidden timer must not silently change scoring/race rules; keep timer visible where required for competitive transparency.

### F. Existing accessibility features worth preserving

- HomeHeaderActions already labels the `?` control **How to play Sudoku** for accessibility services.
- Difficulty controls have `selected` semantics and purpose-oriented descriptions.
- Several tap targets are at least 48dp in code.
- Screen is in a vertical scrolling column, which helps smaller displays.

Verify actual TalkBack, large system font, contrast, touch accuracy and small-device layouts before describing the home screen as broadly accessible.

## 3. Before changing a single line — short usability observation

**Do a five-minute test with 2–5 unfamiliar participants**, ideally including someone who doesn't play many mobile games and an experienced Sudoku player. Give the person a phone with no saved Solo session (or a separate test profile), **without explaining the controls**.

Tasks:
1. “Start an Easy Sudoku.”
2. “Leave the game, then find your way back to where you stopped.”
3. “Start a new Medium Sudoku.” Observe whether they understand the distinction from Continue.
4. “Play today's Sudoku.”
5. “Find how to play Sudoku with another person.”

Ask afterwards: *“What do you think the number 36/81 means?”* and *“What do you think would happen if you tapped Hard right now?”*

Record clicks, unexpected paths, hesitations, and participant quotes—**not speculative scores**. Avoid timing an older adult as though slow navigation is a failure. Let people finish and ask whether they felt confident.

**Decision rule:** If participants navigate successfully and understand the existing labels, **leave the UI alone**. If a specific misunderstanding repeatedly appears, make the smallest wording/grouping change that fixes it. Don't rebuild the home screen or force tutorials.

## 4. Suggested optional priority

| Idea | Effort | Provisional priority | Status |
|---|---|---|---|
| “Continue puzzle” and clarify selected difficulty applies to New Puzzle | Small | Consider first | Optional |
| Show progress excluding the original clues, with honest wording | Small | Consider first | Optional |
| “Co-op / Versus” plain-language descriptions or alternative titles | Small | Test first | Optional |
| Daily card wording change | Tiny | Nice to have | Optional |
| Timer visibility preference | May require save/settings UX | Later if asked | Optional |
| Full home-layout redesign | Large/unnecessary from current evidence | **Not recommended** | No request |

## 5. Work chat handoff

- This file is a **consideration note**, not a directive. User explicitly permits discarding/deleting it if it doesn't help.
- Stay focused on the established **offline-first Sudoku core and release QA**; don't let minor copy testing derail v0.11D integration, two-phone acceptance, Profile/Collection work or store readiness.
- If implementation is chosen: verify current main code and fresh screenshots, add a small scoped change, preserve Solo/Daily/multiplayer save semantics and mode selection, and run relevant native/UI/accessibility tests.
- Only commit UI changes if a real usability benefit outweighs the change. Visual polish should preserve the current cream/forest paper-book identity and large targets.
- No source code, Android binaries, assets or published store copy were changed by creating this document.
