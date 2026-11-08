package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class AchievementsTest {
    private fun rec(id: String, mode: String, result: String, elapsed: Long = 0L): GameRecord =
        GameRecord(id, mode, result, "original", elapsed, 17, 1000L)

    @Test fun starterProfileHasNoEarnedAchievementsAndOnlyStarterEmblem() {
        assertEquals(0, Achievements.earnedCount(emptyList()))
        assertTrue(Cosmetics.allowed("tile", emptyList()))
        assertFalse(Cosmetics.allowed("leaf", emptyList()))
        assertFalse(Cosmetics.allowed("duo", emptyList()))
        assertFalse(Cosmetics.allowed("flash", emptyList()))
        assertFalse(Cosmetics.allowed("not-a-real-emblem", emptyList()))
    }

    @Test fun achievementsAndUnlocksIgnoreDuplicateMatchIds() {
        val solved = rec("s1", SOLO, "SOLVED", elapsed = 14 * 60_000L)
        val coop = rec("c1", COOP, "SOLVED")
        val win = rec("r1", VERSUS, "WIN")
        val source = listOf(solved, solved, coop, coop, win, win)
        val progress = Achievements.progress(source).associateBy { it.id }
        assertEquals(1, progress.getValue("first-grid").current)
        assertEquals(1, progress.getValue("two-minds").current)
        assertEquals(1, progress.getValue("challenger").current)
        assertEquals(1, progress.getValue("rivalry").current)
        assertEquals(0, progress.getValue("quick-thinker").current)
        assertTrue(Cosmetics.allowed("leaf", source))
        assertTrue(Cosmetics.allowed("duo", source))
        assertTrue(Cosmetics.allowed("flash", source))
        assertFalse(Cosmetics.allowed("laurel", source))
    }

    @Test fun fastSolveUsesActiveElapsedTimeAndAvoidsZeroDuration() {
        val short = rec("fast", SOLO, "SOLVED", 9 * 60_000L)
        assertTrue(Achievements.progress(listOf(short)).first { it.id == "quick-thinker" }.unlocked)
        val invalid = rec("none", SOLO, "SOLVED", 0)
        assertFalse(Achievements.progress(listOf(invalid)).first { it.id == "quick-thinker" }.unlocked)
    }

    @Test fun chosenEmblemSurvivesNetworkRoundTripWithoutLeakingVersusGrid() {
        val hostAppearance = Appearance(ink = "violet", board = "midnight", font = "mono", emblem = "laurel")
        val guestAppearance = Appearance(emblem = "duo")
        val session = GameSession.create(fixture(), mode = VERSUS, hostAppearance = hostAppearance)
        session.admitGuest("Friendly rival", appearance = guestAppearance)
        session.setPresence()
        assertTrue(session.submit(HOST, session.snapshot().move(0, number = 1)).accepted)
        val visible = session.snapshotForGuest()
        assertEquals(hostAppearance, visible.hostAppearance)
        assertEquals(guestAppearance, visible.guestAppearance)
        assertTrue(visible.guestCells.isEmpty())
        assertEquals(0, visible.cells[0].value)
        val encoded = ByteArrayOutputStream()
        Protocol.write(encoded, Message("state", snapshot = visible))
        val decoded = Protocol.read(ByteArrayInputStream(encoded.toByteArray()))
        assertEquals(WIRE_PROTOCOL_VERSION, decoded.version)
        assertEquals("laurel", decoded.snapshot!!.hostAppearance.emblem)
        assertTrue(decoded.snapshot!!.guestCells.isEmpty())
    }

    @Test fun selectingLockedCosmeticsFallsBackToDefault() {
        val appearance = Appearance("rose", "circuit", "mono", "laurel")
        val available = Cosmetics.accessible(appearance, emptyList())
        assertEquals(Appearance(), available)
        assertTrue(Cosmetics.valid(appearance))
        assertFalse(Cosmetics.valid(appearance.copy(emblem = "untrusted")))
    }
}
