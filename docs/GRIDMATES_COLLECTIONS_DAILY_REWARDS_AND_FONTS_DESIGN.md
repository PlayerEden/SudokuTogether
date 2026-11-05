# GridMates: Sudoku — New Portrait Collections, Daily Rewards & Cross-Device Fonts

**Owner feedback date:** 2026-11-05
**Status:** **PLAYTEST FEEDBACK + DESIGN OPTIONS — not yet implementation approval.** The owner and spouse played the current game and requested discussion and a durable Work-chat handoff. This file records priorities, trade-offs and QA, without overwriting active development or pretending changes are already live.
**Repository:** PlayerEden/SudokuTogether
**Source of truth for newest player-facing code:** draft [PR #28 — v0.11C nearby + Woodland](https://github.com/PlayerEden/SudokuTogether/pull/28), branch `feat/gridmates-nearby-animals`, stacked on `feat/gridmates-coach-launcher`. Preserve existing QA checkpoint `checkpoint/v0.11c-nearby-woodland-qa`. `development` contains planning/docs and an earlier promoted code baseline, **not** the complete newest v0.11C Android app.
**Related:** [Brand and vision](GRIDMATES_BRAND_AND_SERIES.md), [Visual identity](GRIDMATES_VISUAL_IDENTITY_BRIEF.md), [Monetization](GRIDMATES_MONETIZATION_AND_PLAY_TESTING_PLAN.md), [Share cards concept](GRIDMATES_SHAREABLE_CARDS_CONCEPT.md).

## 1. Owner's actual requests

1. Expand the current animal **profile character portraits** beyond Woodland with an **Ocean collection** and a **Bug/Insect collection**, staying recognizably in the same appealing illustrated style.
2. Make completing the **Daily Challenge** feel a little special. Possible exclusive unlockable portraits/cosmetics or a special reward currency/points; **weekly rotations** and recycling are acceptable to avoid enormous art production burdens. Owner explicitly wants to discuss before committing exact reward rules.
3. **Fix unlockable font differences across phones**. Owner reports that their spouse cannot see the same font variations on her Android phone; suspects font selection depends on the device. This may be a **cross-device consistency/bug** rather than a demand for more font styles.

Prioritize a pleasant, welcoming and nostalgic puzzle-book experience; avoid grind pressure, daily punishments, ads-for-rewards or pay-to-win. Keep normal Sudoku and hints freely accessible.

## 2. What is in the latest verified code (read 2026-11-05)

### Woodland

- `core/src/main/kotlin/com/playereden/sudokutogether/core/Cosmetics.kt` defines **12 Woodland portrait IDs** `woodland_<animal>` with `Cosmetics.portraits`, visible unlock thresholds and `Appearance.portrait` (default empty string, backward-compatible).
- **Owl and Fox** are starters; other Woodland animals currently unlock via completed puzzle counts. `Cosmetics.completedPuzzles(records)` deduplicates session IDs and includes valid Solo, Daily, Co-op completions and Versus wins. Existing emblems continue to work.
- `app/src/main/java/com/playereden/sudokutogether/AnimalPortraits.kt` maps fixed IDs to packaged `R.drawable.woodland_<animal>` assets, displays portraits in `AnimalPortrait`, and groups them in `WoodlandCollection`.
- Twelve actual PNG images are bundled under `app/src/main/res/drawable-nodpi/`. `design/woodland/packaged-assets.json` records resources, sizes and SHA-256 hashes. **This is a useful reference pipeline for future art**; don't recreate the existing collection or alter already-earned IDs.
- PR #28 reports 117 JVM tests and 78 accepted Android 8/API26 executions, including saved progress upgrade tests. This is a **draft stacked branch**, not a Play release.

### Fonts — reproduced mechanism, symptom not yet reproduced on second device

In `app/src/main/java/com/playereden/sudokutogether/CosmeticUi.kt`:
```kotlin
fun fontFor(id: String): FontFamily = when (id) {
    "serif" -> FontFamily.Serif
    "mono" -> FontFamily.Monospace
    else -> FontFamily.SansSerif
}
```
`Cosmetics.fonts` currently defines **Clear**, **Bookish**, **Terminal**, with IDs `clear`, `serif`, `mono`. These are Android/Compose **generic system font families**, not exact bundled font faces. Devices/vendors can resolve them differently or with very similar numeral glyphs, and preview scaling/weight/layout may further obscure changes. This supports the owner's suspicion but **does not yet prove which specific phone-dependent issue occurred**. There is currently **no `app/src/main/res/font/` bundled font resource directory in PR #28**.

**Correct fix direction:** choose legally redistributable typefaces, bundle their specific static TTF/OTF assets in `app/src/main/res/font/` and construct `FontFamily(Font(R.font.<...>, FontWeight...))` (or appropriate tested weights). Use the exact same bundled typeface in Style Preview and all intended number/entry rendering, with sensible fallback. Not all fallback/given numbers need identical appearance; distinguish permanent puzzle givens from player-entered numbers as intended.

Android official reference: https://developer.android.com/develop/ui/compose/text/fonts

### Daily — current behavior

`DailyChallenge.kt` makes one deterministic offline Easy puzzle per **local calendar date**, stores its independent progress and a once-saved `GameRecord` with `dailyDate`. `DailyChallenge.complete` is idempotent for a stored session and retains durable history. It does **not** yet mint a special currency, seasonal collections or Daily-specific exclusives. Daily has an existing history retention ceiling; old finished board payloads can be pruned while the completed result record survives.

Important: because the game is **offline/local-calendar** and has no authoritative service clock, it cannot perfectly prevent someone from changing the system date or editing local files to manipulate a future virtual currency. Design a simple casual-friendly incentive, not an economy that claims tamper-proof financial-like value.

## 3. New portrait collections — suggested art direction

**Ocean collection — proposed starter lineup (not approved final roster):** Octopus, Sea Turtle, Crab, Jellyfish, Seahorse, Dolphin. Possible later: Shark, Manta Ray, Whale, Starfish, Seal, Clownfish.

**Bug/Insect collection — proposed starter lineup (not approved final roster):** Ladybug, Bumblebee, Butterfly, Dragonfly, Beetle, Firefly. Possible later: Praying Mantis, Moth, Caterpillar, Ant, Grasshopper, Cricket.

### Consistency requirements

- **Reference Woodland assets directly** when making new art. Same simple friendly face language, head scale, stroke/edge treatment, shape silhouette, background composition, lighting/shading, color restraint, image bounds, transparency and 30–96dp legibility. Avoid inconsistent photorealism, AI-art artifacts, extra fingers/limbs, mismatched backgrounds and excessive effects.
- Use **small pilots**: 3–6 Ocean and 3–6 Bug icons for art/style review, then expand once approved. Don't commit to drawing 24 on day one. Avoid duplicates that are hard to differentiate at small sizes.
- **Grouped browsing**: Collection > Portraits > Woodland / Ocean / Bugs, with visible counts, selected portrait, clear unlock condition, previews for locked items, and no maze of nested menus. A collection can also be listed as a card with an Expand/Explore action; evaluate on small phones.
- Preserve the existing `Appearance.portrait` ID field, unlock semantics and all Woodland/legacy emblems, don't rotate or delete identifiers. Add namespaced stable IDs `ocean_octopus`, `bugs_ladybug`, etc. Avoid string collisions with existing inks/fonts/emblems.
- **Actual unlock rules are still undecided**. Distinguish starter items, standard milestones, and possible Daily-shop rewards in display; don't silently give new collections Woodland's same 0–15 solve curve unless reviewed.
- Package compressed, appropriately sized local assets, maintaining no-network offline play and track APK growth. Keep checksum/art manifest and originals, with source/licensing/provenance documented. Optional pack downloads are unnecessary for initial rollout.
- Verify correct rendering and ownership across LAN without sending artwork binary or arbitrary asset identifiers; both phones must have compatible app version/assets. A device lacking a new ID should fall back gracefully, never crash or corrupt a saved chosen portrait.

## 4. Daily Challenge rewards — options and recommendation

**A. New exclusive item every single day:** Very frequent novelty, **but unsustainable** art costs, frustrating FOMO, missing-day stress, date-zone risks, and ever-growing catalog. **Not recommended** for first launch.

**B. Weekly rotating featured reward(s):** One featured cosmetic, or a small themed collection rotates for seven days. Much lower art work; can be implemented with versioned deterministic rotations. Watch for players missing something permanently and time-zone correctness. A weekly cadence might be a later *shop refresh*, not a replacement for the existing Daily Challenge.

**C. Every Daily solve grants a small amount of "Puzzle Stamps" (working name), redeemable in a rotating rewards shelf:** **Recommended design to prototype**, if owner approves. Daily remains a daily puzzle; the *shop* can rotate weekly or monthly, with old rewards eventually cycling back. Reuse curated items to create depth without a new art obligation every day. Players who miss a day simply earn on another day, no harsh reset.

### Minimal example flow — illustrative, NOT final values

```text
Daily Challenge → Complete valid puzzle
  ↓
  "Daily complete!" + standard result
  "You earned 1 Puzzle Stamp" (proposed)
  ↓
  Collection → Daily Rewards
    Ocean: Octopus badge · 5 stamps (example)
    Bugs: Firefly portrait · 8 stamps (example)
    A themed number ink · 4 stamps (example)
  ↓
  Claim chosen item when enough stamps collected
```

The owner also suggested an important **fallback if the daily featured exclusive was already earned:** award Stamps instead, or allow players to spend them on prior missed collection items. **No empty Daily reward**, and no unusable duplicate. A fair "collect / save up / choose" model fits the cozy puzzle-book philosophy better than punishing randomness.

### Open decisions for owner/Work chat

- Pick the reward object/currency name ("Puzzle Stamps", "Page Marks", "Puzzle Tokens", etc.) and whether it is actually needed versus unlocking by completed Daily count.
- Earn mechanism: **1 reward per unique completed Daily date**, not each reopening, rerunning, app restart or replay; does a hinted/assisted completion also qualify? **Recommend YES** (accessibility and coaching), and keep performance score separate.
- Should new Daily users be given a tiny initial gift? Optional, not required.
- Rewards shelf rotation: weekly/monthly/no expiration; **recommend catalog returns and no irreversible miss**.
- No requirement to complete 7 consecutive days, no lost streak, no extra login, no ads to collect, and no paid currency in this first version.
- Pricing and rarity: small, understandable targets; avoid excessive grinding (e.g., unlock a portrait after a modest number of Daily completions). Store UI should explicitly state how and when the reward is earned.
- **Long term:** if earnable cosmetics and optional paid gacha eventually coexist, **keep free Daily Stamps completely separate from a paid random-pack currency** until a carefully reviewed economy exists. Never inadvertently create cash-equivalent value, false odds or high-pressure spending.
- Daily progress is intentionally device-local and not cloud-synced yet. Display accurate "saved on this device" language, especially for unlocks and stamps.

### Alternative even simpler launch version

**No currency at all:** after 3, 7, 14, 30 *distinct* Daily completions, award a themed cosmetic from a fixed (non-expiring) catalog. This is much easier to test/maintain and might be the wiser release-v1 option. A currency/shop can be introduced later if players actually want choices. **This is the recommended low-risk fallback if Work estimates the Stamps shop adds too much complexity.**

## 5. Engineering safety of currency and rotating items

If implementing rewards:
- Add versioned persisted wallet/cosmetic ownership state to local profile storage with backward-compatible defaults; old app data must remain decodable. Keep durable record of which `dailyDate` has been awarded and permanent list of claimed items. Existing earned Woodland logic is derived from match records and must continue to work.
- Award transaction must be **atomic and idempotent**, coupled to validated Daily completion and local save readback. Don't show earned currency/items if a save failed, and don't grant duplicates after repeated completion callbacks, process death, retry or reopening history.
- No destructive reset of unfinished Daily sessions, old stored results, opponent modes or room join policy. One Daily date should not reward twice even if the board is reopened or archived.
- Redeeming rewards: verify cost/available balance/claim state and persist the spend and grant together; prevent negative balances, duplicate redemptions and partial writes.
- Recovery and backward compatibility: under partial data loss, never silently delete previously owned cosmetics. Migration should reconcile old completed Daily dates once with a documented one-time policy rather than back-award arbitrary value or awards to incomplete records.
- Separate **Catalog**, **Earned Ownership**, **Wallet**, **Entitlement / cosmetic ID** and **daily grant record**. Render only assets actually packaged and accessible; unknown or now unavailable items have safe placeholders; keep the user's equipped item if still valid.
- Save local-time Daily identity/rotation carefully; define timezone changes and clock-backtracking behavior. Do **not** promise server-grade fraud prevention without a server; no real-money-redeemable rewards.
- There is no need for global online ranking, login or server infrastructure for this casual offline rewards model.

## 6. Font fix — recommended first slice

**Reproduce first:** Compare same app version, same unlocked font IDs, same number/board preview on the user's phone vs spouse's phone. Confirm chosen font is saved, applied in actual board not just preview, and the visible glyph change is expected. They may currently see identical-looking digits for generic Serif/Monospace due to OEM font mapping or weight/size; don't assume selection is broken without evidence.

**Implementation suggestion:**
1. Select **three distinct, clearly readable and commercially redistributable** typefaces with license files and source attribution. Consider font proportions/clarity of digits **1–9** especially; avoid numerals that look decorative at 24–40dp.
2. Bundle exact chosen font binaries with the Android app in `app/src/main/res/font/` (not remote download). Use Kotlin Compose `FontFamily(Font(R.font.<fontfile>, FontWeight.Normal), ...)` mapped under existing IDs `clear`, `serif`, `mono` to **preserve equipped cosmetics and saved profiles**. Fonts can have distinct internal art names; do not alter persisted identifiers needlessly.
3. Use **the same family mapping** in StylePreview, CosmeticSelector, actual Sudoku input/notes if intended, and any partner style rendering. Starting/given clues should remain legible and visually distinct; don't accidentally restyle all app navigation or chat unless specifically desired.
4. Test font fallback and glyph coverage. Use pinned weights and consistent spacing; avoid a device silently substituting generic fonts. Evaluate if default Clear should remain the current look for backwards-compatible perception.
5. Compare two physical phones with screenshots, plus API26 and current target Android versions, narrow/landscape/200% text; verify offline and across save/resume/update. Do a visual diff test if feasible and store only *licensed* font binaries in the app repo.
6. Check asset provenance/legal license before committing font resources. **Never share font binary files in public chat or generated external deliverables**; manage within the app's properly licensed source/assets workflow.

**Acceptance:** A player can equip a font, reopen the game, and see that same intentionally distinct numeral style in preview + actual board on both phones, independent of OEM defaults. No missing glyphs, layout collisions or changed unlock conditions.

## 7. Suggested order while Work chat is paused

**Priority 1 — Font consistency bug**: reproduce on two phones and fix with bundled fonts, new screenshot/native QA, preserve existing IDs and saves.

**Priority 2 — Art-system prototype**: create 3–6 Ocean and 3–6 Bug pieces that genuinely match existing Woodland style; evaluate visual quality on small portrait circles before expanding; keep licensing and Android asset growth in mind.

**Priority 3 — Choose the smallest Daily reward loop**: compare Daily milestone unlocks vs Puzzle Stamps + rotating shelf, and get owner approval on economy design. Test with real completion scenarios. **Do not silently ship a currency/shop just because it is proposed**.

**Priority 4 — Broader collections and release polish**: expand roster, profile/collection layout, accessible descriptions, QA, then incorporate into existing release roadmap once cost/benefit is clear.

**Do not** restart from `development` alone and lose the current v0.11C stacked source. Keep [PR #28](https://github.com/PlayerEden/SudokuTogether/pull/28) and its exact QA checkpoint intact; no source code, Play listing, release or assets were changed by this document.

## 8. Work-chat acceptance checklist (for eventual implementation)

- [ ] Reproduce font look issue using the *same* app build on two Android phones; record exact behavior.
- [ ] Validate chosen, licensed, bundled fonts on two phones and all number rendering contexts (preview, board, optional partner style), including 200% text and offline.
- [ ] Preserve existing font IDs, all saved portraits/achievements/records, network appearance payload, code-required host/join flow, and private app signing identity.
- [ ] Produce small Ocean/Bug visual pilot closely matched to Woodland, with art review before creating full sets.
- [ ] Test art small (30/44/72dp), narrow screens, list grouping, unlock rules and unknown portrait fallbacks.
- [ ] Owner chooses Daily reward policy before implementation; if added, verify one reward per unique completed day, atomic award/redeem, duplicate/restart/clock/timezone tests, old saves and retention.
- [ ] No requirement to watch an ad for Daily gifts; no pay-to-win or punitive streak requirements.
- [ ] Full regression/test evidence + APK verification on a milestone branch and rollback checkpoint; avoid user-data-destructive reinstall guidance.
