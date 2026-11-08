package com.playereden.sudokutogether.core

import com.playereden.sudokutogether.core.HumanSudoku.Technique.HIDDEN_SINGLE
import com.playereden.sudokutogether.core.HumanSudoku.Technique.LOCKED_CANDIDATE
import com.playereden.sudokutogether.core.HumanSudoku.Technique.NAKED_PAIR
import com.playereden.sudokutogether.core.HumanSudoku.Technique.NAKED_SINGLE
import org.junit.Assert.*
import org.junit.Test

class SudokuCoachTest {
    private val classic = puzzle("530070000600195000098000060800060003400803001700020006060000280000419005000080079")

    @Test fun nakedSingleShowsOneFilledPeerForEachExcludedDigit() {
        val hint = SudokuAssistance.nextCoachHint(classic, classic.clues)!!
        val step = hint.steps.single()
        assertEquals(NAKED_SINGLE, step.technique)
        assertEquals(40, hint.cell)
        assertEquals(step.explanation, hint.explanation)
        assertEquals(listOf(5), step.digits)
        assertTrue(step.eliminations.isEmpty())
        assertTrue(step.sourceCells.all { SudokuAssistance.isPeer(40, it) })
        assertEquals((1..9).filter { it != 5 }, step.sourceCells.map { classic.clues[it] }.sorted())
    }

    @Test fun claimingExplainsTheHiddenSingleItUnlocksWithoutChangingTheBoard() {
        val puzzle = puzzle("600945002002673000054128060001567400576894231849312006003056920000031600465289003")
        val visible = puzzle.clues.toMutableList()
        val hint = SudokuAssistance.nextCoachHint(puzzle, visible)!!
        assertEquals(2, hint.steps.size)
        val (claiming, placement) = hint.steps
        assertEquals(LOCKED_CANDIDATE, claiming.technique)
        assertEquals(listOf(78, 79), claiming.sourceCells)
        assertEquals(listOf(62, 70, 71), claiming.targetCells)
        assertEquals(listOf(7), claiming.digits)
        assertEquals(mapOf(62 to listOf(7), 70 to listOf(7), 71 to listOf(7)), claiming.eliminations)
        assertEquals(62, hint.cell)
        assertEquals(claiming.explanation, hint.explanation)
        assertEquals(HIDDEN_SINGLE, placement.technique)
        assertEquals(listOf(8, 13, 32, 44, 53, 80), placement.sourceCells)
        assertEquals(listOf(26), placement.targetCells)
        assertEquals(listOf(7), placement.digits)
        assertTrue(placement.eliminations.isEmpty())
        assertEquals(puzzle.clues, visible)
        assertEquals(hint, SudokuAssistance.nextCoachHint(puzzle, visible))
        assertEquals(26, SudokuAssistance.nextHint(puzzle, visible)?.cell)
    }

    @Test fun nakedPairRecordsOnlyTheDigitsEachTargetActuallyLoses() {
        val puzzle = puzzle("005014607000590020000300000703050180060030040100008006907000400000000008001040905")
        val hint = SudokuAssistance.nextCoachHint(puzzle, puzzle.clues)!!
        assertEquals(listOf(NAKED_PAIR, HIDDEN_SINGLE), hint.steps.map { it.technique })
        val pair = hint.steps.first()
        assertEquals(listOf(35, 44), pair.sourceCells)
        assertEquals(listOf(26, 62), pair.targetCells)
        assertEquals(listOf(2, 9), pair.digits)
        assertEquals(mapOf(26 to listOf(9), 62 to listOf(2)), pair.eliminations)
    }

    @Test fun pointingAndOtherEliminationsCanBeShownWithoutAPlacement() {
        val puzzle = puzzle("000008005205090670000000400360000200092106040054070000000030800020500701006807090")
        val hint = SudokuAssistance.nextCoachHint(puzzle, puzzle.clues)!!
        assertEquals(listOf(LOCKED_CANDIDATE, LOCKED_CANDIDATE, NAKED_PAIR), hint.steps.map { it.technique })
        val pointing = hint.steps.first()
        assertEquals(listOf(48, 50), pointing.sourceCells)
        assertEquals(listOf(51, 52, 53), pointing.targetCells)
        assertEquals(mapOf(51 to listOf(3), 52 to listOf(3), 53 to listOf(3)), pointing.eliminations)
        assertTrue(hint.steps.all { it.eliminations.isNotEmpty() })
        assertNull(SudokuAssistance.nextHint(puzzle, puzzle.clues))
    }

    @Test fun invalidChangedAndAlreadySolvedBoardsHaveNoProof() {
        val changedGiven = classic.clues.toMutableList().apply { this[0] = 0 }
        val wrongEntry = classic.clues.toMutableList().apply { this[2] = 1 }
        val solved = puzzle("534678912672195348198342567859761423426853791713924856961537284287419635345286179")
        assertTrue(Sudoku.conflicts(wrongEntry).isEmpty())
        assertNull(SudokuAssistance.nextCoachHint(classic, wrongEntry))
        assertNull(SudokuAssistance.nextCoachHint(classic, changedGiven))
        assertNull(SudokuAssistance.nextCoachHint(classic, listOf(0)))
        assertNull(SudokuAssistance.nextCoachHint(classic, solved.clues))
    }

    private fun puzzle(clues: String) = Puzzle("coach-reference", clues.map { it.digitToInt() })
}
