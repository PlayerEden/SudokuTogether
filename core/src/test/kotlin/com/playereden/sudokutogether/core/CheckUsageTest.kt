package com.playereden.sudokutogether.core

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class CheckUsageTest {
    private fun finish(start: SoloProgress): SoloProgress {
        var current = start
        for ((cell, digit) in listOf(0 to 1, 1 to 2, 9 to 4, 10 to 5)) {
            current = PersonalPlay.edit(current, cell, Kind.VALUE, digit,
                elapsedMs = (current.moves + 1) * 1000L, nowMs = 9000L)!!
        }
        assertTrue(current.completed)
        return current
    }
    @Test fun checkedSoloFinishesRetainASeparateCountAndAssistedClassification() {
        val started = PersonalPlay.newSolo(fixture(), 1000L)
        val finished = finish(PersonalPlay.recordCheck(started))
        val record = PersonalPlay.addCompletedSolo(LocalPlayerData(), finished).records.single()
        assertEquals(1, record.checksUsed);assertTrue(record.assisted)
        assertEquals(0, record.hintsUsed)
        assertEquals(0, started.checksUsed)
    }
    @Test fun checkedDailyFinishesRetainTheSameFairnessMetadata() {
        val day = "2026-11-03"
        val data = DailyChallenge.open(LocalPlayerData(), day, 1000L) { fixture().copy(difficulty = "EASY") }
        val finished = finish(PersonalPlay.recordCheck(DailyChallenge.find(data, day)!!))
        val saved = DailyChallenge.complete(data, day, finished)
        val record = saved.records.single()
        assertEquals(DAILY, record.mode);assertEquals(day, record.dailyDate)
        assertTrue(record.assisted);assertEquals(1, record.checksUsed);assertEquals(0, record.hintsUsed)
        assertEquals(1, DailyChallenge.find(saved, day)!!.checksUsed)
    }
    @Test fun undoAndSerializationCannotRemoveARecordedCheck() {
        val checked = PersonalPlay.recordCheck(PersonalPlay.newSolo(fixture(), 1000L))
        val placed = PersonalPlay.edit(checked, 0, Kind.VALUE, 1, elapsedMs = 1000L)!!
        val undone = PersonalPlay.edit(placed, 0, Kind.UNDO, elapsedMs = 2000L)!!
        val restored = Json.decodeFromString<SoloProgress>(Json.encodeToString(undone))
        assertEquals(0, restored.cells[0].value);assertEquals(1, restored.checksUsed)
        assertTrue(restored.assisted);assertEquals(0, restored.hintsUsed)
    }
    @Test fun oldRecordsAndProgressDefaultToNoSolutionChecks() {
        val oldRecord = """{"id":"old","mode":"solo","result":"SOLVED","puzzleId":"oldp","elapsedMs":4000,"moves":4,"finishedAtMs":5000}"""
        val record = Json.decodeFromString<GameRecord>(oldRecord)
        assertEquals(0, record.checksUsed);assertFalse(record.assisted)
        val clues = fixture().clues.joinToString(",")
        val cells = fixture().clues.joinToString(",") { "{\"value\":$it}" }
        val oldProgress = """{"id":"old","puzzle":{"id":"oldp","clues":[$clues]},"cells":[$cells],"startedAtMs":1000}"""
        val progress = Json.decodeFromString<SoloProgress>(oldProgress)
        assertEquals(0, progress.checksUsed);assertFalse(progress.assisted)
    }
    @Test fun completedProgressCannotAcquireAnotherCheck() {
        val finished = finish(PersonalPlay.newSolo(fixture(), 1000L))
        assertEquals(finished, PersonalPlay.recordCheck(finished))
    }
    @Test fun checkCounterSaturatesWithoutRejectingFurtherHelp() {
        val progress = PersonalPlay.newSolo(fixture(), 1000L).copy(checksUsed = 9999)
        assertEquals(9999, PersonalPlay.recordCheck(progress).checksUsed)
        assertNotNull(SudokuAssistance.checkEntries(progress.puzzle, progress.cells.map { it.value }))
    }
}
