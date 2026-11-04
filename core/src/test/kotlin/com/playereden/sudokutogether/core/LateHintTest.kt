package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test

/** Characterizations of the existing valid-state behavior, not new hint claims. */
class LateHintTest {
    private val clues = "530070000600195000098000060800060003400803001700020006060000280000419005000080079".map { it.digitToInt() }
    private val solved = "534678912672195348198342567859761423426853791713924856961537284287419635345286179".map { it.digitToInt() }
    private val puzzle = Puzzle("late-hint", clues)
    @Test fun oneCorrectEmptySquareStillHasALogicalHint() {
        val values = solved.toMutableList().also { it[2] = 0 }
        val hint = SudokuAssistance.nextHint(puzzle, values)
        assertNotNull(hint);assertEquals(2, hint!!.cell);assertEquals(4, hint.digit)
        assertEquals(0, values[2])
    }
    @Test fun twoCorrectEmptySquaresStillHaveALogicalHint() {
        val values = solved.toMutableList().also { it[2] = 0;it[3] = 0 }
        val hint = SudokuAssistance.nextHint(puzzle, values)
        assertNotNull(hint);assertEquals(2, hint!!.cell);assertEquals(4, hint.digit)
        assertEquals(0, values[2]);assertEquals(0, values[3])
    }
}
