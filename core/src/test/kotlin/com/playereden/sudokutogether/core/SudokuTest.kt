package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test

class SudokuTest {
    @Test fun generatedPuzzlesHaveOneSolutionAndRepeatBySeed() {
        for (seed in 1L..12L) {
            val puzzle = Sudoku.generate(seed)
            assertEquals(81, puzzle.clues.size)
            assertTrue(puzzle.clues.count { it != 0 } >= 30)
            assertEquals(1, Sudoku.solutionCount(puzzle.clues))
            assertEquals(puzzle, Sudoku.generate(seed))
        }
        assertNotEquals(Sudoku.generate(1).clues, Sudoku.generate(2).clues)
    }
    @Test fun duplicateStartingCluesAreNotSolvable() {
        val invalid = fixture().clues.toMutableList().also { it[0] = 3 }
        assertEquals(0, Sudoku.solutionCount(invalid))
        assertEquals(0, Sudoku.solutionCount(listOf(1, 2)))
    }
    @Test fun onlyAFullLegalBoardPreservingCluesCompletes() {
        assertTrue(Sudoku.complete(fixture(), solution))
        assertFalse(Sudoku.complete(fixture(), fixture().clues))
        assertFalse(Sudoku.complete(fixture(), solution.toMutableList().also { it[0] = 2 }))
        val changedClue = solution.map { if (it == 3) 4 else if (it == 4) 3 else it }
        assertFalse(Sudoku.complete(fixture(), changedClue))
    }
    @Test fun conflictsIdentifyBothEndsOfADuplicate() {
        val values = List(81) { if (it in listOf(0, 4, 36)) 7 else 0 }
        assertEquals(setOf(0, 4, 36), Sudoku.conflicts(values))
    }
}
