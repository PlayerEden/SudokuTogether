package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class VersusTest {
    @Test fun raceStartsOnJoinAndPlayersHaveIndependentGridsAndUndo() {
        val game = GameSession.create(fixture(), mode = VERSUS)
        assertFalse(game.editable())
        game.admitGuest("Rival")
        game.setPresence()
        assertTrue(game.editable())
        assertNotNull(game.snapshot().raceStartedAtMs)
        val hostMove = game.snapshot().move(0, number = 1)
        assertTrue(game.submit(HOST, hostMove).accepted)
        assertEquals(1, game.snapshot().cells[0].value)
        assertEquals(0, game.snapshot().guestCells[0].value)
        val rival = game.snapshotForGuest()
        assertTrue(rival.guestCells.isEmpty())
        assertEquals(0, rival.cells[0].value)
        assertEquals(1, rival.hostProgress - 77)
        val guestMove = rival.move(0, number = 1)
        assertTrue(game.submit(GUEST, guestMove).accepted)
        assertEquals(1, game.snapshot().guestCells[0].value)
        assertEquals(1, game.snapshot().cells[0].value)
        assertEquals(0, game.snapshotForGuest().undoTargets[HOST] ?: 0)
        assertTrue(game.submit(GUEST, game.snapshotForGuest().move(0, Kind.UNDO)).accepted)
        assertEquals(0, game.snapshot().guestCells[0].value)
        assertEquals(1, game.snapshot().cells[0].value)
    }

    @Test fun firstValidSolveWinsAndOpponentCannotKeepEditing() {
        val game = GameSession.create(fixture(), mode = VERSUS)
        game.admitGuest("Rival")
        game.setPresence()
        for ((i, digit) in listOf(0 to 1, 1 to 2, 9 to 4, 10 to 5)) {
            assertTrue(game.submit(HOST, game.snapshot().move(i, number = digit)).accepted)
        }
        assertEquals(HOST, game.snapshot().winner)
        assertTrue(game.snapshot().completed)
        assertFalse(Sudoku.complete(game.snapshotForGuest().puzzle, game.snapshotForGuest().cells.map { it.value }))
        assertEquals(ResultCode.COMPLETE, game.submit(GUEST, game.snapshotForGuest().move(0)).code)
        assertEquals(HOST, GameSession(game.saved()).snapshot().winner)
    }

    @Test fun socketUpdatesNeverRevealOpponentsCellsEvenInsideOutcomes() {
        val events = LinkedBlockingQueue<ClientEvent>()
        HostServer(GameSession.create(fixture(), mode = VERSUS)).use { host ->
            val port = host.start()
            GuestClient(listener = events::offer).use { guest ->
                guest.connect("127.0.0.1", port, host.code, "Rival")
                val joined = await(events) { it.token != null }.snapshot!!
                assertEquals(VERSUS, joined.mode)
                assertTrue(joined.guestCells.isEmpty())
                assertTrue(host.submitLocal(host.session.snapshot().move(0, number = 1)).accepted)
                val update = await(events) { it.snapshot?.hostProgress == 78 }
                assertEquals(0, update.snapshot!!.cells[0].value)
                assertTrue(update.snapshot.guestCells.isEmpty())
                assertEquals(0, update.outcome!!.snapshot.cells[0].value)
                assertTrue(update.outcome.snapshot.guestCells.isEmpty())
            }
        }
    }

    @Test fun tamperedRaceProgressOrOwnershipIsRejectedOnSaveAndWire() {
        val game = GameSession.create(fixture(), mode = VERSUS)
        game.admitGuest("Rival")
        game.setPresence()
        assertTrue(game.submit(HOST, game.snapshot().move(0, number = 1)).accepted)
        val master = game.saved()
        validateSnapshot(master.snapshot)
        assertThrows(IllegalArgumentException::class.java) {
            validateSnapshot(master.snapshot.copy(hostProgress = 81))
        }
        val guestBoard = master.snapshot.guestCells.toMutableList()
        guestBoard[0] = Cell(value = 3, owner = HOST, revision = master.snapshot.revision)
        assertThrows(IllegalArgumentException::class.java) {
            validateSnapshot(master.snapshot.copy(guestCells = guestBoard))
        }
        val filtered = game.snapshotForGuest()
        validateSnapshot(filtered)
        assertThrows(IllegalArgumentException::class.java) {
            validateSnapshot(filtered.copy(winner = GUEST, completed = true))
        }
        assertThrows(IllegalArgumentException::class.java) {
            validateSnapshot(filtered.copy(guestProgress = 81))
        }
    }

    private fun <T> await(queue: LinkedBlockingQueue<T>, predicate: (T) -> Boolean): T {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8)
        while (System.nanoTime() < deadline) {
            val item = queue.poll(100, TimeUnit.MILLISECONDS)
            if (item != null && predicate(item)) return item
        }
        throw AssertionError("Expected network event did not arrive")
    }
}
