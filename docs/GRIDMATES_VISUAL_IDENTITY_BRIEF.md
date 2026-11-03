# GridMates — Visual Identity & UI Design Brief

**Prepared:** 2026-11-03  
**Product:** GridMates: Sudoku (formerly Sudoku Together)  
**Series:** GridMates, with GridMates: Crossword planned next  
**Status:** **Creative direction proposed for visual exploration, NOT final art/design approval.** The *name and series concept* are owner-approved.  
**Audience:** Implementation/Work chat, UI designers, Android developers, QA  
**Related decisions:** [GridMates brand and series vision](GRIDMATES_BRAND_AND_SERIES.md) · [Current project handoff](PROJECT_HANDOFF.md) · [Release roadmap](ROADMAP_V08_TO_LAUNCH.md)

## 1. What has actually been approved

- **Locked umbrella brand:** **GridMates**.
- **Locked intended public game name:** **GridMates: Sudoku**.
- **Next game planned:** **GridMates: Crossword**, developed separately *after* Sudoku is ready.
- **GridMates: Minesweeper** and other games are possibilities, not committed releases.
- The user asked to capture the visual identity conversation so the **separate implementation chat can work from it**. The specifics in this document are a **design brief and recommendations**; they have *not* been individually reviewed as finished artwork or UI.

**Intended brand positioning:** *Clean puzzle-tech with friendly warmth*. The product should feel like a polished, credible mobile puzzle game: modern, inviting, thoughtful, distinctive, and more game-like than a plain utility, without clutter or aggression.

## 2. Design principles

1. **Smart:** clarity, structure, strong puzzle hierarchy, satisfying completion.
2. **Friendly:** warm but not childish; works for solo players, partners and gentle rivals.
3. **Modern:** clean mobile-native visuals, high-quality spacing and interaction.
4. **Modular:** a recognizable GridMates family while each game has its own personality.
5. **Trustworthy:** accessible, predictable, easy to read; no false multiplayer/cloud claims.
6. **Puzzle first:** visually elevate the game **without reducing the usable 9×9 board, thumb access, clue readability, or device compatibility**.

**Reference adjectives only (not copying another brand):** Duolingo-like recognizability, Nintendo-like friendliness, polished native mobile puzzle UI.

**Avoid:** overdone sci-fi/hacker styling, neon chaos, toy-like cartoon art, heavy gradients, glossy skeuomorphism, tiny buttons, noisy textures, cluttered 9×9 board, old-style utilitarian spreadsheet UI, excessively competitive language, or designs that look auto-generated rather than purpose-designed.

## 3. Identity system: symbol + wordmark

### Recommended primary logo direction: "Connected tiles"
A simple **2×2 or 3×3 rounded grid** of tiles. One or two tiles connect, share an accent, or subtly offset to imply both **logic/puzzles** and **two players collaborating**. Prioritize a clean silhouette identifiable at small sizes. The master mark must be puzzle-agnostic: **not a nine-by-nine Sudoku board**, and no crossword-specific letters.

Create at least three rough options before selecting a final logo:

- **A — Connected tiles (recommended):** four rounded cells, a deliberate accent/link between two tiles; strongest small-size shape.
- **B — Tile-dot wordmark:** geometric `GridMates` lettering; the dot on the `i` becomes a small square/tile. Symbol can stand alone.
- **C — Shared-grid window:** simple grid/corner or interlocking tile edges; more structured/premium, with a warmer accent.

Test each on: dark/light background, monochrome, 48px, and as an Android adaptive launcher foreground. Keep line weights and negative space large enough to survive icon masking.

### Wordmark guidance
- Geometric or rounded sans-serif; modestly bold, not a futuristic display font.
- Treat **Grid** as clear/structured and **Mates** as friendly/warm; any contrast should be subtle and cohesive.
- Optional custom square `i` dot or grid corner in a letter, only if legibility remains excellent.
- Favor spelling **GridMates** as one word, capital G and M; product label **GridMates: Sudoku**.

### Explicit caution
The brief describes **directions**, not delivered logo assets. Do not ship clip art, copied wordmarks or unlicensed fonts. Produce original vectors and test small-size readability; obtain user approval before locking final logo files.

