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

    @Test fun rematchKeepsTheFinishedResultUntilTheSameRivalReturns() {
        val hostEvents = LinkedBlockingQueue<HostEvent>()
        val guestEvents = LinkedBlockingQueue<ClientEvent>()
        val game = GameSession.create(fixture(), mode = VERSUS)
        val nextPuzzle = fixture().copy(id = "next-puzzle")
        HostServer(game, listener = hostEvents::offer).use { host ->
            val port = host.start()
            val token = GuestClient(listener = guestEvents::offer).use { guest ->
                guest.connect("127.0.0.1", port, host.code, "Rival")
                val welcome = await(guestEvents) { it.token != null }
                val joined = welcome.snapshot!!
                assertFalse(host.nextRace(joined.epoch, nextPuzzle, afterFinish = true))
                guest.submit(joined.move(0, number = 2))
                val placed = await(guestEvents) { it.snapshot?.guestProgress == 78 }.snapshot!!
                guest.submit(placed.move(1, kind = Kind.NOTE, number = 2))
                await(guestEvents) { it.snapshot?.cells?.get(1)?.notes == 2 }
                for (i in joined.puzzle.clues.indices.filter { joined.puzzle.clues[it] == 0 }) {
                    assertTrue(host.submitLocal(game.snapshot().move(i, number = solution[i])).accepted)
                }
                val finished = await(guestEvents) { it.snapshot?.completed == true }.snapshot!!
                assertEquals(HOST, finished.winner)
                assertEquals(81, finished.hostProgress)
                assertEquals(78, finished.guestProgress)
                assertEquals(2, finished.cells[0].value)
                assertTrue(finished.guestCells.isEmpty())
                welcome.token!!
            }
            await(hostEvents) { it.snapshot.completed && !it.status.guestActive }
            val finished = game.snapshot()
            assertFalse(host.nextRace(finished.epoch, nextPuzzle, afterFinish = true))
            assertEquals(finished, game.snapshot())

            GuestClient(listener = guestEvents::offer).use { guest ->
                guest.connect("127.0.0.1", port, "", "Rival", token)
                val welcome = await(guestEvents) { it.token == token }
                val returned = welcome.snapshot!!
                assertEquals(finished.epoch, returned.epoch)
                assertEquals(HOST, returned.winner)
                assertFalse(welcome.status!!.guestActive)
                assertFalse(host.nextRace(finished.epoch, nextPuzzle, afterFinish = true))
                guest.setForeground(true)
                await(guestEvents) { it.status?.guestActive == true }
                assertFalse(host.nextRace(finished.epoch, nextPuzzle, afterFinish = false))
                assertFalse(host.nextRace("old-epoch", nextPuzzle, afterFinish = true))
                host.setForeground(false)
                assertFalse(host.nextRace(finished.epoch, nextPuzzle, afterFinish = true))
                host.setForeground(true)
                assertFalse(host.nextRace(finished.epoch, nextPuzzle, afterFinish = true) { false })
                assertTrue(host.nextRace(finished.epoch, nextPuzzle, afterFinish = true))
                val next = await(guestEvents) { event -> event.snapshot?.let { it.epoch != finished.epoch } == true }.snapshot!!
                assertEquals(finished.sessionId, next.sessionId)
                assertEquals(nextPuzzle, next.puzzle)
                assertEquals("Rival", next.guestName)
                assertEquals(77, next.hostProgress)
                assertEquals(77, next.guestProgress)
                assertEquals(nextPuzzle.clues.map(::Cell), next.cells)
                assertTrue(next.guestCells.isEmpty())
                assertNull(next.winner)
                assertFalse(next.completed)
                assertFalse(host.nextRace(finished.epoch, nextPuzzle, afterFinish = true))
                assertEquals(ResultCode.WRONG_GAME, host.submitLocal(finished.move(0)).code)
                assertTrue(host.nextRace(next.epoch, fixture(), afterFinish = false))
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
