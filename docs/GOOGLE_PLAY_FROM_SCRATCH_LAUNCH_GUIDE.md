# GridMates: Sudoku — Google Play From-Scratch Launch Guide

**Created:** 2026-11-03  
**Audience:** Project owner (first-time publisher, **no Play Console account yet**) and separate Work/implementation chat  
**Status:** Practical setup and release plan; **NOT** evidence of account creation, Play approval, uploaded builds, ads or payments  
**Canonical game repo:** `PlayerEden/SudokuTogether`; active code is on development and later stacked branches  
**Confirmed business direction:** **FREE** game, **rare interstitial ads** between completed puzzles, optional one-time **Remove Ads** purchase. No permanent banners, no paid hints, no pay-to-win, and no subscription requirement. Future earnable cosmetics and optional cosmetic purchases are separate ideas.

> **Start here:** The owner has **not yet created a Google Play Console developer account**. First steps require the owner to create, verify and pay for an account using Google, not GitHub or the Android build. Do not represent preparation as a Play release. See also [Monetization Plan](GRIDMATES_MONETIZATION_AND_PLAY_TESTING_PLAN.md), [Project Handoff](PROJECT_HANDOFF.md), [Brand Plan](GRIDMATES_BRAND_AND_SERIES.md), and [Visual Identity Brief](GRIDMATES_VISUAL_IDENTITY_BRIEF.md).

## 1. Five names that sound alike but do different jobs

| Service | Plain-language meaning | Needed when |
|---|---|---|
| **Google Play Store** | The storefront where Android users download GridMates | Distribution |
| **Google Play Console** | The developer account/dashboard for listings, testers, releases and policy forms | **First** |
| **Google Play testing tracks** | Private/invite/open ways to distribute builds before/around launch | After the account and first build |
| **Google AdMob** | The ads network and publisher payment system | **Later**, after core testing |
| **Google Play Billing** | The checkout and purchase entitlement for one-time Remove Ads | When monetization is implemented |
| **Play Games Services** | Optional game identity/achievements/features; **NOT automatically cloud backups** | Future optional integration |

### Who does what?

**Project owner:** creates/manages the Play Console account; chooses personal vs organization, submits legal and payment details privately to Google, verifies identity/device, approves listings and policies, recruits testers, manages rollout, creates AdMob/merchant setup when appropriate. Do **not** post personal IDs, passwords, tax info, payments credentials, keystores or API credentials in public GitHub issues.

**Work/implementation chat:** audits code/permissions; makes release-safe Android builds; prepares AAB/artwork/policy draft; implements and tests Billing/AdMob only when scheduled; assembles QA evidence and checklists. It cannot confirm a Play Console account exists without the owner completing verification or sharing non-sensitive status.

## 2. First thing to do: create a Play Console account

1. Pick the Google account that should **own** the developer account for the long term. Turn on strong sign-in and two-step verification; protect recovery methods.
2. Go to **https://play.google.com/console/** and start developer registration. Minimum age is **18**.
3. Choose **Personal** if publishing as an individual; choose **Organization** if publishing for a legally established business. An organization registration generally involves business verification and a **D-U-N-S number**. *Do not set up an organization account only for cosmetic branding.*
4. Pay the **$25 USD one-time registration fee** to Google (not a monthly subscription).
5. Complete requested developer information: public-facing developer name, legal name/address, account contact email/phone, and public developer contact email; verify identity. Read what Google says will be publicly displayed before deciding what contact/address to provide.
6. For a new **Personal** account, complete the **Android-device verification** task in Play Console using the official Play Console mobile app on a supported real, non-rooted Android phone (Android 10+ per current help).
7. Wait for all required account verification tasks to show complete. **Do not assume payment alone enables publishing.**

**What the owner can prepare now:** a stable Google login; a developer display name; a dedicated public support email; identity documentation and payment method for Google; and a plan for recruiting 12+ beta testers. **Do not put sensitive registration materials in GitHub.**

