package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test

class LocalPlayUpdateTest {
    private fun nearFinish(): SoloProgress {
        val puzzle = Puzzle("one-square", solution.mapIndexed { i, n -> if (i == 0) 0 else n }, "EASY")
        return PersonalPlay.newSolo(puzzle, 1000L).copy(elapsedMs = 2000L, hintsUsed = 1)
    }

    @Test fun acceptedOldDailyFinishRemainsAvailableAfterItsArchiveEntryIsEvicted() {
        val progress = nearFinish()
        val recent = (1..31).map { day -> DailyProgress("2026-10-${day.toString().padStart(2, '0')}",
            progress.copy(id = "recent-$day", cells = solution.map { Cell(it) }, finishedAtMs = 3000L)) }
        val data = LocalPlayerData(activeSolo = progress.copy(id = "regular-solo"),
            dailyProgress = recent + DailyProgress("2026-09-01", progress))
        val op = progress.snapshot().move(0, number = 1)
        val changed = PersonalPlay.applyEdit(data, "2026-09-01", op, 4500L, 5500L)
        assertNull(DailyChallenge.find(changed.data, "2026-09-01"))
        assertEquals(31, changed.data.dailyProgress.size)
        assertEquals("regular-solo", changed.data.activeSolo!!.id)
        val finished = changed.progress!!
        assertTrue(finished.completed)
        assertEquals(progress.id, finished.id)
        assertEquals(4500L, finished.elapsedMs)
        assertEquals(1, finished.moves)
        assertEquals(1, finished.hintsUsed)
        assertEquals(solution, finished.cells.map { it.value })
        assertTrue(changed.data.records.single().assisted)
        assertEquals(progress.id, changed.data.records.single().id)
    }

    @Test fun ordinarySoloKeepsItsIndependentResultAndDailyProgress() {
        val progress = nearFinish()
        val daily = DailyProgress("2026-09-01", progress.copy(id = "daily"))
        val original = LocalPlayerData(activeSolo = progress, dailyProgress = listOf(daily))
        val changed = PersonalPlay.applyEdit(original, null, progress.snapshot().move(0, number = 1), 4500L, 5500L)
        assertEquals(listOf(daily), changed.data.dailyProgress)
        assertEquals(changed.progress, changed.data.activeSolo)
        assertEquals(SOLO, changed.data.records.single().mode)
    }

    @Test fun aStaleOperationCannotChangeADifferentSessionOrLaterCellRevision() {
        val progress = nearFinish()
        val original = LocalPlayerData(activeSolo = progress)
        val op = progress.snapshot().move(0, number = 1)
        for (bad in listOf(op.copy(epoch = "old"), op.copy(sessionId = "other"), op.copy(expectedCellRevision = 99), op.copy(cell = 81))) {
            val changed = PersonalPlay.applyEdit(original, null, bad, 4500L, 5500L)
            assertEquals(original, changed.data)
            assertNull(changed.progress)
        }
    }
}
