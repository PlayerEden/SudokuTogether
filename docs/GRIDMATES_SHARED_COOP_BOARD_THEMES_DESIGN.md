# GridMates: Sudoku — Shared Co-op Board Themes & Personal Number Styles

**Owner idea:** November 7, 2026  
**Status** — v0.11F implements automatic theme selection in Co-op. Both phones use the current room appearances. The selected theme stays visible during a temporary disconnect and can change when a player changes their equipped appearance. [Implementation and QA](V0.11F_LOCAL_MULTIPLAYER_QA.md)

**Reference game source:** draft PR #28, branch [feat/gridmates-nearby-animals](https://github.com/PlayerEden/SudokuTogether/tree/feat/gridmates-nearby-animals) (v0.11C). Start any future implementation from the newest integrated source, **not from the earlier development-branch Android snapshot**.  
**Related:** [Collections, Daily rewards and fonts](GRIDMATES_COLLECTIONS_DAILY_REWARDS_AND_FONTS_DESIGN.md), [GridMates Brand & Series](GRIDMATES_BRAND_AND_SERIES.md), [Project Handoff](PROJECT_HANDOFF.md).

## 1. Why this feature exists

In local **Co-op**, if one player has unlocked/equipped a board theme and the other is using basic Classic, **both phones should display the unlocked board theme for their *shared puzzle*.** When both use different non-default board themes, the owner proposes that the **higher-tier board theme wins** (using a deliberate tier/order definition). This is a small, rewarding way to show off cosmetics to the person you're playing beside, while enhancing the feeling of sharing one puzzle.

**Crucial exception:** **each player's own entered numbers keep their personal number ink/color and font**, so the shared board should never copy a single player's full Appearance and erase the other player's individual style. The originating author of a placed value remains visibly recognizable.

This is about sharing the **appearance of the common game board** temporarily—not transferring permanent cosmetic ownership, granting entitlements or taking away anyone's own profile customization. It fits the "Good puzzles. Good company." vision.

## 2. What v0.11C actually does (verified source)