## 4. Candidate color language (exploration, NOT locked)

Suggested starting values for design mockups. Adjust after contrast checks, device previews and user feedback. These are not final validated accessibility tokens.

| Role | Suggested token | Hex | Intended use |
|---|---|---|---|
| Brand anchor | `brand.navy` | `#17263F` | Wordmark, headings, deep surfaces |
| Primary action | `brand.blue` | `#316FEA` | Selected actions, navigation cues |
| Cooperative accent | `brand.teal` | `#159D91` | Partner/join affordances, secondary accent |
| Warm reward accent | `brand.gold` | `#E6AD37` | Celebrations, achievements (never only status cue) |
| Main light surface | `surface.light` | `#F7F9FC` | Calm neutral backdrops |
| Text dark | `text.primary` | `#17263F` | Core readability |
| Muted text | `text.secondary` | `#56657B` | Secondary labels; check contrast |
| Dark-mode base | `surface.dark` | `#111B2B` | Dark theme exploration |

**Series logic:** Shared navy/neutral typography and a common icon geometry; each game gets a recognizable signature accent.

- **GridMates: Sudoku:** **blue + teal**, optional small gold victory/achievement moments. Calm, precise, thoughtful.
- **GridMates: Crossword:** **indigo/purple** (candidate `#7058BF`) with shared brand anchor. Word-focused, thoughtful, a slightly literary personality.
- **GridMates: Minesweeper (uncommitted):** **green** (candidate `#31875D`) with a slightly more tactical feel, but not militaristic.

**Contrast rules:** Verify WCAG-level legibility for text and non-text controls; do not rely on hue alone for puzzle states, partner ownership, errors or validation. Provide light/dark variants, color-blind-safe differentiation, and selected/focused/disabled states. Avoid using decorative pale colors for tiny Sudoku candidates.

## 5. Typography

- **Brand/headings:** Explore **Nunito Sans**, **Manrope**, or **Plus Jakarta Sans** (subject to license verification, bundle size, Android rendering and user review). Rounded geometric warmth but professional.
- **App/body/UI:** Favor an easy-to-read platform/system sans or a compatible humanist sans; prioritize native scaling and clarity over forced brand uniformity.
- **Sudoku numerals:** use crisp, unambiguous numerals with consistent alignment and clear distinction between givens, user entries, candidates and completion. Avoid decorative faces for default numbers. Optional cosmetic number fonts should not undermine puzzle legibility.
- **Layout:** support Android large-font scaling and TalkBack; avoid relying on font weight alone to communicate state.

These are **options, not approved font selections**. Check commercial font licensing, offline bundling, package size and fallback before shipping.

## 6. Interface design rules

**Visual character:** native, tactile, rounded-but-restrained cards, consistent grid spacing, soft/subtle elevation, clean icons, calm backgrounds, decisive tap feedback. Prefer recognizable navigation and an easy-to-learn hierarchy.

**Do:**
- Establish a cohesive spacing scale, radius scale, typography hierarchy and composable token set instead of hard-coded one-offs.
- Keep the **Sudoku board** prominent and legible above decorative cards. Avoid sacrificing cell size or keypad area just to make the menu prettier.
- Make primary actions clear: **Continue / New Solo / Daily / Co-op / Versus** as applicable to current app scope, not fictional features.
- Use bottom-thumb-reachable controls while respecting gesture bars, keyboard/insets, compact phones and orientation.
- Use high-quality native UI transitions/haptics sparingly; respect reduced motion and haptic preferences.
- Give helper copy and errors clear actionable explanations.
- Keep optional cosmetics player-specific and preserve existing local records and personalization.

**Don't:**
- Invent online rooms, synced profiles, cross-game inventories or internet matchmaking in UI.
- Make design changes that expose hidden opponent information or break co-op/versus visibility rules.
- Imply a completed store release or cloud service.
- Replace the current functional Compose layout without testing small devices, landscape, screen readers, text scale and home/board actions.

