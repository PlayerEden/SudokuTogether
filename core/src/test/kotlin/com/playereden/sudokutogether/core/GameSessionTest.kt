package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test

class GameSessionTest {
    @Test fun consecutiveOwnUndosOnOneSquareRestoreEveryEarlierState() {
        val game = session()
        game.submit(HOST, game.snapshot().move(0, Kind.NOTE, 2))
        game.submit(HOST, game.snapshot().move(0, number = 1))
        assertTrue(game.submit(HOST, game.snapshot().move(0, Kind.UNDO)).accepted)
        assertEquals(2, game.snapshot().cells[0].notes)
        assertTrue(game.submit(HOST, game.snapshot().move(0, Kind.UNDO)).accepted)
        assertEquals(Cell(revision = 4), game.snapshot().cells[0])
    }
    @Test fun undoLineageSurvivesSavingAndStillRejectsPartnerIntervention() {
        val game = session()
        game.submit(HOST, game.snapshot().move(0, Kind.NOTE, 2))
        game.submit(HOST, game.snapshot().move(0, number = 1))
        game.submit(HOST, game.snapshot().move(0, Kind.UNDO))
        val restored = GameSession(game.saved()).also { it.setPresence() }
        assertTrue(restored.submit(HOST, restored.snapshot().move(0, Kind.UNDO)).accepted)
        restored.submit(HOST, restored.snapshot().move(0, Kind.NOTE, 2))
        restored.submit(GUEST, restored.snapshot().move(0, Kind.NOTE, 3))
        assertFalse(restored.submit(HOST, restored.snapshot().move(0, Kind.UNDO)).accepted)
        assertEquals(6, restored.snapshot().cells[0].notes)
    }
    @Test fun undoingOwnReplacementRestoresThePartnersEarlierUndoEligibility() {
        val game = session()
        game.submit(GUEST, game.snapshot().move(0, Kind.NOTE, 3))
        game.submit(HOST, game.snapshot().move(0, number = 1))
        game.submit(HOST, game.snapshot().move(0, Kind.UNDO))
        assertTrue(game.submit(GUEST, game.snapshot().move(0, Kind.UNDO)).accepted)
        assertEquals(0, game.snapshot().cells[0].notes)
    }
    @Test fun startingCluesCannotBeEditedOrErased() {
        val game = session()
        assertEquals(ResultCode.GIVEN, game.submit(HOST, game.snapshot().move(2, number = 8)).code)
        assertEquals(ResultCode.GIVEN, game.submit(GUEST, game.snapshot().move(2, Kind.CLEAR)).code)
        assertEquals(3, game.snapshot().cells[2].value)
    }
    @Test fun sharedNotesAreExplicitAndEnteringNumberClearsThem() {
        val game = session()
        assertTrue(game.submit(HOST, game.snapshot().move(0, Kind.NOTE, 2)).accepted)
        assertEquals(2, game.snapshot().cells[0].notes)
        assertTrue(game.submit(GUEST, game.snapshot().move(0, Kind.NOTE, 2, false)).accepted)
        assertEquals(0, game.snapshot().cells[0].notes)
        game.submit(HOST, game.snapshot().move(0, Kind.NOTE, 4))
        game.submit(GUEST, game.snapshot().move(0, number = 1))
        assertEquals(0, game.snapshot().cells[0].notes)
        assertEquals(1, game.snapshot().cells[0].value)
        assertEquals(ResultCode.INVALID, game.submit(HOST, game.snapshot().move(0, Kind.NOTE, 9)).code)
    }
    @Test fun differentCellsAcceptConcurrentInputButSameCellRejectsStaleNotes() {
        val game = session(); val start = game.snapshot()
        assertTrue(game.submit(HOST, start.move(0, number = 1)).accepted)
        assertTrue(game.submit(GUEST, start.move(1, number = 2)).accepted)
        assertEquals(ResultCode.STALE, game.submit(GUEST, start.move(0, Kind.NOTE, 3)).code)
        assertEquals(1, game.snapshot().cells[0].value)
        assertEquals(2L, game.snapshot().revision)
    }
    @Test fun undoRestoresOwnCellAndNeverRemovesPartnersLaterWork() {
        val game = session()
        game.submit(HOST, game.snapshot().move(0, Kind.NOTE, 2))
        game.submit(HOST, game.snapshot().move(0, number = 1))
        assertTrue(game.submit(HOST, game.snapshot().move(0, Kind.UNDO)).accepted)
        assertEquals(0, game.snapshot().cells[0].value)
        assertEquals(2, game.snapshot().cells[0].notes)
        game.submit(GUEST, game.snapshot().move(0, number = 9))
        assertFalse(game.submit(HOST, game.snapshot().move(0, Kind.UNDO)).accepted)
        assertEquals(9, game.snapshot().cells[0].value)
    }
    @Test fun duplicateIdentifiersAreIdempotentAndCannotCarryChangedPayload() {
        val game = session(); val op = game.snapshot().move(0)
        assertTrue(game.submit(HOST, op).accepted)
        assertEquals(ResultCode.DUPLICATE, game.submit(HOST, op).code)
        assertEquals(1L, game.snapshot().revision)
        assertFalse(game.submit(HOST, op.copy(number = 9)).accepted)
    }
    @Test fun rejectsOldGameInvalidDigitsAndUnrecognizedPlayers() {
        val game = session(); val op = game.snapshot().move(0)
        assertEquals(ResultCode.WRONG_GAME, game.submit(HOST, op.copy(epoch = "old")).code)
        assertEquals(ResultCode.INVALID, game.submit(HOST, op.copy(number = 10)).code)
        assertFalse(game.submit("intruder", op).accepted)
        game.newPuzzle(fixture())
        assertEquals(ResultCode.WRONG_GAME, game.submit(HOST, op).code)
    }
    @Test fun presencePausesBothPlayersAndGuestReplacementInvalidatesToken() {
        val game = session(); val token = game.saved().guest!!.token
        game.setPresence(host = true, guest = false)
        assertEquals(ResultCode.PAUSED, game.submit(HOST, game.snapshot().move(0)).code)
        game.setPresence(host = false, guest = true)
        assertEquals(ResultCode.PAUSED, game.submit(GUEST, game.snapshot().move(0)).code)
        game.replaceGuest()
        assertThrows(IllegalArgumentException::class.java) { game.admitGuest("Partner", token) }
    }
    @Test fun acknowledgedMovesAreSavedAndSaveFailuresKeepPriorState() {
        var durable: SavedSession? = null
        var fail = false
        val game = GameSession.create(fixture(), persist = { if (fail) error("disk full") else durable = it })
        val before = game.snapshot()
        assertTrue(game.submit(HOST, before.move(0)).accepted)
        assertEquals(1, durable!!.snapshot.cells[0].value)
        fail = true
        assertEquals(ResultCode.SAVE_FAILED, game.submit(HOST, game.snapshot().move(1, number = 2)).code)
        assertEquals(0, game.snapshot().cells[1].value)
        assertEquals(1L, game.snapshot().revision)
    }
    @Test fun fillingWrongBoardDoesNotCompleteAndCorrectFinalMoveDoes() {
        val game = GameSession.create(Puzzle("last", solution.mapIndexed { i, n -> if (i == 0) 0 else n }))
        game.submit(HOST, game.snapshot().move(0, number = 9))
        assertFalse(game.snapshot().completed)
        game.submit(HOST, game.snapshot().move(0, number = 1))
        assertTrue(game.snapshot().completed)
        assertEquals(ResultCode.COMPLETE, game.submit(HOST, game.snapshot().move(0, Kind.CLEAR)).code)
    }
}
