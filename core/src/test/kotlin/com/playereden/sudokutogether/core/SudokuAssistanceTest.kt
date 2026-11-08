package com.playereden.sudokutogether.core

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class SudokuAssistanceTest {
    @Test fun readOnlyHintIsLegitimateDeductionFromVisibleNumbers() {
        val puzzle = Sudoku.generate(20261025L)
        val original = puzzle.clues.toList()
        val hint = SudokuAssistance.nextHint(puzzle, puzzle.clues)
        assertNotNull("36-clue puzzle should provide an introductory hint", hint)
        hint!!
        assertEquals(0, puzzle.clues[hint.cell])
        assertTrue(hint.digit in 1..9)
        assertTrue(hint.explanation.contains("Row ${hint.cell / 9 + 1}"))
        val afterHint = puzzle.clues.toMutableList().also { it[hint.cell] = hint.digit }
        assertEquals("Suggested digit must preserve the unique solution", 1, Sudoku.solutionCount(afterHint))
        assertEquals("A hint must never mutate the puzzle", original, puzzle.clues)
    }

    @Test fun invalidOrAlreadySolvedVisibleGridCannotLeakAnAnswer() {
        val puzzle = Sudoku.generate(729L)
        assertNull(SudokuAssistance.nextHint(puzzle, listOf(0)))
        val changedClue = puzzle.clues.toMutableList()
        val given = changedClue.indexOfFirst { it > 0 }
        changedClue[given] = 0
        assertNull(SudokuAssistance.nextHint(puzzle, changedClue))
        val empty = puzzle.clues.indexOfFirst { it == 0 }
        val wrong = puzzle.clues.toMutableList()
        val wrongDigit = (1..9).first { test ->
            val trial = puzzle.clues.toMutableList().also { it[empty] = test }
            Sudoku.solutionCount(trial) == 0
        }
        wrong[empty] = wrongDigit
        assertNull(SudokuAssistance.nextHint(puzzle, wrong))
    }

    @Test fun optionalPencilCleanupIsAtomicAndUndoRestoresEveryAffectedNote() {
        val puzzle = Sudoku.generate(81L)
        val first = puzzle.clues.indices.first { a ->
            puzzle.clues[a] == 0 && puzzle.clues.indices.any { b ->
                puzzle.clues[b] == 0 && SudokuAssistance.isPeer(a, b)
            }
        }
        val peer = puzzle.clues.indices.first { i ->
            puzzle.clues[i] == 0 && SudokuAssistance.isPeer(first, i)
        }
        val s0 = PersonalPlay.newSolo(puzzle, 1000L)
        val s1 = PersonalPlay.edit(s0, peer, Kind.NOTE, 3, elapsedMs = 100L)!!
        assertEquals(1 shl 2, s1.cells[peer].notes)
        val entered = PersonalPlay.edit(s1, first, Kind.VALUE, 3, elapsedMs = 200L, autoCleanNotes = true)!!
        assertEquals(0, entered.cells[peer].notes)
        assertTrue(entered.usedNoteAssist)
        val restored = PersonalPlay.edit(entered, first, Kind.UNDO, elapsedMs = 300L)!!
        assertEquals(0, restored.cells[first].value)
        assertEquals(1 shl 2, restored.cells[peer].notes)
        assertEquals(300L, restored.elapsedMs)
        val withoutAssist = PersonalPlay.edit(s1, first, Kind.VALUE, 3, elapsedMs = 200L)!!
        assertEquals(1 shl 2, withoutAssist.cells[peer].notes)
    }

    @Test fun hintedSoloRecordIsMarkedAssistedAndOldSavesStillDecode() {
        var p = PersonalPlay.recordHint(PersonalPlay.newSolo(fixture(), 1000L))
        assertEquals(1, p.hintsUsed)
        for ((cell, number) in listOf(0 to 1, 1 to 2, 9 to 4, 10 to 5)) {
            p = PersonalPlay.edit(p, cell, Kind.VALUE, number, elapsedMs = (p.moves + 1) * 1000L)!!
        }
        assertTrue(p.completed)
        val record = PersonalPlay.addCompletedSolo(LocalPlayerData(), p).records.single()
        assertTrue(record.assisted)
        assertEquals(1, record.hintsUsed)

        val json = Json { encodeDefaults = false }
        val legacy = json.encodeToString(PersonalPlay.newSolo(fixture(), 1000L))
        val restored = json.decodeFromString<SoloProgress>(legacy)
        assertEquals(0, restored.hintsUsed)
        assertFalse(restored.usedNoteAssist)
        val oldRecord = json.decodeFromString<GameRecord>(
            """{"id":"old","mode":"solo","result":"SOLVED","puzzleId":"id","elapsedMs":1000,
                "moves":4,"finishedAtMs":2000}"""
        )
        assertFalse(oldRecord.assisted)
        assertEquals(0, oldRecord.hintsUsed)
    }
}
