# GridMates — Brand and Puzzle Series Vision

**Implementation baseline:** Continue from v0.11A on `feat/gridmates-coach-launcher`, based on `feat/gridmates-brand-copy` at92ebc3b. [Staged delivery plan](GRIDMATES_DELIVERY_PLAN.md) owns the current order; earlier v0.10D/development references below are design-time context. Stage1 is launcher polish and explicit entry checking; sharing/profile/scoring/monetization are later stages.
**Decision recorded:** 2026-11-03  
**Status:** User-approved creative naming decision; implementation, trademark/store clearance and launch assets pending  
**Canonical game repo:** https://github.com/PlayerEden/SudokuTogether  
**Working app source:** `development`

## 1. Names locked by the project owner

- **Umbrella / series brand:** **GridMates**
- **Current Sudoku game — final intended public-facing title:** **GridMates: Sudoku** (formerly working-titled *Sudoku Together* / *SudokuTogether*).
- **Next planned game:** **GridMates: Crossword** (new project after Sudoku reaches the agreed quality/release stage).
- **Possible later games:** **GridMates: Minesweeper** and other puzzle formats. These are **ideas, not committed releases**.

The owner specifically wants a recognizable **series of distinct puzzle games**, not one giant app that must contain every puzzle. Each game should stand alone, but the same GridMates identity can signal friendly social play and quality.

**Brand premise:** Familiar puzzles are fun alone and especially engaging with a friend. Offer true solo play plus genuinely thoughtful cooperative/competitive options, not forced connectivity.

**Visual direction (proposed, not final art approval):** [GridMates Visual Identity & UI Design Brief](GRIDMATES_VISUAL_IDENTITY_BRIEF.md). This companion document captures logo concepts, a candidate shared palette and per-game accents, font and icon explorations, UI/accessibility principles, and a stepwise implementation checklist for the Work chat.

**Tagline candidate, not approved:** “Two minds. One puzzle.” (Could undersell solo; evaluate during brand/design pass.)

## 1A. Experience clarification — nostalgic spirit, full-featured execution (2026-11-03)

**Owner priority:** Capture the cozy simplicity of traditional paperback pencil-and-paper puzzle books **without recreating paper textures literally or sacrificing useful modern features**. Think a pocket puzzle collection that is delightful to open, solve and revisit.

- Strong offline solo puzzles; great usability, fast resume, Daily and personal history.
- Local Wi-Fi co-op and optional gentle competition — broad cloud matchmaking and public online services are **not essential to the series promise**.
- **A rich but grounded player profile is important**, not a cut: earned/customizable avatars and icons, cosmetics, showcase badges, statistics, achievements, puzzle history and a sense of identity.
- Puzzle formats should be distinctive and occasionally offer thoughtful twists, while always respecting classic modes.
- **Longer-term:** evaluate Google Play Games integration and truly portable backup / restoration for people upgrading phones. Local play remains usable without an account. Platform sign-in and cloud progress backup are separate integrations, not automatic or currently implemented.

Full implications and a tentative profile screen outline: [Visual Identity & UI Brief](GRIDMATES_VISUAL_IDENTITY_BRIEF.md). Product priority ≠ feature shipped; changes should be staged and tested after the current core Sudoku gates.

## 1B. Local multiplayer is a purpose, not a limitation (2026-11-03)

**Owner's product vision:** Choosing LAN / nearby multiplayer is **not merely a way to avoid server costs, programming complexity or public internet matchmaking**. It's a deliberate, human-centered nod to **connecting with the people already in our lives** — a spouse, sibling, parent, child, friend, or someone sitting beside us. It recalls asking someone nearby to help finish a newspaper crossword or puzzle-book clue. Sometimes one person starts a puzzle and another joins in with a single suggestion; that casual shared moment is the heart of GridMates.

**Core principle:** *Use a small puzzle to make room for real-world connection.* Make it enjoyable to solve alone, and naturally rewarding to invite someone nearby to participate, without turning the experience into a worldwide social network.

### Implications for game design

- **Local-first is an intentional identity.** Preserve offline Solo and optional same-Wi-Fi cooperation/competition as first-class modes, not placeholder features awaiting an online replacement. Don't label LAN a lesser version of "real multiplayer."
- **Make joining feel effortless.** Host/Join should be easy enough that a person can say "Want to do this puzzle with me?" and be playing soon, with reliable room codes and/or nearby discovery. QR pairing is a future option after real two-phone QA. Do not invent claims of Bluetooth, remote play or internet hosting.
- **Support interaction, not distraction.** Gameplay should create reasons to communicate in person (shared discoveries, a clue one person spots, complementary contributions). Avoid constant chat prompts, notifications, friend-finding feeds, streak pressure or worldwide rankings as default goals.
- **Friendly co-op and light competition.** Shared puzzles and local races can be playful; avoid making every partnership a performance contest. Celebrate a puzzle solved together. Preserve individual accomplishment in Solo.
- **Respect genuine closeness and privacy.** No account or stranger matchmaking should be required for core local play. Do not collect personal contact graphs just to facilitate nearby gameplay.
- **Friction and accessibility matter.** Offline portability, convenient resume, calm visuals and easy sharing are part of enabling spontaneous kitchen-table/couch/car-ride (passenger) puzzle moments. Multiplayer reliability must still pass **real two-phone LAN tests** before release claims; philosophy doesn't waive engineering gates.
- **Keep future options open without shifting the promise.** Remote play, optional accounts and cloud sync could be useful to people who are close emotionally but far away physically. Treat those as later extensions only if they support the same relational intent, rather than chasing social-network scale for its own sake.
- **GridMates: Crossword** can particularly embody the original "three-letter word for..." moment. Preserve crossword-specific natural conversation, not a generic Sudoku-style multiplayer wrapper.

