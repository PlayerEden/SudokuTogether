package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test

class OceanCollectionTest {
    private fun record(id: String, mode: String = SOLO, result: String = "SOLVED") =
        GameRecord(id, mode, result, "puzzle", 1000, 1, 1000, assisted = mode == SOLO)

    @Test fun octopusIsTheOnlyOceanStarter() {
        val ocean = Cosmetics.portraits.filter { it.id.startsWith("ocean_") }
        assertEquals(listOf("Octopus", "Harbor Seal", "Hermit Crab", "Manta Ray", "Jellyfish", "Narwhal"), ocean.map { it.name })
        assertEquals(listOf("ocean_octopus"), ocean.filter { Cosmetics.allowed(it.id, emptyList()) }.map { it.id })
        assertEquals("Starter", ocean.first().requirement)
        val look = Appearance(portrait = "ocean_octopus")
        assertTrue(Cosmetics.valid(look))
        assertEquals(look, Cosmetics.accessible(look, emptyList()))
    }

    @Test fun eachOceanMilestoneUsesExistingQualifyingFinishes() {
        val finishes = List(75) { index ->
            when (index % 4) {
                0 -> record("$index")
                1 -> record("$index", DAILY)
                2 -> record("$index", COOP)
                else -> record("$index", VERSUS, "WIN")
            }
        }
        val milestones = mapOf(
            "ocean_seal" to 18,
            "ocean_hermit_crab" to 25,
            "ocean_manta_ray" to 35,
            "ocean_jellyfish" to 50,
            "ocean_narwhal" to 75
        )
        for ((id, target) in milestones) {
            val excluded = listOf(finishes.first(), record("loss", VERSUS, "LOSS"), record("abandoned", SOLO, "ABANDONED"))
            val before = finishes.take(target - 1) + excluded
            val reached = finishes.take(target) + excluded
            val look = Appearance(portrait = id)
            assertEquals(CosmeticProgress(target - 1, target), Cosmetics.progress(id, before))
            assertFalse(Cosmetics.allowed(id, before))
            assertEquals("", Cosmetics.accessible(look, before).portrait)
            assertEquals(CosmeticProgress(target, target), Cosmetics.progress(id, reached))
            assertTrue(Cosmetics.allowed(id, reached))
            assertEquals(look, Cosmetics.accessible(look, reached))
        }
    }
}
