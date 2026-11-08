package com.playereden.sudokutogether.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SharedCoopBoardTest {
    @Test fun ranksEquippedBoardsWithAClassicFallback() {
        assertEquals("classic", Cosmetics.sharedCoopBoard("classic", "classic"))
        assertEquals("linen", Cosmetics.sharedCoopBoard("linen", "linen"))
        assertEquals("midnight", Cosmetics.sharedCoopBoard("classic", "midnight"))
        assertEquals("midnight", Cosmetics.sharedCoopBoard("midnight", "classic"))
        assertEquals("circuit", Cosmetics.sharedCoopBoard("midnight", "circuit"))
        assertEquals("circuit", Cosmetics.sharedCoopBoard("circuit", "midnight"))
        assertEquals("linen", Cosmetics.sharedCoopBoard("linen", "future"))
        assertEquals("classic", Cosmetics.sharedCoopBoard("future", null))
    }

    @Test fun bothViewsKeepTheSharedBoardWhileTheGuestIsAway() {
        val room = GameSession.create(fixture())
        val guestStyle = Appearance(board = "midnight", ink = "cobalt", font = "serif")
        val identity = room.admitGuest("Rival", appearance = guestStyle)
        room.setPresence(guest = false)
        val resumed = GameSession(room.saved())
        resumed.admitGuest("Rival", identity.token, guestStyle)
        for (snapshot in listOf(room.snapshot(), resumed.snapshotForGuest())) {
            assertEquals("midnight", Cosmetics.sharedCoopBoard(snapshot.hostAppearance.board, snapshot.guestAppearance.board))
            assertEquals(Appearance(), snapshot.hostAppearance)
            assertEquals(guestStyle, snapshot.guestAppearance)
        }
        assertFalse(Cosmetics.allowed("midnight", emptyList()))
        resumed.replaceGuest()
        assertEquals("classic", Cosmetics.sharedCoopBoard(resumed.snapshot().hostAppearance.board, null))
    }
}
