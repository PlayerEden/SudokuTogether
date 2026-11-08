# GridMates: Sudoku — Monetization and Play Testing Direction

**Decision date:** 2026-11-03
**Status:** Owner-approved ad **format and restraint principle**; detailed frequency/pricing/integration remain proposals. **Not implemented or released.**
**Product:** GridMates: Sudoku, with possible future reuse of business patterns in other GridMates puzzle games.
**Reference:** [GridMates Brand & Series](GRIDMATES_BRAND_AND_SERIES.md) · [Visual Identity Brief](GRIDMATES_VISUAL_IDENTITY_BRIEF.md) · [Project Handoff](PROJECT_HANDOFF.md)

## Owner-approved store business model (2026-11-03) — DECIDED

**Final direction for initial Google Play listing:** **Free to install**. Support development with **rare, respectful full-screen interstitial advertisements** at eligible between-puzzle transitions and offer an **optional permanent one-time Remove Ads purchase** through Google Play Billing. No mandatory subscriptions, no permanent banners, no purchase required to play normal modes or get Sudoku hints.

This **supersedes** the earlier **$0.99 paid-download proposal**. It is not a pending premium/free decision. The app should be listed as **Free** when the store listing is configured. Google Play does not permit changing an already-free listing into paid later; a future premium product would be a distinct strategy/listing.

**Locked ad experience:**
- No ads inside an active puzzle, hints/coaching, menus that risk interruption, results celebrations, or multiplayer joining/reconnecting.
- Never after *every* puzzle; use generous cooldowns and completion thresholds. Exact pacing is still for user review and real playtest validation.
- Candidate moment: an intentional navigation after a *completed* puzzle, after the player has seen results. If an ad is unavailable, continue normally; never block gameplay behind ad loading.
- No paid hints, no pay-to-win; protect casual/new Sudoku players.
- Ads should be disabled for valid Remove Ads owners; one-time entitlement is restorable using Play purchase infrastructure. **Price not decided** ($2.99 was illustrative).

**Future, not an initial monetization commitment:** earned cosmetic collections; consider optional cosmetic purchases later, possibly randomized packs. Never compromise puzzle access, fairness or accessibility.

**Implementation status:** The free model is an approved *product decision*, **not a claim that ads, Billing, consent, cloud backup or store publishing already work**. Staged implementation requires release readiness, privacy/consent, test ads, test purchases, and QA. Do not interrupt current Android stability/UI work.

### Future optional cosmetic collection / gacha — discussion only

- Earned, free collectible avatars, frames, board themes and victory effects can add personality.
- Paid cosmetic purchases are an optional later investigation, not required for first release.
- If paid random-reward packs are introduced, disclose actual item probabilities before/near purchase and comply with applicable Google Play, regional and age-related rules. Prefer fair, non-manipulative design, honest duplicate handling and optional direct purchases.
- No paid gameplay advantages, no gated Sudoku hints and no time pressure that undermines the calm paperback-puzzle-book identity.

## Owner decisions / positioning

- The app should remain a calm, satisfying, paperback-puzzle-book-inspired game with rich but unobtrusive features.
- **Prefer occasional full-screen advertisements shown only at natural between-puzzle breaks, not always-present banner ads.**
- **Definitely not frequent:** do not show an interstitial after every completion and do not interrupt active gameplay.
- The owner is interested in exploring a **one-time Remove Ads purchase**, rather than requiring a subscription.
- Publishing in Google Play internal/closed testing, and potentially open beta later when eligible, is a direction worth exploring.
- Monetization is secondary to player trust and comfort, and no ad system or purchase is built merely by recording this decision.

## Initial ad placement policy — design proposal

**Only candidate trigger:** after the player has completely finished a puzzle *and intentionally navigates onward*, e.g. selecting **Next puzzle**, **Back to Home**, or another clear transition. **Never cover the actual victory/result moment**; let users review their accomplishment. Only use preloaded ads to avoid blocking navigation; if no ad is ready, continue normally.

**No ad during:**
- puzzle solving, opening a hint, Pencil/Notes/Undo, corrections, timers, pause/resume, saving, recovery, or any important UI interaction;
- a half-completed puzzle, app opening, app exiting, app returning from background, or joining/reconnecting to a local multiplayer session;
- multiplayer/co-op action or lobby flows, especially where ad delay could disadvantage or disconnect a player;
- a purchase/billing process, privacy/consent flow, accessibility interaction, or error/recovery.

**Frequency options to test — not yet approved:**
- No forced ad on the first completed puzzle in a fresh session.
- Minimum time-based cooldown around 20–30 minutes between interstitials and require multiple completed puzzles between ads (e.g. 3+); choose whichever restriction is stricter.
- At most 1 forced interstitial per session during early testing; zero ads in very short sessions. Note: puzzle sessions may take a long time, and **we might decide on even fewer ads or none in some sessions**.
- Never delay movement or artificially make completion/navigation conditional on watching an ad; dismiss/close behavior must follow ad network policies.
- Measure complaints, return rate, puzzle completion, accidental taps and actual revenue before increasing frequency. Default **toward fewer ads**, not more.

**No "watch an ad for a hint".** Sudoku coaching and learning remain available regardless of monetization. No ad gating for difficulty levels, ordinary puzzles, saved progress, or multiplayer.

## One-time "Remove Ads" idea

- Offer a clear, **non-consumable** Google Play Billing product, provisionally named **Ad-Free** or **Remove Ads**.
- The previously floated **$2.99 is an illustrative idea, not approved pricing**. Research fee/tax/region and product value before configuring.
- Once entitlement is valid, suppress all standard forced advertising. Clarify optional rewarded placements, if introduced later, rather than surprising buyers.
- Support Google Play purchase restore on compatible accounts/devices and reconcile purchases safely; do not conflate a purchase entitlement with profile/game save backup.
- Respect sandbox/test purchases and Google Play billing requirements; do not trust a local boolean as the sole durable source of entitlement.
- No subscription required for the basic ad-free experience.

## Potential integration and compliance tasks (future milestone)

1. Get eligible Google Play Console developer access; configure internal and closed testing and understand account-specific availability requirements.
2. Produce signed release **AAB**, verify target SDK/Android changes, package/application ID, upgrade and signing plan, final branding, privacy policy, store data disclosures.
3. For ads, investigate Google AdMob or alternative policy-compliant provider. Create proper app/ad unit identifiers, SDK integration, Google test ads, consent/age/privacy and measurement; never use live ads for development testing.
4. For ad removal, configure Google Play Billing one-time product and verify successful purchases, pending/declined/canceled paths, restore, offline behavior, refunds and account changes.
5. Test ads against actual game flow on multiple devices, accessibility and portrait/landscape; test no banner overlays, no puzzle interruption, no lost saves.
6. Run controlled testing before exposing broad players. Confirm platform rules and API/library versions **at implementation time**, since requirements change.

## Rollout recommendation

**Phase A:** clean, no-real-ads internal release for UX and stability feedback.
**Phase B:** closed beta with 2-device LAN acceptance (if multiplayer is advertised), save/upgrade tests, privacy and store checks.
**Phase C:** optional monetization experiment using test ads and test billing. Decide final pacing and fair pricing with user approval.
**Phase D:** consider public open testing (if eligible) or production launch when both game QA and compliance are complete.

## Coordination guardrails

This is a **planning-only record**. The Work/implementation chat should not interrupt its current gameplay/visual/release safety work solely because this file exists. No production AdMob account, payment purchase, billing product or live ad placement has been authorized by this document. Keep all approved creative decisions distinct from proposed thresholds and unverified implementation claims.
