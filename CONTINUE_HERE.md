# Sudoku Together — START HERE

**This repository is the durable source of truth for the project, including when the ChatGPT conversation fills up.**

> **Important:** `main` currently contains this landing page and manual GitHub Actions workflows; **the full Android game is on [`development`](https://github.com/PlayerEden/SudokuTogether/tree/development)**. Do not start from the tiny `main` code tree and assume the game is gone.

## Pick up the game in a new chat

1. **[Open the current living project handoff](https://github.com/PlayerEden/SudokuTogether/blob/development/docs/PROJECT_HANDOFF.md)** — exact state, source branch, verified builds, pending tests, decisions, issues, rollout and rollback.
2. **[Open the development source](https://github.com/PlayerEden/SudokuTogether/tree/development)** — full Android project, tests and prior docs.
3. **[Open the product roadmap](https://github.com/PlayerEden/SudokuTogether/blob/development/docs/ROADMAP_V08_TO_LAUNCH.md)** — v0.8 through launch.
4. Check [recent PRs](https://github.com/PlayerEden/SudokuTogether/pulls), [issues](https://github.com/PlayerEden/SudokuTogether/issues) and [Actions runs](https://github.com/PlayerEden/SudokuTogether/actions) for work after the handoff's last update.

**Current verified milestone:** **v0.8D**, optional logical Solo hints, reversible opt-in pencil-note cleanup and assisted-result records, on top of v0.8C difficulty selection. Full APK/test build [passed](https://github.com/PlayerEden/SudokuTogether/actions/runs/40610640428); Android emulator [passed 9/9 tests](https://github.com/PlayerEden/SudokuTogether/actions/runs/40610910679). **Next task:** v0.8E assist fairness, duplicate Solo timer/accessibility polish; then physical two-phone Wi-Fi QA before Split Clues. Do not confuse planned features with implemented ones.

**Known-good rollback checkpoint:** [`checkpoint/v0.8d-qa-green`](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8d-qa-green) at `89fdd86163ff4c414c96a03689b4246c21b396e2`. The previous [v0.8C checkpoint](https://github.com/PlayerEden/SudokuTogether/tree/checkpoint/v0.8c-qa-green) is also preserved. Do not force-push or delete either.

**Why not make a separate game yet?** We want to finish Sudoku Together first. The future reusable Android foundation idea is preserved in [the architecture note](https://github.com/PlayerEden/SudokuTogether/blob/development/docs/REUSABLE_ANDROID_PUZZLE_FOUNDATION.md), but it is **paused** until Sudoku is ready.

## New-chat message to paste

> Continue Sudoku Together at https://github.com/PlayerEden/SudokuTogether. First read the default branch's CONTINUE_HERE.md and the latest development/docs/PROJECT_HANDOFF.md. Verify the latest branch, PR, CI runs and tests before making changes. We completed v0.8D Solo hints and pencil cleanup (verified QA); next is v0.8E usability/assisted-record fairness, then real two-phone LAN QA before Split Clues. Keep GitHub as the project history/backup, update the handoff and roadmap when work progresses, preserve a known-good rollback checkpoint, and provide a verified Android APK and emulator screenshots when practical. Finish Sudoku itself; defer building the cross-game framework.

## Development/backup principles

- All source, Gradle configs, specs, design history, QA notes, test code and the milestone handoff belong in GitHub.
- GitHub Actions APK **artifacts expire** (currently 14 days). Save the source and exact working commit; do not rely on an old chat download link as the only backup.
- The QA build workflow is manual/opt-in. Running it from the default `main` uses the current `development` app source; choosing another branch on the dispatch page builds that selected branch.
- Keep release signing keys, passwords, account information and other secrets **outside this public repository**. No real Play Store deployment is currently claimed.
- After a new passing milestone, update `development/docs/PROJECT_HANDOFF.md` with code branch/commit, run URLs, failures, unresolved manual tests, user-visible improvements and the next decision.
