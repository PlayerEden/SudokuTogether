package com.playereden.sudokutogether.core

/**
 * Read-only hints based on the player's currently visible Sudoku grid.
 * Never looks up the original solved board or edits a cell.
 * Restricted to solo by the Android presentation layer; later hidden-clue
 * and competitive modes will need their own explicit assistance policy.
 */
object SudokuAssistance {
    data class Hint(val cell: Int, val digit: Int, val technique: HumanSudoku.Technique, val explanation: String)

    fun nextHint(puzzle: Puzzle, visible: List<Int>): Hint? {
        if (visible.size != 81 || puzzle.clues.size != 81) return null
        if (visible.any { it !in 0..9 }) return null
        if ((0..80).any { puzzle.clues[it] != 0 && puzzle.clues[it] != visible[it] }) return null
        // An incorrect entry in a uniquely-solvable puzzle removes the
        // solution; never give an invented deduction for a broken grid.
        val report = HumanSudoku.assess(Puzzle("visible-hint", visible))
        if (!report.uniqueSolution || !report.logicallySolved && report.steps.isEmpty()) return null
        val targetIndex = report.steps.indexOfFirst {
            it.technique == HumanSudoku.Technique.NAKED_SINGLE ||
                it.technique == HumanSudoku.Technique.HIDDEN_SINGLE
        }
        if (targetIndex < 0) return null
        val step = report.steps[targetIndex]
        val cell = step.cells.single()
        val digit = step.digits.single()
        if (visible[cell] != 0 || puzzle.clues[cell] != 0) return null

        val previousLogic = report.steps.take(targetIndex)
            .lastOrNull { it.technique == HumanSudoku.Technique.LOCKED_CANDIDATE ||
                it.technique == HumanSudoku.Technique.NAKED_PAIR }
        val basis = when (step.technique) {
            HumanSudoku.Technique.NAKED_SINGLE -> "Only $digit fits this square after checking its row, column and box."
            HumanSudoku.Technique.HIDDEN_SINGLE -> "$digit has only one available place in its row, column or box."
            else -> return null
        }
        val leadIn = previousLogic?.let {
            "First use ${if (it.technique == HumanSudoku.Technique.LOCKED_CANDIDATE) "a locked candidate" else "a naked pair"}: ${it.reason}. "
        } ?: ""
        return Hint(cell, digit, step.technique,
            "${leadIn}Row ${cell / 9 + 1}, column ${cell % 9 + 1}: $basis")
    }

    fun isPeer(first: Int, second: Int): Boolean =
        first in 0..80 && second in 0..80 && first != second &&
            (first / 9 == second / 9 || first % 9 == second % 9 ||
                (first / 27 == second / 27 && (first % 9) / 3 == (second % 9) / 3))
}
