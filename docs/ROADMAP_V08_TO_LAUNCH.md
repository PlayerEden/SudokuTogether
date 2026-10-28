# Sudoku Together — Product & Release Roadmap (v0.8 → Google Play)

Status: **active development plan**. Working title only; all target versions are milestones, not release promises. Source of truth: this document and the stacked draft PRs. Current reference baseline: v0.7 QA on branch `polish/v0.7-thumb-layout`.

## Product promise and boundaries

**Core loop:** Open → choose Solo / Co-op / Versus → select challenge → solve recognizable classic 9×9 Sudoku comfortably on an Android phone → get a satisfying result → play another puzzle without repetition or friction. Co-op is two Android devices on the **same Wi-Fi**, with optional distinct-information teamwork later. No web game, always-online login, advertisements, purchased advantages, or external multiplayer servers in initial release.

**Guiding rules:** Classic Sudoku stays valid; opt-in assistance; no misleading difficulty labels; never discard existing local saves; never leak hidden Split Clues over the LAN; test on actual phones; add no feature without a reason to play it.

## v0.8 — Puzzle depth and longevity

| Gate | Work | Acceptance criteria |
|---|---|---|
| 8A — generation foundation | Eliminate the 8-seed rotation, add persisted recent-puzzle deduplication and injectable/testable puzzle supply | New puzzles vary across restarts and all modes; unique solution verified; 8 earlier seeds remain reproducible as legacy saves; tests cover repeat rejection, collision fallback, and deterministic puzzles |
| 8B — objective difficulty | Create human-technique solving/grading (naked singles, hidden singles, locked candidates, then pairs if needed). Characterize true puzzle complexity, **never equate fewer clues with harder** | Easy/Medium/Hard categories only appear after rated generator produces reliable examples and deterministic tests; impossible/ambiguous puzzles rejected; computer guesses are not called human logic |
| 8C — player-facing choice | Mode-specific difficulty selection, remember preference for new puzzles, display difficulty in gameplay/records, validate co-op/versus both receive same challenge | Existing saves continue to load; old records still render; peer compatibility/version policy explicit; user can choose difficulty before starting; "New puzzle" does not secretly alter difficulty |
| 8D — optional assistance | Candidate assistance, valid-next-step hints (explain the technique), auto-removal of related notes, configurable mistake feedback | Classic assist-off remains unchanged; hints cannot leak opponent values or private clues; no hints in race unless matching, explicit room rules; undo/save still correct |
| 8E — visual polish | Better selected cell/row/column/box, keypad counters, large-font and orientation audits, first game teaching | Easy thumb reach; no clipping; TalkBack-friendly board; polished win/replay flow |

**Important:** the existing eight seeds are not a finite source-code puzzle catalog; `Sudoku.generate(seed)` already supports other seeds. The rotation is the limiting component. First code slice in this PR fixes that without changing the saved game structure.

## v0.9 — Trustworthy two-phone cooperation

| Gate | Work | Acceptance criteria |
|---|---|---|
| 9A — real-device LAN gate | Host/join/discovery/manual IP, two-way notes, edit conflicts, undo safety, pause, app kill/restart, reconnect and race completion | Physical-phone checklist run and findings recorded; no progress loss or permanent stuck state |
| 9B — joining UX | Nearby discovery refresh, optional QR with LAN endpoint + room code, copy invite and clear troubleshooting | QR never claims internet matchmaking; expired room gives actionable error; no secret publicly posted |
| 9C — Split Clues (optional) | Per-player starting clue ownership and masked projections, host-side authoritative solution, validated allocation | Hidden digits absent from guest network state, saves, notifications, notes/conflict highlights, hints, and accessibility. Same puzzle mathematically unique with both halves but not uniquely solvable by either individual half. Resume/replace validated |
| 9D — team result | After-game contribution and time metrics if they can be calculated fairly without spoiling answers | No premature correctness leaks; explicit distinction between contribution and skill; no mandatory competition in co-op |
| 9E — protocol hardening | Versioned wire/save migrations, rate-limits, cancellation, sanitized chat, reconnect state | Tests for malformed frames, stale edits, replay attempts, guest resets, persistence failures and protocol mismatch |

**Do not start Split Clues before 9A**. Its public-facing promise depends on a proven LAN foundation.

## v0.10 — Replayability without grinding

- Daily challenge: one stable daily puzzle with an offline-consistent local-date rule, replay-safe records and a fair date rollover policy. No claimed global leaderboard without a server.
- Personal bests per difficulty, completion streaks optional and forgiving, stats/records migration; separate solo, co-op, versus and assisted outcomes.
- Expanded earned cosmetics and achievements with readable unlock requirements; friendly post-match summary, retry/new game.
- Restrain incentives: completing one Sudoku should feel worthwhile even with no streak.
- Accessibility/device matrix: narrow phones, tablets, 200% font, portrait/landscape, Android back gesture, TalkBack.

