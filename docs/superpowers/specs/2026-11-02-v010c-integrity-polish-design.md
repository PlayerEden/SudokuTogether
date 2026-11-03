# v0.10C: save integrity and native finish/history polish

The user authorized this pass after the November 2 report/source review and explicitly delegated implementation and overhaul decisions. Continue from `development` commit `a4448bf9246ac6b855c1825b42b91fae3eae9f5b`; do not resume the obsolete v0.2 work. The code baseline is the v0.10B green checkpoint, with later documentation-only changes.

## Selected approach

Preserve the current native Kotlin/Compose game and make a focused reliability and visual-quality update. A full UI/engine rewrite would put working saves and gameplay at unnecessary risk; adding more modes would leave the identified problems unresolved. Keep Solo, Daily, co-op and versus behavior and serialized profile schema 1 compatible. Larger boards, Split Clues, remote accounts, generic game-framework extraction, production publishing, and the deferred two-phone test remain later work.

## Save integrity

The current LocalPlayerStore replaces unreadable data in memory with a new profile after an unchecked backup attempt. Replace that behavior with explicit recovery: maintain an atomically written last-good private profile backup; validate all stored progress and undo cells; recover a valid backup with an honest warning; and preserve damaged primary bytes before any recovery rewrite. If no valid copy exists, or damaged bytes cannot be preserved, keep source files intact and block writes. A placeholder may render the recovery message but must never overwrite the user's files. No automatic reset or claim of cloud/uninstall backup.

Writes must validate before persistence, preserve the previous healthy snapshot, finish the AtomicFile transaction and verify the committed bytes before changing in-memory data. A failed backup/write must not count a completed puzzle or change the visible saved state. Existing Android AtomicFile `.bak`/`.new` behavior remains supported. Move initial profile I/O/validation off the UI thread; loading and blocked recovery must have clear UI states. No destructive reset action is introduced.

## Daily retention and completion

Apply the old experiment's useful intent to current code: retain every unfinished Daily within the 128-slot safety cap, and keep at most 31 completed review boards whenever progress is updated or a new day is opened. Completion records remain separate. Repeated completion recording must remain idempotent even if that old board has already been pruned.

The old patch alone would break finishing an old date: GameViewModel currently looks up the completed board in the archive after saving. Use a typed local-edit result containing both the new persistent profile and the accepted progress. Keep the just-finished progress in UI state for its result screen even when review retention evicts it. Old completed boards outside the retained review window need not reappear after process restart; their records must remain. Existing regular Solo progress is unaffected.

## Native polish and evidence

Capture actual Compose screens for Daily History, Daily play, Solo finish, and an older Daily finish, at normal text and 200% text; inspect a compact portrait and landscape setup. Seed test data only in controlled instrumentation fixtures and restore it afterward. Extend the existing screenshot mechanism instead of relying on unobstructed emulator launcher captures.

Fix observed layout problems in history rows and recent records with adaptive stacking/wrapping, preserve 48dp action targets, and keep the existing calm visual system. An old saved Daily must not say "Today's challenge" or give misleading tomorrow-only instructions. Finish metadata must remain visible after archive eviction. Keep guidance concise and native navigation predictable.

## Delivery

Version `0.10.0-dev3` / code 15, isolated `.qa` package; no wire change. Run the existing core/Android tests plus new recovery, eviction, idempotency and UI regressions; build/lint locally, run supported emulator tests, and obtain one fresh whole-change review. Save source, QA evidence, screenshots and handoff on a dedicated GitHub branch and draft PR. Keep known-green checkpoints and `main` intact. A debug QA APK is not a production-signing or physical-device certification claim.
