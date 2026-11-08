# GridMates: Sudoku — Share Results UX Design Brief

**Date:** 2026-11-03  
**Status:** **Owner-approved feature intent and entry-point pattern**; detailed UI art, exact technical implementation, rollout version and future card formats are **proposals pending Work-chat review**. No working share-card UI or export is claimed.  
**Applies to:** current GridMates: Sudoku; reuse selectively for future GridMates titles.  
**Companion:** [Shareable Cards and Social Sharing Concept](GRIDMATES_SHAREABLE_CARDS_CONCEPT.md), [GridMates Brand](GRIDMATES_BRAND_AND_SERIES.md), [Visual Identity Brief](GRIDMATES_VISUAL_IDENTITY_BRIEF.md), [Project Handoff](PROJECT_HANDOFF.md).

## 1. Owner's clarified decision

A **plain optional button such as "Share results" on the results/completion screen after a game** is exactly the intended discoverability level. It should be as normal and effortless as playing another puzzle, without being a centerpiece, unskippable popup or mandatory social promotion.

The earlier concept covers profile cards, stats, earned cosmetics, Daily, Solo, co-op/versus, social media/messaging, gentle organic marketing and future shareable challenges. **This brief narrows the first UX slice** and gives the Work chat tangible design/acceptance criteria.

**Why:** GridMates is about enjoying puzzles alone or sharing little moments with people already important to us—even those currently far away. Use Android's built-in share experience to support that without accounts, public internet gameplay or a social-network server. The owner likes **"Good puzzles. Good company."** as a tagline candidate, not final locked artwork.

## 2. What a player should see

### Results/completion screen — recommended hierarchy

On the existing completion result card, keep:

1. **Puzzle complete** / **Daily challenge complete** / **Solved together** / **Victory** with authentic result and earned badge if any.
2. **Important facts:** mode, current actual difficulty where known, recorded time (where truthful), and whether the finish was Assisted or Unassisted. Show hints used only where recorded.
3. **Primary action**: current normal **"Play another solo puzzle" / "Start another race" / "Start another co-op puzzle" / "Back home"**. Nothing in sharing should delay or replace this.
4. **Secondary, visible, 48dp-ish touch-friendly button:** **"Share result"** (preferred short label) or "Share results" (both acceptable). Optionally small native-looking share icon. Use a real button/control, **not a headline-sized marketing banner**, auto-popup, notification, or overlay.
5. Existing secondary "View personal records" and review actions remain accessible; avoid crowding narrow/landscape/large-text screens. Work chat can decide whether a compact actions row or vertically stacked buttons is best after screenshots.

**Do not** automatically open Sharesheet upon completion, nag after decline, grant a bonus for sharing, or make a viewer watch an ad to share. Ad policy remains rare between-puzzle ads, and sharing a result is **not** an ad trigger.

### Interaction from the player's perspective

```text
Finish the puzzle
  ↓
See normal celebratory results (no blocking prompt)
  ↓
Optional [Share result] button
  ↓
Preview the generated card
  ↓
[Share]   [Save image]   [Copy text / link, if applicable]   [Cancel]
  ↓
Android Sharesheet (Messages, social apps, etc.)
  ↓
Return safely to the same completed result screen
```

Preview is helpful because exported pictures may contain the player's name or stat details. MVP may simplify the controls, but **there must be explicit preview/confirmation before publishing identifying information**. Dismiss/cancel must not corrupt the result, reset the match or initiate another game.

## 3. MVP completion card — compact, accurate, attractive

Proposed export: **1080×1350 PNG (4:5 portrait)**, composed as a card independent of the phone UI; supports comfortable display in texts/social apps. Do not simply screenshot the result screen including Android controls.

Suggested structure:

```text
┌──────────────────────────────┐
│  GRIDMATES · SUDOKU       ◇  │
│                              │
│       PUZZLE COMPLETE        │
│           HARD               │
│                              │
│       14:26      1 hint      │
│        TIME       USED       │
│                              │
│        ASSISTED              │
│                              │
│   [subtle decorative grid]   │
│                              │
│  Good puzzles. Good company. │
└──────────────────────────────┘
```

**All numbers above are fictional illustration values, not current user data.** Use the verified result record to populate the real card.

UI principles:
- Follow the game's current restrained cream/forest/gold language, original emblem, legible text and accurate owned profile customization.
- Card should feel like a collectible from GridMates' puzzle-book family, not an AI-generated promotional poster.
- Show **one or two highlights**, not every raw stat. Avoid visual clutter, unearned badges, fabricated rankings or generic confetti slogans.
- **Do not show a numeric score until an actual score system is implemented, validated and persisted.** If no score exists, a time + difficulty + assistance-status card is plenty.
- **Avoid completed Sudoku digits** on Daily Challenge or spoiler-sensitive shared content.
- A subtle brand/tagline credit is fine. A Play link belongs in user-editable share text **only after a real store URL is confirmed**; some apps ignore image captions. A future copy-link action can address that.

### Exact MVP fields

| Field | Source | Safety/fallback |
|---|---|---|
| GridMates: Sudoku identity | Local resources | Never imply app already released |
| Result label and mode | Validated completed Solo/Daily record | No sharing unfinished/failed saves as completed |
| Difficulty | Stored puzzle/record grade | "Unrated" or omit for legacy/unknown; never relabel |
| Completion time | Saved elapsed time | No current-running-timer substitution |
| Assistance | Hints/assist metadata | "Assisted" vs "Unassisted" accurate to source |
| Optional player style | Actually owned local cosmetic | Do not show locked items as earned |
| Score / global rank | Only if actually implemented | **Omit in MVP** |

