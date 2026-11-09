# GridMates: Sudoku — Project handoff

Start from `main`, the canonical Android source and documentation. The [delivery plan](GRIDMATES_DELIVERY_PLAN.md) tracks the next product work. The [v0.11D integration audit](V0.11D_INTEGRATION_AND_QA.md) records exact source, APK and verification details.

The November 8 owner decision puts the offline launch first. Complete local play, device QA and Play testing before evaluating a budget for online services. Future service work starts with separately approved, invite-only friend challenges when adoption and earnings can support it. [Release and expansion gates](GRIDMATES_DELIVERY_PLAN.md#online-expansion-after-the-offline-launch)

## Current candidate — v0.11E

[Draft PR #33](https://github.com/PlayerEden/SudokuTogether/pull/33) adds Profile with statistics, history and achievements, a unified Collection, and **Settings → Progress** export and restore. It targets API 36 and builds a separate production App Bundle. Continue the candidate from that PR. The remaining release checks cover the current build on real phones, modern Android accessibility, and Play Console setup and testing. [QA evidence](V0.11E_PROFILE_PROGRESS_QA.md) · [Delivery plan](GRIDMATES_DELIVERY_PLAN.md)

## Merged QA milestone — v0.11D

The launcher stays **Sudoku** and the in-app name is **GridMates: Sudoku**. Versus shows both identities and player-filled progress, with a reconnect state that retains the last known opponent. Number styles now use licensed bundled fonts under the existing cosmetic IDs. [Number font assets](GRIDMATES_FONT_ASSETS.md) · [Native rollout plan](superpowers/plans/2026-11-07-gridmates-native-rollout.md)

The previous [v0.11C checkpoint](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.11c-nearby-woodland-qa) retains open nearby joining, optional code protection and Woodland portraits. Its [exact APK receipt](V0.11C_DELIVERY_RECEIPT.md) is the reference for the 19 → 20 upgrade rehearsal. Keep earlier source and APK checkpoints for recovery. [QA update guide](GRIDMATES_UPDATE_AND_SAVE_PREPARATION.md)

The owner reported a successful v0.11B two-phone smoke test. The v0.11D audit records the next physical co-op and Versus acceptance cases. [Current gates and evidence](V0.11D_INTEGRATION_AND_QA.md)

## Product decisions and design references

- **Additional offline cosmetic expansion consideration:** [board-theme and number-font expansion](GRIDMATES_BOARD_THEMES_AND_NUMBER_FONTS_EXPANSION.md) covers distinct new puzzle-board palettes (Parchment/Garden/Ocean/Graphite first), 2–3 pilot number fonts (Pencil/Rounded/Typewriter), reasonable Solo-first unlocks, current v0.11D bundled-font licensing, real 1–9 previews and 2-phone/accessibility/save-upgrade QA. **Planning only**—art and milestone thresholds still need owner review. Preserve all existing board/font IDs and evaluate with the Co-op shared-board design.


- **Optional, discardable home-screen UX note:** [Home first-impression usability considerations](GRIDMATES_HOME_FIRST_IMPRESSION_USABILITY_CONSIDERATIONS.md) records screenshot-driven feedback for first-time/older or less tech-confident Sudoku players; test difficulty/Continue meaning, progress counting starting clues, mode labels and Daily wording before touching the current layout. This is **not an approved redesign or release blocker**.

- Local play and nearby Wi-Fi are the core of GridMates. Finish Sudoku before expanding the series or extracting a reusable framework. [Brand and series](GRIDMATES_BRAND_AND_SERIES.md)
- The selected launch model is free-to-install with rare between-puzzle ads and an optional Remove Ads purchase. [Monetization plan](GRIDMATES_MONETIZATION_AND_PLAY_TESTING_PLAN.md) · [Store setup guide](GOOGLE_PLAY_FROM_SCRATCH_LAUNCH_GUIDE.md)
- Design briefs cover an optional [Share result flow](GRIDMATES_SHARE_RESULTS_UX_DESIGN.md), [profile and result cards](GRIDMATES_SHAREABLE_CARDS_CONCEPT.md), and [coaching and scoring](GRIDMATES_PUZZLE_COACH_AND_SCORING_PROPOSAL.md).
- Future product ideas include [Ocean and Bug collections and Daily rewards](GRIDMATES_COLLECTIONS_DAILY_REWARDS_AND_FONTS_DESIGN.md), [offline gift codes](GRIDMATES_GIFT_CODES_SIMPLE_DESIGN.md), [router-free nearby play](GRIDMATES_OFFLINE_NEARBY_BLUETOOTH_DESIGN.md), [post-match puzzle comparison](GRIDMATES_VERSUS_POST_MATCH_COMPARISON_DESIGN.md) and [shared co-op board themes](GRIDMATES_SHARED_COOP_BOARD_THEMES_DESIGN.md).
- The [Sudoku research reference](SUDOKU_DEEP_RESEARCH_AND_GAME_DESIGN_REFERENCE.md) documents the solving techniques and engine experiments for future coaching and difficulty work.

## Historical milestones

The entries below preserve the branch and release status as it stood when each milestone was recorded.

### November 4 — owner acceptance and successful two-phone smoke test

The owner reports that the game and a two-phone test work, and asks to lock the current baseline. [Playtest record, screenshot findings and remaining work](V0.11B_OWNER_PLAYTEST_2026-11-04.md). Preserve v0.11B at [checkpoint/v0.11b-two-phone-smoke](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.11b-two-phone-smoke), source77f900f73f21dcb5a02cdae01fab71f00514b898 and the already delivered APK. The current visual direction is retained. Basic physical co-op is owner-reported successful; the full reconnect/kill/complete-game/versus checklist is not individually established. Do not keep reporting physical LAN as wholly untested or claim the full checklist passed.

The owner has now chosen the easiest default for older/less technically confident players: open nearby joining, with Require a code optional for the host. Plain labels, readable device-scaled text, generous targets and clear connection states guide the next design. A stable multiplayer header and clearer chat remain the next refinement recommendation. Admission/compatibility details still need a contained implementation plan; current room/protocol behavior is unchanged.

Generated decorative assets and a larger unlockable profile-icon collection were raised as possibilities. Preserve current native UI and all existing emblems/unlocks; art theme/count remain proposals. [Audience/artwork discussion](V0.11B_OWNER_PLAYTEST_2026-11-04.md) and [visual brief](GRIDMATES_VISUAL_IDENTITY_BRIEF.md). Continue coach/sharing/profile stages after the usability pass. No new APK, main/development promotion or Play publication accompanies this documentation update.

## v0.11B — latest verified launcher/entry-check source

Continue from [feat/gridmates-coach-launcher](https://github.com/PlayerEden/SudokuTogether/tree/feat/gridmates-coach-launcher), stacked on v0.11A; do not restart from older development or discard the previous drafts. Production source0f640aaa is unchanged by recovery/test fixes. [Exact QA](V0.11B_COACH_LAUNCHER_QA.md), [delivery plan](GRIDMATES_DELIVERY_PLAN.md), [recovery](V0.11B_RECOVERY_LOG.md), [work log](V0.11B_WORK_LOG.md).

Android launcher **Sudoku**; full **GridMates: Sudoku** brand remains. Optional Solo/Daily Check my work is honest solution comparison, read-only until explicit Erase, with Undo/resume and separately saved Assisted metadata. Logical hints remain deductions and assistance stays unlimited. No local checks in rooms, new score, art replacement, release publication or package/signing/save reset.

Version18 /0.11.0-dev2-qa, min26/target35, schema1/protocol5, original QA certificate. Fresh103 JVM +46 API26 native cases pass; lint0 errors/14 warnings. Exact original17→18 update preserves complete profile bytes, nonempty history, entered answer, pencil note, ID and preferences. Runtime launcher/application labels and compiled icon equality verified. APK SHA-256 `87bb39b735745856f790bc4987e368f76e78cd70edea0c084ac52b0ff3870615`. Prior transient candidate953e6b9 was not delivered and its cleared raw logs are historical only.

Delivery complete: [draft PR27](https://github.com/PlayerEden/SudokuTogether/pull/27) targets feat/gridmates-brand-copy; [v0.11B checkpoint](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.11b-coach-qa-green) preserves this stage. APK, inspected preview and public QA evidence are saved. [Exact artifact hashes and source preservation receipt](V0.11B_DELIVERY_RECEIPT.md). The final delivery commit only updates documentation; it does not change the verified APK.

Existing next product stage: stage2 coach proof/highlights/progressive explanation, then optional result sharing, profile/cards and calibrated scoring. The November4 playtest recommends a contained multiplayer usability pass first. Remaining physical LAN subcases, release API/signing/AAB, modern-device/TalkBack/tablet and final art gates remain. Resolve the deferred historical brand-document source-metadata Minor during the next documentation pass. Main/development are not promoted.

## v0.11A — latest GridMates branding/copy candidate

Continue from [`feat/gridmates-brand-copy`](https://github.com/PlayerEden/SudokuTogether/tree/feat/gridmates-brand-copy), based on verified v0.10D. The newer save/visual work and the planning documents on development are both carried forward. Do not restart from the older promoted v0.10B app or merge stacked drafts out of order.

The owner approved **GridMates: Sudoku**, then authorized branding and concise game UI. Home now prioritizes playing; redundant slogans, explanations and development notes are removed. App/version/local-storage information and APK sharing are in Settings → About. Connection help is on demand. Existing warnings, result fairness, classic rules, callbacks, storage and LAN remain intact. Three original marks are presented in native screens; connected tiles is provisional until owner selection. The current palette is retained for comparison. Profile expansion follows branding selection; Crossword follows Sudoku completion.

Version17 / `0.11.0-dev1-qa`; min26/target35, `.qa`, schema1/protocol5, v0.10D QA certificate remain. [Draft PR26](https://github.com/PlayerEden/SudokuTogether/pull/26); [QA checkpoint](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.11a-gridmates-qa-green); [exact QA and limitations](V0.11A_GRIDMATES_QA.md); [work log](V0.11A_WORK_LOG.md). 87 JVM tests and31 accepted API26 native cases passed; lint0 errors/20 warnings. The exact original16→17 APK update preserved the seeded whole-profile hash, active puzzle/note, identity and preferences. The upgrade fixture does not independently demonstrate a nonempty completed-history upgrade. Extreme-text word wrapping, physical LAN, newer Android, tablets and TalkBack remain open. Main/development are not promoted and repository/application identifiers are not renamed.

## v0.10D — latest native visual/convenience pass

[Draft PR25](https://github.com/PlayerEden/SudokuTogether/pull/25) is stacked on v0.10C. [Verified v0.10D checkpoint](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.10d-visual-qa-green) preserves this presentation milestone.

Continue from [`feat/v0.10d-game-visuals`](https://github.com/PlayerEden/SudokuTogether/tree/feat/v0.10d-game-visuals), stacked on v0.10C `ddf661e5e612d763e538ad97b47197d8771f2989`. Do not restart from older development/main source and drop the C/D changes. The [visual QA audit](V0.10D_VISUAL_QA.md) and [work log](V0.10D_WORK_LOG.md) own the exact acceptance record; the previous sections below preserve history.

Player-facing work: cohesive cream/forest/gold theme, original adaptive icon, compact mode cards with immediate Resume/Daily actions, bottom Play/Records/Collection/Settings navigation, previewable cosmetics and clearer unlocks, readable records, focused settings, framed classic board and icon tools, explicit cell context, trophy results, and IME Next/Done joining. Solo returns Home directly with saved progress; online rooms retain leave confirmation. No engine, persistence or transport code changed; schema1/protocol5 stay compatible.

Independent review found two important 200% layout problems; both reproduced RED on a compact API26 emulator before the fix. Large-text multiplayer headers now reflow; narrow/short windows can scroll the whole interface, landscape rails begin at520dp, and size-dependent board layout uses same-pass SubcomposeLayout. A finished game no longer leaves an empty controls rail. 87 JVM tests and 42 API26 native cases passed; lint 0 errors/18 warnings. Both independent-review regressions are GREEN after observed RED fixtures.

QA `0.10.0-dev4-qa`, code16, `.qa`, min26/target35. Private debug QA identity is backed up separately as `SudokuTogether-v0.10D-private-QA-signing.zip` (never commit/share it publicly). Its certificate differs from v0.10C: keep a progress-bearing old install on a signing conflict. Production signing/update migration, physical two-phone LAN, newer Android runtime, TalkBack/tablets, final brand/Play, Split Clues, variants and foundation extraction remain later gates. Main/development are not promoted by this pass; retain the previous checkpoints and stack the new draft on v0.10C.

## v0.10C takeover — latest isolated source

Continue from [`fix/v0.10c-save-integrity-polish`](https://github.com/PlayerEden/SudokuTogether/tree/fix/v0.10c-save-integrity-polish), [draft PR23 into development](https://github.com/PlayerEden/SudokuTogether/pull/23). Base: development a4448bf; application fixes at c80faea. Keep the previous v0.10B checkpoint. Do not restart obsolete v0.2 branches or merge the older prune-only experiment.

QA rollback checkpoint: [`checkpoint/v0.10c-qa-green`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.10c-qa-green). The earlier v0.10B checkpoint remains intact.

Final QA: 85 core + 2 JVM tests passed;31 Android8/API26 cases and 1 persisted-race repeat passed, with font/orientation scenarios isolated into separate instrumentation processes. APK and 14-image evidence ZIP are saved in chat. Lint: 0 errors / 13 warnings. The API35 software-emulator boot timed out before tests; physical two-phone acceptance stays deferred.

This pass implements verified profile recovery/commit checks and blocked-save protection, structural Undo validation, safe immediate Daily pruning with retained finish state, Android8 date compatibility, adaptive native History/results and isolated timer recomposition. It also rejects superseded local starts before persistence and preserves recovery explanations after hosting. Read the [v0.10C audit](V0.10C_SAVE_INTEGRITY_QA.md) and [work log](V0.10C_WORK_LOG.md) for exact current checks, artifact fingerprint, review decisions and limits.

QA version 0.10.0-dev3-qa/code 15, `.qa`, min26/target35; schema 1/player IDs/LAN 5 remain compatible. Debug signing can differ from an existing QA APK; do not uninstall a progress-bearing app to bypass a mismatch. Next is deliberate integration of this draft, then v0.11 signing/update and brand/Play foundations. Two-phone acceptance stays deferred, with Split Clues, larger boards and framework extraction later. The sections below preserve the earlier promoted v0.10B baseline; the latest audit takes precedence for this branch.

## 1. Where the real project lives

- Repository: https://github.com/PlayerEden/SudokuTogether
- **Promoted baseline branch:** [`development`](https://github.com/PlayerEden/SudokuTogether/tree/development) — full source at the earlier promoted v0.10B milestone. The latest v0.10C/D source is identified above; preserve those unmerged changes when starting the next slice.
- **Previous promoted milestone feature branch:** [`feat/v0.10b-daily-history-results`](https://github.com/PlayerEden/SudokuTogether/tree/feat/v0.10b-daily-history-results) — Daily History, real completion summaries, safe unfinished-puzzle retention, merged [PR #22](https://github.com/PlayerEden/SudokuTogether/pull/22) (source promoted into `development`). Previous v0.10A implementation [PR #21](https://github.com/PlayerEden/SudokuTogether/pull/21).
- **Latest known-green source checkpoint:** [`checkpoint/v0.10b-qa-green`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.10b-qa-green) at exact code commit `83df54621bb2713917b518ee8ccfed4c25511a80`. Earlier verified checkpoints: [`v0.10A`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.10a-qa-green), [`v0.8E`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8e-qa-green), [`v0.8D`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8d-qa-green) and [`v0.8C`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8c-qa-green). Never force-push/delete these.
- **IMPORTANT:** default [`main`](https://github.com/PlayerEden/SudokuTogether/tree/main) is still a **small documentation/workflow skeleton**, not the full working Android project. It links to this handoff. **Do not conclude the game is missing because you checked only `main`.**
- GitHub PRs #1–#22 include an older stacked feature history (many still drafts), while **PR #22 has been merged into `development`**. Do not merge or delete earlier stacked branches out of order. Do not independently merge them out of order or delete their branches without first planning and verifying consolidation. The eventual `main` promotion is tracked in [repository issue #18](https://github.com/PlayerEden/SudokuTogether/issues/18); it must not delay the next playable feature.

## 2. Current playable product — v0.10B (development QA)

**Native Android** (Kotlin / Jetpack Compose, not HTML/WebView), 9×9 classic Sudoku. Modes:
- Solo/offline with local persistence, Undo, Notes, erase, completion and records.
- Two Android phones on **the same Wi-Fi**: host and join a shared co-op board, guest reconnect, room code/copy, chat, player styling.
- Local head-to-head versus with independent boards and completion checks.
- Player identity, local achievements, earned cosmetics, profile, haptic preference, accessible selection/row-column-box highlights, fixed bottom number dock, per-digit remaining counts.
- Fresh generated uniquely solvable puzzles with 64 recent-puzzle ID avoidance.
- **Easy, Medium, Hard** selection before a new game. Difficulty derives from a deduction-only technique grader (singles; locked candidates; naked pairs), not clue count alone. Tier selector refuses silent downgrades; generated puzzles must match the requested technique grade.
- Difficulty travels inside the puzzle/host-authoritative snapshot and is retained for rematches; legacy records and old saves without grade load as **Legacy** instead of being falsely relabeled.
- **Optional Solo Hint:** opens a logical deduction based on the player's visible grid; shows a location, number and reasoning but never fills it. If user-entered numbers make the grid impossible, no speculative hint is given. Hint use is persisted before display.
- **Optional Solo pencil-mark cleanup:** disabled by default, enabled in profile. A new placed digit removes that candidate from related empty cells; Undo restores both the number and automatically removed notes.
- **Assisted result tracking:** saved Solo progress remembers hint usage and note-assist usage; completed Solo records display `Assisted`. Older saved games and history remain readable using defaults.
- Assistance is **Solo-only** pending a fair, host-authoritative policy for competitive/co-op rooms.
- **v0.8E polish:** Solo timer is displayed once above the board; hints and Help have 48dp minimum touch targets and explicit accessibility descriptions; Help explains the opt-in controls.
- **Fair records:** Solo shows separate assisted and unassisted bests, overall and per difficulty. The old records stay intact; older runs without assist metadata are correctly treated as unassisted before the assist feature existed. Legacy difficulty remains unassigned. Speed achievement criteria remain unchanged to avoid revoking historical earned rewards.
- LAN wire protocol remains **v5** (two-phone testing deferred). Debug QA app uses application ID suffix `.qa`; version `0.10.0-dev2`, versionCode `14`.

- **Offline Daily Challenge (v0.10A):** a new home card offers one uniquely solvable, logically Easy puzzle per device-local calendar date. Date-salted versioned SHA-256 selection ensures the same dated puzzle across restarts, without internet. Daily sessions use separately persisted progress/history and records, retaining up to 128 daily board slots (up to 31 completed review boards plus unfinished puzzles, bounded by 128). Normal Solo progress, records and multiplayer sessions remain untouched. Daily hints, notes, Undo and elapsed time reuse tested Solo behavior. Daily completion is idempotent, date-tagged and assisted-aware; daily records do **not** count as regular Solo wins/bests. The user can review a finished day's board but cannot create another new board for the same date.
- **v0.10B Daily History:** offline profile lists saved daily dates with completion state and elapsed time. The user can reopen any older **already saved** daily board to continue or review, but cannot generate extra backdated puzzles. Six most recent rows display first, with Show all for the full archive.
- **v0.10B safe retention:** never silently discard unfinished daily progress when 31 days pass. Preserve up to 128 unfinished slots; completed boards are pruned to the latest 31 when appropriate. If all 128 slots are unfinished, creating another challenge shows an explicit message and preserves existing data. Completed daily result records persist independently of reviewable boards.
- **v0.10B finish summary:** Solo and Daily completed games show saved elapsed time, moves, grade, assisted/unassisted status and hints used. No fabricated global times, ranks or competitor statistics.
- **Limits to avoid overstating:** difficulty labels are human-technique tiers, not calibrated difficulty ratings; Daily is Easy-only in this first slice. There is no internet lobby, online synced daily contest, cloud save, global rankings, Split Clues, co-op/versus hints, final store identity or Google Play production release.

## 3. Verified QA evidence, not assumptions

- **v0.10B Kotlin/Android full build:** [run 40621938766](https://github.com/PlayerEden/SudokuTogether/actions/runs/40621938766) **SUCCESS**: core tests, Android JVM unit tests, lint, instrumentation Kotlin compilation and isolated Android APK assembly.
- **v0.10B Android emulator:** [run 40622291879](https://github.com/PlayerEden/SudokuTogether/actions/runs/40622291879) **SUCCESS, 10/10 tests, 0 failures/errors/skips** (confirmed from uploaded Android JUnit XML). Includes Daily History → reopen daily board, Solo persistence, Hint, profile, co-op/versus regressions.
- **Second v0.10B Android emulator:** [run 40623046297](https://github.com/PlayerEden/SudokuTogether/actions/runs/40623046297) **SUCCESS, 10/10 cases, no failures/errors/skips**. The screenshot artifact only contains Home: an intermittent emulator system Wait overlay/ADB accessibility crawler prevented reliable Daily/Profile image capture. **UI interaction confirms navigation, but no visual proof of the new Daily History/profile/finish card is claimed.** Next visual pass should use Compose-owned screenshot captures.
- **One failed, corrected first regression:** [build 40621625278](https://github.com/PlayerEden/SudokuTogether/actions/runs/40621625278) had one assertion expecting 32 retained slots; the correct count was 33 (two unfinished plus 31 completed). Test corrected and full build subsequently passed.
- **v0.10B QA APK:** `SudokuTogether-QA-40621938766`, **10,745,836 bytes**, SHA-256 `c931401634c25403301141256a97f4165f2d2310bb38886b55bf9d1b964eb917`, verified against GitHub's checksum file. Actions artifacts expire (14 days); committed source, tests, handoff and a pinned checkpoint are durable.
- **Previous v0.10A green:** [build 40616087461](https://github.com/PlayerEden/SudokuTogether/actions/runs/40616087461), [Android emulator 40616779175](https://github.com/PlayerEden/SudokuTogether/actions/runs/40616779175) (10/10).
- **Still unverified physically:** user's actual phone Daily History/complete-game results, save upgrade, local midnight transition, narrow screen, TalkBack and large fonts. Two-phone Wi-Fi testing is **explicitly deferred by user**, not passed or waived. Never declare Split Clues/network release ready before real-device LAN acceptance.
- Specific audit: [`V0.10B_DAILY_HISTORY_QA.md`](V0.10B_DAILY_HISTORY_QA.md).

## 4. Previous development order — v0.10C now recorded above

**Previous order (superseded by the current v0.10C audit above):** restrained improvements toward launch readiness, preserving the physical multiplayer gate:

1. Review actual Daily History and Solo/Daily completed-game screens, including large-font/landscape accessibility and sensible touch targets. Capture actual on-device screenshots where possible.
2. Assess measured offline replayability: meaningful daily achievements or streak presentation that never pressures users, misreports missed days, or pretends online/global competition. Protect existing saves and avoid pointless gamification.
3. Polish Android release requirements: app identity distinctiveness, privacy/data safety inventory, target API, app signing/AAB, store listing/testing eligibility and update path. **Do not publish** until verified.
4. Add core regression + emulator tests, produce QA APK and verify checksum. Maintain GitHub docs, milestone branch and known-green checkpoint.
5. When the user volunteers to test with two phones, run v0.9A real-device co-op/versus checklist; do not prompt or assume readiness. Keep QR/privacy-sensitive Split Clues blocked until this passes.

Roadmap: [v0.10 issue #13](https://github.com/PlayerEden/SudokuTogether/issues/13) active; [v0.9 LAN issue #12](https://github.com/PlayerEden/SudokuTogether/issues/12) deferred; [v0.11 issue #14](https://github.com/PlayerEden/SudokuTogether/issues/14) later.

## 5. Remaining release roadmap

- **v0.8E:** fair assisted/unassisted records, duplicate-timer cleanup, explanatory help and touch-target improvements **implemented and Android-tested**. Physical accessibility/font and Solo assist playtests are still pending.
- **v0.9A/B:** two-phone LAN playtest and invite/reconnect validation **deferred until user is ready**; still a required gate before multiplayer release claims. Can work on non-device-dependent robustness/tests separately.
- **v0.9C:** **Split Clues** remains blocked behind actual LAN gate. Optional collaborative mode. Each phone sees a different subset of givens; cannot leak through guest network state, notes, hints, previews, screen-reader semantics, or saved mirror. This depends on v0.9A.
- **v0.10A implemented/Android-verified:** daily puzzle with local calendar date, independent saves and assisted-aware results. **v0.10B next:** stronger completion summary, daily history and native usability.
- **v0.11:** distinctive legal/brand review, app icon and store visuals, Android/Play target API validation at submission, signing/AAB, privacy/data safety, closed testing eligibility, accessibility, performance/stability and launch gates.
- **Later only:** reusable native Android puzzle-game template so future crossword/Minesweeper-type games can reuse infrastructure. Concept preserved in [`REUSABLE_ANDROID_PUZZLE_FOUNDATION.md`](REUSABLE_ANDROID_PUZZLE_FOUNDATION.md) and [issue #16](https://github.com/PlayerEden/SudokuTogether/issues/16). **Paused at user request.** Do not begin extracting it while Sudoku unfinished.

Full acceptance matrix: [`ROADMAP_V08_TO_LAUNCH.md`](ROADMAP_V08_TO_LAUNCH.md).
Milestone QA: [`V0.10A_DAILY_CHALLENGE_QA.md`](V0.10A_DAILY_CHALLENGE_QA.md), [`V0.8E_SOLO_FAIRNESS_QA.md`](V0.8E_SOLO_FAIRNESS_QA.md), [`V0.8D_SOLO_ASSISTS_AUDIT.md`](V0.8D_SOLO_ASSISTS_AUDIT.md), [`V0.8C_DIFFICULTY_SELECTION_HANDOFF.md`](V0.8C_DIFFICULTY_SELECTION_HANDOFF.md).
Game specs: [original design](superpowers/specs/2026-10-25-sudoku-together-design.md) and [co-op extras](superpowers/specs/2026-10-25-co-op-extras-design.md).
Device checklist: [`TESTING.md`](TESTING.md).

## 6. Project decisions and development standards

- User wants a **genuine, intuitive native mobile game** rather than AI-generated-feeling panels, unnecessary copy, or a browser-like UI. Keep Sudoku central; use thumb-friendly dock, calm cream/forest palette, native Material/Compose controls and deliberate feedback.
- Preserve familiar Sudoku rules and optional assistance, while adding differentiated teamwork, meaningful replayability and natural onboarding.
- Play from separate phones on same Wi-Fi (no remote multiplayer promise at launch); no account required.
- Host-authoritative state with appropriate per-player projections; protect answers/clues when Split Clues is introduced.
- Do not wipe player saves; treat serialization schema defaults, Android signing and package identifiers seriously.
- **GitHub is development memory**: after a substantive pass, update this handoff, roadmap statuses and testing evidence, link from default README, note known issues/next tests and preserve a known-green checkpoint. Keep the source and docs in GitHub even if chats fill up.
- The user typically wants actual changes, verified APKs **downloadable directly in chat**, and meaningful before/after emulator screenshots where possible. Do not claim a tool build succeeded without confirming a completed green CI run.
- GitHub Actions public QA workflows are opt-in/manual; avoid unplanned automatic runs and resource waste.
- The `main` and `development` copies of QA/emulator workflows now both dispatch from the real `development` source by default. **Do not treat a successful workflow from a stale branch as evidence for untested current changes.** GitHub stores history remotely; for disaster resilience, periodically clone or export the repository to a separate device/drive as a truly independent backup.

## 7. New-chat recovery procedure

1. Open [repo README on `main`](https://github.com/PlayerEden/SudokuTogether/blob/main/README.md), which points here.
2. Fetch the **latest** [`development`](https://github.com/PlayerEden/SudokuTogether/tree/development) and this file. **Also check the newest branch, draft PR, issues and actual GitHub Actions run**, since this static handoff may lag behind live work.
3. Consult [roadmap](ROADMAP_V08_TO_LAUNCH.md), [current milestone audit](V0.10A_DAILY_CHALLENGE_QA.md), and relevant tests/design before editing.
4. For any new feature, create a small branch off the newest verified source and note which checkpoint it descends from. Preserve previous milestone source in Git history and checkpoint refs.
5. Implement, review, run core/Android tests and emulator QA, note manual gaps. Deliver APK when verified. Update the **living handoff**, root pointer if branch changes, and `development` **only after the milestone meets its stated acceptance gate**.
6. If something fails, recover from the checkpoint branch or last known-green commit, not from a guess about what a previous chat accomplished.

## 8. Source-of-truth precedence

Actual committed source code + test results > verified CI runs > this living handoff > draft design/plans > chat discussion. A design proposal is **not** an implemented feature. Never infer a Play release or two-phone test from a passing emulator build.