- \`Appearance\` has independent fields \`ink\`, \`board\`, \`font\`, \`emblem\`, \`portrait\` in \`core/.../Cosmetics.kt\`.
- Current board IDs in \`Cosmetics.boards\`: \`classic\` (starter), \`linen\`, \`midnight\`, \`circuit\`. Their unlock requirements differ; **no formal “tier” or priority property exists yet**.
- The LAN state already conveys \`Snapshot.hostAppearance\` and \`Snapshot.guestAppearance\`. Host/guest cosmetic changes already update session appearance; this makes a shared board selection based on *both* choices plausible without a new online service.
- \`app/.../PlayScreen.kt\` currently renders the board frame with \`boardFillFor(state.appearance.board)\`; \`SudokuBoard(... localStyle=state.appearance ...)\` uses \`localStyle.board\` to choose dark/light board background, grid borders, selection/peer/conflict highlighting and foreground contrast. **Thus the same Co-op puzzle may currently look different on the two phones.**
- For each non-given **Co-op** cell, rendering already picks the authored \`Appearance\` based on \`cell.owner\` (HOST or GUEST), then uses \`fontFor(authored.font)\` and \`inkFor(authored.ink, darkBoard)\`. This is the behavior to **preserve**, with \`darkBoard\` derived from the *effective shared board*.
- Starting givens have neutral font/ink; the optional \`uniformNumbers\` setting can override authors' individual number looks. **Respect this accessibility/UX toggle**; don't call authored colors universal when a player has specifically selected a uniform-number display.
- \`GameViewModel.chooseCosmetic\` persists a player's own equipped board and transmits an appearance change to the connected room. The player’s actual equipped board selection should remain unchanged even when temporarily viewing a partner's theme.
- Existing cosmetic code validates *known* theme IDs and unlock conditions for the local player. A temporarily shared theme **does not become "owned" by the guest**; avoid running the guest's \`Cosmetics.allowed\` check against a host's valid shared board display. Maintain the existing controlled allowlist and safe fallback on unknown IDs.

Source locations (current reference):
- [Cosmetics.kt](https://github.com/PlayerEden/SudokuTogether/blob/feat/gridmates-nearby-animals/core/src/main/kotlin/com/playereden/sudokutogether/core/Cosmetics.kt)
- [PlayScreen.kt](https://github.com/PlayerEden/SudokuTogether/blob/feat/gridmates-nearby-animals/app/src/main/java/com/playereden/sudokutogether/PlayScreen.kt)
- [GameSession.kt](https://github.com/PlayerEden/SudokuTogether/blob/feat/gridmates-nearby-animals/core/src/main/kotlin/com/playereden/sudokutogether/core/GameSession.kt)

## 3. Proposed behavior: Automatic Shared Board (Co-op only at first)

### Selection rule (owner's preferred direction)

1. If both are using **Classic**, show Classic for both.
2. If one has **Classic** and the other has a valid non-default equipped board, **use the non-default board for both**.
3. If **both** have different non-default board themes, use the one with the **highest explicit shared-theme rank**; don't infer rank from a mix of Solo vs Versus unlock requirements.
4. If tied for rank, choose the **host's** equipped board for deterministic symmetry; never depend on device join order or local player viewpoint.
5. If an incoming theme is unknown/unavailable on one client, fall back gracefully to Classic or another known compatible theme and **never crash or render unreadable numbers**. Display version mismatch when necessary; we should favor keeping room functionality usable.

**Provisional rank example (for discussion, not official rarity):**

| Board ID | Public name | Proposed technical preference rank |
|---|---|---:|
| \`classic\` | Classic | 0 |
| \`linen\` | Linen | 1 |
| \`midnight\` | Midnight | 2 |
| \`circuit\` | Circuit | 3 |

**Warning:** Rank is a *tie-breaking theme display preference*, **not** a claim that Circuit is officially rarer/stronger than Midnight, nor a reason to modify how existing cosmetics unlock. Future Ocean/Bug-themed board designs can get curated ranks after player approval. Consider whether a friendly **“Select shared board”** host/room option would be better once we have many themes; automatic highest rank is the default concept now.

### Illustrative cases

| Host equipped | Guest equipped | Co-op board seen by both | Number styling |
|---|---|---|---|
| Classic | Classic | Classic | Each player’s ink/font by cell author |
| Classic | Midnight | Midnight | Host's number style for host moves; guest's for guest moves |
| Linen | Classic | Linen | Each player’s own |
| Midnight | Circuit | Circuit, under proposed rank | Each player’s own |
| Circuit | Linen | Circuit | Each player’s own |
| Unknown/new ID | Classic | Safe known fallback | No crash or invalid font/color |
| Disconnected guest | Host keeps playing/resumes when allowed | **Policy to test:** don't visibly flicker/reset theme in the middle of a saved game | Ownership unchanged |

### When should selection happen?

**Recommendation:** when two players are admitted to the room and again at a **new puzzle / rematch**. The current game may show the host's theme while waiting for a guest, then adopt the shared theme at join. Once a shared puzzle is underway, ideally **keep the board skin stable** to avoid distracting flashes if a player changes their equipped appearance mid-match. A later round can pick up new cosmetics.

This "round-stable" variant can require an explicit canonical **effectiveBoardId** on the host-authoritative round snapshot or a carefully specified calculation with a freeze event. The Work chat should weigh this against a **simpler initial implementation**: compute the same deterministic shared ID from host/guest appearance on both devices and update on legitimate cosmetic changes. If shipping the simple variant first, document that the board can change mid-round and verify no abrupt readability problems. **Do not invent a local-only freeze that leaves host and guest disagreeing.**

### Where this does and doesn't apply

- **Co-op:** automatic shared-board theme is the primary owner request.
- **Solo/Daily:** continue to use the local player's equipped board. No change.
- **Versus:** for now, each player retains their own board/background. Their number style is personal too. This avoids changing race fairness/visibility without a separate decision; owner said "co-op or whatever," so Work chat may propose whether future Versus should share a match arena theme.
- **Room host vs guest:** hosting confers connection authority, but **not automatic ownership of the most interesting theme**; either player's board can become the shared appearance.
- **My Collection:** the borrowed board shows during Co-op only. It does not appear as earned in the guest's inventory, stats or gift-code redemptions.

## 4. UI copy and discoverability

Keep this a lightweight delight, not a multi-step setup requirement.

**In Co-op:** small theme caption near the room's board or details, e.g.:

> **Shared board: Midnight** · brought by your partner

Or, if your own theme wins:

> **Shared board: Circuit** · your theme

If both own the same board:

> **Shared board: Linen**

No need for extra badges, giant animations or promotional prompts. Consider a one-time, unobtrusive explanation in Collection:

> "Co-op uses the highest-ranked equipped board theme from either player. Your own number styles stay yours."

**Important design trade-off:** a dark shared theme can be uncomfortable for someone with a visual preference or light sensitivity. Long-term, provide **“Use my board appearance” / “Follow shared board”** as a **local display override**, while keeping the **shared theme selection** for the room unchanged. Such a setting should not modify the other person's board or unlock any theme. Also preserve \`uniformNumbers\` and high contrast options. Decide whether that override is necessary in first pass based on real screenshots and accessibility QA.

**Do not** claim every theme has a meaningful “tier” if no official display rank is configured. Avoid labeling the shared player "higher level" because of cosmetics.

## 5. Suggested implementation boundaries

### Phase A — Smallest reviewable source change

- Extract a **pure** \`resolveSharedCoopBoard(hostBoardId, guestBoardId, guestPresent)\` or \`sharedBoardId(snapshot, role)\` function in the appropriate shared module. Validate IDs against a stable curated rank table; deterministic ties; Classic fallback.
- Apply the effective ID consistently for **both** the board frame and the SudokuBoard's board background/grid/highlight/darkBoard contrast. Do **not** set \`state.appearance.board = resolvedId\`; that would falsely equip a theme for a player who hasn't unlocked it.
- Keep individual \`Appearance.ink\` + \`Appearance.font\` tied to \`cell.owner\`, and preserve \`uniformNumbers\` user option, basic neutral givens, correct contrast on light/dark boards.
- Compute from host/guest snapshot appearances, not the current device's local \`state.appearance.board\`, after both participants have valid state. Host and guest should resolve the same shared board.
- Confirm whether \`GameSession.snapshotForGuest()\` keeps both player appearances (it currently does) so no full-snapshot privacy change is needed. If a canonical round-stable theme ID is introduced, version/migrate with care.
- Never send font files, color arbitrary values or untrusted paths over the LAN. Keep only allowlisted cosmetic IDs in protocol.

### Phase B — Round stability and polish

- Decide how long the selected shared board lasts (join/current round/rematch). Add a short theme explanation in Room Details only if it improves understanding.
- Optional local accessibility override if needed; should not send a private visual preference to the other device.
- For future theme packs, use one source of truth for \`id\`, name, asset identity, rank and contrast theme; preserve IDs across saves and updates.

## 6. Verification / QA checklist for Work

- [ ] Same host/guest theme selection results in same effective Co-op board ID **on both phones** regardless of role.
- [ ] Classic + Midnight → both Midnight; Linen + Classic → both Linen.
- [ ] Midnight + Circuit → both selected higher-rank theme (with owner-approved rank table).
- [ ] If both have the same rank, tie-break deterministically, even after reconnect.
- [ ] Both players' **own entered numbers** retain the author's font/ink and distinctive appearance on the shared board; neutral givens stay neutral.
- [ ] Notes/pencil marks and \`uniformNumbers\` render as intended under darker themes.
- [ ] Grid, selection, partner highlight, conflicts and notes remain readable and correct when the other player brought the dark board.
- [ ] Switching cosmetics mid-game doesn't corrupt notes, change authored cell ownership, desync board IDs or cause unacceptable flicker.
- [ ] On guest disconnect/rejoin, paused room, host resume, replacement guest and next puzzle, selection stays consistent with documented policy.
- [ ] Old saved \`Appearance\` objects, existing Woodland portraits, board unlocks and preferences remain unchanged.
- [ ] No temporary shared theme is permanently awarded to a player; locked board collection state remains locked.
- [ ] No regression to LAN admission, optional room code, chat, host/guest snapshots, Android build signing or save integrity.
- [ ] Test real two-phone Co-op + at least one older protocol/version mismatch, narrow screen/landscape, dark/light, large fonts and TalkBack.
- [ ] Solo/Daily remain personal-theme-only; Versus unchanged unless separately approved.

## 7. Work-chat coordination

**What is now decided:** The owner **wants shared Co-op board appearance** based on one player having a theme or the **higher-ranked equipped** board, while player-entered **number cosmetics stay individualized**. A pleasant product improvement without changing puzzle rules.

**What remains proposed:** exact rank order, mid-round updates vs round-stable selection, local accessibility overrides and whether the same idea later applies to Versus.

This design note is **not authorization to implement it ahead of more urgent QA/release items**. Read alongside latest Work-chat handoff; propose the smallest isolated development slice, verify with two devices, and preserve stacked PR checkpoints and rollback.