### Messaging exploration — NOT approved public tagline

- "A little puzzle. A little time together."
- "Better together, even when you're sitting right next to each other."
- "Good puzzles. Good company."
- The messaging should be **inviting, not preachy or anti-internet**. Don't shame solo players; a full, satisfying offline Solo experience remains essential.

**Priority:** This is approved design philosophy, not an instruction to stop current Work-chat tasks or change multiplayer transport. The implementation priority stays Sudoku stability, visual quality, real two-device QA, and Play release readiness. It may shape future menu wording, onboarding, store copy, product decisions, and features without adding complexity for its own sake.

## 1C. Nuance: technology-positive, people-first (2026-11-03)

**Owner clarification:** GridMates is **not anti-internet, anti-technology or anti-online-community**. The owner values those things; many other games already emphasize broad online discovery and social ecosystems. GridMates deliberately offers a *complementary* experience that helps people spend meaningful moments with people they already know. Do not frame the vision as a criticism of internet friendship or a requirement that everyone be physically together.

**Scope flexibility:** Optional internet-hosted multiplayer, remote co-op between family/friends, cloud saves or other online services may make sense much later. **Nothing here forbids those additions.** They are simply not the goal, a launch requirement or a current commitment. The owner explicitly asked **not to lock out future possibilities**.

**Candidate brand tagline the owner likes:** **“GridMates — Good puzzles. Good company.”** Favorited by the owner as of 2026-11-03; **not yet a formally locked storefront tagline or final artwork approval**. It works for both enjoyable Solo and gentle social play; avoid making the game feel exclusive to partnered players.

### Possible lightweight global visibility — idea only, not a commitment

The owner raised a *tentative* possibility of a **broadcast/public global leaderboard** for friendly score comparison and a degree of profile/collectible show-off **without full online social gameplay**. They are *not sure they want it*, so **do not add a leaderboard, backend, or Play Games integration without a later decision**.