## v0.11 — Shipping and brand readiness

- Distinct final name and name/trademark/store search; own icon, adaptive icon, splash, native typography, audio/haptics toggle, store screenshots.
- Target current Play API requirement; compile and install against supported Android versions, permission audit, AAB build, release signing custody, Play App Signing and upgrade behavior.
- Privacy policy that accurately discloses local records, LAN transfer/chat, any telemetry (none unless consciously added), data retention, and backup behavior.
- Closed testing eligibility, release tracks and testers handled in Play Console; verify current requirements before submission.
- Stress and regression suite, host crashes/ANRs, offline airplane mode solo, rapid navigation, Android vitals once published.
- Publish only when launch-critical bugs are closed. A release is not proven by `assembleDebug` alone.

## Deferred beyond v1

Remote internet rooms, public competitive ranks, accounts/cloud sync, purchases, advertising, non-9×9 variants and cross-platform support. Revisit from playtest feedback and maintenance capacity.

## Cross-game foundation — parallel strategic track

**Design goal:** Sudoku Together is the first complete game on a **reusable native Android puzzle foundation**, not a one-off project. Future Crossword/Minesweeper games should inherit host/join/discovery/chat, local save handling, profile/settings, native menu patterns, shared CI and release setup, while implementing their own rules, board UI, win/loss states and hidden-information security.

- F0: document current Sudoku coupling and capture regression baselines (alongside v0.8C).
- F1: extract stable platform persistence and LAN discovery/framing behind adapters after the current difficulty/save compatibility gate.
- F2–F3: generic session/visibility interfaces and selectively reusable Compose shells, without forcing Sudoku assumptions onto other board games.
- F4: prove reuse with a separate Minesweeper prototype before claiming the foundation is ready.
- F5: publish a separate, clean **GitHub template** with unique app identity/signing/save instructions; do not use the currently skeletal default Sudoku branch as a template.

Design and acceptance detail: [Reusable Android Puzzle Foundation](REUSABLE_ANDROID_PUZZLE_FOUNDATION.md). Track: [Foundation issue #16](https://github.com/PlayerEden/SudokuTogether/issues/16). This must **not** delay finishing Sudoku or compromise old saved games.

## Engineering guardrails

1. **Baseline:** keep existing QA package ID `.qa`; never overwrite original local records accidentally; retain old signing identity for genuine release updates.
2. **Branch discipline:** make one draft PR per milestone, stacked on the last validated branch; document source commit and CI run for each APK.
3. **Puzzle validation:** unique completed solution, no contradictory givens, deterministic seed replay; rating must use named human-solving techniques rather than a clue-count shortcut.
4. **Migration:** new serialized fields use safe defaults; old host, guest and solo saves tested explicitly.
5. **Test evidence:** local unit tests + Android build, emulator UI pass, and separate real two-phone acceptance. Red CI is investigated even if individual tests pass.
6. **Release gate:** privacy, permissions, correct branding, signed AAB and store review requirements are checked afresh, not assumed.
7. **Stage order:** finish 8A before choosing difficulty UX; verify 8B before labeling a puzzle Hard; finish 9A before promising Split Clues.

## Device checklist (manual)

- Fresh install and upgrade install both phones using exact same test APK.
- Solo without Wi-Fi and after force-stop; puzzle/notes/timer/records survive normal process restart.
- Same home Wi-Fi co-op: nearby join, room code, 2-way input/notes, simultaneous same-cell edits, own safe Undo, chat and reconnect.
- Versus: same original clues but isolated edits and valid-winner verification.
- Rotation/font scale, compact screen keyboard overlap, system haptics disabled, partner disconnect and background state.
- One complete game in each mode; no unexplained game-ending state.

## Research notes

Difficulty should correspond to human deduction techniques, not just clue count; naked/hidden singles and locked candidates are recognized steps in Sudoku constraint propagation. Google Play measures user-visible crash and ANR quality, so stable real-device behavior matters beyond APK compilation.

References: https://cdn.aaai.org/ocs/2517/2517-11201-1-PB.pdf ; https://support.google.com/googleplay/android-developer/answer/17492799

## Current gate status

- v0.7: QA APK builds; emulator interaction tests passed despite a post-test shell reporting bug that has been repaired; two-phone real testing pending.
- v0.8A: **implementation in progress** on `feat/v0.8-puzzle-foundation`.
- v0.8B through v0.11: **planned, not shipped**.
