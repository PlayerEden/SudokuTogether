# Sudoku Together
## Project Status, Roadmap & Possible Finished Vision

**Status date:** November 2, 2026  
**Report type:** Development snapshot and strategic direction (not a release announcement)  
**Working title:** Sudoku Together; final storefront identity undecided  
**Repository:** https://github.com/PlayerEden/SudokuTogether  
**Canonical source branch:** development  
**Verified gameplay baseline:** v0.10B (Android QA), checkpoint \`checkpoint/v0.10b-qa-green\`

> **Project position:** The Android Sudoku game has a functioning and emulator-tested core, multiple modes, offline daily puzzles, player records, and local customization. The outstanding work is primarily product polish, actual-device acceptance, Google Play readiness, and a deliberate decision about whether multiplayer must be launch-ready. The two-phone physical test is explicitly postponed until the developer is ready.

## 1. Executive summary

**What exists:** A native Kotlin/Jetpack Compose Android Sudoku app. Solo is offline-first, with rated Easy/Medium/Hard generated puzzles, saved sessions, notes, Undo, assistive hints and records. An Easy offline Daily Challenge provides date-stable boards, independently saved progress, recent history and completion summaries. Same-Wi-Fi co-op and private-board versus are implemented in code, alongside room chat, player appearance and local achievements.

**What is verified:** v0.10B completed full Gradle build/unit/lint/APK checks and two Android emulator test runs, each reporting **10/10 passing interaction tests**. The checked code lives at commit \`83df54621bb2713917b518ee8ccfed4c25511a80\`; later changes to \`development\` are documentation-only. This proves a working automated baseline, **not** certification on physical phones or Google Play.

**What is outstanding:** Thorough single-phone playtesting and visual/accessibility review, durable-update/signing checks, API-level upgrade (current target is 35; Google Play requires 36 for new standard Android app submissions as of August 31, 2026), distinct branding/store assets, privacy declarations, app bundle and Play testing. Real two-phone LAN QA is pending by choice. Hidden-information **Split Clues** is a proposal, not an implemented mode.

**Recommended direction:** Finish a focused v0.10C user-experience and save-reliability pass, then v0.11 release engineering and branding. Keep the LAN test in a separate deferred gate. Choose between a **Solo + Daily first release** and a **complete Together launch** before finalizing the store listing.

## 2. Product identity and design intent

**Core promise:** Sudoku should be immediately recognizable, pleasant to play with one hand, usable offline, and rewarding to revisit. When two phones are available on trusted local Wi-Fi, collaborative and competitive modes should add genuine social value instead of turning Sudoku into a server-dependent experience.

**Design pillars**

- **Authentic Sudoku:** standard 9 × 9 rules, unique puzzle solutions, logical difficulty labels, no artificial “hard” rating based only on clue count.
- **Player ownership:** progress saved locally; reasonable Undo, notes, explanations and accessible controls; no account necessary for core play.
- **Optional help, not forced autoplay:** Solo hints explain one logical step. Assisted completions are distinguished from unassisted records.
- **Meaningful replay:** new puzzles, per-difficulty progress, daily boards, honest records, gentle achievements and unlockable visual choices.
- **Local social play:** co-op and head-to-head on the same Wi-Fi, with host authority and explicit privacy boundaries.
- **No feature bloat at launch:** avoid pay-to-win, manufactured global rankings, unnecessary logins, or general-game-engine refactoring before Sudoku is good.

**Audience and positioning (hypothesis, not validated research):** Casual Sudoku players who want a calm offline puzzle experience, plus pairs who want to solve or race together locally. This should be tested with real players, not treated as proven market demand.

## 3. What is actually implemented

| Area | Current status | What exists / important boundary |
|---|---|---|
| Solo classic Sudoku | **Implemented; automated QA** | 9 × 9 game, timer, notes, erase, Undo, restart/resume, locally recorded finishes. |
| Puzzle supply | **Implemented; automated QA** | Generated unique-solution puzzles, recent-ID avoidance, Easy/Medium/Hard based on human-technique grading. |
| Assistance | **Implemented; automated QA** | Solo-only logical hint and optional nearby pencil-mark cleanup; Undo restores automatic note changes. |
| Fair personal records | **Implemented; automated QA** | Unassisted versus assisted times, per difficulty; legacy records preserved. |
| Offline Daily Challenge | **Implemented; automated QA** | One reproducible Easy puzzle per device-local calendar date; no internet or public ranking. |
| Daily save/history | **Implemented; automated QA** | Daily and ordinary Solo saves are independent; Continue/Review old saved days; safeguards protect unfinished boards. |
| Completion feedback | **Implemented; automated QA** | Saved elapsed time, difficulty, move count, assistance status and hints on Solo/Daily finishes. |
| Identity, achievements, cosmetics | **Implemented; automated QA** | Local name/profile, earned milestones, number colors/fonts, board styles and emblems. |
| Same-Wi-Fi co-op | **Coded; physical acceptance pending** | Host/join, shared numbers and notes, room code, chat, reconnect logic and host-authoritative state. |
| Same-Wi-Fi versus | **Coded; physical acceptance pending** | Separate private boards, race completion/winner logic and local results. |
| Split Clues | **Planned, not implemented** | Each partner would receive distinct clues with rigorous hidden-information protection. |
| QR invite / remote rooms / cloud accounts | **Not in verified build** | QR invite planned after LAN gate; internet multiplayer/accounts are longer-term possibilities. |
| Google Play production release | **Not complete** | No verified final brand, production AAB/signing/store submissions, or physical-device acceptance. |

**A note on daily retention:** The verified v0.10B code permits at most **128 saved Daily board slots**, favoring unfinished progress and retaining up to **31 completed boards for review** when capacity allows. Finished-game records live separately (subject to the overall local history cap). Do not describe this as cloud backup.

## 4. Verified evidence, source control and outstanding branch

**Current verified checkpoint:** \`checkpoint/v0.10b-qa-green\` at \`83df54621bb2713917b518ee8ccfed4c25511a80\`. The \`development\` branch is the full app source; the default \`main\` is currently a docs/CI landing page, **not** the complete Android project.

**Automated validation:**

- **Build / core / Android JVM / lint / APK:** https://github.com/PlayerEden/SudokuTogether/actions/runs/40621938766 — passed.
- **Android emulator gameplay interactions:** https://github.com/PlayerEden/SudokuTogether/actions/runs/40622291879 — 10/10 passed.
- **Second emulator interaction run:** https://github.com/PlayerEden/SudokuTogether/actions/runs/40623046297 — 10/10 passed.
- **Audit details:** https://github.com/PlayerEden/SudokuTogether/blob/development/docs/V0.10B_DAILY_HISTORY_QA.md

**Limit of that evidence:** The screenshot automation did not reliably capture the new Daily History and completed-game panels. The emulator interacted with those screens successfully, but that does not establish visual quality at large fonts or on a real device. There is no verified two-phone field test.

**Separate, unreconciled experimental work:** Branch \`feat/v0.10a-daily-save-safety\` diverges from \`development\` (2 commits ahead, 14 behind at this review) and modifies the daily retention code and tests. Its changes are **not** part of the latest green checkpoint. Review and cherry-pick only genuinely missing fixes after comparing with v0.10B; do **not** merge the old branch blindly. Previous work from it must not be claimed as newly verified.

**Backup reality:** GitHub protects source code, commits, tests, docs and rollback refs. GitHub Actions APK artifacts have short retention, currently configured for about 14 days; a published release asset or independent repository clone/ZIP is preferable for durable binary backup. Android app data is local, \`allowBackup=false\`, and should **not** be assumed recoverable after uninstalling or clearing storage.

## 5. Technology, tools and working resources

| Resource / tool | Current use |
|---|---|
| GitHub: \`PlayerEden/SudokuTogether\` | Canonical source history, PRs/issues, development handoff, audit trail and pinned recovery checkpoints. |
| Kotlin + Jetpack Compose | Native Android app, screens, accessibility semantics and touch controls. No HTML/WebView game. |
| Android Gradle / JDK 17 | Compiles \`:app\` and \`:core\`; Gradle wrapper is in repository. |
| Core Sudoku engine | Unique-solution generation, candidate validation, named human-solving techniques, playable session rules and records. |
| Android local storage | Atomic JSON saved player/session data and device preferences. Solo and Daily progress are independent. |
| LAN networking | Android Wi-Fi / local room discovery plus host-authoritative co-op and versus protocol (wire version 5). |
| GitHub Actions | Manual-only build/unit/lint/APK workflow and Android emulator instrumentation/screenshots. Builds are traced to exact commits. |
| APK QA distribution | Isolated \`.qa\` application ID avoids overwriting a regular app package; a QA build is not a signed store release. |
| Google Play / Play Console | Intended distribution route; requires up-to-date target API, signing, listings, declarations and any applicable tester gates. |
| Developer documentation | The repository's \`CONTINUE_HERE.md\`, \`docs/PROJECT_HANDOFF.md\`, roadmap, milestone QA audits and issue tracker are the continuity system. |
| Reusable puzzle foundation design | A separate planning document and issue; **paused** until Sudoku is stable and finished. |

**Inspected Android configuration (v0.10B):** \`applicationId = com.playereden.sudokutogether\`, \`minSdk 26\`, \`compileSdk 35\`, \`targetSdk 35\`, \`versionCode 14\`, \`versionName 0.10.0-dev2\`. The Android manifest requests internet/network/Wi-Fi/multicast permissions used by local networking. A focused privacy/permission review is still needed.

**Primary internal links**

- GitHub repository: https://github.com/PlayerEden/SudokuTogether
- New-chat recovery page: https://github.com/PlayerEden/SudokuTogether/blob/main/CONTINUE_HERE.md
- Living project handoff: https://github.com/PlayerEden/SudokuTogether/blob/development/docs/PROJECT_HANDOFF.md
- Product and release roadmap: https://github.com/PlayerEden/SudokuTogether/blob/development/docs/ROADMAP_V08_TO_LAUNCH.md
- Current milestone audit: https://github.com/PlayerEden/SudokuTogether/blob/development/docs/V0.10B_DAILY_HISTORY_QA.md
- Latest verified checkpoint: https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.10b-qa-green
- Release issue: https://github.com/PlayerEden/SudokuTogether/issues/14
- Multiplayer gate issue: https://github.com/PlayerEden/SudokuTogether/issues/12
- Reusable-foundation concept: https://github.com/PlayerEden/SudokuTogether/blob/development/docs/REUSABLE_ANDROID_PUZZLE_FOUNDATION.md

## 6. Proposed next phases, in practical order

### Phase A — v0.10C: finish the one-phone experience

**Objective:** Make everyday Solo and Daily play intuitive, satisfying and resilient before inventing more modes.

- Get reliable, **Compose-owned screenshots** of Daily History, the end-of-game result and active gameplay; inspect clipping and hierarchy rather than relying only on test tags.
- Test compact portrait/landscape, larger Android font scaling, accessibility announcements, touch target sizes and Back navigation.
- Improve first-play explanation, completion/replay actions, Daily review flow, and any awkward scrolling/overlap.
- Decide whether an optional forgiving daily streak or small set of daily achievements genuinely improves retention; avoid pressure to play every day or false comparisons between assisted and classic solves.
- Reconcile the stale daily-save experimental branch against the tested v0.10B retention logic.
- Add regression tests, confirm install/update and local-save compatibility on a physical phone, and pin a new known-green checkpoint.

**Exit criteria:** The screens are visually reviewed, saves remain safe, key journeys pass automation and at least one real Android device, and no unresolved high-severity Solo/Daily bug remains.

### Phase B — v0.11A: Android release foundation

**Objective:** Be able to produce a valid, reproducible, updatable production build.

- Upgrade to the applicable Google Play target level: **API 36 minimum for standard new app submissions since August 31, 2026**; verify the exact requirement again at actual submission.
- Update necessary SDK/Gradle dependencies, run compatibility tests, check adaptive layouts and Android permission behavior.
- Decide production signing key custody and Play App Signing; produce and verify a signed Android App Bundle (\`.aab\`). Never commit private signing passwords/keys into a public repository.
- Validate release-install/update pathways, data retention and configuration independent of the QA \`.qa\` package.
- Review app data/backup/recovery choices; optional export/import is attractive but must not delay essential reliability unless it is a product requirement.
- Consolidate the full verified source into default \`main\` after planned integration validation (issue #18); preserve source history and checkpoints.

**Exit criteria:** Reproducible compliant AAB, protected signing process, passing test suite, stable upgrade path and reliable GitHub release documentation.

### Phase C — v0.11B: product identity, store presentation and privacy

**Objective:** Present an original, comprehensible Sudoku product to testers and to Google Play.

- Resolve the **working title**, including a distinctiveness/store/trademark search; avoid confusing existing similarly named apps.
- Finalize adaptive icon, splash/launcher assets, typography, settings, color contrast, store screenshots and short/long listing copy.
- Publish a policy accurate to offline profile storage, Wi-Fi connection/chat, sharing, permissions and any actual SDK/data behavior. Google Play requires a privacy policy and complete Data Safety declaration.
- Provide in-app privacy access and support details; run content-rating and other Play Console setup.
- Check developer-account-specific testing gates. Certain newer **personal** Play accounts require **12 continuously opted-in closed testers for at least 14 days** before requesting production access; do not assume account eligibility without checking.
- If claiming multiplayer in the listing, complete the physical multiplayer acceptance first.

**Exit criteria:** All supplied assets and policy declarations accurately represent the app; store test track accepts the signed bundle and required test process.

### Phase D — v0.9A/B/C: multiplayer gate, when the developer is ready

**v0.9A: real two-phone QA (explicitly deferred).** On two matching Android devices over the same Wi-Fi: host/join/discovery, notes, simultaneous edits, safe Undo, chat, race results, background/restart, reconnect and persistence. Document failure cases and fix them; never mark this passed from emulator-only tests.

**v0.9B: optional QR/invite convenience.** Improve local room discovery and sharing only after real network behavior is measured. QR should contain only the appropriate local endpoint/invite data and should not imply worldwide matchmaking.

**v0.9C: possible Split Clues.** A distinctive cooperative puzzle where each player sees some starting clues. This requires strict host-approved projection so private clues cannot leak via guest snapshots, notes, saves, screen readers, hints or reconnect. It is **not required** for a simpler first launch and should remain blocked behind v0.9A.

## 7. Possible definition of “finished”

There are **two credible launch scopes**, plus longer-term ambitions. Neither is a formal commitment yet.

### Option 1 — Focused first release: Solo + Daily

A polished offline Sudoku app featuring fresh puzzles, honest rated difficulty, accessible notes/Undo/hints, daily challenge/history, personal records, offline-earned cosmetics and dependable saved progress. Co-op/versus would be excluded from the public launch promise (and should be disabled or clearly beta-labelled if available in the binary) until physical LAN acceptance is complete.

**Why choose it:** A smaller, independently testable product can reach players earlier, while multiplayer continues separately.  
**Main condition:** It still needs actual phone QA, store compliance and an accessible, stable player experience.

### Option 2 — “Together” release: Solo + Daily + verified local multiplayer

Everything in Option 1, plus two-phone same-Wi-Fi co-op and private-board races that reliably connect, recover and save, with clear invitation instructions. **Split Clues and QR invitation may enhance this version, but need not be present to make it a real finished v1** if the basics are excellent.

**Why choose it:** The social differentiation is stronger and matches the project's working title.  
**Main condition:** Real two-device testing and network reliability become launch blockers, not optional polish.

### Potential later evolution, *not* the launch definition

- Split Clues as a signature co-op mode; rotating/differentiated daily challenges after proper calibration.
- Better cooperative milestones, cosmetic collections and quality-of-life accessibility improvements.
- Remote internet multiplayer, friends, accounts and cloud sync **only if** the maintenance/backend burden and user demand justify it.
- A reusable Android puzzle foundation and future Crossword Together / Minesweeper Together **only after** Sudoku has a stable reference implementation and tests.

**Shared definition of done for any published scope:** A new player can install, understand, play, finish and return without losing progress; rules and achievement labels are truthful; UI works on supported Android devices; there are no critical crashes or unrecoverable save failures; legal/store requirements are met; a release AAB is signed and update-tested; screenshots, tests, docs and the actual published feature set agree.

## 8. Risks, decisions and controls

| Risk or unresolved decision | Why it matters | Proposed control / decision |
|---|---|---|
| Two-phone play remains untested | Co-op and races are coded but not physically certified. | Keep gate deferred; do not advertise verified multiplayer or Split Clues before hands-on tests. |
| API 35 currently targeted | Google Play new standard app submissions require API 36. | Prioritize upgrade plus Android compatibility regression before store submission. |
| Local save loss on uninstall/data clear | \`allowBackup=false\`, no cloud recovery or user export is verified. | State limitation clearly; test upgrades and consider safe export/import or Android backup policy. |
| QA APK versus production signature | Debug \`.qa\` builds do not prove production upgrade path. | Protect release key and exercise signed update install with retained real data. |
| Daily/Profile visual review incomplete | Emulator screen navigation passed but screenshots are not a full layout review. | Capture Compose-owned images and inspect actual devices, larger fonts, orientation. |
| Title may not be distinctive | Store confusion, legal checks and weak search identity. | Finalize brand using storefront and trademark checks before asset production. |
| Old daily-save side branch is divergent | Blind merge could regress the verified retention rules. | Compare commits, isolate any missing patch and rerun full QA; preserve both histories. |
| Default GitHub \`main\` is skeletal | A new chat/developer may mistake it for a missing project. | Continue using \`development\` and handoff now; plan safe tested source consolidation. |
| More scope than testing time | Optional features can postpone a real release indefinitely. | Choose explicit launch scope; prioritize blockers and reliability over novelty. |

**Current choices already made:** Development is native Android, offline-first, no mandatory account, source history in GitHub, and the reusable framework is postponed. Physical two-phone testing is postponed **by developer choice**, not cancelled or certified.

**Decisions still open:** Final product name; Solo-first versus verified-multiplayer launch scope; daily streak/achievement depth; whether local export/import is launch-critical; exact release key and Google Play account/tester arrangements; whether Split Clues is part of v1 or a follow-up.

## 9. Release readiness checklist

- [x] Native Android app and Sudoku rules implemented.
- [x] Solo, rated puzzles, hints, records, Daily Challenge and Daily History implemented.
- [x] Last verified v0.10B full Android build and 10/10 emulator runs passed twice.
- [x] GitHub development source, living handoff, roadmap and rollback checkpoints exist.
- [ ] Visual QA: Daily History/results and large-font/landscape/TalkBack matrix.
- [ ] Real single-phone installs, upgrades, saves, offline play and process-restart acceptance.
- [ ] Review and reconcile experimental daily-save side branch.
- [ ] API 36+ migration and revalidation.
- [ ] Distinct production branding, adaptive icon and storefront assets.
- [ ] Production signing/AAB and tested stable upgrade identity.
- [ ] Privacy policy, Data Safety, content rating, support details and required Play testing.
- [ ] Decide launch scope; physical two-phone acceptance if multiplayer is advertised.
- [ ] Split Clues / QR only if selected and their prerequisites are validated.
- [ ] Confirm no release-critical issues; archive checksum, release notes and rollback procedure.

## 10. Where this report fits in our workflow

**This is a dated snapshot, not the living handoff.** At the start of the next chat or development pass: open \`main/CONTINUE_HERE.md\`, then \`development/docs/PROJECT_HANDOFF.md\`, then compare active PRs/issues, checked source and latest green Actions runs. Treat this report as the **project vision and status baseline**, not authority for a newer build that may exist after November 2.

**Update rhythm:** After each substantial milestone, record the branch, commit, feature delta, bug fixes, automated QA links, manual test gaps and exact next action in \`PROJECT_HANDOFF.md\` and the roadmap. Pin a new checkpoint only after its tests pass. Publish one reproducible APK/AAB with checksum when appropriate. Store secrets outside GitHub.

**Source precedence:** Verified code + reproducible QA evidence > current GitHub handoff and issue state > this dated report > older chat discussion. Do not turn proposed ideas into “completed” items without code and test evidence.

## References and official release resources

**Project records (inspected November 2, 2026)**  
1. https://github.com/PlayerEden/SudokuTogether/tree/development  
2. https://github.com/PlayerEden/SudokuTogether/blob/development/docs/PROJECT_HANDOFF.md  
3. https://github.com/PlayerEden/SudokuTogether/blob/development/docs/ROADMAP_V08_TO_LAUNCH.md  
4. https://github.com/PlayerEden/SudokuTogether/blob/development/docs/V0.10B_DAILY_HISTORY_QA.md  
5. https://github.com/PlayerEden/SudokuTogether/actions/runs/40621938766  
6. https://github.com/PlayerEden/SudokuTogether/actions/runs/40622291879  
7. https://github.com/PlayerEden/SudokuTogether/actions/runs/40623046297  

**Official Google Play policies (verified November 2, 2026)**  
8. Android target API requirements: https://support.google.com/googleplay/android-developer/answer/11926878  
9. User Data and privacy policy: https://support.google.com/googleplay/android-developer/answer/10144311  
10. Data Safety requirements: https://support.google.com/googleplay/android-developer/answer/18265346  
11. New personal developer account testing: https://support.google.com/googleplay/android-developer/answer/14151465

**Report boundary:** No downloads, installs, physical device measurements, production Play Console actions or store listing changes were performed for this report. The project and policies can change; verify release requirements again at submission.
