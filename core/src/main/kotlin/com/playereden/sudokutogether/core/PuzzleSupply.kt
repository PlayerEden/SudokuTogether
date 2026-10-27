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

    fun remember(recentIds: Collection<String>, puzzleId: String): List<String> =
        (recentIds.filter { it != puzzleId } + puzzleId).takeLast(RECENT_LIMIT)
}
