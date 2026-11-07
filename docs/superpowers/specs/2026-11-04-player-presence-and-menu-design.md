# GridMates player presence and menu design

November 4, 2026. **Status: direction and clickable prototype for owner review; no native UI implementation or new APK.** Existing product baseline is v0.11C at `975e31a1e8cbfebad1fbbc7602b3340c357da7ef`.

November 7 reconciliation uses v0.11C's current handoff at `0fe17b4587e0f596b4ed74fd7e18ec267d282e34`. The intervening commits add design links. See the [playtest follow-up in the delivery plan](../../GRIDMATES_DELIVERY_PLAN.md#november-57-playtest-follow-up).

## Intent

The owner likes the Woodland icons and wants multiplayer to feel like playing with another person. They want an easy, coherent game interface, achievements/profile/collection organization, and player icons together. Keep the cream/forest/gold design, older-player readability, normal text scaling, short labels, existing game rules and earned progress.

## Approaches and current proposal

1. **Compact paired players and progress — recommended.** Put both portraits/names and two comparable progress tracks above the Sudoku board. It adds identifiable people and real opponent activity while preserving board space. The prototype explores this approach; the owner has authorized continuing the mockup, not approved every native detail.
2. **Opponent miniature board.** More physical visual activity, but takes more space and may reveal patterns in the opponent's solving. Defer; do not reveal their digits, notes or selected cell in Versus.
3. **A large character scene or animated race course.** Greater emphasis on spectacle, but competes with Sudoku and needs more assets/layout space. Defer while the basic interface is being resolved.

## Versus

- Permanent identity panel above the board, using actual equipped portraits/emblems; two separate tracks show entries filled out of the puzzle's initially empty cells. Starting clues do not count as a head start.
- Progress is not advertised as validated correct answers. First valid completed solution still wins.
- Connected, waiting and reconnecting are separate truthful states. Preserve current pause/admission/rejoin behavior. No fake thinking/activity state.
- Animate actual remote progress changes subtly, respecting reduced motion. Group occasional entry milestones rather than announcing every keystroke. Do not use continuous flashing or autoplay sound.
- Keep the main board/keypad generous. Never put critical identity or status behind the keypad. Long names must wrap or have a readable short presentation; do not squeeze the mode title.
- Chat stays attached to a player identity. Existing quick replies can appear as a small contextual message. The prototype's opponent activity is explicitly simulated by its outside controls, not a live connection.
- A match introduction and shared result/rematch layout are follow-ups once the permanent panel works. They must respect the real race-start timestamp and reconnect behavior.
- The [post-match comparison brief](https://github.com/PlayerEden/SudokuTogether/blob/1818cd54e2fc5c5eaecf37389295ae3d55b898c9/docs/GRIDMATES_VERSUS_POST_MATCH_COMPARISON_DESIGN.md) fits that result flow. Aggregate progress can ship first. A full final-board comparison must wait for a completed race and consent from both players, using a separate post-match payload.

## Navigation

Retain four bottom destinations: **Play / Profile / Collection / Settings**. Profile takes Records' place and owns name editing, achievement access, statistics and history. Top-level destinations use consistent headings without a redundant Back-to-home button; nested screens retain Back. The home avatar opens Profile, and changing an icon opens the same Collection browser from every entry point.

Collection has **Player icons / Boards / Numbers**. Put legacy basic emblems and Woodland portraits in one icon catalog, with **All / Basic / Woodland** family filters. Preserve collection/art-family identity. Choosing a basic emblem clears the animal portrait exactly as today; choosing an animal preserves the compatible underlying emblem. Never create separate identities or lose old unlocks to unify the UI.

Use compact, equal-size cards with Selected / Available / Locked states. Tapping any card previews it; a locked item shows its genuine requirement and progress; only an owned item offers **Use icon**. The equipped summary stays small; style preview expands on request. Group ink/font choices under Numbers. Do not make players traverse the whole animal catalog to reach fonts or achievements.

The [cosmetics playtest brief](https://github.com/PlayerEden/SudokuTogether/blob/1818cd54e2fc5c5eaecf37389295ae3d55b898c9/docs/GRIDMATES_COLLECTIONS_DAILY_REWARDS_AND_FONTS_DESIGN.md) adds Ocean and Bug portrait families and a cross-device numeral issue. Add new families through the same browser. Use the same bundled font mapping for the preview and game once the font fix is selected, with existing IDs and unlock conditions. Daily rewards can surface an earned item in this catalog once the reward model is chosen.

Profile shows one identity, a few meaningful statistics and a prominent achievement entrance/showcase. Keep detailed statistics and history one tap away. Separate Solo/Daily/Co-op/Versus and assisted/unassisted where meaningful. Preserve historical unknown values rather than inventing scores, dates or rankings. New profiles get a welcoming empty state. Zero-progress tracks remain visually empty, without a misleading terminal dot.

Settings holds preferences/help/about. Portable backup follows the separate [update/save preparation](../../GRIDMATES_UPDATE_AND_SAVE_PREPARATION.md); a stub export action must not be shipped.

The [co-op shared theme brief](https://github.com/PlayerEden/SudokuTogether/blob/1818cd54e2fc5c5eaecf37389295ae3d55b898c9/docs/GRIDMATES_SHARED_COOP_BOARD_THEMES_DESIGN.md) belongs in a separate native slice. Resolve the board theme for the common puzzle independently of each author's ink/font. Keep the technical theme priority separate from unlock rarity. The ranking and when to apply it need to be settled before changing round state.

## Prototype boundaries

`design/prototypes/player-presence.fragment.html` is a self-contained throwaway interaction prototype. It uses six existing Woodland portraits plus three basic example entries, not the complete production catalog. Sample game/achievement totals illustrate layout and are not a user's saved data. Play/Profile/Collection/Settings navigation, icon-family filters, locked-item preview, local equip/name/preferences and opponent connection/progress scenarios are inspectable. The board is illustrative; it is not a new Sudoku engine. Detailed Statistics/History/Achievements screens remain descriptive placeholders for the next native design step.

Prototype preview assets derive from the existing packaged portraits; no new generated art, unlock rules, remote service, monetization, protocol or save schema is introduced. A conversation preview is not installed Android evidence. Validate native text scaling, TalkBack, 320dp phones and short/landscape screens before adopting the layout.

## Generation opportunities after layout review

Achievement badge families first, then earned avatar frames/profile banners, small completion/waiting illustrations and clearly separated future portrait collections. Use code-native text, numbers, controls and progress indicators. Do not replace the approved Woodland portraits or add constant animated backgrounds. Keep original generation prompts/provenance and packaged-asset metadata as with the current collection.

## Native implementation acceptance

Use actual profile/record/appearance sources; preserve every existing emblem/portrait ID and entitlement. Render the full catalog through one browser, with all entry points agreeing. Use the real session's host/guest progress and connection state; never infer correctness or presence from a timer alone. Confirm board/keypad access, long-name/large-text and waiting/reconnect layouts, result rules, existing navigation, save preservation and original-APK upgrade continuity. The screenshot findings (stat rectangles, repeated rows, zero-track dots and uneven card heights) require native before/after checks, not an assertion that this prototype fixed them.