No sign-in, permissions to contacts, network calls, special gallery permissions or new backend are necessary for local PNG generation and Android Sharesheet.

## 4. From results to records and profile sharing later

The first slice should be **the finished Solo/Daily screen only**. Once it is robust:
- **Recent Finishes/Records:** expose a "Share" affordance on a selected **completed saved record**, not on every row if it clutters the journal. Historical records might lack newer metadata; handle gracefully.
- **Profile Showcase:** share a personalized portrait card with selected avatar/frame, authentic unlocked badges, solved counts, best times **by difficulty and assistance type**. Preview lets player hide their display name or sensitive stats.
- **Co-op and Versus:** offer result-sharing after the **real physical two-phone LAN QA gate** passes. Default to "You & a friend" or anonymous partner label; do not leak other player's identity without appropriate consent, private room details, IPs, hidden clues or raw chats. Winning/losing result must be correct for that player, not assumed from host role.
- **Same-puzzle remote challenge:** attractive future idea but requires portable/versioned puzzle identity and a verified replay route; does **not** require synchronous online multiplayer. Not MVP and not a requirement for the share button.
- **Full social profiles/global leaderboards:** remain optional, undecided. Static sharing does not imply searchable profiles.

## 5. Implementation sketch for the Work chat

### Known actual code points (read 2026-11-03)

- Latest verified presentation branch: `feat/v0.10d-game-visuals` (stacked on `fix/v0.10c-save-integrity-polish`), draft PR #25. Preserve its QA baseline and checkpoints.
- `app/src/main/java/com/playereden/sudokutogether/PlayScreen.kt` has `PuzzleFinishCard()` with completion heading, stats and existing Play Another / Back Home / View personal records actions. **This is the ideal first entry point**.
- `core/src/main/kotlin/com/playereden/sudokutogether/core/PersonalPlay.kt` has `GameRecord` with `mode`, `result`, `elapsedMs`, `moves`, `difficulty`, `assisted`, `hintsUsed`, `dailyDate`; `SoloProgress` contains current finished puzzle state. Do not mutate these just to share.
- Existing `FileProvider` in `AndroidManifest.xml` and `app/src/main/res/xml/share_paths.xml` supports cache sharing; current `MainActivity.shareApk()` is **for sending the QA APK**, not exporting PNG results. Public Play game sharing should use the verified Play Store URL, not test APK distribution.

### Proposed architecture

```text
Completed validated result state
           ↓
  Pure immutable ShareCardData
           ↓
  Local deterministic PNG renderer
           ↓
  Share preview (editable privacy toggles)
           ↓
Temporary cache PNG + FileProvider URI
           ↓
Android ACTION_SEND / image/png Sharesheet
```

Implement as a **small, isolated UI/export module**; it must not change Sudoku solver, score evaluation, save format, LAN protocol or app signing. Android native Canvas drawing or captured Compose content may be evaluated; pick the one producing consistently crisp, deterministic images in QA. Render/export off the main UI thread when feasible. Own error messages and fallback "Copy result as text"; do not crash if Sharesheet has no suitable target or cache write fails.

**Do not silently upload the card.** Only user-selected apps receive it through Android's explicit share action. Don't ask for contacts or require social account access.

## 6. UX + privacy acceptance criteria

- [ ] A completed Solo and Daily game shows **Share result** on the existing finish screen, non-primary but clearly visible. No button during an active/unfinished puzzle.
- [ ] The main next-puzzle/home action is still easy to reach. The share button does not interfere with completion, save, back navigation or replay.
- [ ] A player can open preview, cancel, and remain safely on the original result screen; sharing is opt-in.
- [ ] PNG is crisp and correct on multiple Android devices, in narrow portrait and landscape layouts, including 200% font scale; contrast and accessible button description are verified.
- [ ] All shared stats exactly match the persisted result; no fake/global records, scores, earned cosmetics or online service claims.
- [ ] A hinted puzzle shows the correct Assisted status and hint count, and unassisted finishes remain distinct.
- [ ] No Daily solution digits or guest private content leak into the image or share text.
- [ ] Export works in airplane mode without a server; cancellation/no share target/export failure all preserve the game.
- [ ] External intents use a URI with temporary read access and avoid exposing local save files or authentication material.
- [ ] Real-device smoke test on Android Sharesheet and multiple targets (e.g. Messages, WhatsApp, Discord, Instagram, as installed), including image-only/text-only behavior.
- [ ] Old save data, profile records, QA signing, resume, multiplayer protocol and release gates remain intact.
- [ ] No forced ads, rewards, analytics, auto-posting or sharing prompts.

## 7. Release priority and scope boundaries

**Recommended milestone:** first version of Share Result should be scoped as a **contained polish feature after existing save-integrity and visual integration**, potentially during Play beta iteration—not a launch blocker unless owner explicitly chooses. Don't delay required production AAB/API/Play account work, current usability tasks or two-phone verification for speculative extras.

**Recommended feature sequence:**
1. UX review against current `PuzzleFinishCard` and screenshot layout.
2. Solo/Daily share button + locally generated result card + safe preview/Sharesheet + full QA.
3. Historical Records/Profile cards when local profiles/cosmetics are accurate and attractive.
4. Local multiplayer result cards after verified two-phone match outcomes and privacy design.
5. Real Play URL, optional remote same-puzzle challenge links and other enhancements later.

No implementation, Android build, app store submission or live social sharing occurs by writing this document. Work chat should **review first**, propose a targeted task/checkpoint and only implement when it fits current priorities.