**Accessibility acceptance examples:** tappable items at least about 48dp where applicable; screen-reader labels for icon-only controls; accessible focus/selection distinction; adequate contrast verified in light/dark; labels and patterns in addition to color for collaborator identity.

## 7. Game-specific icon system

### GridMates master mark
A **puzzle-agnostic tile/link symbol**, reusable on websites, eventual splash/intro and future game branding.

### GridMates: Sudoku launcher icon
- Rounded-square / Android adaptive-icon composition, shared GridMates mark vocabulary.
- **Mini stylized grid of 4–9 tiles**, strong bold geometry, one or two accent tiles, perhaps a subtle numeral or `S` only if it helps at tiny sizes.
- **Avoid** fitting a full Sudoku board or dense number labels into the launcher icon.
- Build color, monochrome, adaptive foreground/background, and safe-zone tests.
- Ensure the icon stands out against neighboring puzzle apps without imitating a competitor.

### Future games
- **Crossword:** same geometry/wordmark family, different **purple/indigo** accent and a **crossing-letter/cross tile** idea—not the Sudoku icon simply recolored.
- **Minesweeper (potential):** same family but green signature and a careful reveal/flag concept.

All game icons should look related at a glance but distinguishable if installed side by side.

## 8. Motion, microcopy and voice

**Motion:** tiny tile snap, subtle selection response, restrained completion flourish or cooperative connection animation. Never obscure the puzzle, compromise speed, or make reduced-motion users uncomfortable. Sound is optional; preserve mute preferences and avoid reward spam.

**Copy voice:** supportive, concise, warm, lightly cheerful. Good reference lines:
- “Ready to solve?”
- “Play solo or team up.”
- “Your daily puzzle is ready.”
- “Invite a friend.”
- “Nice work.”

Avoid combat language such as “Crush your enemies” or overdone meme/gamification text. Versus can still feel spirited; the tone should remain welcoming.

**Tagline to test, NOT approved:** “Two minds. One puzzle.” Good for co-op but might undersell the offline solo experience; consider context-specific use rather than locking it as master-brand copy.

## 9. Work-chat handoff: phased design exploration

**Important:** This is a planning brief, **not permission to perform a risky wholesale code rewrite or release rebrand**. Retain the current development gates from the living handoff.

1. **Inventory:** capture current Compose screen/layout screenshots, colors, typography, icon files, accessibility controls, available themes and customization. Note what already works before changing anything.
2. **Present controlled visual proposals:** three rough logo directions, one token/palette draft, a wordmark direction, and a Sudoku launcher-icon family. Present as mockups / alternatives, not preapproved finished assets.
3. **Prototype on real screens:** apply selected draft tokens to **Home, puzzle board, profile and completion**. Prefer minimally invasive native Compose component styling.
4. **Compare/QA:** before/after screenshots on compact and larger phone sizes, light/dark, portrait/landscape, 200% font and screen reader; preserve board playability, keypad, tap targets, saved cosmetic selection, and local multiplayer identity distinctions.
5. **Seek explicit visual sign-off:** lock logo, color values, type and icon **after** owner reviews mockups.
6. **Integrate safely for v0.11 branding:** audit user-facing old title, share flows, adaptive icon, splash, store copy and screenshots. Treat Android `applicationId`, signing, saves and repository naming as a **separate explicit migration decision**, not automatic design changes.
7. **Document evidence:** before/after, selected tokens, exact source commit, test results and rollback checkpoint in GitHub. No false test claims.

**Priority:** finish **GridMates: Sudoku** first. Do not pull engineers away to build Crossword or a cross-game framework prematurely.

## 10. Decisions still open

- Final logo composition and chosen variant.
- Exact approved color palette, including contrast-tested theme variants.
- Font pairing and font licenses.
- Master GridMates slogan (if any).
- Final app icon/adaptive icon and animation/splash treatment.
- How much series branding appears in Sudoku vs a Sudoku-focused standalone home screen.
- Whether to rename the existing GitHub repository, and whether any Android identifiers should ever change (separate migration plan required).

**Decision record convention:** Update this document as the user approves/rejects specific artwork and tokens. Distinguish **proposed**, **approved**, **implemented**, and **verified**. Do not equate this brief with shipping work.
