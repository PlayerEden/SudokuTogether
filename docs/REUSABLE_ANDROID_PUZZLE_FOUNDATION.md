# Reusable Native Android Puzzle-Game Foundation

Status: **architecture direction and incremental extraction plan**, not a claim that Sudoku Together is already a generic engine. This document lives with the current v0.8C development branch so it is preserved alongside game work.

## Mission

Build a complete **Sudoku Together** with reusable Android/Kotlin foundations that can later start **Crossword Together**, **Minesweeper Together**, and other offline-first, small-scale cooperative/competitive games **without rebuilding menus, room handling, profiles, saves, test automation and app packaging**. Each game still gets its own authentic mechanics, branding, accessibility controls and game-specific QA.

**Priority order:** finish Sudoku reliably; extract stable reusable boundaries while we develop; verify by building a second distinct prototype; only then publish a clean starter/template repository. We must not claim plug-and-play reuse before that proof.

## Current architecture audit (2026-10-27)

- `:app` is a native Android Jetpack Compose app. `MainActivity` handles lifecycle, theme, APK sharing and model wiring. `SudokuScreen` has home, mode selection, puzzle controls, chat, profile, records and cosmetics.
- `GameViewModel` combines Sudoku-specific operations/solver selection with portable concerns like local player data, role/navigation, haptics preferences, lifecycle, multiplayer discovery and error feedback.
- `RoomDiscovery.kt` uses Android NSD (same-Wi-Fi discovery) and local network addresses; much of this could work for other games.
- `:core` is currently both the Sudoku rules module **and** a LAN multiplayer implementation. `Protocol.kt` serializes Sudoku-specific `Puzzle`, `Snapshot`, `Operation` and `Outcome`. `HostServer`, `GuestClient`, `GameSession` depend directly on those models.
- `SessionStore.kt` and `LocalPlayerStore.kt` implement defensively persisted local game/profile saves, but their data contracts are Sudoku-specific.
- Existing GitHub Actions workflows build/test Android APKs and run emulator tests. They can be copied with new app IDs and branch configuration.

**Conclusion:** You can already copy the whole repo to start another Kotlin/Compose game, but it is **not yet** a drop-in reusable foundation: the shared transport, persistence and UI must be detached from Sudoku assumptions. This is a refactoring opportunity, not a reason to delay v0.8C.

## Planned boundaries

```text
:app                         game-specific Android application,
                             theme, app icon, final navigation wiring
:game:sudoku                 Sudoku puzzle generation, logical solver,
                             board, actions, completion and hint policy
:foundation:session          generic player/room lifecycle contracts,
                             host authority, reconnect state, timing
:foundation:lan              Wi-Fi discovery, sockets, frame limits,
                             versions, connection failure handling
:foundation:storage          atomic saves, schema version handling,
                             explicit migrations and recovery
:foundation:player           player identity, local achievements hooks,
                             settings and cosmetic preference contracts
:foundation:ui               optional shared Compose mode selector,
                             room join/chat sheets, result shell,
                             confirmation/help/accessibility patterns
```

This is a **target architecture**, not modules already created. Begin with a small practical extraction, not all seven modules at once. Android's official modularization guidance warns that too many modules impose build and maintenance cost.

### Strict boundary rules

1. A game's rules must not import Android SDK, socket classes or Compose.
2. Transport must not reference Sudoku `Cell`, `Snapshot`, `Puzzle`, `Operation`, digits 1–9 or board size 81. An app-specific serializer/adapter and versioned game ID determine the payload. Unsupported game/version combinations fail clearly.
3. The game owns legal moves, completion, undo policy, scoring, visibility and difficulty. The foundation owns delivery, connection lifecycle, retries/backpressure and persistence mechanism.
4. A trusted host retains canonical state. Guests receive a deliberately **projected, permitted** state: Minesweeper must not leak unrevealed mines; Split Clues must not leak partner digits; competitive puzzles must not leak rival answers. Never serialize an entire hidden-answer board into a guest message just because the UI masks it.
5. Foundation saves use explicit versioning and game-specific migration adapters. Copying a repo must never overwrite another game's saved data. A new app needs a new `applicationId`, package namespace, save filenames and signing identity.
6. Reusable UI is a shell, **not a universal board renderer**. Sudoku uses a 9×9 number grid, crossword needs word entry/clue focus and keyboard, Minesweeper needs reveal/flag cells, mine-safe first tap and a loss state.
7. Test coverage includes sample contracts and per-game fixtures. No universal rule assumption such as “every game ends only on a correct filled grid” or “every player can edit all squares”.
8. The foundation must remain offline-capable: a Wi-Fi room is a local session, not internet matchmaking; no account/backend is silently introduced.

