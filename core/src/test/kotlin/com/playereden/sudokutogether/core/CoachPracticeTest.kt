package com.playereden.sudokutogether.core

import com.playereden.sudokutogether.core.HumanSudoku.Technique.HIDDEN_PAIR
import com.playereden.sudokutogether.core.HumanSudoku.Technique.HIDDEN_SINGLE
import com.playereden.sudokutogether.core.HumanSudoku.Technique.LOCKED_CANDIDATE
import com.playereden.sudokutogether.core.HumanSudoku.Technique.NAKED_PAIR
import com.playereden.sudokutogether.core.HumanSudoku.Technique.NAKED_SINGLE
import com.playereden.sudokutogether.core.HumanSudoku.Technique.X_WING
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachPracticeTest {
    @Test fun lessonsPresentSixRulesInOrderUsingCoachProofs() {
        val lessons = CoachPractice.lessons
        assertEquals(listOf(
            "only-digit" to NAKED_SINGLE,
            "only-square" to HIDDEN_SINGLE,
            "locked-candidate" to LOCKED_CANDIDATE,
            "naked-pair" to NAKED_PAIR,
            "hidden-pair" to HIDDEN_PAIR,
            "x-wing" to X_WING
        ), lessons.map { it.id to it.technique })

        for (lesson in lessons) {
            val board = lesson.puzzle.clues
            assertEquals(lesson.id, 1, Sudoku.solutionCount(board))
            assertEquals(lesson.id, 0, board[lesson.target])
            assertEquals(lesson.id, lesson.step.targetCells.first(), lesson.target)
            val trace = SudokuAssistance.nextCoachHint(lesson.puzzle, board)!!.steps
            assertEquals(lesson.id, trace.take(lesson.setup.size + 1), lesson.setup + lesson.step)
        }
    }

    @Test fun placementPromptsOfferAllDigitsAndUseTheForcedAnswer() {
        val examples = mapOf(
            "only-digit" to Example(40, listOf(5), listOf(5)),
            "only-square" to Example(65, listOf(1, 2, 6, 8), listOf(1))
        )
        for ((id, expected) in examples) {
            val lesson = CoachPractice.lessons.single { it.id == id }
            assertEquals(id, expected.target, lesson.target)
            assertEquals(id, (1..9).toList(), lesson.options)
            assertEquals(id, expected.candidates, lesson.candidatesAt(lesson.target))
            assertEquals(id, expected.answers, lesson.correctDigits)
            assertTrue(id, lesson.setup.isEmpty())
            val trial = lesson.puzzle.clues.toMutableList().apply {
                this[lesson.target] = lesson.correctDigits.single()
            }
            assertEquals(id, 1, Sudoku.solutionCount(trial))
        }
    }

    @Test fun eliminationPromptsAcceptEveryRemovalFromTheirTarget() {
        val examples = mapOf(
            "locked-candidate" to Example(62, listOf(4, 7, 8), listOf(7)),
            "naked-pair" to Example(26, listOf(1, 4, 9), listOf(9)),
            "hidden-pair" to Example(48, listOf(2, 3, 4, 9), listOf(4, 9)),
            "x-wing" to Example(31, listOf(5, 8), listOf(5))
        )
        for ((id, expected) in examples) {
            val lesson = CoachPractice.lessons.single { it.id == id }
            assertEquals(id, expected.target, lesson.target)
            assertEquals(id, expected.candidates, lesson.options)
            assertEquals(id, expected.answers, lesson.correctDigits)
            for ((cell, digits) in lesson.step.eliminations) for (digit in digits) {
                assertTrue(id, digit in lesson.candidatesAt(cell))
                val trial = lesson.puzzle.clues.toMutableList().apply { this[cell] = digit }
                assertEquals(id, 0, Sudoku.solutionCount(trial))
            }
        }
    }

    @Test fun hiddenPairUsesPriorEliminationsWithoutRemovingItsOwnAnswers() {
        val lesson = CoachPractice.lessons.single { it.id == "hidden-pair" }
        assertEquals(listOf(LOCKED_CANDIDATE, LOCKED_CANDIDATE, NAKED_PAIR), lesson.setup.map { it.technique })
        assertEquals(listOf(1, 9), lesson.candidatesAt(51))
        assertEquals(listOf(1, 6, 7, 9), lesson.candidatesAt(18))
        assertEquals(listOf(1, 9), lesson.candidatesAt(6))
        assertEquals(listOf(2, 3, 4, 9), lesson.candidatesAt(48))
        assertEquals(listOf(2, 3, 4, 9), lesson.candidatesAt(50))
        assertTrue(lesson.candidatesAt(5).isEmpty())
    }

    private data class Example(val target: Int, val candidates: List<Int>, val answers: List<Int>)
}
