package com.playereden.sudokutogether

import com.playereden.sudokutogether.core.Appearance
import com.playereden.sudokutogether.core.GameSession
import com.playereden.sudokutogether.core.GUEST
import com.playereden.sudokutogether.core.HOST
import com.playereden.sudokutogether.core.Kind
import com.playereden.sudokutogether.core.Operation
import com.playereden.sudokutogether.core.Puzzle
import com.playereden.sudokutogether.core.RoomStatus
import com.playereden.sudokutogether.core.VERSUS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VersusPresenceStateTest {
    private val clues = "530070000600195000098000060800060003400803001700020006060000280000419005000080079"
        .map { it.digitToInt() }
    private val hostLook = Appearance(portrait = "woodland_owl")
    private val guestLook = Appearance(portrait = "woodland_fox")
    private val active = RoomStatus(guestActive = true, reserved = true)

    private fun host() = GameSession.create(
        Puzzle("presence", clues, "EASY"), hostName = "Joseph", mode = VERSUS, hostAppearance = hostLook
    )

    @Test fun bothSeatsSeeTheSamePeopleAndProgressInLocalOrder() {
        val room = host()
        room.admitGuest("Taylor", appearance = guestLook)
        room.setPresence()
        val open = clues.indices.filter { clues[it] == 0 }
        move(room, HOST, open[0], Kind.VALUE, 5)
        move(room, GUEST, open[1], Kind.VALUE, 1)
        move(room, GUEST, open[2], Kind.VALUE, 2)
        move(room, GUEST, open[3], Kind.NOTE, 3)

        val hostView = versusPresenceState(room.snapshot(), HOST, active, connected = true)
        val guestView = versusPresenceState(room.snapshotForGuest(), GUEST, active, connected = true)

        assertEquals(VersusPhase.LIVE, hostView.phase)
        assertEquals(VersusPhase.LIVE, guestView.phase)
        assertEquals(open.size, hostView.target)
        assertEquals(hostView.target, guestView.target)
        assertEquals(VersusPlayer("Joseph", hostLook, 1), hostView.self)
        assertEquals(VersusPlayer("Taylor", guestLook, 2), hostView.rival)
        assertEquals(hostView.self, guestView.rival)
        assertEquals(hostView.rival, guestView.self)

        move(room, GUEST, open[1], Kind.CLEAR)
        assertEquals(1, versusPresenceState(room.snapshot(), HOST, active, connected = true).rival?.filled)
    }

    @Test fun waitingHasAnOpenSeatAndNoRivalScore() {
        val view = versusPresenceState(host().snapshot(), HOST, RoomStatus(), connected = false)

        assertEquals(VersusPhase.WAITING, view.phase)
        assertEquals(0, view.self.filled)
        assertNull(view.rival)
    }

    @Test fun pauseAndReconnectRetainTheKnownRivalAndLastCount() {
        val room = host()
        room.admitGuest("Taylor", appearance = guestLook)
        room.setPresence()
        val openCell = clues.indexOf(0)
        move(room, HOST, openCell, Kind.VALUE, 4)
        move(room, GUEST, openCell, Kind.VALUE, 4)
        val hostView = versusPresenceState(
            room.snapshot(), HOST, active.copy(guestActive = false), connected = false
        )
        val guestView = versusPresenceState(
            room.snapshotForGuest(), GUEST, active.copy(hostActive = false), connected = true
        )
        val disconnected = versusPresenceState(room.snapshotForGuest(), GUEST, active, connected = false)
        val rejoined = versusPresenceState(room.snapshotForGuest(), GUEST, active, connected = true)

        assertEquals(VersusPhase.PAUSED, hostView.phase)
        assertEquals(VersusPlayer("Taylor", guestLook, 1), hostView.rival)
        assertEquals(VersusPhase.PAUSED, guestView.phase)
        assertEquals(VersusPhase.DISCONNECTED, disconnected.phase)
        assertEquals(VersusPlayer("Joseph", hostLook, 1), disconnected.rival)
        assertEquals(VersusPhase.LIVE, rejoined.phase)
        assertEquals(disconnected.rival, rejoined.rival)
    }

    @Test fun aCompletedRaceStaysFinishedAfterDisconnect() {
        val room = host()
        room.admitGuest("Taylor", appearance = guestLook)
        room.setPresence()
        val solution = "534678912672195348198342567859761423426853791713924856961537284287419635345286179"
        for (cell in clues.indices.filter { clues[it] == 0 }) {
            move(room, HOST, cell, Kind.VALUE, solution[cell].digitToInt())
        }
        val finished = room.snapshotForGuest()
        val view = versusPresenceState(finished, GUEST, RoomStatus(reserved = true), connected = false)

        assertEquals(HOST, finished.winner)
        assertEquals(VersusPhase.FINISHED, view.phase)
        assertEquals(view.target, view.rival?.filled)
        assertEquals(0, view.self.filled)
    }

    private fun move(room: GameSession, role: String, cell: Int, kind: Kind, number: Int = 0) {
        val snapshot = if (role == HOST) room.snapshot() else room.snapshotForGuest()
        val operation = Operation(
            snapshot.sessionId, snapshot.epoch, cell, snapshot.cells[cell].revision, kind,
            number = number, enabled = true
        )
        assertTrue(room.submit(role, operation).accepted)
    }
}
