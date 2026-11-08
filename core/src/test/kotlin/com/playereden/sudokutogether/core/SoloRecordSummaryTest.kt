package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test

class SoloRecordSummaryTest {
    private fun record(
        id: String, duration: Long, tier: String? = null, assisted: Boolean = false,
        mode: String = SOLO, result: String = "SOLVED"
    ) = GameRecord(id, mode, result, "puzzle-$id", duration, 30, 2000, tier, assisted)

    @Test fun unassistedBestIsNotImprovedByFasterAssistedRuns() {
        val records = listOf(
            record("classic-easy", 600_000, "EASY"),
            record("assisted-fast", 40_000, "EASY", assisted = true),
            record("classic-hard", 750_000, "HARD"),
            record("assisted-hard", 90_000, "HARD", assisted = true)
        )
        val all = PersonalPlay.soloSummary(records)
        assertEquals(4, all.solved)
        assertEquals(2, all.unassisted)
        assertEquals(2, all.assisted)
        assertEquals(600_000L, all.bestUnassistedMs)
        assertEquals(40_000L, all.bestAssistedMs)
        val easy = PersonalPlay.soloSummary(records, "EASY")
        assertEquals(1, easy.unassisted)
        assertEquals(1, easy.assisted)
        assertEquals(600_000L, easy.bestUnassistedMs)
        assertEquals(40_000L, easy.bestAssistedMs)
        val medium = PersonalPlay.soloSummary(records, "MEDIUM")
        assertNull(medium.bestUnassistedMs)
        assertNull(medium.bestAssistedMs)
    }

    @Test fun oldRecordsRemainUnassistedAndUnratedRatherThanBeingDiscarded() {
        val old = record("pre-assists", 1_200_000L)
        val all = PersonalPlay.soloSummary(listOf(old))
        assertEquals(1, all.unassisted)
        assertEquals(0, all.assisted)
        assertEquals(1_200_000L, all.bestUnassistedMs)
        assertEquals(0, PersonalPlay.soloSummary(listOf(old), "EASY").solved)
    }

    @Test fun summaryExcludesIncompleteMultiplayerAndDeduplicatesHistory() {
        val solo = record("s1", 50_000L)
        val records = listOf(
            solo, solo.copy(elapsedMs = 5),
            record("versus", 1_000L, mode = VERSUS, result = "WIN"),
            record("unsolved", 1_000L, result = "LOSS")
        )
        val result = PersonalPlay.soloSummary(records)
        assertEquals(1, result.solved)
        assertEquals(50_000L, result.bestUnassistedMs)
    }
}
