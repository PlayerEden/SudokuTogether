package com.playereden.sudokutogether.core

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class WoodlandCollectionTest {
    private fun record(id: String, mode: String = SOLO, result: String = "SOLVED", assisted: Boolean = false) =
        GameRecord(id, mode, result, "puzzle", 1000, 1, 1000, assisted = assisted)
    @Test fun aDifferentCosmeticGroupCannotBecomeARestoredPortrait() {
        val records = listOf(record("earned"))
        for (id in listOf("forest", "tile", "leaf", "not-an-asset-path")) {
            val restored = Cosmetics.accessible(Appearance(ink = "cobalt", emblem = "leaf", portrait = id), records)
            assertEquals("Unknown portrait $id must use the existing emblem", "", restored.portrait)
            assertEquals("leaf", restored.emblem)
            assertEquals("cobalt", restored.ink)
            assertTrue("Restoring an unknown portrait must not block joining", Cosmetics.valid(restored))
        }
    }
    @Test fun legacyIdentityKeepsSixEmblemsAndNoPortrait() {
        val old = Json.decodeFromString<Appearance>("""{"ink":"cobalt","board":"linen","font":"serif","emblem":"leaf"}""")
        assertEquals("", old.portrait);assertEquals("leaf", old.emblem);assertTrue(Cosmetics.valid(old))
        assertEquals(listOf("tile", "leaf", "duo", "flash", "star", "laurel"), Cosmetics.emblems.map { it.id })
    }
    @Test fun twelveAnimalsHaveTwoStarterChoicesAndVisibleMilestones() {
        val woodland = Cosmetics.portraits.filter { it.id.startsWith("woodland_") }
        assertEquals(12, woodland.size)
        assertEquals(12, woodland.map { it.id }.toSet().size)
        assertEquals(setOf("woodland_owl", "woodland_fox"), woodland.filter { Cosmetics.allowed(it.id, emptyList()) }.map { it.id }.toSet())
        assertFalse(Cosmetics.allowed("woodland_rabbit", emptyList()))
        assertTrue(Cosmetics.allowed("woodland_rabbit", listOf(record("first", assisted = true))))
    }
    @Test fun assistedDailyAndTeamSolvesCountWithoutDuplicateRewards() {
        val records = listOf(record("a", assisted = true), record("a", assisted = true), record("b", DAILY), record("c", COOP), record("d", VERSUS, "WIN"), record("e", VERSUS, "LOSS"))
        assertEquals(4, Cosmetics.completedPuzzles(records))
        assertTrue(Cosmetics.allowed("woodland_hedgehog", records));assertFalse(Cosmetics.allowed("woodland_robin", records))
        assertTrue(Cosmetics.allowed("leaf", records));assertFalse(Cosmetics.allowed("laurel", records))
    }
    @Test fun unknownOrUnearnedPortraitsFallBackAndPreserveExistingStyle() {
        assertFalse(Cosmetics.valid(Appearance(portrait = "not-an-asset-path")))
        assertEquals("", Cosmetics.accessible(Appearance(portrait = "woodland_cat"), emptyList()).portrait)
        val look = Appearance(ink = "cobalt", emblem = "leaf", portrait = "woodland_owl")
        assertEquals(look, Cosmetics.accessible(look, listOf(record("a"))))
        assertTrue(Cosmetics.allowed("woodland_cat", (1..15).map { record(it.toString(), DAILY, assisted = true) }))
        assertTrue(Cosmetics.valid(look))
    }
}
