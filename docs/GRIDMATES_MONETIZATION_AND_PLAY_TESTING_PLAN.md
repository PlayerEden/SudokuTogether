# GridMates: Sudoku — Monetization and Play Testing Direction

**Decision date:** 2026-11-03
**Status:** Owner-approved ad **format and restraint principle**; detailed frequency/pricing/integration remain proposals. **Not implemented or released.**
**Product:** GridMates: Sudoku, with possible future reuse of business patterns in other GridMates puzzle games.
**Reference:** [GridMates Brand & Series](GRIDMATES_BRAND_AND_SERIES.md) · [Visual Identity Brief](GRIDMATES_VISUAL_IDENTITY_BRIEF.md) · [Project Handoff](PROJECT_HANDOFF.md)

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
