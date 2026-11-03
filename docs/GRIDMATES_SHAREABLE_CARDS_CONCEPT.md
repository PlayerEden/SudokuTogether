# GridMates: Sudoku — Shareable Cards and Social Sharing (Concept)

**Created:** 2026-11-03  
**Status:** **Owner strongly interested in exploring this; design proposal, not an approved implementation spec or shipped feature.**  
**Audience:** Work implementation chat + GridMates series product planning  
**Related documents:** [GridMates Brand & Series](GRIDMATES_BRAND_AND_SERIES.md), [Puzzle Coach and Scoring Proposal](GRIDMATES_PUZZLE_COACH_AND_SCORING_PROPOSAL.md), [Monetization Plan](GRIDMATES_MONETIZATION_AND_PLAY_TESTING_PLAN.md), [Google Play Launch Guide](GOOGLE_PLAY_FROM_SCRATCH_LAUNCH_GUIDE.md)

## 1. Owner intent and why it matters

User suggests that players could **share profile cards, their statistics/earned cosmetics, solo completions, Daily results, and local co-op/versus match outcomes** via messaging apps and social media. This supports people who are physically far away while keeping GridMates' intentional *offline-first, nearby-connection* identity. A share is an invited real-world interaction, not automatic entry into a mass online platform. It could also create natural word-of-mouth discovery/advertising for the game.

**Principles:** (1) sharable by choice, never posted automatically; (2) visually appealing and cohesive with GridMates' cream/forest/gold puzzle-book-inspired identity; (3) meaningful and truthful game statistics; (4) offline card generation and normal Android share UI, no custom server or account required; (5) soft and tasteful branding—not aggressive advertising; (6) privacy by default, particularly partner identities and minors.

**Product choice still open:** concrete card formats, exact layouts, score presentation, text copy and shipping milestone. Work should not interpret this as an instruction to pause ongoing QA, Play release work or develop a server.

## 2. Proposed card formats and content

### A. Completion card — **first-priority pilot**

After a valid Solo/Daily puzzle completion, offer **Share result** *next to*, not instead of, **Next puzzle / Review board**. Card can display:
- GridMates brand/emblem, familiar simple tagline candidate: **"Good puzzles. Good company."**
- Mode (Solo/Daily), actual difficulty, completion time, assisted/unassisted, total hints if relevant.
- **Score only if the separate score engine exists, is genuinely tested and is saved for that record.** Do not fabricate scores for old games.
- Optional customized badge/avatar/frame when that item is actually owned.
- Date or streak only if actually supported and correct. Never claim a ranking if there is no ranking service.
- Optional spoiler-safe pattern, such as a non-numbered 9x9 graphic, without publishing the completed board or a Daily Challenge solution.

### B. Profile showcase card

From Profile/Records/Collection: player-controlled display name (or anonymous "GridMates player"), avatar, chosen frame/theme, three favorite badges, truthful solved counts and personal bests **with difficulty and assisted distinction**. Ability to preview and **hide individual fields** (name, record statistics, achievements) before export. Share a static *snapshot*; it does **not** grant other viewers live access to the profile or silently upload player records.

### C. Co-op / versus postcard

After completed match: **Solved together** with time, selected tasteful moment/stat and two approved display names, or **Friendly matchup** with outcome. Player's own result may be shared, but **do not publish guest/partner's display name/avatar or detailed private stats without explicit consent or a partner-approved opt-in.** Safe default is **"You & a friend"**, anonymized partner, or team aliases. Do not include network address, room code, IP, token, private clue/snapshot content. Account for mismatch/reconnect/save errors and do not invent results.

### D. Optional challenge sharing later

A card may carry a verified public **Play Store link** to invite someone to download GridMates when the listing exists. Later possibilities: **play the same puzzle** from a deterministic/shareable seed ID or opt-in lightweight puzzle challenges. That *requires* a versioned puzzle identity, honest sharing and validation to avoid leakage/answer spoilers or incompatible generators. **Not a v1 prerequisite**. A static image alone is not a playable deep link. When appropriate, Android App Links require owned domain + verified Digital Asset Links; a plain store link is simpler.

## 3. Example non-final copy

**Solo:** "A good puzzle deserves a little bragging. Hard Sudoku · 14:26 · 1 hint · Assisted. GridMates — Good puzzles. Good company."

**Daily:** "Today's grid: solved. No spoilers here. GridMates: Sudoku."

**Co-op:** "We figured it out together. 18:04 · GridMates: Sudoku."

**Versus:** "Friendly rivalry, puzzle edition. Play a round with me sometime?"

Keep share language human and restrained. Don't output generic AI-styled marketing paragraphs, exaggerated 'MASTER WINNER' claims or loud watermark overlays.

## 4. Minimal Android-first technical design (no server)

Current **v0.10D** Android code already has a `FileProvider` in `app/src/main/AndroidManifest.xml` and an existing share-A**PK** implementation in `MainActivity.kt`; `app/src/main/res/xml/share_paths.xml` currently authorizes `cache/shared/` for that purpose. **This does not mean image-card sharing is implemented yet.** It is a useful existing primitive to adapt carefully.