Official help:
- [Start a Play Console developer account](https://support.google.com/googleplay/android-developer/answer/6112435)
- [Required information, Personal vs Organization](https://support.google.com/googleplay/android-developer/answer/13628312)
- [Verify a physical Android device for new Personal accounts](https://support.google.com/googleplay/android-developer/answer/14316361)

## 3. Create the GridMates app entry — still NOT public

Once the account is verified, in Play Console choose **Create app**.

**Recommended entries** (as of this plan):
- App/game name: **GridMates: Sudoku** (approved public name; verify naming/store conflicts before public promotion).
- Type: **Game**.
- Main language: **English (US)** unless owner chooses otherwise.
- Pricing: **Free** (**approved**). Important: an app previously offered for free on Google Play **cannot later become a paid-download listing**; a distinct listing/package would be required. The earlier $0.99 paid-download proposal has been superseded.
- Public support/contact email: owner's chosen support address.
- App package identity: Work chat must confirm the permanent choice before the initial release. **Current code package/application ID is `com.playereden.sudokutogether`**, and QA/debug installs use `.qa`. **Do NOT automatically rename the package to match GridMates;** visible title and internal application ID are different things. Resolve continuity, signing, saves and distribution first.

Creating this app entry is **not publishing the game**.

Official help: [Create and set up an app](https://support.google.com/googleplay/android-developer/answer/9859152); [Free vs paid pricing](https://support.google.com/googleplay/android-developer/answer/6334373).

## 4. Understand APK versus AAB and app signing

- **APK:** direct install file, such as the QA builds currently sideloaded.
- **AAB:** Android App Bundle; the normal upload format for new Play releases. Google generates optimized device downloads from it.
- **Play App Signing:** Google's distribution signing system. Create/protect an upload signing key, choose the app signing approach, and keep recovery documentation and key backups private.

The **production** package ID, versionCode, signing identity, update path, and safe save migration are release-critical. Old QA debug keys are **not** interchangeable with production signing. **Never tell the owner to uninstall a progress-bearing app just to bypass a signature mismatch.**

Official help: [Prepare and roll out releases](https://support.google.com/googleplay/android-developer/answer/9859348) and [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756).

## 5. Google Play testing explained

| Track | Plain English | Plan for GridMates |
|---|---|---|
| **Internal testing** | Private link to selected testers, up to **100**. | Start with owner, family and trusted friends; test installation, saves, gameplay. |
| **Closed testing** | Invited testers; collect realistic feedback. | Main quality gate and potentially **mandatory** for new Personal accounts. |
| **Open testing** | Public test listing so more people can join. | Consider once eligible and stability is proven; not necessarily first. |
| **Production** | Official public release in chosen territories. | Only when all release, QA and policy gates pass. |
| **Pre-registration** | People express interest in an upcoming launch; not the same as playable beta. | Optional near release rather than too early. |

### Mandatory gate likely applicable to this owner

For a **new Personal Play developer account**, Google currently requires a **closed test with at least 12 opted-in testers for 14 continuous days** before applying for production access. Google then reviews production-readiness answers; completing 14 days alone does **not** automatically grant publication. **Internal testing does not satisfy the closed-test requirement.** Open testing and pre-registration may be unavailable until production access is granted.

**Practical plan:** recruit more than 12 so a tester dropping out doesn't ruin the target; provide real instructions and collect actual usage/feedback rather than merely sending an opt-in link. Check the exact eligibility panel in the owner's newly created account.

Official help: [New Personal account testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465) and [Testing and releases](https://support.google.com/googleplay/android-developer/answer/9859348).

## 6. Work-chat engineering release gates — current state, not promises

**Known GitHub status at guide creation:** `development` holds the previously promoted v0.10B code plus recent planning docs. **Draft PR #23** (`fix/v0.10c-save-integrity-polish`) and **draft PR #25** (`feat/v0.10d-game-visuals`, stacked on PR #23) include newer save-safety and visual changes. Their QA docs report **87 JVM tests + 42 Android 8/API 26 emulator cases** passing for v0.10D. Both PRs are **unmerged** as of this guide; do **not** restart release work from the older v0.10B source and discard those branches. Preserve validated checkpoints and avoid cross-chat branch conflicts.

Release readiness requirements to address with actual evidence:
- [ ] Confirm canonical branch, integratability of PR #23 then #25, regression results and checkpoints.
- [ ] Final public `GridMates: Sudoku` user-facing name, original launcher/adaptive icon, store visuals and screenshots. Name/trademark/store-confusion review before broad launch.
- [ ] **Target API 36 or higher** for ordinary new Android apps/updates as of 2026-08-31; current v0.10D target API is **35**, so it needs update and compatibility QA. Review local-network behavior/permissions on newer OS versions when making this change.
- [ ] Signed release AAB with owner-approved production app ID/key strategy; release build reproducible, verified and free from private secrets.
- [ ] Safe update/saves: test fresh installs, upgrades, force-stop, rotations, date rollovers, daily archive and restored records. Local recovery does **not** currently equal cloud/uninstall backup.
- [ ] Modern Android physical device testing, small screens, portrait/landscape, font scaling, accessibility/TalkBack, app startup/crashes.
- [ ] **Two real phones on same Wi-Fi**, host/join, conflicts, undo, pause, reconnect and versus result tests **before advertising multiplayer as release-verified**. This test was previously deferred by owner; must be scheduled when feasible. Split Clues remains blocked.
- [ ] App permissions, content rating, privacy policy, Data safety declaration, ads declaration and an accessible support contact.
- [ ] Validate any ad/billing SDK and consent implementation separately, if monetization is part of that release.

Reference audits: [PR #23](https://github.com/PlayerEden/SudokuTogether/pull/23), [PR #25](https://github.com/PlayerEden/SudokuTogether/pull/25), [v0.10C QA](https://github.com/PlayerEden/SudokuTogether/blob/fix/v0.10c-save-integrity-polish/docs/V0.10C_SAVE_INTEGRITY_QA.md), [v0.10D QA](https://github.com/PlayerEden/SudokuTogether/blob/feat/v0.10d-game-visuals/docs/V0.10D_VISUAL_QA.md).

API policy: [Google Play target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878).

## 7. Fill out the store listing and Google's policy questions

The owner and Work chat can work together on these, with the owner reviewing/approving final claims:

**Listing:** title; concise short description; full description; game category/tags; icon; feature graphic; accurate screenshots from real gameplay; support email; countries/regions; Play's current listing and screenshot rules.

**Policy & safety:** public privacy-policy URL; **Data safety** answers about app and SDK data; whether it contains advertising; content rating questionnaire; target audience/age group; app access requirements (if any); permissions and any applicable declarations. **Do not invent claims** like cloud saves, online rooms or global ranks.

Important: entering the **closed test** track generally requires more completed app setup and Data safety review than simply using internal testing. The Data safety form applies to apps on closed/open/production tracks (internal-only apps have an exemption until expanded).

Official help: [App content and review](https://support.google.com/googleplay/android-developer/answer/9859455), [Data safety](https://support.google.com/googleplay/android-developer/answer/10787469), [Store listing setup](https://support.google.com/googleplay/android-developer/answer/9859152).

## 8. Planned ads — **not required for the first technical beta**

**Approved product direction:** The game is **free**, with **rare interstitial (full-screen) ads** only *between completed puzzles*, never inside active gameplay or on the completion celebration itself; no banners. Players should never need ads to receive a hint. App should never delay navigation because an ad failed to load.

**Proposed pacing to test, not an approved exact number:** no ad for first completed puzzle; require multiple completions and a significant cooldown (e.g., 20–30 min), defaulting to fewer/no ads when in doubt. Consider stricter per-session caps. Avoid all ads during co-op, versus, room joining and recovery.

**Actual technical work, later:** owner creates an AdMob account; app creates proper test/demo ad units and privacy/consent controls; QA validates frequency, dismissibility, user navigation, no interrupted saves, correct disclosure. **Do not click real ads yourself** to test. In Google AdMob, new apps require an **app-readiness review**, normally after a **publicly available supported-store listing** is linked; therefore closed-beta test ads must not be misrepresented as live ad monetization.

Official help: [AdMob app readiness](https://support.google.com/admob/answer/10564477), [Test ads](https://support.google.com/admob/answer/9388275), [User Messaging Platform consent](https://developers.google.com/admob/android/privacy).

## 9. One-time Remove Ads purchase — also later

Use **Google Play Billing** for a **non-consumable one-time product** (not a subscription). Valid purchase should disable standard interstitials permanently and restore on another eligible device using the purchasing Google account. The previously discussed **$2.99 is a price idea, not final owner approval**. Configure billing and Google payments/merchant details as needed when implementing; Play and AdMob payments are separate systems.

**QA:** successful/failed/pending/canceled purchase, interrupted flow, restored purchase, refunds, offline access and account changes. Do not treat a local `removeAds=true` flag as authoritative. Google Play purchase entitlement is **not a substitute for profile and puzzle-save backup**.

Official help: [One-time Play Billing products](https://developer.android.com/google/play/billing/one-time-products) and [Play developer payment profile](https://support.google.com/googleplay/android-developer/answer/3092739).

**Future only:** earnable cosmetic collection and optional cosmetic purchases, possibly randomized packs. Paid randomized items would require truthful odds and appropriate compliance. They are **not required to publish Sudoku**.

## 10. Useful release sequence — no fixed deadlines

1. **OWNER:** open/register Play Console; choose account identity and secure it; pay $25; complete legal and device verification.
2. **OWNER + WORK:** create GridMates app record as Free; confirm final title, developer name, package ID decision and region.
3. **WORK:** integrate the best verified game source, target API 36+, polish branding, test/produce signed release AAB and QA evidence.
4. **OWNER + WORK:** complete listing and initial disclosures; **start internal testing without real ads**.
5. **OWNER:** recruit testers; begin required closed testing when eligible; gather notes from real players and two-phone LAN sessions.
6. **WORK + OWNER:** triage crashes and usability, preserve saves, update beta AAB through Play, get close to a reproducible release candidate.
7. **OWNER + WORK:** if still desired for launch, build/test rare interstitials, consent and a one-time Remove Ads purchase; deal with AdMob readiness constraints and don't claim live serving prematurely.
8. **OWNER:** after any personal-account closed-test requirement, apply for production access; answer Google's review questions accurately.
9. **OWNER:** approve store listing/privacy/price regions; publish after all quality gates, and monitor Android vitals and player feedback.
10. **FUTURE:** consider Play Games achievements, genuine progress portability/cloud saves, cosmetic collections and GridMates: Crossword. These are not blockers to beginning testing.

## 11. Common traps to avoid

- Paying the developer fee does **not** automatically publish an app.
- A GitHub Actions success or an installed QA APK does **not** equal Play Store acceptance.
- Sending 12 invitation emails does **not** mean 12 testers have opted in for 14 continuous days.
- A developer's own physical Android device verification does **not** replace game QA on physical devices.
- Play Games Services login is **not** automatic backup; current app disables Android Auto Backup.
- Changing the visible app title does **not** require changing Android `applicationId`; changing package/signing later can break updates and saved data.
- Internal-only tests and demo advertisements are **not** proof of AdMob monetization readiness.
- Don't begin account setup with passwords/identity data pasted into public GitHub or chat.
- Don't publish claims about multiplayer until two-phone acceptance, and don't pretend screenshots are from untouched live gameplay unless verified.

## 12. First-session checklist for the owner

- [ ] Decide which Google account should permanently own the developer registration.
- [ ] Pick a developer display name and an accessible public support email.
- [ ] Open **https://play.google.com/console/** and begin **Personal** registration unless a real business/organization account is required.
- [ ] Complete one-time $25 payment and Google's legal/contact verification.
- [ ] Complete official Android device verification on a real compatible phone.
- [ ] Confirm the dashboard shows the account ready and then create **GridMates: Sudoku** as a **Free Game**.
- [ ] Tell Work chat only the **non-sensitive state**: account created? identity/device verification complete? app entry created? testing tracks available? **Never send passwords or private keystores.**

**Stop here until those account tasks are done.** The Work chat can separately continue technical Play readiness work without account credentials.

## 13. Keep this guide current

Google policies and Play Console navigation can change. The official links above were reviewed on **2026-11-03**; recheck at each submission. Update this file with real account/test status and dated verification, without adding sensitive information to this public GitHub repository. Distinguish `planned`, `implemented`, `tested`, `uploaded`, `approved`, and `published`.

Do **not** change source code or merge the active PRs solely as a side effect of documentation.
