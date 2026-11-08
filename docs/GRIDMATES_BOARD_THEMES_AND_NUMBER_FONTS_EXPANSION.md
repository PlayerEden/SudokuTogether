# GridMates: Sudoku — Expand Board Themes & Unlockable Number Styles

**Recorded:** November 8, 2026  
**Status:** **Owner-requested roadmap consideration / art and engineering handoff.** Expansion is desired; **exact names, designs, unlock costs and rollout are proposals, not approved final assets or working app features.**  
**Code baseline:** canonical `main`, v0.11D reference. Don't restore old development branch Android code over main.  
**Related:** [Font assets already bundled](GRIDMATES_FONT_ASSETS.md), [Co-op shared board themes proposal](GRIDMATES_SHARED_COOP_BOARD_THEMES_DESIGN.md), [Collections and Daily rewards planning](GRIDMATES_COLLECTIONS_DAILY_REWARDS_AND_FONTS_DESIGN.md), [Sudoku learning research](SUDOKU_DEEP_RESEARCH_AND_GAME_DESIGN_REFERENCE.md), [Delivery Plan](GRIDMATES_DELIVERY_PLAN.md), [Project Handoff](PROJECT_HANDOFF.md).

## 1. Product goal

Give players **more distinctive cosmetic goals** beyond collectible profile portraits. The puzzle board and digits are on screen for most of a Sudoku session; they should offer pleasant, meaningful variety with *no impact on puzzle difficulty, number validity, hints, competition fairness or accessibility*.

The aesthetic should remain **warm, polished, intentionally designed puzzle-book style**, not a noisy animated background or heavy AI-art decoration. Each addition must look good at 81-cell mobile scale. Existing options **remain unlocked and compatible**; no changes to how current saves interpret them.

**Owner intent:** substantially expand (1) unlockable **board themes** and (2) unlockable **number font styles**. Existing idea: in future Co-op, share the higher-ranked equipped board on both phones while each player's authored numbers retain their own fonts/colors. This document supplies theme candidates and compatibility constraints for that feature.

**Do not treat this as a mandate to create 20 assets in one Work session.** Review a small distinctive set, get owner playtest feedback, then build a broader collection.

## 2. Verified current game state (main branch, November 8)

`core/src/main/kotlin/com/playereden/sudokutogether/core/Cosmetics.kt` currently lists:

| Type | IDs | Current names | Unlock conditions |
|---|---|---|---|
| Boards | `classic`, `linen`, `midnight`, `circuit` | Classic, Linen, Midnight, Circuit | Classic starter; Linen after 2 Solo; Midnight after 5 Solo; Circuit after 3 Versus wins |
| Number fonts | `clear`, `serif`, `mono` | Clear, Bookish, Terminal | Clear starter; Bookish after 1 Solo; Terminal after 2 Co-op completions |

- Android v0.11D already packages licensed static **Noto Sans, Noto Serif, and Liberation Mono** fonts in `app/src/main/res/font/` with an in-APK license notice. **Bundling to fix phone differences is completed at this checkpoint**. New font ideas should follow the same portable, licensed local asset approach, not fall back to device/OEM font families. See [Font Assets](GRIDMATES_FONT_ASSETS.md).
- Current `CosmeticUi.kt` handles `boardFillFor(id)` and font selection previews, and `StylePreview` shows actual chosen digit appearance.
- Current board designs are mostly palette/background variations. More elaborate ideas may require a small theme palette/model expansion for borders, selected cells, peer emphasis and contrast—not just a single background color.
- `Appearance` has separate `board`, `font`, `ink` and portrait/emblem fields. The existing LAN system passes curated IDs; when adding IDs, update allowlists, rendering, unlock logic, saved display, network compatibility and fallback.
- The **Classic number display** setting intentionally suppresses equipped number font/ink display; keep it as a per-device preference. Explain clearly why a chosen font may not appear when this setting is on.
- Co-op currently distinguishes authored player numbers. Keep each player's font/ink individual when implementing shared-board display later.
- The v0.11D bundle included a native number font preview check; **cross-device physical comparison for expanded fonts still required**.

