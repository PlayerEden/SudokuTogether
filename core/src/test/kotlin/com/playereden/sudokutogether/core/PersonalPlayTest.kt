package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test

class PersonalPlayTest {
    @Test fun soloNumbersNotesAndUndoDoNotEditStartingClues() {
        val game = PersonalPlay.newSolo(fixture(), 1000)
        assertNull(PersonalPlay.edit(game, 2, Kind.VALUE, 9, elapsedMs = 100))
        val notes = PersonalPlay.edit(game, 0, Kind.NOTE, 3, elapsedMs = 500)!!
        assertEquals(1 shl 2, notes.cells[0].notes)
        val reverted = PersonalPlay.edit(notes, 0, Kind.UNDO, elapsedMs = 600)!!
        assertEquals(0, reverted.cells[0].notes)
        assertTrue(reverted.history.isEmpty())
        assertEquals(600L, reverted.elapsedMs)
    }

    @Test fun soloCompletionIsSavedOnceAndBestTimeIsComputedFromResults() {
        var game = PersonalPlay.newSolo(fixture(), 1000)
        for ((cell, digit) in listOf(0 to 1, 1 to 2, 9 to 4, 10 to 5))
            game = PersonalPlay.edit(game, cell, Kind.VALUE, digit, elapsedMs = ((game.moves + 1) * 2000).toLong())!!
        assertTrue(game.completed)
        assertEquals(8000L, game.elapsedMs)
        assertEquals(4, game.moves)
        val saved = PersonalPlay.addCompletedSolo(LocalPlayerData(), game)
        val repeated = PersonalPlay.addCompletedSolo(saved, game)
        assertEquals(1, repeated.records.size)
        assertEquals(1, PersonalPlay.stats(repeated).soloWins)
        assertEquals(8000L, PersonalPlay.stats(repeated).soloBestMs)
        assertNull(PersonalPlay.edit(game, 0, Kind.CLEAR, elapsedMs = 9000))
    }
}
