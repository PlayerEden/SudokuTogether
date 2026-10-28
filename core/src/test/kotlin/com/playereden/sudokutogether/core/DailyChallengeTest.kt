package com.playereden.sudokutogether.core

import java.time.Instant
import java.time.ZoneId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class DailyChallengeTest {
    @Test fun sameLocalDateAlwaysHasExactlyOneSameUniquelySolvableEasyPuzzle() {
        for (day in listOf("2026-10-27", "2026-10-28", "2026-12-31", "2027-01-01")) {
            val first = DailyChallenge.puzzle(day)
            val second = DailyChallenge.puzzle(day)
            assertEquals(first, second)
            assertEquals("EASY", first.difficulty)
            assertEquals(1, Sudoku.solutionCount(first.clues))
            assertEquals(HumanSudoku.Grade.EASY, HumanSudoku.assess(first).grade)
        }
        assertNotEquals(DailyChallenge.puzzle("2026-10-27").id,
            DailyChallenge.puzzle("2026-10-28").id)
    }

    @Test fun theDayUsesLocalCalendarAndOnlySwitchesAtLocalMidnight() {
        val instant = Instant.parse("2026-10-28T01:30:00Z")
        assertEquals("2026-10-27", DailyChallenge.today(ZoneId.of("America/New_York"), instant))
        assertEquals("2026-10-28", DailyChallenge.today(ZoneId.of("UTC"), instant))
        val before = Instant.parse("2026-10-28T03:59:59Z")
        val after = Instant.parse("2026-10-28T04:00:00Z")
        assertEquals("2026-10-27", DailyChallenge.today(ZoneId.of("America/New_York"), before))
        assertEquals("2026-10-28", DailyChallenge.today(ZoneId.of("America/New_York"), after))
    }

    @Test fun dailyIsIndependentOfRegularSoloAndNeverResetsSameDayProgress() {
        val original = PersonalPlay.newSolo(fixture(), 1_000)
        val originalData = LocalPlayerData(activeSolo = original)
        val day = "2026-10-27"
        val opened = DailyChallenge.open(originalData, day, 2_000)
        assertEquals(original, opened.activeSolo)
        val first = DailyChallenge.find(opened, day)!!
        val empty = first.puzzle.clues.indexOfFirst { it == 0 }
        val moved = PersonalPlay.edit(first, empty, Kind.NOTE, 2, elapsedMs = 200L)!!
        val edited = DailyChallenge.update(opened, day, moved)
        val reopened = DailyChallenge.open(edited, day, 9_000)
        assertEquals(moved, DailyChallenge.find(reopened, day))
        assertEquals(original, reopened.activeSolo)
        val tomorrow = DailyChallenge.open(reopened, "2026-10-28", 10_000)
        assertEquals(moved, DailyChallenge.find(tomorrow, day))
        assertNotEquals(first.puzzle.id, DailyChallenge.find(tomorrow, "2026-10-28")!!.puzzle.id)
        assertEquals(original, tomorrow.activeSolo)
    }

    @Test fun unfinishedDailyHistoryIsNeverSilentlyDeleted() {
        var data = LocalPlayerData()
        var date = java.time.LocalDate.of(2026, 10, 1)
        repeat(34) {
            data = DailyChallenge.open(data, date.toString(), it * 1000L)
            date = date.plusDays(1)
        }
        assertEquals(34, data.dailyProgress.size)
        assertNotNull(DailyChallenge.find(data, "2026-10-01"))
        assertNotNull(DailyChallenge.find(data, "2026-11-03"))
    }

    @Test fun finishedDailyHistoryIsPrunedBeforeUnfinishedWork() {
        val unfinished = PersonalPlay.newSolo(fixture(), 1L)
        var data = LocalPlayerData(dailyProgress = listOf(DailyProgress("2026-09-01", unfinished)))
        var date = java.time.LocalDate.of(2026, 10, 1)
        repeat(34) {
            val solved = PersonalPlay.newSolo(fixture(), 100L).copy(finishedAtMs = 200L)
            data = data.copy(dailyProgress = data.dailyProgress +
                DailyProgress(date.toString(), solved))
            date = date.plusDays(1)
        }
        data = DailyChallenge.open(data, "2026-11-04", 300L)
        assertNotNull(DailyChallenge.find(data, "2026-09-01"))
        assertNull(DailyChallenge.find(data, "2026-10-01"))
        assertNotNull(DailyChallenge.find(data, "2026-11-04"))
        assertEquals(1 + DailyChallenge.MAX_COMPLETED_DAYS, data.dailyProgress.size)
    }

    @Test fun reachingSafetyLimitRejectsNewDayWithoutDroppingAnyUnfinishedSave() {
        val original = PersonalPlay.newSolo(fixture(), 100L)
        val start = java.time.LocalDate.of(2026, 1, 1)
        val progress = (0 until DailyChallenge.MAX_SAVED_DAYS).map { offset ->
            DailyProgress(start.plusDays(offset.toLong()).toString(), original)
        }
        val existing = LocalPlayerData(dailyProgress = progress)
        val blocked = start.plusDays(DailyChallenge.MAX_SAVED_DAYS.toLong()).toString()
        val error = assertThrows(IllegalArgumentException::class.java) {
            DailyChallenge.open(existing, blocked, 999L)
        }
        assertTrue(error.message.orEmpty().contains("unfinished"))
        assertEquals(DailyChallenge.MAX_SAVED_DAYS, existing.dailyProgress.size)
        assertEquals(progress.first(), existing.dailyProgress.first())
        assertNull(DailyChallenge.find(existing, blocked))
    }

    @Test fun dailyCompletionStoresSeparateIdempotentAssistedRecord() {
        val date = "2026-10-27"
        val original = PersonalPlay.newSolo(fixture(), 1000L)
        var progress = PersonalPlay.recordHint(original)
        for ((cell, number) in listOf(0 to 1, 1 to 2, 9 to 4, 10 to 5)) {
            progress = PersonalPlay.edit(progress, cell, Kind.VALUE, number,
                elapsedMs = (progress.moves + 1) * 1000L)!!
        }
        val initial = LocalPlayerData(activeSolo = original,
            dailyProgress = listOf(DailyProgress(date, original)))
        val done = DailyChallenge.complete(initial, date, progress)
        assertEquals(original, done.activeSolo)
        assertEquals(1, done.records.size)
        val record = done.records.single()
        assertEquals(DAILY, record.mode)
        assertEquals(date, record.dailyDate)
        assertTrue(record.assisted)
        assertEquals(1, record.hintsUsed)
        assertEquals(1, DailyChallenge.completedDays(done.records))
        assertEquals(0, PersonalPlay.stats(done).soloWins)
        assertEquals(done, DailyChallenge.complete(done, date, progress))
    }

    @Test fun savesWithoutDailyFieldsRemainReadable() {
        val json = Json { encodeDefaults = false }
        val old = """{"schema":1,"playerId":"legacy","records":[]}"""
        val restored = json.decodeFromString<LocalPlayerData>(old)
        assertTrue(restored.dailyProgress.isEmpty())
        val wrapped = DailyChallenge.open(restored, "2026-10-27", 100L)
        assertEquals(wrapped, json.decodeFromString<LocalPlayerData>(json.encodeToString(wrapped)))
    }
}