- **Google Play Games Services** offers hosted Android leaderboards with daily/weekly/all-time and public/social views; it may avoid a custom leaderboard server. See [Google Play Console leaderboards](https://support.google.com/googleplay/android-developer/answer/2990418) and [Android leaderboard overview](https://developer.android.com/games/pgs/leaderboards).
- Standard public documentation does not clearly establish an unconditional $0/usage guarantee. Verify current quotas, limits, service costs/terms and implementation effort in the owner's eventual Play Console setup before making budget promises.
- The native Play Games leaderboard shows **Play Games identity and supported ranking data**, **not automatically GridMates' custom avatar frames, badges, unlocked cosmetics or entire player profile**. A fully custom online showcase would require additional design, permissions, and potentially a backend. **Optional locally generated shareable achievement/profile cards** could offer a no-server way to show off.
- If pursued, preserve **offline Solo + LAN multiplayer without a Google sign-in requirement**; make public score sharing transparent and user-controlled, and show respectful privacy/settings language.
- Do not base a global competition on the current speculative scoring rubric. Finish scoring logic, assistance fairness, difficulty comparability, basic tamper resistance and real-world calibration before promising fair rankings.
- Avoid leaderboard visibility becoming the main interaction, turning a calm puzzle into stressful competition, or replacing local co-op/versus.

**Current decision:** Preserve as an *optional future research note*. Priorities remain core puzzle experience, local game reliability, helpful hints, profile enjoyment, privacy and Google Play release testing.

## 1D. Shareable accomplishments — exploring a bridge beyond nearby play (2026-11-03)

The owner is **enthusiastic about opt-in shareable GridMates cards** for personal profiles, earned cosmetics, completed Solo/Daily puzzles, and cooperative/versus match results. The intention is to share little moments with friends/family **even when not physically together**, through existing messages and social media, while also generating organic word-of-mouth awareness of the game.

Use existing Android Sharesheet and on-device image generation: **no required GridMates public accounts, servers, social network or automatic posting**. Keep stats honest and give players control of names and partner details; distinguish assisted/unassisted results and avoid Daily solution spoilers. A future verified Play Store download link on the card/share text is possible **only after the app is actually published**. A global online leaderboard remains a separate undecided idea.

**Status:** strongly liked concept, **not yet final feature scope or committed launch milestone**. See [Shareable Cards Concept](GRIDMATES_SHAREABLE_CARDS_CONCEPT.md) for phased exploration and Work-chat guardrails.

## 2. Shared GridMates identity

**Across the series:**
- A coherent GridMates wordmark/launcher-icon family, visual language, calm modern mobile polish, legible typography, accessible input and intuitive navigation.
- Consistent naming scheme `GridMates: <Puzzle>`; every store listing and title must still clearly indicate the actual game.
- Reusable high-level UX where it makes sense: home, modes, local player profile/personalization, room joining, chat, results, onboarding, accessibility, persistence and update reliability.
- Offline-first solo experiences and optional multiplayer **where supported**, with scope clearly stated per game.
- Honest claims: no implied cloud accounts, internet matchmaking, synchronized global profiles or cross-game progress until actually implemented and tested.
- Each game must be enjoyable on its own and respect the classic puzzle's rules.

**Separate per game:**
- Core puzzle engine, difficulty/rule logic, terminology, controls, board design, progression and distinctive personality.
- App identity, saves/schema, store listing, signing, version history, independent QA and separate source repository as appropriate.
- Cross-game player accounts/friends/inventory are *possible later concepts*, not v1 prerequisites or approved commitments.

## 3. Game sequence and feature design

### Game 1 — GridMates: Sudoku (active)
Native Android Kotlin/Jetpack Compose project, currently stored in `PlayerEden/SudokuTogether`. Existing Solo, Daily, local-Wi-Fi co-op and versus development continues under the **existing release roadmap and QA gates**. Keep classic Sudoku familiar and high quality. Optional hidden-information Split Clues remains **proposed and blocked** behind real two-phone acceptance testing.

Do not divert core development into series framework refactoring or a premature game-platform migration.

### Game 2 — GridMates: Crossword (next planned)
Separate crossword game that builds on lessons/components from Sudoku **after** the Sudoku milestone. The experience should feel crossword-native, not Sudoku with different text.
Initial **ideas to explore, not approved features**:
- Offline Solo with solid clue content, accessible keyboard/clue navigation, reasonable difficulty, resume and local history.
- Two-phone co-op on a shared crossword, optionally with separate across/down clues or complementary clue sets.
- Versus with private boards and a fair shared puzzle, if it proves genuinely fun.
- Appropriate content sourcing/licensing, clue quality, localization and word validation.
- Optional continuity of room/chat/result and cosmetic design patterns where reuse reduces work **without** compromising puzzle-specific needs.

### Later — GridMates: Minesweeper (candidate only)
Possible solo plus careful cooperative reveal/flag play, with host-only hidden mines and appropriate difficulty/fairness. This game is **not yet selected for production**.

## 4. Architecture: preserve the option, do not start the extraction yet

A reusable native Android puzzle foundation was already considered in `docs/REUSABLE_ANDROID_PUZZLE_FOUNDATION.md` and [issue #16](https://github.com/PlayerEden/SudokuTogether/issues/16). **That work stays paused until Sudoku is sound.** Before extracting abstractions, prove real reuse in the Crossword project; don't break current saves, protocols, tests or UI for hypothetical future games.

**Important:** This is a *brand and roadmap* commitment, **not** a claim that GridMates accounts, shared multiplayer servers, cross-game friends, sync or framework are implemented.

## 5. Safe rebrand checklist (for the implementation chat)

Naming choice is approved; **no app code or repository rename is authorized by this document alone**. Fold the actual rename into the existing v0.11 brand/Play-readiness work:
1. Check Google Play / other app-store name confusion, trademark risk and public availability **before promotional publication**. Creative approval is not legal clearance.
2. Choose final wordmark, icon family, color/type system, launch screen and store presentation for **GridMates: Sudoku**. Retain accessible gameplay visuals.
3. Audit all user-visible “Sudoku Together” strings, app display title, share text, notifications, in-app Help/about, store copy and screenshots.
4. Explicitly decide whether to rename the GitHub repository. If renamed, keep the same repository and historical commits; update internal documentation/automation references only when ready.
5. **Do not blindly change `applicationId` / package names** or signing identity: Android updates and installed player data may depend on them. Plan and validate upgrade migration, especially after QA/production IDs diverge.
6. Update docs, onboarding and test snapshots alongside branded UI changes; record tests and retain known-green rollback refs.
7. Do not claim a GridMates Google Play release until signed AAB, privacy, test tracks and device QA are actually verified.

## 6. Coordination rule across conversations

- **This planning conversation:** names, gameplay concepts, brand direction, series ideas, future cross-game patterns.
- **Implementation/work conversation:** code, source commits, QA, APK/AAB and release execution.
- **GitHub:** durable approved decisions and explicit proposed-vs-built distinctions, so context does not depend on a particular chat remaining open.

If the owner changes a design decision, update this document and link the implementation work to the revised decision. Preserve prior milestones/checkpoints and do not represent future plans as features already shipped.
