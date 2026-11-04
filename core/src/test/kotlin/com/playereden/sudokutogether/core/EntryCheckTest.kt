package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test

class EntryCheckTest {
    private val clues = "530070000600195000098000060800060003400803001700020006060000280000419005000080079".map { it.digitToInt() }
    private val puzzle = Puzzle("check-fixture", clues, "EASY")
    private fun result(values: List<Int>): SudokuAssistance.EntryCheck {
        val result = SudokuAssistance.checkEntries(puzzle, values)
        assertNotNull("A valid original puzzle can be checked", result)
        return result!!
    }

    @Test fun aWrongEntryWithoutDuplicatesIsIdentifiedWithoutChangingTheBoard() {
        // R1C3 can locally accept1, but the original unique solution requires4.
        val values = clues.toMutableList().also { it[2] = 1 }
        assertTrue(Sudoku.conflicts(values).isEmpty())
        val before = values.toList()
        val checked = result(values)
        assertEquals(SudokuAssistance.CheckKind.INCORRECT_ENTRY, checked.kind)
        assertEquals(2, checked.cell)
        assertTrue(checked.usesSolution)
        assertEquals(before, values)
        assertEquals(1, values[2])
    }

    @Test fun duplicateFeedbackIdentifiesAnEditableSquareWithoutASolutionComparison() {
        val values = clues.toMutableList().also { it[2] = 5 }
        val checked = result(values)
        assertEquals(SudokuAssistance.CheckKind.CONFLICT, checked.kind)
        assertEquals(2, checked.cell)
        assertEquals(setOf(0, 2), checked.conflicts)
        assertFalse(checked.usesSolution)
        assertEquals(5, values[2])
    }

    @Test fun correctEntriesAreVerifiedWithoutFillingAnEmptySquare() {
        val values = clues.toMutableList().also { it[2] = 4 }
        val before = values.toList()
        val checked = result(values)
        assertEquals(SudokuAssistance.CheckKind.CORRECT_SO_FAR, checked.kind)
        assertEquals(-1, checked.cell)
        assertTrue(checked.usesSolution)
        assertEquals(before, values)
    }

    @Test fun anUntouchedPuzzleDoesNotProvideCountedCorrectnessInformation() {
        val checked = result(clues)
        assertEquals(SudokuAssistance.CheckKind.NO_ENTRIES, checked.kind)
        assertFalse(checked.usesSolution)
    }

    @Test fun malformedInputsDoNotProduceAComparison() {
        assertNull(SudokuAssistance.checkEntries(puzzle, emptyList()))
        assertNull(SudokuAssistance.checkEntries(Puzzle("short", listOf(0)), clues))
    }

    @Test fun changedGivensAreNotTreatedAsPlayerEntries() {
        assertNull(SudokuAssistance.checkEntries(puzzle, clues.toMutableList().also { it[0] = 0 }))
        assertNull(SudokuAssistance.checkEntries(puzzle, clues.toMutableList().also { it[0] = 4 }))
    }

    @Test fun ambiguousOriginalsCannotBeUsedToDeclareAPlayerEntryWrong() {
        val blank = Puzzle("ambiguous", List(81) { 0 })
        assertNull(SudokuAssistance.checkEntries(blank, List(81) { if (it == 2) 1 else 0 }))
    }

    @Test fun outOfRangeDigitsDoNotReachTheSolver() {
        for (digit in listOf(-1, 10)) {
            assertNull(SudokuAssistance.checkEntries(puzzle, clues.toMutableList().also { it[2] = digit }))
        }
    }
}