Source references:
- [Cosmetics.kt](https://github.com/PlayerEden/SudokuTogether/blob/main/core/src/main/kotlin/com/playereden/sudokutogether/core/Cosmetics.kt)
- [CosmeticUi.kt](https://github.com/PlayerEden/SudokuTogether/blob/main/app/src/main/java/com/playereden/sudokutogether/CosmeticUi.kt)
- [GameTheme.kt](https://github.com/PlayerEden/SudokuTogether/blob/main/app/src/main/java/com/playereden/sudokutogether/GameTheme.kt)

## 3. Proposed board theme collection

**Guiding idea:** different **physical puzzle materials, artistic moods or environments** rather than nine nearly identical shades of green. Use restrained decoration *around* the grid; don't interfere with cells or digits.

### First expansion candidates (concepts only)

| Proposed stable ID | Display name | Visual identity | Proposed unlock path |
|---|---|---|---|
| `board_parchment` | **Parchment** | Warm ivory/aged paper, brown pencil lines; exceptionally readable | Early Solo milestone |
| `board_garden` | **Garden** | Sage/forest palette, subtle botanical trim outside cells | Solo milestone |
| `board_ocean` | **Ocean** | Soft marine blue / muted teal, gentle edge details | Daily milestone or earned collection reward |
| `board_graphite` | **Graphite** | Cool charcoal and soft silver; distinct from existing dark teal Midnight | Moderately earned / alternate dark board |
| `board_rosewood` | **Rosewood** | Warm burgundy/brown notebook, light cream rules | Later Solo milestone |
| `board_blueprint` | **Blueprint** | Drafting-table blue grid and restrained technical detailing | Co-op/Versus or advanced milestone |
| `board_autumn` | **Autumn** | Soft amber/rust/cream; lightly textured paper framing | Rotating collection or general play |
| `board_gold_edition` | **Gold Edition** | Luxury parchment with fine gold trim, **not** low-contrast gold digits | Longer earned milestone / special gift |

**Start with 3–4 board pilots** before all eight. Suggested initial four: **Parchment, Garden, Ocean, Graphite**. Test how different they look beside Classic/Linen/Midnight/Circuit; if a candidate is too similar, replace it rather than padding the catalog.

### Board implementation

- Create a **single source-of-truth theme record**: stable ID, display name, unlock requirement or entitlement, category, background, grid/thick rules, selected cell, peers, partner highlight, error highlight, given text contrast, dark/light flag, thumbnail and *optional* shared-board rank. Avoid scattered `if (id in ...)` lists everywhere.
- Distinguish **theme palette** from the **global app UI theme**: selected board theme should not recolor every menu or remove the friendly GridMates brand.
- Use opaque high-contrast foregrounds for givens and numbers. Every included number ink must stay readable on every board; if necessary compute accessible display ink per background without changing the saved cosmetic ID.
- Texture/illustration, if used, should be subtle, cached and bounded. Prefer procedural or bundled resource design over fetching new backgrounds from the network. Avoid large image downloads or slow redraws on a 9×9 grid.
- Keep the classic no-frills option always available. A dark-board choice should never prevent someone from reading or solving the puzzle.
- Support **large text, small screens, landscape, selected/peer/error states, pencil notes, and colorblind alternatives**; don't use color alone to denote conflicts or ownership.
- [Co-op shared boards](GRIDMATES_SHARED_COOP_BOARD_THEMES_DESIGN.md): future ranking and deterministic tie-break must explicitly handle new IDs. Board **rarity** and shared-board **display priority** need not be identical. Consider a local accessibility override to view one's own board without changing teammate entitlement.

## 4. Proposed number font collection

Focus on **digits 1–9** at real Sudoku sizes. Two fonts that look interesting in normal prose may produce nearly indistinguishable numerals, or a decorative 1/7/4/9 that is impossible to distinguish in a 9×9 grid.

### First expansion candidates (typeface *styles*, no font binary chosen)

| Proposed stable ID | Name | Distinguishing features | Proposed unlock |
|---|---|---|---|
| `font_pencil` | **Pencil** | Human handwritten pencil-like numerals, clearly readable | Early Solo / casual milestone |
| `font_rounded` | **Rounded** | Friendly soft geometric sans numerals, stable stroke width | Starter-plus or Daily milestone |
| `font_typewriter` | **Typewriter** | Printed mechanical serif digits, visibly unlike Terminal Mono | Co-op milestone |
| `font_handwritten` | **Handwritten** | Natural not-too-slanted handwriting, larger and legible | Mid-tier Solo milestone |
| `font_editorial` | **Editorial** | Elegant print/book-number style with clear 1/4/7 differentiation | Advanced milestone |
| `font_block` | **Block** | Bold, orderly and very high-contrast digit shapes | Earned accessibility-friendly alternate |
| `font_digital` | **Digital** | Restrained LCD calculator character, for a later tech pack | Event / collection / longer milestone |

**Start with 2–3 pilots**: **Pencil, Rounded, and Typewriter**. Test if actual 1–9 characters are sufficiently different from bundled Clear/Bookish/Terminal. Don't ship a new font that is basically a rename of an existing one.

### Font asset requirements

- Select an exact **commercially redistributable** font license/typeface (for example an appropriately vetted OFL font) only after viewing the glyph samples. **Do not assume any font is free to bundle because it is free to preview.**
- Bundle exact font resources within the APK, document source/version/license/copyright and optional checksums similarly to [GridMates Font Assets](GRIDMATES_FONT_ASSETS.md). Keep full license texts as needed inside the app package. Do **not** redistribute raw font binaries to users through this chat or unrelated artifacts.
- Keep current `clear`, `serif`, `mono` stable, preserving selections across upgrade. Use namespaced distinct IDs for new ones, never recycle IDs of old cosmetics.
- Confirm mapping works in the actual Sudoku board for **entered numbers and pencil notes** (where intended), in the Collection selection preview, and for partner-authored cells in multiplayer. Clues can remain in the uniform readable house font as currently designed.
- A proper font **preview should show “123456789”**, ideally along with a tiny actual-sized grid, not only the current sample “1 2 3”.
- Avoid confusing numerals (especially 1 vs 7, 3 vs 8, 4 vs 9), clipping at 9-cell widths, excessive stroke variation, irregular height and bad baseline. Observe in light and dark themes and at 200% system text settings.
- Preserve the independent **Classic number display / uniformNumbers** setting and show user-friendly notice if it overrides equipped number cosmetics. Don't silently modify the user's equipped cosmetic to match a display preference.
- Keep APK asset growth bounded; evaluate static subsetting only if licensing permits and no app text beyond 0–9 depends on that font. Never remove required notices.

## 5. Unlock economy: variety without tedious grinding

**Current Woodland portraits unlock quickly**, and owner feedback says a player has already earned most of them. New boards/fonts could provide longer-lived progression but should not demand hundreds of puzzles.

The following **is a progression proposal, not finalized conditions**:

| Player milestone example | Possible reward |
|---|---|
| Complete 2–5 puzzles | Parchment or Rounded |
| Complete 8–12 puzzles | Garden or Pencil |
| Complete 15–25 puzzles | Graphite or Typewriter |
| Complete 3–7 Daily challenges (distinct dates) | Ocean |
| Complete 3–5 co-op games | A playful print/font style |
| Win a handful of Versus races | Blueprint or Graphite |
| Complete a longer—but reasonable—general achievement | Gold Edition |

**Fairness guideline:** Every major category must have **multiple Solo-play accessible options**. Don't force older/solo players to use Co-op, Versus, daily attendance or online services just to enjoy most of the board/font variety. Keep special multiplayer achievements as *bonus* choices rather than the only way to obtain several themes.

Daily **Puzzle Stamps** / rotating rewards remain a separately undecided system. New cosmetic catalog must support ordinary milestone unlocks first; optionally make some *future* themes eligible for Daily Stamps or gift codes without assuming a wallet system already exists.

Cosmetics are **visual only**. No help advantage, extra scoring, puzzle hints or solution reveals. Don't confuse a high shared-board **priority rank** with player merit; it is a display rule.

### Cosmetic metadata / migration thoughts

Present `unlocked vs locked` state, a plain-language requirement, and visible progress-to-next-unlock where reliably computable.

Do not make unlock eligibility dependent solely on a currently truncated recent-record list if that could later **relock earned cosmetics** when older records are pruned. Existing `Cosmetics.allowed` currently calculates achievements from completion records; expansion should evaluate **durable earned entitlements or monotonic lifetime stats**, versioned/backward-compatible, rather than relying on an eventually prunable history.

If gifted/daily-redemption items are added later, keep entitlement-granted ownership separate from milestone eligibility, with idempotent claiming and save/upgrade tests.

## 6. UI/UX plan

- Collection screen should group **Board Themes** and **Number Styles** with a legible count and “Equipped” state, while keeping portraits and other collections reachable.
- Board preview should be a **real miniature 9×9 grid**, not just a flat swatch with “9”. Show givens, entered digits, notes, selection and grid contrast where practical.
- Font preview should show **all digits 1–9** and a small line of pencil marks, at sizes players actually see. When testing typefaces, use exactly the same sample board to compare fairly.
- Keep browsing straightforward on narrow Android phones. Avoid endless horizontally scrolling cards with no idea what's further right; consider grouped vertical grid/accordion if collections grow.
- Every locked item should show its unlock path and progress in plain language. Don't require a tutorial to understand a cosmetic.
- Allow previewing a locked cosmetic **without granting/equipping it** (optional if easy to implement); don't alter saves or notify partner of a non-owned theme.
- Switching cosmetics changes visuals, never the actual puzzle, players' entries, difficulty, assistance count, or current LAN match state.

## 7. Suggested Work chat implementation stages

1. **Design + pilot comparison:** On current main, create static concept swatches / grid + number samples for 3–4 boards and 2–3 licensed font candidates. Let owner review them **before** committing to bulk implementation. No APK required for pure design evaluation; use proper QA for any Android rendering change.
2. **Theme model/refactor:** If approved, centralize theme palette properties and render all selected states consistently across grid/cell/background/preview. Add new options with safe ID/migration/network behavior.
3. **Font packaging:** Bundle vetted exact typefaces, licenses and versioned metadata. Test digits on two real phones to avoid repeating the previous device-font issue.
4. **Unlocks and Collection:** Define reasonable Solo-first thresholds, preview organization, locked/owned UI, and durable unlocks that survive historical record pruning.
5. **Multiplayer and release QA:** Verify authored number font/color ownership on shared Co-op board, uniformNumbers override, opponent style in Versus, old-client compatibility, and **updating over current installed APK** without losing saves.

**Release priority:** This is offline-core polish / collections expansion, **not** permission to derail important v0.11D QA or Play readiness, create an online marketplace, introduce currencies or add paid cosmetics. Small coherent batches and meaningful physical playtesting are better than shipping many hard-to-read styles.

## 8. Acceptance checklist

- [ ] All four old board IDs and all three old font IDs continue to resolve, unlock and display correctly after update.
- [ ] New themes are visibly distinct, high-contrast and readable under all number ink choices and accessibility modes.
- [ ] At least first 3–4 board pilots and 2–3 font pilots approved after real phone preview.
- [ ] Each new font uses the exact bundled licensed resource on both phones, not a system generic family.
- [ ] Font samples show 1–9 legibly; no clipping for 1–9 in board, notes, preview or partner cells.
- [ ] Existing profile/achievements/records, skins, app ID, signing identity and saved games are retained in an in-place APK upgrade.
- [ ] Clear and stable saved ownership, no re-locking due to history pruning; consistent requirements and accurate progress text.
- [ ] LAN mode and shared Co-op visual design remain sound; new IDs do not crash old clients, unrecognized cosmetics fall back gracefully.
- [ ] No decrease in Sudoku grid performance or responsiveness on lower-end supported phones.
- [ ] No mandatory ads, cloud login, paid purchase, new game logic or changes to difficulty/scoring as part of this cosmetic expansion.

**Decision still needed:** Exact new theme/font artwork, which options belong to v1, unlock paths/tiers, and whether the owner wants to prioritize palettes vs textured board finishes. This document gives the Work chat clear candidates without prematurely making those choices.
