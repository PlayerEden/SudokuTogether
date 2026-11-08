# GridMates: Sudoku — Versus Post-Match Puzzle Comparison

**Proposed:** November 6, 2026  
**Status:** User-suggested feature idea; **design/review only, not implemented or an approved release gate**.  
**Relevant game source:** \`feat/gridmates-nearby-animals\` (draft PR #28, v0.11C), stacked on prior v0.11 source. Keep current checkpoint, protocol, saves and QA intact.  
**Related:** [Share Result UX](GRIDMATES_SHARE_RESULTS_UX_DESIGN.md), [Shareable Cards](GRIDMATES_SHAREABLE_CARDS_CONCEPT.md), [Sudoku Research Study](SUDOKU_DEEP_RESEARCH_AND_GAME_DESIGN_REFERENCE.md), [Brand Vision](GRIDMATES_BRAND_AND_SERIES.md).

## Owner idea — intent

After two nearby players compete in **Versus** on independent instances of the **same starting Sudoku puzzle**, offer a friendly **Compare Puzzles** option on the completed match results screen. Instead of only learning who won, friends can see how far they each progressed, which areas they tackled, and eventually discuss different approaches. This matches **“Good puzzles. Good company.”**

Comparison is **post-match only**; don't reveal a rival's actual answers or notes during the race. Keep the result, Next Race, Home and optional Share Result controls easy to reach. Don't turn a friendly challenge into a stressful leaderboard.

## Verified facts from v0.11C source (not speculation)

- \`GameSession.create(... mode=VERSUS)\` creates independent \`Snapshot.cells\` for the host and \`Snapshot.guestCells\` for the guest, both based on the **same immutable puzzle.clues**.
- \`GameSession.submit\` validates each player's independent edits; **the first valid completed solution wins** and marks the overall session completed, after which further edits are rejected. The rival board is normally incomplete at race end.
- \`GameSession.view\` currently computes **hostProgress/guestProgress as filled cell counts, including starting givens**. The UI subtracts the number of givens when displaying “entries.” **A filled entry does not establish that it is correct**.
- Critical existing privacy boundary: \`snapshotForGuest()\` substitutes the guest's own cells into the visible \`cells\` field, removes \`guestCells\`, and restricts Undo information. \`VersusTest.kt\` explicitly tests that **opponent's answer cells never reach the guest in live socket snapshots/outcomes**. This must remain true **during the match**.
- \`PlayScreen.kt\` currently shows a \`PuzzleFinishCard\` (Victory / Race finished) and a progress strip (“You N · Rival M entries”), but **does not show two full grids side by side**.
- \`GameViewModel.recordMultiplayer\` saves WIN/LOSS, puzzle ID, difficulty and a wall-clock elapsed estimate; it currently records \`moves=0\` for multiplayer. **Do not call this a real per-player move total.** It also doesn't persist two full boards, chronological move timings or a reliable winner-vs-loser step-by-step replay within \`GameRecord\`.
- The host-authoritative saved session contains more state than the per-guest network view. Hosting itself is a trust role; don't promise cryptographic secrecy against a malicious host. Scope the UX to not exposing more than the user consciously opts to share.

## Design recommendation: an optional comparison screen

Suggested results flow:

\`\`\`text
         🏆 Match Complete
           You won / Rival won
          Same puzzle • Hard

  [ Next Race ]     [ Compare Puzzles ]
  [ Share Result ]  [ Home / Records ]

Compare Puzzles →
  Side-by-side mini 9×9 grids:
    YOU                         FRIEND
    Completed                   31/39 of empty cells filled
    full correct board          frozen board at finish

  [Highlights: starting clues / your entries / empty squares]
  [Optional: reveal actual opponent entries after mutual consent]
  [Back to results]
\`\`\`

All counts above are examples and must never be displayed as actual values until sourced from the real session. Both grids show the **same initial clue arrangement**; label **completed vs frozen at match finish** rather than portraying both as finished.

### Scope tier A — cheap, no extra data exposure

Show **progress summaries** that both clients already receive: winner/loser, difficulty, same puzzle, number of non-given filled squares, and a 9×9 *progress heatmap* for the player's **own** board. Don't create a fake board for the opponent if only aggregate progress exists. If individual opponent cell locations aren't already available, a progress bar or filled square count is the honest alternative.

Potential UI: own grid on one side, rival progress bar/stat summary on the other. This is not yet a true two-grid comparison but can be a safe first iteration.

### Scope tier B — true side-by-side boards post-match (recommended eventual feature)

After the race is **authoritatively completed** and both participants **explicitly agree to share their final boards**, reveal:
- Your actual board with given clues visually distinct from your placements.
- Opponent's board **as it was when the winner finished**; show empty squares as empty, not fabricated.
- Optionally cells where both entered the same final digit, or differing placements. **This is not evidence of solving order.**
- A small understandable legend: givens, player entries, unfinished cells, and *optional* errors after the player opts into correctness review.

**Privacy guardrail:** do not simply add \`guestCells\` to existing Versus snapshots or send both participants' live answers by default. Introduce a **new post-match payload** or explicitly authorized read-only comparison snapshot, with separate consent from both users. If the guest disconnects before granting permission, fall back gracefully to aggregate counts or own-board-only; **no silent backfill of private answers**. Reconnection and completed-state validation must be handled; beware a new room/epoch arriving before consent is complete. A network-enabled player should not unknowingly share their notes or errors.

**Simpler offline alternative:** since both friends are often sitting together, they can compare by looking at each other's phones, with an optional “Show my finished board” mode on their own phone. No protocol change or partner-data exchange required. Work chat should assess whether this is enough for v1.

### Scope tier C — richer insight / replay (not v1)

Possible later:
- Tap a digit/box to compare which area each player focused on, **only if real per-cell snapshots with timestamps have been captured and consented**.
- “How we approached it”: a controlled replay of accepted placements/corrections/notes in timestamp order, if history is preserved and complete.
- Time to first correct placement, total notes/undos, accuracy or best streak—but **only after implementing authoritative per-player event metrics** with a clear definition and storage policy.
- Share a friendly comparison card. Exclude private room codes, hidden answers and partner names unless consensual, per [Shareable Cards](GRIDMATES_SHAREABLE_CARDS_CONCEPT.md).

Current \`SavedSession.history\` is bounded to **256** edits, \`recent\` to **128** accepted operations, and records do not capture timestamps per move or chronological full boards. Do not reconstruct and present an invented “full replay” from these partial logs.

## Fairness / player experience

- **Same clues, independent solutions:** this is how current Versus works, so comparison is apples-to-apples about the same puzzle.
- **Speed-first game:** winner finishes before opponent; losing player should feel invited to discuss, not shamed by an accuracy grade or punitive stats.
- Progress counts include **entered numbers even if wrong**, and don't automatically mean “correct answers.” Label honestly as “entries” or “squares filled.”
- If showing “correct vs incorrect,” this requires deliberate solution checking and a user-visible reveal decision. After match, it can be fun to review mistakes; don't force spoil a player who wants to keep solving later.
- **Do not claim faster completion time for the unfinished player.** Only the winner solved; the other player was interrupted when the winner completed.
- Be explicit whether any “race time” is total race duration, winner's finish duration, or elapsed view from local device; use the existing times carefully and avoid precision claims unsupported by recorded event timestamps.
- Short screen on narrow portrait/large fonts should allow stacking grids vertically; 9×9 digits may become illegible if forced side by side. Tap-to-enlarge mini-boards or a switcher can work better on a phone.
- Use accessible patterns/borders in addition to color. The whole compare experience is optional and never blocks Next Race.

## Minimal implementation plan (Work-chat candidate)

1. **Read source** in PR #28 (and current successor branch if newer) and sketch a results button placement using actual phone screenshots. Do not assume published features based on docs.
2. Make an offline, read-only **own-grid vs aggregate rival progress** screen; no protocol/save changes, minimal QA.
3. Review explicit opt-in finished-board reveal with owner; add a tiny **post-match-only** read-only protocol message with mutual consent and host epoch validation if full two-board compare is selected.
4. Validate no opponent answers leak while \`completed=false\`. Add a failing-before/passing-after network test and lifecycle/disconnect/role coverage for host/guest both directions.
5. Verify correct winner/result/board at finish, own/offline view, reconnection, immediate new race, saved-state durability, and two physical phones. Do not reset signing or persistent match progress.
6. Only after real match summaries exist consider share-card rendering and richer timeline metrics.

## Work-chat QA checklist

- [ ] Same puzzle on both phones; independent boards and notes in active versus.
- [ ] Winner stops game at first truly completed valid Sudoku, loser retains frozen partial state; no fabricated winner time for loser.
- [ ] No opponent individual answers/progress map in live guest snapshots or outcomes.
- [ ] Result screen offers clearly optional Compare Puzzles and Next Race still works.
- [ ] Compare UI reports known entry counts accurately, never treats entries as proven correct.
- [ ] No accidental leak of private notes, names, chat, room code, address or token.
- [ ] Explicit post-match consent before transmitting the rival's final per-cell data.
- [ ] Reconnect, peer decline/disconnect, stale epoch, new race during comparison, background/resume handled.
- [ ] API26, current target API, narrow phones, landscape, 200% font, TalkBack and physical two-phone test.
- [ ] Backward compatible saves/protocol if possible; if protocol changes, version negotiation and safe rejection for older app builds.
- [ ] No score/accuracy/history claims unless actual metadata are stored and verified.

**Decision state:** An attractive, low-pressure feature idea from the owner. This document **does not authorize immediate coding, protocol changes or new production release**. Preserve the existing game and staged work priorities. 
