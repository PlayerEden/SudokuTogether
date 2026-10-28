# Sudoku Together — START HERE

**This repository is the durable source of truth for the project, including when the ChatGPT conversation fills up.**

> **Important:** `main` currently contains this landing page and manual GitHub Actions workflows; **the full Android game is on [`development`](https://github.com/PlayerEden/SudokuTogether/tree/development)**. Do not start from the tiny `main` code tree and assume the game is gone.

## Pick up the game in a new chat

1. **[Open the current living project handoff](https://github.com/PlayerEden/SudokuTogether/blob/development/docs/PROJECT_HANDOFF.md)** — exact state, source branch, verified builds, pending tests, decisions, issues, rollout and rollback.
2. **[Open the development source](https://github.com/PlayerEden/SudokuTogether/tree/development)** — full Android project, tests and prior docs.
3. **[Open the product roadmap](https://github.com/PlayerEden/SudokuTogether/blob/development/docs/ROADMAP_V08_TO_LAUNCH.md)** — v0.8 through launch.
4. Check [recent PRs](https://github.com/PlayerEden/SudokuTogether/pulls), [issues](https://github.com/PlayerEden/SudokuTogether/issues) and [Actions runs](https://github.com/PlayerEden/SudokuTogether/actions) for work after the handoff's last update.

**Current verified milestone:** **v0.10B**, offline Daily History to continue/review previously saved days, accurate Solo/Daily completion summaries, and capped retention that does not silently delete unfinished boards. Full QA [build passed](https://github.com/PlayerEden/SudokuTogether/actions/runs/40621938766); Android emulator [passed 10/10 tests](https://github.com/PlayerEden/SudokuTogether/actions/runs/40622291879) and [10/10 again](https://github.com/PlayerEden/SudokuTogether/actions/runs/40623046297). **Next:** visually review profile/finish screens via Compose-owned captures and focus on sensible Android launch/accessibility tasks. Do not mistake automation for physical-device certification. **The user has postponed physical two-phone testing until ready**; v0.9A remains unverified and blocks Split Clues validation. Physical single-phone day-rollover/200%-font/TalkBack checks remain pending.

**Known-good rollback checkpoint:** [`checkpoint/v0.10b-qa-green`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.10b-qa-green) at `83df54621bb2713917b518ee8ccfed4c25511a80`. Earlier [v0.10A](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.10a-qa-green) and [v0.8E](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8e-qa-green), [v0.8D](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8d-qa-green) and [v0.8C](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8c-qa-green) checkpoints are preserved.

**Why not make a separate game yet?** We want to finish Sudoku Together first. The future reusable Android foundation idea is preserved in [the architecture note](https://github.com/PlayerEden/SudokuTogether/blob/development/docs/REUSABLE_ANDROID_PUZZLE_FOUNDATION.md), but it is **paused** until Sudoku is ready.

## New-chat message to paste

> Continue Sudoku Together at https://github.com/PlayerEden/SudokuTogether. First read the default branch's CONTINUE_HERE.md and the latest development/docs/PROJECT_HANDOFF.md. Verify the latest branch, PR, CI runs and tests before making changes. We completed v0.10B offline Daily History, fair Solo/Daily finish summaries and safe unfinished-daily retention (verified QA); next is native accessibility/visual QA and Android launch readiness. The user deferred two-real-phone LAN QA until ready; that v0.9A gate remains pending and blocks Split Clues validation. Keep GitHub as the project history/backup, update the handoff and roadmap when work progresses, preserve a known-good rollback checkpoint, and provide a verified Android APK and emulator screenshots when practical. Finish Sudoku itself; defer building the cross-game framework.

## Development/backup principles

- All source, Gradle configs, specs, design history, QA notes, test code and the milestone handoff belong in GitHub.
- GitHub Actions APK **artifacts expire** (currently 14 days). Save the source and exact working commit; do not rely on an old chat download link as the only backup.
- The QA build workflow is manual/opt-in. Running it from the default `main` uses the current `development` app source; choosing another branch on the dispatch page builds that selected branch.
- Keep release signing keys, passwords, account information and other secrets **outside this public repository**. No real Play Store deployment is currently claimed.
- After a new passing milestone, update `development/docs/PROJECT_HANDOFF.md` with code branch/commit, run URLs, failures, unresolved manual tests, user-visible improvements and the next decision.
