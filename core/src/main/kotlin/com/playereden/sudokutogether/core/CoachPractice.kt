package com.playereden.sudokutogether.core

import com.playereden.sudokutogether.core.HumanSudoku.Technique
import com.playereden.sudokutogether.core.SudokuAssistance.CoachStep

/** One-move exercises built from the same deductions the Coach shows during play. */
object CoachPractice {
    class Lesson internal constructor(
        val id: String,
        val description: String,
        val puzzle: Puzzle,
        val step: CoachStep,
        val setup: List<CoachStep>
    ) {
        val technique: Technique get() = step.technique
        val target: Int = step.targetCells.first()
        val correctDigits: List<Int> = if (step.eliminations.isEmpty()) step.digits
            else step.eliminations.getValue(target)
        val options: List<Int> = if (step.eliminations.isEmpty()) (1..9).toList() else candidatesAt(target)

        /** Candidates at the start of the exercise, with setup deductions already applied. */
        fun candidatesAt(cell: Int): List<Int> {
            if (puzzle.clues.getOrNull(cell) != 0) return emptyList()
            val peerDigits = puzzle.clues.filterIndexed { peer, digit ->
                digit != 0 && SudokuAssistance.isPeer(cell, peer)
            }
            val setupRemovals = setup.flatMap { it.eliminations[cell].orEmpty() }
            return (1..9).filterNot { it in peerDigits || it in setupRemovals }
        }
    }

    val lessons: List<Lesson> by lazy {
        listOf(
            lesson(
                "only-digit", Technique.NAKED_SINGLE,
                "Use the filled squares in the target's row, column and box to rule out digits.",
                "530070000600195000098000060800060003400803001700020006060000280000419005000080079"
            ),
            lesson(
                "only-square", Technique.HIDDEN_SINGLE,
                "Look for a digit with only one possible square in a row, column or box.",
                "100007090030020008009600500005300900010080002600004000300000010040000007007000300"
            ),
            lesson(
                "locked-candidate", Technique.LOCKED_CANDIDATE,
                "A row or column can confine a digit to one box, clearing it from the rest of that box.",
                "600945002002673000054128060001567400576894231849312006003056920000031600465289003"
            ),
            lesson(
                "naked-pair", Technique.NAKED_PAIR,
                "Two squares with the same two candidates reserve those digits in their shared row, column or box.",
                "005014607000590020000300000703050180060030040100008006907000400000000008001040905"
            ),
            // One clue is cleared from the Coach test board so either of two extra candidates is a valid answer.
            lesson(
                "hidden-pair", Technique.HIDDEN_PAIR,
                "When two digits can go only in the same two squares, cross off the other candidates in those squares.",
                "000008005205090670000000400360000200092106040050070000000030800020500701006807090"
            ),
            lesson(
                "x-wing", Technique.X_WING,
                "A digit confined to the same two columns in two rows can be crossed off elsewhere in those columns.",
                "041729030769003402032640719403900170607004903195370024214567398376090541958431267"
            )
        )
    }

    private fun lesson(id: String, technique: Technique, description: String, clues: String): Lesson {
        val puzzle = Puzzle("practice-$id", clues.map { it.digitToInt() })
        val trace = checkNotNull(SudokuAssistance.nextCoachHint(puzzle, puzzle.clues)) {
            "No Coach proof for practice lesson $id"
        }.steps
        val index = trace.indexOfFirst { it.technique == technique }
        check(index >= 0) { "No $technique step for practice lesson $id" }
        val setup = trace.take(index)
        check(setup.all { it.eliminations.isNotEmpty() }) { "Practice setup must only remove candidates" }
        return Lesson(id, description, puzzle, trace[index], setup)
    }
}