## What new games can inherit

| Existing system | Target reuse | What stays unique |
|---|---|---|
| Home/profile/settings/cosmetics | Shared native Compose patterns and persisted settings | Title, visual identity, game modes, earned rewards |
| Wi-Fi discovery/host/join/chat | Generic LAN service with typed per-game messages | Rules for moves, collaboration, pause and hidden information |
| Undo/save/reconnect/room codes | Persistence mechanisms, session lifecycle and communication | Legal undo decisions, per-game save schema, move interpretation |
| Achievements, completion records | Generic event/stat contracts and profile presentation | Which events count, scores and win/loss logic |
| QA, Gradle, signing and build scripts | Starter CI and reproducible APK/AAB pipeline | App IDs, assets, store listing, release signing key |
| Sudoku 9×9 board and human solver | **Not reusable as-is** | Crossword grid words/clues or Minesweeper hazards/reveals |

## Migration sequence with gates

**F0 — Design and tests (parallel with v0.8C):** inventory what couples to Sudoku. Capture existing integration tests, saved-game fixtures, LAN version behavior, current APK checksum and a gameplay screenshot. Add no breaking source refactor before baseline is green.

**F1 — Isolate platform utilities:** extract generic atomic storage and NSD/socket frame components without changing user behavior. Keep adapters so Sudoku public APIs stay compatible. **Gate:** JVM and Android tests; byte-equivalent reconnect/save behavior; same APK features.

**F2 — Explicit game adapter:** a small `GameDefinition` / `GameSessionAdapter` contract for legal actions, projected public state, versioned persistence, completion and result events. Sudoku remains one concrete adapter. **Gate:** old saved solo/host/guest sessions still load; two-phone room and hidden-state tests pass.

**F3 — Extract reusable Compose shells:** preserve the existing distinctive Sudoku visuals while moving only naturally shared home/mode, join/chat, profile and result components. **Gate:** no UI regressions at small screens or large system fonts.

**F4 — Prove reuse with a small Minesweeper slice:** use the same native shell and local room transport but distinct reveal/flag/loss semantics and hidden mines. If this takes nearly as much work as starting over, inspect the abstraction boundaries and simplify them.

**F5 — Publish a clean starter/template repository:** only after F4, create a separate clean `PlayerEden/Android-Puzzle-Foundation` (working name), with a minimalist sample game, READMEs, license decision, CI and clear instructions for replacing app identity, resources, manifests, signing and save schema. Existing Sudoku remains its own game repo. A GitHub **template repository** creates an independent repository with a fresh history, unlike a fork.

## Future game acceptance examples

**Crossword:** clue selection, physical/soft keyboard navigation, intersection constraints, source/licensing for clue corpus, multiple words/answers and independent win condition. Optional two-phone solving can share entered letters while revealing clues according to room rules.

**Minesweeper:** first-click-safe mine placement, reveal flood fill, flagging, timer, loss/retry, multiple difficulties, and host-only hidden mine state. An optional co-op mode shares revealed tiles/flags but never sends the full mine map to the guest until completion.

## GitHub and release considerations

- The `SudokuTogether` repository's default `main` has historically contained only a minimal skeleton and QA workflows; feature code lives on stacked PR branches. **Do not make the current `main` a template** until the actual stable game and reusable foundation are on an appropriate default branch.
- Use GitHub **Use this template** once the clean starter exists; do not fork Sudoku and ship residual old titles, package identifiers, icon assets, privacy wording or signing secrets.
- The new codebase must pass its own device, puzzle-rule and UI tests. Similar game genre does not guarantee identical architecture.

## Relationship to existing roadmap

- Continue v0.8C selectable Sudoku difficulty and safe migration first.
- Start F0 now and perform F1 opportunistically after v0.8C verification; avoid risky giant refactors between saved-game and multiplayer changes.
- v0.9 real-device LAN acceptance helps validate F2 before Split Clues.
- F4 should happen once Sudoku's core session/transport behavior is solid; it **doesn't** require delaying all Sudoku polish until Minesweeper exists.
- Gate eventual Play submission independently for every app.

References: https://developer.android.com/topic/modularization and https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template
