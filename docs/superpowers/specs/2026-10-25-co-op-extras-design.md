# Sudoku Together v0.2: help, chat, contributions, and Split Clues

Status: proposed design, awaiting review and approval. These features are not in the delivered 0.1.0 APK.

Planning review: a fresh Android/game builder check found no important unanswered planning questions. User approval of the written design is still pending.

## Intent and agreed direction

Keep familiar Sudoku enjoyable for two people on separate Android phones. The user likes adding How to Play, in-game chat, friendly contribution results, and an optional cooperation twist. They specified that Split Clues shows the locations of the other player's starting clues without showing their numbers, and is selected before creating a room.

Split the **starting clues**, not all 81 answers. For example, a board with 36 starting clues gives each person 18 visible clue numbers. Both phones show all 36 clue locations and share the same 45 editable squares.

The user has not separately chosen the defaults and detailed rules below. These are builder recommendations for this design review. This extends the approved initial design; existing Wi-Fi hosting, foreground play, shared notes, safe own undo, and signing identity remain.

## Approaches

| Approach | Benefit | Tradeoff | Decision |
| --- | --- | --- | --- |
| Polish classic co-op only | Adds guidance and communication with minimal rules to learn. | Does not yet give each player unique information. | Included as the standard mode. |
| Classic plus optional Split Clues | Familiar rules remain available; unique clue information encourages communication. | Requires different player views, careful feedback rules, and more testing. | User selected this direction and a pre-room toggle. |
| Make every game use private clues or forced turns | Guarantees an added constraint in every game. | Removes the relaxed standard option the user wants. | Not selected. |

## Room options and scope

Before hosting a new room, offer **Split Clues** (default off) and **Contribution points** (default on). Explain each briefly, including that points appear after completion. Both options and the mode name are visible to the joining player before joining. Manual joining must show the same information before admitting the guest or entering play; the room code still controls admission. No gameplay settings change mid-puzzle. New Puzzle keeps the room's options and clearly confirms them before replacing progress. Starting a new room allows different choices. Resume restores saved options.

How to Play and chat are available in both modes. Standard remains a complete, simultaneous shared Sudoku experience. No lives, time limits, compulsory turns, internet service, account, leaderboard, difficulty claim, or deduction-based ranking is added. No separate competitive race mode is included in this update.

## Split Clues

- Every starting clue belongs to host or guest for that puzzle. Each player sees their own clue digits. A partner-owned clue shows a distinctive neutral marker and the description **Partner clue**, including its row and column. Use text/symbols as well as color.
- All starting clues stay locked on both phones. A hidden clue is never treated as an editable blank, counted as a missing answer, overwritten, erased, or included in number-entry scoring. Selecting it permits pointing it out or discussing it, not entering a digit or note there.
- Both players see every accepted entered number and pencil mark in the ordinary editable squares. Existing move and own-undo behavior continues.
- The recommended first version keeps private clue digits masked until completion. Players tell each other through chat or speech; pointing at a cell sends its location only. There is no automatic clue reveal or separate Reveal button in this scope. On completion, both phones show the full solved grid.
- Allocate clues evenly (difference at most one), distribute ownership through the 3 by 3 boxes, and ensure the complete puzzle remains uniquely solvable. Validate that each player's visible starting-clue subset has at least two solutions, so neither initial view alone defines a unique solution. Use a bounded allocation search; report a recoverable preparation failure rather than silently changing mode if it cannot find a valid split.
- Ownership stays fixed across disconnects, host restart, and rejoining. A new puzzle receives a new allocation. Replacing the guest preserves guest-side clue ownership rather than revealing the host's half.
- Before a guest has joined, the host sees only the host half and a clear waiting state. Split Clues is presented as a two-person mode. After the guest has joined, normal absence/disconnect pausing applies.

### Feedback must respect what a player can see

Duplicate highlights compare numbers visible to that player: their own clue digits and all shared entered numbers. A hidden clue must not leak through duplicate coloring, candidate pruning, same-number highlighting, accessibility descriptions, error messages, counters, undo metadata, or immediate correctness/score feedback. Therefore the two screens may legitimately highlight different conflicts. How to Play explains this. Host completion still uses the complete canonical puzzle and requires all starting clues to be preserved.

The host necessarily stores the complete puzzle to run the game. This remains trusted home-LAN play, not an anti-cheat or encrypted private-information system. Nevertheless, guest-visible messages and its saved mirror must contain only that guest's allowed view until completion. Masking digits just in the drawing code is insufficient.

## How to Play

Provide a clearly named button on the home screen and during play. Cover the row/column/box rules with a small original example; selecting squares; fixed clues; number entry; pencil marks; erase; own-move undo; duplicate highlights versus proof of correctness; partner selections; joining and reconnecting; chat and cell references; Split Clues; and contribution scoring. Explain that notes are shared and require manual cleanup in other cells. Include the existing same-Wi-Fi and keep-both-apps-visible guidance.

Help can be dismissed back to the exact puzzle state. It is an in-app overlay, so opening it does not itself leave the activity or announce the player as absent. Incoming confirmed state continues to update while reading. Layout must scroll at large text sizes without trapping controls.

## Chat and pointing

A collapsible Chat action opens messages without permanently taking space from the board. Show an unread indicator and identify authors. Text is ordinary local room chat, not a message to external accounts. A selected-cell reference such as R4 C7 can accompany text. **Look at this square** sends a temporary partner marker and optional location-only chat reference, including for hidden clues.

