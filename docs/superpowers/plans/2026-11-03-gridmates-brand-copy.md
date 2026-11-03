# GridMates Branding and Concise UI Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans task by task. Steps use checkbox syntax.

**Goal:** Apply the chosen GridMates name, remove redundant UI prose and retain useful guidance, then present native logo comparisons.

**Architecture:** Preserve existing Compose screens, GameActions and data/transport contracts. Add a small vector branding component and a Settings About dialog using the existing APK-sharing callback.

**Tech Stack:** Existing Kotlin2.1.20 / Compose2025.04.01 / Gradle8.11.1 / JDK17.

**Spec:** `docs/superpowers/specs/2026-11-03-gridmates-brand-copy-design.md`

## Global Constraints

- min26/target35, package/namespace unchanged, schema1/protocol5, v0.10D QA certificate.
- version17 / `0.11.0-dev1-qa`; no new external dependency.
- Concise visible copy; keep destructive/save-error warnings, logical explanations and result-fairness facts.
- Existing v0.10C/D source remains; carry the approved development-branch brand documents forward.
- Provisional original logo, no final art lock or repository/release promotion.

## Review Focus

- Upgrade with a saved puzzle/preferences must retain identity, notes, progress and records.
- The moved Share APK action must still prepare and share the installed APK, then return to Settings.
- Short labels must not confuse Daily, resume, destructive replacement or joining.
- Compact/200% text must keep primary actions and information/help reachable.
- Co-op/Versus pause, code, chat and private projections must stay wired to the same actions.

### Task1: Name, concise screens and app information

**Files:** Modify HomeScreen.kt, JournalScreen.kt, JoinScreen.kt, PlayScreen.kt, CosmeticUi.kt, DailyHistoryCard.kt, SudokuScreen.kt, MainActivity.kt, strings.xml, app/build.gradle.kts; add AboutGame.kt. Adapt existing native selectors for intentional visible labels. Test GridMatesConvenienceTest.kt.

**Interfaces:** ProfileScreen gains `share: () -> Unit`; Settings consumes it. `AboutGame(onDismiss: () -> Unit, share: () -> Unit)` displays app/version/storage information. Existing GameActions stay unchanged.

- [ ] Run baseline JVM/native convenience checks and observe the new About-sharing test fail because the destination is absent.
- [ ] Apply concise copy and the name; move app information/share to About and connection guidance behind its help action. Keep safety and fairness facts.
- [ ] Pass the About-sharing regression and existing convenience/gameplay/keyboard checks; visually inspect native screens and commit.

### Task2: Original vector concepts and final acceptance

**Files:** Create GridMatesBrand.kt and proposal SVGs; update launcher foreground; add native BrandConceptCaptureTest.kt; update README/handoff/brand docs/QA/work log.

**Interfaces:** `GridMatesMark(modifier: Modifier, style: GridMatesMarkStyle)` draws original vectors. HomeScreen consumes a defaulted mark style so native previews can render each proposed identity without a development selector in the product.

- [ ] Draw three original concepts and show them in the production Home layout, with compact monochrome/icon samples. Connected tiles is the provisional default.
- [ ] Run JVM/lint/APK/test assembly, selected native acceptance, an actual v0.10D upgrade check and inspect the screenshot comparisons. Pure copy/artwork receives visual assessment, not tests that mirror strings.
- [ ] Obtain one fresh read-only whole-branch review; resolve material findings with targeted regressions and document decisions/limits.
- [ ] Preserve source and docs in a new draft stacked on v0.10D, retain a checkpoint, and deliver the signed QA APK and visual proposals for the owner's choice.

Preflight: ProfileScreen's share callback is passed from SudokuApp's existing `share`; no ViewModel contract changes. Home's defaulted mark style affects presentation only. No data/interface conflict found.
