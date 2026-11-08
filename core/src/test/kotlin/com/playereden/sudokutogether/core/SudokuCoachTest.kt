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

    @Test fun hiddenPairExplainsWhichOtherCandidatesDisappearAfterPointing() {
        val puzzle = puzzle("000008005205090670000000400360000200092106040054070000000030800020500701006807090")
        val hint = SudokuAssistance.nextCoachHint(puzzle, puzzle.clues)!!
        assertEquals(listOf(LOCKED_CANDIDATE, LOCKED_CANDIDATE, NAKED_PAIR), hint.steps.take(3).map { it.technique })
        val pointing = hint.steps.first()
        assertEquals(listOf(48, 50), pointing.sourceCells)
        assertEquals(listOf(51, 52, 53), pointing.targetCells)
        assertEquals(mapOf(51 to listOf(3), 52 to listOf(3), 53 to listOf(3)), pointing.eliminations)
        val pair = hint.steps.firstOrNull { it.technique.name == "HIDDEN_PAIR" }
        assertNotNull("The Coach stopped before the hidden pair", pair)
        assertEquals(listOf(48, 50), pair!!.sourceCells)
        assertEquals(listOf(2, 3), pair.digits)
        assertEquals(mapOf(48 to listOf(9), 50 to listOf(9)), pair.eliminations)
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

    @Test fun xWingFindsExactlyTheSameProofWhenRowsAndColumnsAreSwapped() {
        // HoDoKu's X-Wing example, after the placements shown in its diagram.
        // https://hodoku.sourceforge.net/en/show_example.php?file=bf201&tech=X-Wing
        val rowPuzzle = puzzle("041729030769003402032640719403900170607004903195370024214567398376090541958431267")
        fun transpose(cell: Int) = cell % 9 * 9 + cell / 9
        val columnPuzzle = rowPuzzle.copy(clues = rowPuzzle.clues.indices.map { rowPuzzle.clues[transpose(it)] })
        val corners = listOf(13, 16, 40, 43)
        for ((grid, sources, target) in listOf(
            Triple(rowPuzzle, corners, 31),
            Triple(columnPuzzle, corners.map(::transpose).sorted(), transpose(31))
        )) {
            val hint = SudokuAssistance.nextCoachHint(grid, grid.clues)
            assertNotNull("No proof for the X-Wing", hint)
            val step = hint!!.steps.first()
            assertEquals("X_WING", step.technique.name)
            assertEquals(sources, step.sourceCells)
            assertEquals(listOf(target), step.targetCells)
            assertEquals(listOf(5), step.digits)
            assertEquals(mapOf(target to listOf(5)), step.eliminations)
            assertEquals(0, Sudoku.solutionCount(grid.clues.toMutableList().apply { this[target] = 5 }))
            assertEquals(HumanSudoku.Grade.BEYOND_CURRENT_LOGIC, HumanSudoku.assess(grid).grade)
        }
    }

    @Test fun stalledUniqueBoardHasAnExplicitOneSquareReveal() {
        val puzzle = puzzle("100007090030020008009600500005300900010080002600004000300000010041000007007000300")
        val board = puzzle.clues.toMutableList()
        // The first solution is reachable in 300 nodes. Proving it unique takes longer.
        assertNull(Sudoku.uniqueSolutionWithin(board, maxNodes = 300))
        val hint = SudokuAssistance.nextCoachHint(puzzle, board)
        assertNotNull("The Coach has no help for this stalled position", hint)
        val reveal = hint!!.steps.single()
        assertEquals("SOLUTION_REVEAL", reveal.technique.name)
        assertTrue(reveal.sourceCells.isEmpty())
        assertTrue(reveal.eliminations.isEmpty())
        val target = reveal.targetCells.single()
        val digit = reveal.digits.single()
        assertEquals(0, board[target])
        assertEquals(1, Sudoku.solutionCount(board.toMutableList().apply { this[target] = digit }))
        assertEquals(puzzle.clues, board)
        assertNull(SudokuAssistance.nextHint(puzzle, board))
    }

    @Test fun ambiguousBoardNeverOffersAReveal() {
        val puzzle = Puzzle("ambiguous", List(81) { 0 })
        assertNull(SudokuAssistance.nextCoachHint(puzzle, puzzle.clues))
    }

    private fun puzzle(clues: String) = Puzzle("coach-reference", clues.map { it.digitToInt() })
}