Recommended bounds: 280 Unicode code points per text message, last 50 accepted messages per puzzle, sanitized control characters, no attachments or rich HTML. Enforce equivalent limits on the host and client, plus bounded send rates and queues. An empty message needs either text or a valid cell reference. The host authenticates the sender from the connection, orders accepted messages, deduplicates IDs, durably saves before acknowledgment, and shares the accepted event with both participants. Do not accept a claimed author from the payload.

An unconfirmed message is visibly pending and becomes failed after connection loss; it is not silently replayed. Rejoining fetches the bounded confirmed history. Chat remains usable while both links exist even if a puzzle is paused, and after completion. It never overrides board-edit pause rules. Starting a new puzzle clears the prior transcript. Replacing a partner clears the transcript so a new guest does not inherit the previous conversation. No chat notification or background delivery promise is added.

## Contributions, points, and results

The completion screen leads with the shared achievement, final grid, and active time spent together. Optional points give friendly number-entry credit; they do not claim to measure who made the best deductions. No live points, per-entry correctness confirmation, mistake penalty, or score for tapping, chatting, pointing, or pencil-mark volume is shown.

For each initially editable square, the host privately records the first accepted value entry matching the puzzle's unique solution. At completion, award that contributor **10 points once for the square**. Clearing, overwriting, or re-entering the value cannot transfer or multiply that credit. Undoing the original credit-creating entry restores its prior credit state as part of the same safe undo; undoing later edits restores the corresponding prior state without creating a second award. Credit/provenance must survive longer than the existing bounded undo history. Do not send the credit ledger or per-entry correctness to either gameplay view before completion. Host-local gameplay UI obeys the same visibility rule.

With 36 clues, 45 editable squares produce 450 total points. Hidden starting clues earn no points. Count credited squares beside points; ties are valid. If points were off, omit individual scoring and retain the shared completion result. There is no special finishing-cell bonus or separate team score in this update.

Active team time counts only while both participants are connected, foreground-active, and the puzzle is unfinished. Waiting for the initial guest, pauses, disconnection, and time the host is shut down are excluded. Persist elapsed time without treating wall-clock jumps as play. This is a descriptive result, not a deadline.

To avoid assigning an earlier guest's work to a new name, replacing the partner disables individual scoring for that puzzle with an explicit explanation in the existing confirmation. Team completion remains available. A reconnect by the original guest preserves scoring.

## State, compatibility, and implementation constraints

Keep Kotlin/Compose and the existing native Wi-Fi transport. Separate the full host session from a validated per-player view. A view carries immutable clue locations/ownership, visible digits, shared editable cells/notes/revisions, mode, and completion status. Hidden given values are absent or masked consistently in both clue and cell representations. Apply projection to welcome/rejoin, state updates, move outcomes, duplicate/error responses, and guest saves. The host retains the full unique puzzle; a projected client view cannot use the old validator that demands complete starting clues and a unique visible solution.

Persist room options, clue ownership, first-correct contribution provenance and undo provenance, active elapsed time, chat history/deduplication, and existing confirmed board/rejoin state. Save acknowledged durable changes atomically and keep backup recovery. Keep temporary pointers and unread UI state separate from durable puzzle changes. A host failure must not produce a confirmed chat/credit change that was never saved.

Use a new protocol version with a clear same-build requirement. Discovery identifies compatibility and mode without advertising the code, private clues, chat, or credentials. Retain numeric-address fallback and Wi-Fi socket selection. Keep the existing frame/save limits; do not duplicate the transcript inside move outcomes or every board snapshot. Check maximum serialized sizes using worst-case Unicode messages.

Migrate valid v1 host saves into Standard with contribution scoring off for that existing puzzle, since their bounded history cannot establish complete first-correct provenance. Preserve the old board, notes, credential, and safe undo data. Legacy guest mirrors must remain recoverable for rejoining the migrated host. Do not reset or overwrite unreadable progress; retain the existing recoverable-error behavior. After installing the same new APK on both phones, old rooms can resume in Standard. New rooms/puzzles can select the new options.

Keep application ID and private signing identity, increment the Android version code, build locally, and leave Actions manual. The delivered 0.1.0 APK remains separately identified until a tested replacement is ready. Real two-phone Wi-Fi acceptance is still outstanding from v0.1 and must not be inferred from JVM or emulator tests.

## Verification and release criteria

Retain the existing behavior/socket tests. Add meaningful coverage for complementary clue allocation, individually ambiguous halves, hidden-cell immutability, both role projections and all message paths, feedback/accessibility masking, complete-board reveal, old-save migration, and persistent ownership after reconnect/restart. Test host/client and simultaneous operations in both modes.

Cover chat author binding, bounds, ordering, duplicate IDs, save-before-ack failures, failed pending messages, transcript recovery/reset, and combined maximum message size. Cover first-correct credit, later erase/retype by either player, undo restoration, more than 256 intervening edits, puzzle resets, replacement policy, scoring off, and no correctness leak through intermediate results. Test active-time pause/restart behavior with a controllable clock.

Android checks exercise help without losing edits, room-option visibility, clearly distinct hidden clues, number/notes/undo, chat with the software keyboard, unread behavior, pointing, result presentation, activity recreation, and reachable controls at larger text settings. Verify signed update installation where possible. Record which cases ran in emulators and which ran on actual phones. Playtest Split Clues with two people before claiming its cooperation feels enjoyable; mathematically valid halves alone do not establish that.

## Decisions left to the builder

Choose exact symbols, spacing, text, DTO/type names, efficient allocation strategy, timer checkpoints, clock representation, and rate-limit constants within these behavior/boundary requirements. Use the existing cream/green/gold visual system. Do not add unrelated modes, assets, services, or dependencies just to implement the extras. New product decisions that alter clue disclosure, scoring meaning, or the trusted-LAN scope come back to the user.
