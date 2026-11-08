# v0.10D — Game presentation and convenience

## Intent and authority

The user requested a thorough pass over player-facing visuals, a real game-app feel, and convenience, following the completed v0.10C save-integrity milestone. They previously delegated overhaul decisions and now explicitly authorized this pass. Styling and layout choices below are the implementer's decisions; they are not represented as user-selected mockups.

## Direction and approaches

Keep the cream, forest-green and warm-gold identity, with a consistent type scale, original vector grid emblem, expressive mode cards, clearer surfaces and a framed board. A colorful arcade skin would compete with the grid and existing cosmetic inks; a small color-only refresh would leave the long settings/profile flow and hidden Resume action intact. The chosen approach is a cohesive native Compose refresh with navigation improvements. This is one presentation milestone, not a rewrite of the engine.

## Player experience

- Home: compact wordmark and Help, distinct Solo / Co-op / Versus choices, mode-specific game card, difficulty choice, immediate saved-Solo Resume before secondary actions, compact Daily card, and game sharing. New-game replacement still requires confirmation. No mandatory identity form before playing.
- Persistent navigation on Home/profile pages: Play, Records, Collection, Settings. Each destination has its own focused content and can return to Play in one tap. Existing in-game profile links open Records.
- Play: clearly labeled top actions, compact progress/time information, framed Sudoku grid, selected-cell context and a pinned thumb-friendly input dock with consistent icons. The whole grid and keypad fit together on compact portrait. Short landscape screens use a board and controls side by side. Small-height/large-text fallbacks scroll rather than clip actions.
- Completion: visually distinct trophy/result panel and clear replay/Home/records actions, retaining the exact saved result and assisted/unassisted meaning.
- Join: styled native room cards, clear same-Wi-Fi instructions, keyboard Next/Done behavior, actionable empty/loading/error states. The manual address and six-digit code remain supported.
- Records: readable overview, fair separate best times, Daily History and recent results. Collection: visible cosmetic previews, selected/locked states and unlock requirements. Settings: player name, classic numbers, haptics, and Solo pencil cleanup, with a clear assisted-result explanation.

## Constraints and anti-goals

- Kotlin/Jetpack Compose; use existing dependencies, minSDK 26 / compile and target 35. No web UI, external image/font dependency, network service or engine/framework extraction.
- Keep package `com.playereden.sudokutogether.qa`, schema 1, protocol 5, save/recovery logic, puzzle grading, result fairness, privacy projections and optional-assistance behavior compatible.
- Keep accessible control labels and at least 48dp primary touch targets. Grid glyphs must fit cells with large system text. Preserve high-contrast dark-board cosmetics.
- Never use correctness, hints or concealed opponent information for decorative progress. Filled-cell progress is not a correctness score.
- No new gameplay, Split Clues, monetization, audio, account, cloud or production-release work.
- Physical two-phone acceptance remains deferred. Signing conflicts must not be resolved by deleting a progress-bearing installation.

## Verification and deliverables

Run core and app JVM tests, lint and Android APK/test assembly. Demonstrate meaningful navigation/layout regressions before their fixes, then run the native smoke flows and focused visual/convenience checks on an API26 emulator. Inspect untouched native screenshots at compact portrait, landscape and 200% text; include the new Home, modes, board selection/notes, Join, all journal destinations and completion. Record exact observed coverage and limits, preserve source in a separate milestone branch/draft PR, and deliver an APK plus visual evidence. Do not claim physical LAN, TalkBack or newer-Android acceptance from emulator captures.