Proposed flow:
1. Pull a **read-only, immutable, validated** card model from an existing `GameRecord`/current finished session or `LocalPlayerData` statistics, not from guessed UI state. Store history is on-device, and historical fields may be absent.
2. Render a deterministic 1080×1350 (4:5) or 1080×1920 (9:16) PNG **on-device** using Android native Canvas or a snapshot of a dedicated Compose-rendered export component. Design responsive in-app preview separately from export image size.
3. Save into a **dedicated temporary app cache subdirectory** and export via `FileProvider` URI. Add only required `<cache-path>` grant and expire temporary cache data; do not expose internal profile backups or keys. Use `Intent.ACTION_SEND`, `image/png`, `EXTRA_STREAM`, optional `EXTRA_TEXT` (store URL/short copy), and Android Sharesheet / `Intent.createChooser` with `FLAG_GRANT_READ_URI_PERMISSION`.
4. **Preview → user taps Share → native Sharesheet → chooses an app**. Let user Save image or Copy text/link independently. Never auto-post, send analytics to a server, request contact lists or require social login.
5. Be aware different target social apps handle image and accompanying text/URL differently: **not every share target publishes the `EXTRA_TEXT` alongside a PNG.** If store discovery is important, give a separate **Copy download link** button or rendered subtle legible URL/brand on card. Test WhatsApp, Messages, Instagram, Discord, etc. on actual devices rather than assuming uniform behavior.
6. For already-published games, use a real, verified store URL. Before Play listing exists, **don't invent an app store link or imply it is available**.

Official Android docs:
- [Jetpack Compose: Send data to other apps](https://developer.android.com/develop/ui/compose/sharing/send)
- [Share files securely with FileProvider](https://developer.android.com/training/secure-file-sharing)
- [Android App Links](https://developer.android.com/training/app-links)

### Distinguish old "share test APK" from future "share game"

The current `MainActivity.shareApk()` sends the installed QA/development APK file. For an official Play release, **don't label that as the new user-facing Share Game action**: a verified Play Store link (once available) is safer and easier for ordinary players, while an APK export might remain only for deliberate sideload/dev QA scenarios. Keep **Share result/profile** (PNG export) and **Share game** (link) as separate actions so players understand what will be sent.

## 5. Privacy and fair competition

- **Explicit sharing only.** No background uploading and no public user profiles required.
- **No minors' or partners' names by default**, and no sharing someone else's name just because they're in a LAN session. All name fields are editable/redactable before sharing.
- **Do not show solution digits or hidden clue ownership** on a shared Daily/Split Clues card; avoid spoiling puzzle answers for recipients.
- Label completion as Assisted/Unassisted and indicate hint count accurately; don't pretend a heavily hinted completion is an unassisted record.
- Keep local profiles private. Generating a card is not the same as enabling online profile search. Show users precisely what data is in an exported picture.
- **No sharing quotas, ad-watch prerequisites or forced promotional prompts**. A share icon after finishing is enough.
- Cross-device stats may differ until cloud sync genuinely exists; a card represents a local snapshot at a point in time.
- Partner-identifying data is higher risk if shared outside the room. Could implement partner approval in a later LAN protocol version, but **avoid schema/protocol modifications for a cosmetic screenshot pilot**.

## 6. Visual and UX requirements

- Card should look like a polished collectible from a cohesive family of GridMates games, *not a screenshot of the entire app UI* with buttons, browser chrome or a phone status bar.
- Three potential variants: **Compact** (messenger 1:1 or 4:5), **Story** (9:16), **Private** (anonymized, no name); initially implement one ratio well.
- Keep the Sudoku 9×9 pattern iconic but subtle; use muted cream/forest/gold and full contrast/readability.
- Showcase **earned** cosmetics authentically; do not preview or falsely imply unlocked items are owned.
- No tapbait marketing. Use a modest GridMates label or tasteful “Good puzzles. Good company.” footer.
- Export only after player has previewed. Keep all normal game navigation accessible with no modal share requirement.

## 7. Suggested staged rollout & test gates

**S1 — Solo finish card, no link dependency:** implement an accurate off-line PNG export with share preview, Android Sharesheet, anonymization and optional copy text. Test on device. Done without touching core solver or LAN transport.

**S2 — Profile showcase:** derive robust stats/owned cosmetics and privacy controls; handle empty/legacy saves; include badging and styles after owners approve their on-card usage.

**S3 — Co-op/versus result sharing:** wait for successful **physical two-phone LAN acceptance**; design truthful, partner-safe outcome summaries, avoid exposing private clues or host/guest tokens.

**S4 — Verified store discovery:** once Play listing is live, include true store URL and optional share text; later consider direct challenge links and cross-game links with proper verification.

**S5 — Instrumentation/feedback (optional):** if measuring click/share rates, do not collect/share analytics unless privacy disclosures are updated; an in-app share button click is not proof the user actually posted content externally.

**QA matrix:** old save fields and records; time/score/difficulty accuracy; name lengths & non-Latin text; 200% device font and export typography; no network/airplane mode; daily spoiler prevention; dark theme as applicable; device rotation; image resolution; app chooser content URI permission; cache expiry; nonexistent share targets; suppressed partner details; completion immediately after process restore.

## 8. Scope and coordination note

This is a **product/design brainstorming record**, not a source-code or app-release change. Work chat owns implementation scheduling; **do not interrupt the current v0.10C/D integration and Google Play release-readiness pass** unless the owner explicitly prioritizes this. The owner appreciates this concept and intends to explore it further; do not silently convert optional modes to committed v1 scope.

