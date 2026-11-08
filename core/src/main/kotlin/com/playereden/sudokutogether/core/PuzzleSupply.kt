package com.playereden.sudokutogether.core

/**
 * Chooses a fresh original puzzle without changing the legacy deterministic
 * Sudoku.generate(seed) contract or saved-game wire format.
 *
 * Keep recent IDs outside this class so it is easy to test the selection logic
 * without Android or persistent-storage dependencies.
 */
object PuzzleSupply {
    const val RECENT_LIMIT = 64
    const val MAX_ATTEMPTS = 12

    fun next(recentIds: Collection<String>, seed: () -> Long): Puzzle {
        val blocked = recentIds.toHashSet()
        repeat(MAX_ATTEMPTS) {
            val puzzle = Sudoku.generate(seed())
            if (puzzle.id !in blocked) return puzzle
        }
        error("Could not make a fresh puzzle. Your previous puzzle was not replaced.")
    }

    const val MAX_RATED_ATTEMPTS = 200

    data class RatedPuzzle(val puzzle: Puzzle, val assessment: HumanSudoku.Assessment)

    /**
     * A bounded, never-downgrading supplier for later difficulty selection.
     * The player UI will use this only after calibration and save migrations
     * are finished in v0.8C. All work should execute on a worker dispatcher.
     */
    fun nextRated(
        requested: HumanSudoku.Grade,
        recentIds: Collection<String>,
        seed: () -> Long,
        attempts: Int = MAX_RATED_ATTEMPTS
    ): RatedPuzzle {
        val floor = when (requested) {
            HumanSudoku.Grade.EASY -> 36
            HumanSudoku.Grade.MEDIUM -> 29
            HumanSudoku.Grade.HARD -> 24
            else -> throw IllegalArgumentException("Only logically rated tiers can be requested")
        }
        require(attempts in 1..MAX_RATED_ATTEMPTS)
        val blocked = recentIds.toHashSet()
        repeat(attempts) {
            val puzzle = Sudoku.generate(seed(), floor)
            if (puzzle.id in blocked) return@repeat
            val assessed = HumanSudoku.assess(puzzle)
            if (assessed.logicallySolved && assessed.grade == requested) {
                return RatedPuzzle(puzzle.copy(difficulty = requested.name), assessed)
            }
        }
        error("Could not generate a fresh ${requested.name.lowercase()} puzzle in $attempts attempts. Try again; no saved puzzle was replaced.")
    }

    fun remember(recentIds: Collection<String>, puzzleId: String): List<String> =
        (recentIds.filter { it != puzzleId } + puzzleId).takeLast(RECENT_LIMIT)
}
