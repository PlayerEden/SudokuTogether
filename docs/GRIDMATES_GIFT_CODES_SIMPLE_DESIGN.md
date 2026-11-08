# GridMates: Sudoku — Simple Gift Codes (Design Idea)

**Recorded:** 2026-11-06  
**Status:** User-requested creative concept for the Work chat. **Not implemented, no active codes created, no artworks made, no code entered into source.**  
**Current relevant source:** `feat/gridmates-nearby-animals` (draft PR #28) has curated Woodland portraits, `Appearance.portrait`, `Cosmetics.allowed` and device-local saves.

## The idea

Add a small **Collection → Redeem Code** option. The creator can give someone a fun code that unlocks a surprise **free cosmetic gift**, especially a custom portrait and a couple of matching items for the creator's wife. Other possible uses later: thank-you gifts, holiday surprises, beta tester treats and community events. No payment, watched ads, login, or connection required.

**Explicit clarification from owner:** Do **not** make the early feature complicated by worrying about globally unique/single-use codes or perfect protection against sharing. It's okay for a simple offline code to be reusable by different people. The purpose is a fun surprise, not a secure monetary entitlement service. **One redemption per local player profile per code** is enough for the first iteration.

### Player experience

```text
Collection → Redeem Code
[ Enter code                  ]
[ Redeem ]
→ "Gift unlocked!"
→ See portrait, frame/badge/title (only implemented assets)
→ [Equip]   [View collection]
```

Plain helpful messages for typo / unrecognized code / already redeemed. The user can revisit the gift in **Special Gifts**. Don't nag or make it the center of Collection.

### Personal surprise bundle — creative details pending

The owner specifically wants a **special, customized profile portrait for his wife**, perhaps with two or three matching extras. **Do not invent her likeness, an exact character design or secret phrase**. Discuss and approve art/name privately before authoring those assets or codes. Match Woodland's family-friendly art/portrait framing and support future Ocean and Bug collections. For gift exclusivity in an offline public-source app, call it **special/personalized**, not provably unique.

### Minimum technical needs

- **App-bundled, fixed allowlist** of cosmetic item identifiers and images. No ability to execute code or load arbitrary remote images from user input.
- Distinct durable `ownedGiftIds` and `redeemedCodeIds` or equivalent, with backward-compatible profile save defaults. Existing `Cosmetics.allowed` presently only checks milestones: ensure gift ownership is recognized or the gifted portrait could disappear after reload.
- Commit a gift and the used-code marker in **one atomic save**, deduplicate repeats and restore after process death, restart and normal APK update.
- Do not expose unannounced surprise words/art in GitHub documentation. A public client can still be inspected, and the owner accepts that offline gift codes are not secure against determined copying.
- Test unknown code, retry, repeat, offline mode, locked/legacy portraits, profile switching, and current LAN appearance allowlist behavior. Partner devices missing an asset must use a safe fallback.
- No currency / gacha / advertising system needed to deliver simple free gifts. Do not turn this into a Play Billing setup.

**Work chat priority:** Idea for future polish. Keep current saved progress, app signing, local co-op, release work, and next QA checkpoint intact. Discuss the personal gift art before implementing a specific code or image.

**Related:** [Collections / Daily Rewards](GRIDMATES_COLLECTIONS_DAILY_REWARDS_AND_FONTS_DESIGN.md) · [Project Handoff](PROJECT_HANDOFF.md).
