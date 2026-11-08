# GridMates — Online Friend Challenges, Asynchronous Matches & Matchmaking

**Research date:** 2026-11-08
**Status:** **Approved as a future, post-offline-release exploration only** (owner decision: 2026-11-08). Not implemented; no chosen provider, account requirement, funded service, or authorization to start backend work. Offline-first game completion, launch, player growth and observed financial sustainability take priority.
**Applies to:** GridMates: Sudoku first, with reusable concepts for Crossword, Word Search and other GridMates games.
**Owner question:** How do games like Words With Friends provide internet matchmaking, invites and matches that continue after an app is closed, and how could GridMates offer those features in addition to offline/LAN play?
**Related:** [Brand and series](GRIDMATES_BRAND_AND_SERIES.md), [Sudoku deep research](SUDOKU_DEEP_RESEARCH_AND_GAME_DESIGN_REFERENCE.md), [Shareable cards](GRIDMATES_SHAREABLE_CARDS_CONCEPT.md), [Offline nearby/Bluetooth exploration](GRIDMATES_OFFLINE_NEARBY_BLUETOOTH_DESIGN.md), [Project handoff](PROJECT_HANDOFF.md).


## November 8 owner decision — OFFLINE FIRST, REVENUE-SUPPORTED ONLINE LATER

**Firm sequencing decision:** Complete and release a **full-featured offline GridMates: Sudoku**, including stable Solo/Daily and LAN/nearby multiplayer, before working on an internet backend. Use the initial player base and optional respectful monetization (rare between-puzzle ads and the one-time Remove Ads purchase, after compliance/QA) to *potentially* build a fund for later online features. The owner is **not committing to online multiplayer for the initial release**, nor requesting work to start it now. Preserve the local-first mission; online will add ways to play with friends far away rather than replace offline modes.

**Practical release/funding gates for any future cloud prototype:**

1. The core Sudoku game is complete enough for publication; release and two-device/local multiplayer checks are passed, and save/upgrade continuity is trusted.
2. There is real audience feedback demonstrating demand for remote friends/challenges; don't mistake wishlists or downloads for sustained usage.
3. If monetization is enabled, observe **actual** recurring net revenue and costs (ad fill/rate, purchases, fees, refunds, privacy obligations). Downloads alone do not produce dependable income. No guaranteed earnings are implied.
4. Estimate chosen provider's real free-tier limits, notifications, write/read volume, operational work and security/privacy responsibilities; configure usage alerts, hard/soft budget ceilings and a shutoff plan. The initial trial **may fit free quotas**, but there is no promise of free operation or a self-funding service.
5. Decide on a small, explicitly approved **online feature budget** and operational owner. A tiny closed, friend-only test can be considered sooner only if its costs and risk stay bounded and don't derail offline completion.
6. Start with **invite-only asynchronous same-puzzle challenges**; then evaluate live remote Versus, live Co-op and optional public matchmaking once a player population warrants it.

**Product rule:** Remote play and online accounts must remain **optional**. If cloud functionality becomes unavailable or unaffordable, offline Solo, Daily, collections and LAN must continue to work. Do not introduce pay-to-win, aggressive ads or forced subscriptions as an online-funding shortcut. Track this as a **future-phase milestone**, not part of the current v0.11D or Google Play initial release acceptance list.

---

## Executive answer

**Yes.** Internet matchmaking is practical for small turn-based and puzzle games. Unlike high-tick action games, Sudoku mainly exchanges **room creation, puzzle identifiers, a small number of moves, progress counters and final results**. There are two independent online experiences to distinguish:

- **Asynchronous friend challenge (best first exploration):** Alice challenges Bob to the same board. Each solves **independently on their own schedule**. The cloud stores the challenge and each completion. On both completed, the comparison is revealed (possibly with elapsed times and assistance breakdowns). Nobody has to be simultaneously online. This is **not classic alternating-turn Words With Friends**, because Sudoku itself has no natural alternating turns; it borrows the **play when available** model.
- **Live remote co-op / race:** both devices join an online room. The online service relays or authoritatively validates moves, tracks presence and synchronizes the canonical game state. More complex, especially for conflicts, Undo, reconnect, room ownership and ensuring a losing rival's answer isn't leaked.

**No forced login for offline play.** Keep existing Solo/Daily/same-Wi-Fi LAN fully functional with no internet, and keep Bluetooth/nearby as a separate possible offline transport. User's local-first relationship-driven mission does **not** prohibit online play with family members far away; the online features should prioritize direct invites/friends over random strangers.

## 1. What a Words With Friends-style service actually does

