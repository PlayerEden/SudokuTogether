package com.playereden.sudokutogether.core

/**
 * Read-only assistance. Logical hints use the visible grid; explicit entry
 * checks compare player entries with the original uniquely-solvable puzzle.
 * Neither path edits a cell.
 * Restricted to solo by the Android presentation layer; later hidden-clue
 * and competitive modes will need their own explicit assistance policy.
 */
object SudokuAssistance {
    data class Hint(val cell: Int, val digit: Int, val technique: HumanSudoku.Technique, val explanation: String)

    data class CoachHint(val steps: List<CoachStep>) {
        val cell: Int get() = steps.first().targetCells.first()
        val explanation: String get() = steps.first().explanation
    }

    data class CoachStep(
        val technique: HumanSudoku.Technique,
        val sourceCells: List<Int>,
        val targetCells: List<Int>,
        val digits: List<Int>,
        val explanation: String,
        /** Candidates removed from each target at this step, empty for placements. */
        val eliminations: Map<Int, List<Int>>
    )

    enum class CheckKind { CONFLICT, INCORRECT_ENTRY, CORRECT_SO_FAR, NO_ENTRIES }
    data class EntryCheck(val kind: CheckKind, val cell: Int = -1, val conflicts: Set<Int> = emptySet()) {
        val usesSolution: Boolean get() = kind == CheckKind.INCORRECT_ENTRY || kind == CheckKind.CORRECT_SO_FAR
    }

    /** Called only after the player explicitly requests a solution check. */
    fun checkEntries(puzzle: Puzzle, visible: List<Int>): EntryCheck? {
        if (puzzle.clues.size != 81 || visible.size != 81 || visible.any { it !in 0..9 }) return null
        if ((0..80).any { puzzle.clues[it] != 0 && puzzle.clues[it] != visible[it] }) return null
        if (Sudoku.solutionCount(puzzle.clues) != 1) return null
        val entries = (0..80).filter { puzzle.clues[it] == 0 && visible[it] != 0 }
        if (entries.isEmpty()) return EntryCheck(CheckKind.NO_ENTRIES)
        val conflicts = Sudoku.conflicts(visible)
        if (conflicts.isNotEmpty()) {
            val target = entries.firstOrNull { it in conflicts } ?: return null
            return EntryCheck(CheckKind.CONFLICT, target, conflicts)
        }
        for (cell in entries) {
            val trial = puzzle.clues.toMutableList().also { it[cell] = visible[cell] }
            if (Sudoku.solutionCount(trial, limit = 1) == 0)
                return EntryCheck(CheckKind.INCORRECT_ENTRY, cell)
        }
        return EntryCheck(CheckKind.CORRECT_SO_FAR)
    }

    fun nextHint(puzzle: Puzzle, visible: List<Int>): Hint? {
        if (!hasUnchangedGivens(puzzle, visible)) return null
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

    /** Shows each elimination through the first placement, or until the solver has no further deduction. */
    fun nextCoachHint(puzzle: Puzzle, visible: List<Int>): CoachHint? {
        if (!hasUnchangedGivens(puzzle, visible)) return null
        val steps = HumanSudoku.coachSteps(visible)
        return if (steps.isEmpty()) null else CoachHint(steps)
    }

    private fun hasUnchangedGivens(puzzle: Puzzle, visible: List<Int>): Boolean =
        puzzle.clues.size == 81 && visible.size == 81 && visible.all { it in 0..9 } &&
            visible.indices.all { puzzle.clues[it] == 0 || puzzle.clues[it] == visible[it] }

    fun isPeer(first: Int, second: Int): Boolean =
        first in 0..80 && second in 0..80 && first != second &&
            (first / 9 == second / 9 || first % 9 == second % 9 ||
                (first / 27 == second / 27 && (first % 9) / 3 == (second % 9) / 3))
}
