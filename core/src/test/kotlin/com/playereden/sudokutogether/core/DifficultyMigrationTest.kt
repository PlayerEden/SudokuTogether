package com.playereden.sudokutogether.core

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class DifficultyMigrationTest {
    private val json = Json { encodeDefaults = true }

    @Test fun oldSavedPuzzleWithoutDifficultyLoadsAsLegacy() {
        val old = Sudoku.generate(20261025L)
        val legacyJson = """{"id":"${old.id}","clues":[${old.clues.joinToString(",")}]}"""
        val restored = json.decodeFromString<Puzzle>(legacyJson)
        assertEquals(old, restored)
        assertNull(restored.difficulty)
        assertEquals(1, Sudoku.solutionCount(restored.clues))
    }

    @Test fun oldRecordsWithoutDifficultyRemainReadableAndUnlabeled() {
        val oldJson = """{"id":"old","mode":"solo","result":"SOLVED",
            "puzzleId":"original","elapsedMs":12000,"moves":44,
            "finishedAtMs":123456}"""
        val restored = json.decodeFromString<GameRecord>(oldJson)
        assertNull(restored.difficulty)
        assertEquals("SOLVED", restored.result)
    }

    @Test fun mediumAndHardLabelsMatchTheSolversTrace() {
        val targets = listOf(HumanSudoku.Grade.MEDIUM to 29, HumanSudoku.Grade.HARD to 24)
        for ((target, floor) in targets) {
            val seed = (2001L..2048L).first { HumanSudoku.assess(Sudoku.generate(it, floor)).grade == target }
            val rated = PuzzleSupply.nextRated(target, emptyList(), { seed }, attempts = 1).puzzle
            assertEquals(target.name, rated.difficulty)
            val session = GameSession.create(rated, mode = COOP)
            val restored = json.decodeFromString<SavedSession>(json.encodeToString(session.saved()))
            assertEquals(target.name, restored.snapshot.puzzle.difficulty)
            assertEquals(target.name, session.snapshotForGuest().puzzle.difficulty)
            val solo = PersonalPlay.newSolo(rated, 1000L)
            val soloRestored = json.decodeFromString<SoloProgress>(json.encodeToString(solo))
            assertEquals(target.name, soloRestored.puzzle.difficulty)
        }
    }

    @Test fun oldSoloAndCoopSavesKeepTheirOriginalPuzzles() {
        val legacy = Sudoku.generate(729L)
        val session = GameSession.create(legacy).saved()
        val decoded = json.decodeFromString<SavedSession>(json.encodeToString(session))
        assertNull(decoded.snapshot.puzzle.difficulty)
        assertEquals(legacy, decoded.snapshot.puzzle)
        validateSaved(decoded)
    }
}