At a conceptual level (not a claim about Zynga's private infrastructure), a server-backed game typically has:
1. **Identity:** player account ID, public display name, opt-in profile image, invitations. Guest/anonymous identities can be used first; stable account-linking for multi-device recovery comes later.
2. **Matchmaking/invites:** creation of a public queue entry or private invitation, compatible rules/mode, join confirmation and an expiry or cancellation route.
3. **Match record:** server-generated match ID, participant IDs, game definition/puzzle ID, rules version, status/turn, authoritative timestamps, changes and result.
4. **Move validation:** check that user belongs to match, that match is active, move is in bounds, version/revision matches, and rules permit that change.
5. **Persistence and sync:** write accepted move/result atomically and notify subscribed clients; when someone comes back later, load match state.
6. **Notifications:** optional push, e.g. “Your friend accepted” or “Your challenge is complete.” Avoid notification spam and respect user preferences.
7. **Safety/operations:** rule access controls, spam/rate limits, duplicate prevention, basic account/report controls if strangers can interact, database retention and cost monitoring.

Words With Friends publicly describes friend invites, automatic opponent matching and asynchronous classic matches: https://www.zynga.com/games/words-with-friends/ and https://wordswithfriends.com/ .

## 2. Feasible GridMates online modes

| Mode | Simultaneous online? | Complexity | Match semantics |
|---|---|---|---|
| **Share same puzzle ID without backend** | No | Very low | Local independent puzzles; recipient manually enters code; **no trusted automatic match results** |
| **Cloud-recorded asynchronous challenge** | No | Low–medium | Same immutable puzzle for two people; compare verified metadata after both finish |
| **Asynchronous collaborative Sudoku** | No | Medium | Friends take turns contributing to one shared board, with versioned edits and Undo policy; optional, not Sudoku's default |
| **Live remote Versus** | Yes | Medium–high | Same puzzle, separate boards, authoritative finish order; hide opponent answers |
| **Live remote Co-op** | Yes | High | Shared board, simultaneous edits, conflict handling, chat, reconnect |
| **Random opponent matching** | Depending on mode | Medium+ plus trust/safety | Match users from queue by mode, suitable difficulty and preferences; queue may be slow with few users |
| **Ranks/leagues/community** | Various | Higher, ongoing | Fairness, abuse monitoring and platform moderation; defer |

### Recommended product order

**First:** an **invite-only asynchronous Sudoku challenge**, because it's useful to family members at a distance, low-latency, resilient to breaks and requires no simultaneous population. Add friend rematches and results comparison.

**Second:** remote invitation for **live Versus**, reusing current host/guest game rules behind a trusted server.

**Third:** remote **Co-op**, after explicit conflict/reconnect testing.

**Fourth (optional):** public random matchmaking, only if the game has enough active players to make queue time reasonable, and if safety/privacy obligations are staffed. Public queues are poor as the first online product: with two people spread across time zones there may be nobody available now. Asynchronous queues mitigate this.

## 3. Current actual code that can be reused

Reference source inspected: \`feat/gridmates-nearby-animals\`, draft PR #28 (v0.11C).

- \`core/.../Sudoku.kt\`: checks completed puzzle validity and counts solutions; reuse game rules for server-side verification.
- \`core/.../HumanSudoku.kt\`: technique-based puzzle grade; keep rating/version immutable for all challenge participants.
- \`core/.../GameSession.kt\`: host-authoritative validation and revisioned actions, per-player fields, match result; can inspire/reuse rule logic but must not assume the mobile phone remains available to host online matches.
- \`core/.../Protocol.kt\`: Android LAN uses serialized message/snapshot protocol and sockets, **not** an internet backend. A cloud transport is not automatically compatible with raw local sockets.
- \`core/.../DailyChallenge.kt\`: date-seeded offline puzzle; this is **not yet** a cross-user invitation or server-verified timing mechanism. Be precise about generation version and timezone.
- Current Versus creates separate \`cells\` and \`guestCells\` on the **same starting puzzle** and ends on the first valid completed board. \`snapshotForGuest()\` intentionally hides rival answer cells. Preserve secrecy for online live games too.

**Key architectural rule:** keep the pure game rules and move validation separate from the transport layer. A server should be able to validate using a shared or ported rules module; do not trust a client's "I won!" string. For low-stakes friend-only async challenges, light verification may be enough to start, but any ranked/global scores require stricter server authorization, authoritative times and anti-cheat controls.

## 4. Minimum backend schema (illustrative only)

~~~text
Player {
  id, display_name, optional avatar_id, optional linked_sign_in
}

Challenge {
  id, inviter_id, invited_id,
  immutable_clues[81], puzzle_id, generation_version, difficulty,
  created_at, accepted_at?, expires_at?,
  state: invited|active|completed|declined|expired
}

ChallengeAttempt {
  challenge_id, player_id,
  state: not_started|playing|finished,
  submitted_board? / validated_moves?,
  elapsed_ms?, assists?, completed_at?,
  revision
}

LiveMatch {
  id, rules_version, mode: coop|versus,
  participant_ids, immutable_puzzle,
  revision, authoritative_state, status, winner_id?,
  created_at, started_at?, ended_at?
}

Invitations/notifications {
  recipient_id, challenge_id, event, read_at?
}
~~~

Server owns participant assignments, immutable board and result transitions. Use authorization rules so an account cannot read somebody else's private challenge data. Match codes/deep links are **invitations**, not unrestricted authorization to view records. For multiplayer keep separate public progress fields vs private player board fields. A database subscription is a delivery mechanism, **not sufficient enforcement** of game rules.

For asynchronous competitive Sudoku decide **before implementation** whether finish time or hint usage affects who wins, whether results are hidden until both complete, and whether a player may retry an attempted board. An easy friendly model is a shared puzzle and the first valid completion for each, comparing elapsed time with Assisted/Unassisted clearly separated. For a fair race against unsupervised offline attempts, **client-reported elapsed time is not cheat resistant**; label results friendly/unranked or introduce a server-verified online start/finish for ranked play.

## 5. One implementation path — invite-only asynchronous challenges

1. Player chooses **Challenge Friend** on a Solo result, Daily-like puzzle, or new puzzle screen.
2. App creates (or retrieves) an **immutable puzzle definition**, grade and generator version. Server records challenge ID, participants and invitation.
3. Invite recipient through an in-game friendship ID, shareable link/code, or normal Android Sharesheet. **A static result PNG by itself is not a deep link.**
4. Recipient opens/accepts at their convenience, after simple authenticated identity or an appropriately scoped invite token.
5. Both solve locally. Save local progress. For guaranteed fair outcome, the cloud must receive proof of completion; when offline, submissions may wait for reconnect and are marked **pending** rather than “globally confirmed.”
6. On submission the server **verifies the final board** against immutable givens and Sudoku constraints and accepts the result atomically once; meaningful score and wall-clock claims need their own trusted definitions.
7. When both valid attempts are recorded, show **Compare Results** (time, difficulty, assistance, stats only if truthful). Optional shareable postcard. Don't show solution digits until the participant chooses spoiler reveal.
8. Use notification (opt-in) for accepted invite or completed result; no mandatory reminders, no spam.

Simpler alternative: **no cloud** same puzzle/shareable ID; good as a first step but no reliable automatically synced winner, invites or ownership across reinstall.

## 6. Backend products to evaluate

**Option A: Firebase (strong Android-first fit)**
- Firebase Authentication: anonymous guest sign-in and later account linking if desired.
- Cloud Firestore or Realtime Database: persisted matches, real-time subscriptions, access security rules.
- Cloud Functions / trusted service for authoritative match completion and matchmaking queue transitions.
- Firebase Cloud Messaging: optional device notifications.
- Firestore Android offline cache is useful, but **last-write-wins client sync alone does not solve simultaneous authoritative Co-op edits**. Validate every mutation with transactions/versioning and server checks.

Official docs:
- https://firebase.google.com/docs/auth/android/anonymous-auth
- https://firebase.google.com/docs/firestore/manage-data/enable-offline
- https://firebase.google.com/docs/firestore/manage-data/transactions
- https://firebase.google.com/pricing
- https://firebase.google.com/docs/rules

**Option B: Supabase (strong relational/data-control fit)**
- Auth: optional anonymous identity, optional linked credentials later.
- Hosted Postgres tables for profiles, invites, attempts, matches; Row Level Security to restrict participant access.
- Realtime channel/subscription for live status, movements and presence.
- Edge functions or other trusted server logic to validate final boards, reserve matches atomically and handle sensitive operations.
- Kotlin client exists; verify Android lifecycle and push notification integrations when implementing.
- Current published free-tier Realtime limits include 2 million messages and 200 peak connections; these are **quotas, not guaranteed lifetime allowances or total operating costs**.

Official docs:
- https://supabase.com/docs/guides/realtime/pricing
- https://supabase.com/docs/guides/auth/auth-anonymous
- https://supabase.com/docs/guides/realtime/limits

**Option C: self-hosted server / service**
- JVM/Kotlin server could reuse existing pure-core rules and hold authoritative matches, with WebSockets for active online players and SQL for persistence.
- Maximum control and portability, but requires deployment, uptime, HTTPS, authentication, monitoring, abuse limits, backups and ongoing maintenance.
- **Not recommended** as the first infrastructure burden for a small friendly Sudoku launch.

**Google Play Games Services caveat:** Google **ended** its built-in real-time and turn-based multiplayer APIs in March 2020. It is not a plug-in “enable online matchmaking” checkbox. Use modern Play Games Services v2 for platform engagement like achievements and leaderboards; official v2 guidance says it **must not be the primary in-game identity/inventory backend**. Source: 
- https://support.google.com/googleplay/android-developer/answer/9875345?hl=en
- https://developer.android.com/games/pgs/migration_overview

## 7. Financial reality

- Sudoku sends tiny amounts of data per move; user count, active subscriptions, reads/writes, notifications, functions, and public social features drive cost.
- Many hobby prototypes **can fit within provider no-cost quotas**, but production may incur costs from usage or required billing. Set provider budgets and alerts, quota limits, retention policies and conservative update rates **before public rollout**.
- Avoid streaming all 81 cells or a full match record after every keystroke; send small revisioned deltas and occasional snapshots. Coalesce pencil marks when appropriate.
- Avoid one persistent Firestore listener for every browseable row; each data update may count as a billable read. Source: https://firebase.google.com/docs/firestore/pricing
- Provider pricing can change. Validate current terms when implementing; do not promise “free forever.”
- **Matchmaking needs an active population**: at a small game's launch, automatic synchronous queue may have long waits even when backend code works. Invites and asynchronous challenges are better early.

## 8. Safety/privacy/quality requirements

- Keep **offline Solo/Daily and nearby LAN** without requiring online account, internet, ads or notifications.
- Cloud presence and online identity are explicit opt-in. If a player creates an **anonymous cloud account**, warn that they may lose access to that cloud identity on uninstall/device change unless it is linked to a recoverable method.
- Don't publish players' email, phone, IP, room secret, private notes or exact location.
- Invite endpoints should avoid account enumeration; random match IDs must be unguessable and server-side access gated.
- Every cloud write must authenticate and authorize sender membership, check mode/revision/immutable clues, enforce bounds and rate limits; client binaries can be modified.
- Last-write-wins is unsafe for Co-op's shared 9×9 board; authoritative operation sequencing and deduplication are required.
- Live Versus must preserve **opponent board secrecy**, using participant-scoped reads and permissions, not simply hiding elements in the Android UI.
- Public random matchmaking with stranger chat requires report/block/safety policy, spam controls and family/minor considerations; far more operationally demanding than friend-only matches.
- Timeouts, disconnect, stale session, replayed operation, deleted/inaccessible identity, loss of connection, resubmission, conflicting moves and old Android versions all need QA.
- Stored match state, Google Play Games rankings and actual bought/equipped cosmetics must not be conflated with proof of account ownership.

## 9. Reuse for other GridMates games

**Sudoku:** Same-puzzle async challenges; live race; live shared Sudoku; step validation based on rows/columns/boxes.

**Word Search:** Two distant players complete the same deterministic puzzle at different times; compare found words and time. Live Co-op could reveal discoveries as they occur. Keep target words/answer cells hidden until relevant completion.

**Crossword:** Natural asynchronous **shared-board turn-taking** or edit collaboration, possibly better matched to the Words With Friends model than Sudoku. Each accepted letter/word is revisioned and sent to shared server; avoid overwriting one another.

**Minesweeper (later idea):** Solo same-seed time challenges are simple if board/first-click semantics are standardized; live competitive fairness requires careful server validation.

**Series-wide infrastructure:** a common **account/friend/invite/match-result layer** could serve multiple GridMates games, but each game retains its own puzzle logic. Don't build a complex unified platform before one game proves the need. Different Android app package identities can share backend identity only if explicitly architected, configured and account-linked; don't assume automatic shared identity.

## 10. Stage gates to propose for Work chat later

| Stage | Deliverable | Sign-off needed |
|---|---|---|
| 0. Feasibility | Compare Firebase and Supabase costs, auth, privacy, schema and one-device offline/online QA | Owner chooses provider |
| 1. Friend-only pilot | A sends immutable puzzle challenge; B opens and solves later; persisted pending/completed outcomes | Physical two-phone, different networks |
| 2. Verified comparison | Trustworthy results/hints/times within stated limits, safe Save/Share Result | Client tampering / duplicate tests |
| 3. Notifications | Opt-in invite/result push, blocking and rate limiting | Delivery/retry and privacy tests |
| 4. Live remote Versus | Authoritative room and finish validation with private boards | Two-network simultaneous QA |
| 5. Live Co-op | Revisioned concurrent operations, recoverable reconnect, styles | Collision/Undo/host migration tests |
| 6. Optional random matchmaking | Fair queues + safe matchmaking; initial small-population UX | Abuse handling, monitoring and queue stats |

**Recommendation:** Keep this as **a future-feasibility note**, not a launch blocker for current local-first GridMates: Sudoku. The app's existing game core is an advantage, but online accounts/backend and operational responsibility are genuinely new work.

## One-sentence Work chat handoff

“Research a minimal **optional invite-only asynchronous Sudoku challenge** backed by Firebase or Supabase, preserving local Solo/LAN and the existing Kotlin rule engine, with validated persisted match completion and no public matchmaking until friend invites prove useful; start by proposing backend choice/cost/security and owner-reviewable scope.”
