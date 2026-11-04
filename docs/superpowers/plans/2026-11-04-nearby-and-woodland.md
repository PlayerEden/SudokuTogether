# Easy Nearby Play and Woodland Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Easy nearby multiplayer by default and one coherent, earned animal portrait collection.
**Architecture:** Extend existing session admission policy/NSD metadata; add curated portrait IDs and packaged resources to the current native framework.
**Tech Stack:** Kotlin/JVM, Jetpack Compose, Android NSD, existing serialization/toolchain, built-in image generation.
**Spec:** ../specs/2026-11-04-nearby-and-woodland-design.md

## Global Constraints
- Preserve package/component/icon/signing/save paths, schema1, min26/target35 and dependencies.
- Version19 /0.11.0-dev3-qa; protocol6, same APK on both phones.
- New rooms default open; legacy saved rooms default protected; guest reservations/tokens remain.
- Existing six emblems retained; Appearance.portrait defaults empty.
- Woodland roster/thresholds and plain native UI follow spec; no QR/cloud/ads/engine changes.

## Review Focus
- Resume protected room after changing new-game toggle: saved policy wins, Tasks1/2.
- Missing discovery flags/obsolete peers: protected default/actionable rejection, Tasks1/2.
- Unread counts at narrow/large text: intact title and reachable actions, Task3.
- Old/unknown/locked portrait IDs, duplicate/assisted records: safe earned identity, Tasks1/4.
- Actual old APK update with nonempty progress/history/old emblems: no reset, Task5.

### Task1: Admission and cosmetic domain
**Files:** core Model.kt, GameSession.kt, HostServer.kt, Protocol.kt, Cosmetics.kt; NearbyAdmissionTest.kt, WoodlandCollectionTest.kt.
**Interfaces:** SavedSession.requireCode:Boolean=true; GameSession.create(...,requireCode:Boolean=true); session.requiresCode:Boolean; Appearance.portrait:String=""; Cosmetics.portraits:List<Cosmetic> and completedPuzzles(records):Int.
- [ ] Write actual-socket open/protected/reserved/rejoin/persisted-policy/version tests and catalog legacy/default/unknown/assisted/dedup tests. Run :core:test, observe intended RED.
- [ ] Implement compatible fields, policy, curated catalog and protocol6. Full core/app JVM GREEN.
- [ ] Commit and ledger actual evidence.

### Task2: Simple nearby joining
**Files:** RoomDiscovery.kt, JoinScreen.kt, HomeScreen.kt, GameViewModel.kt, SudokuScreen.kt/MainActivity.kt actions; NearbyJoinUiTest.kt/model cases.
**Interfaces:** NearbyRoom.requiresCode:Boolean=true; startHosting(...,requiresCode:Boolean=true); GameUiState.requireRoomCode:Boolean=false; GameActions.requireRoomCode:(Boolean)->Unit default no-op; model.setRequireRoomCode(Boolean).
- [ ] Native RED: open room taps endpoint and blank code callback immediately; protected needs6digits; manual fallback; fresh host defaults open; restored protected policy independent of preference.
- [ ] Implement NSD flag, persisted new-game choice, actual saved host policy and truthful code display. Run focused native/model and full JVM GREEN.
- [ ] Commit and ledger.

### Task3: Header and chat
**Files:** PlayScreen.kt, CosmeticUi.kt; MultiplayerPolishUiTest.kt.
**Interfaces:** existing details state houses code/copy/address. Stable primary title row, secondary Chat/Game details; no action API changes.
- [ ] Native RED for unread0/1/99 at320dp and200% text, title geometry,48dp actions, local You/partner names and reachable wrapping replies.
- [ ] Implement native rows/details/chat; run new layout/chat and gameplay convenience GREEN.
- [ ] Commit and ledger.

### Task4: Woodland assets and selection
**Files:** design/woodland originals/manifest; drawable-nodpi; AnimalPortraits.kt; CosmeticUi.kt, HomeScreen.kt, JournalScreen.kt, GameViewModel.kt; WoodlandUiTest.kt/store case.
**Interfaces:** AnimalPortrait(appearance:Appearance,modifier:Modifier=Modifier); curated ID→drawable map and emblem fallback. portrait cosmetic group, cosmeticPortrait preference. Selecting an emblem clears portrait.
- [ ] Generate/inspect12 matching individual portraits, preserve exact originals/prompts and optimize package resources without repainting art.
- [ ] Native RED for optional collection, equipped/locked choice, legacy fallback and restored selection.
- [ ] Implement adaptive collection and home/profile/chat identity, retain emblem selector. Native/catalog/store GREEN.
- [ ] Commit and ledger.

### Task5: Reviewed APK and durable delivery
**Files:** app/build.gradle.kts version, README/handoff/delivery plan, QA/work log and upgrade fixture.
- [ ] Run :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest; affected native new/save/assistance/navigation/Daily/gameplay flows. Record actual counts/warnings.
- [ ] Actual18→19 update with nonempty profile/history/notes/preferences/emblem; verify preservation and default/resumed access. Inspect real normal/large screenshots.
- [ ] One fresh whole-branch review; resolve material findings once with regression evidence and green suite; record limits/minors.
- [ ] Freeze/signature/icon/installed-byte proof; save APK promptly, then preview/evidence. Publish exact own-branch source/draft stacked on feat/gridmates-coach-launcher/checkpoint. Complete plan/handoff; no main/development merge.
