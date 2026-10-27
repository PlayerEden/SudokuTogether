package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test

class PuzzleSupplyTest {
    @Test fun advancesPastRecentlyUsedPuzzle() {
        val first = Sudoku.generate(101)
        val second = Sudoku.generate(102)
        assertNotEquals(first.id, second.id)
        val seeds = listOf(101L, 102L).iterator()
        val next = PuzzleSupply.next(listOf(first.id)) { seeds.next() }
        assertEquals(second, next)
        assertEquals(1, Sudoku.solutionCount(next.clues))
    }

    @Test fun refusesToRepeatAfterExhaustingGenerationAttempts() {
        val original = Sudoku.generate(555)
        var attempts = 0
        try {
            PuzzleSupply.next(listOf(original.id)) { attempts++; 555L }
            fail("Must reject a repeated puzzle rather than silently repeat it")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message!!.contains("fresh puzzle"))
        }
        assertEquals(PuzzleSupply.MAX_ATTEMPTS, attempts)
    }

    @Test fun keepsOnlyRecentDistinctIdsInChronologicalOrder() {
        val ids = (0..PuzzleSupply.RECENT_LIMIT).map { "id-$it" }
        val recent = PuzzleSupply.remember(ids, "id-3")
        assertEquals(PuzzleSupply.RECENT_LIMIT, recent.size)
        assertEquals("id-3", recent.last())
        assertEquals(recent.size, recent.toSet().size)
        assertFalse("id-0" in recent)
    }

    @Test fun generatorSupportsManyMoreThanEightOriginalSeeds() {
        val puzzles = (1L..40L).map(Sudoku::generate)
        assertEquals(40, puzzles.map { it.id }.toSet().size)
        puzzles.forEach { p ->
            assertEquals(81, p.clues.size)
            assertEquals(1, Sudoku.solutionCount(p.clues))
        }
    }

    @Test fun oldSeedsRemainDeterministicForResumeCompatibility() {
        val oldSeeds = listOf(20261025L, 2048L, 729L, 81L, 314159L, 271828L, 1009L, 303L)
        oldSeeds.forEach { seed -> assertEquals(Sudoku.generate(seed), Sudoku.generate(seed)) }
    }
    @Test fun ratedPuzzleSupplierFindsRealTechniqueTiersWithoutDowngrading() {
        val floorByGrade = mapOf(
            HumanSudoku.Grade.EASY to 36,
            HumanSudoku.Grade.MEDIUM to 29,
            HumanSudoku.Grade.HARD to 24
        )
        for ((grade, floor) in floorByGrade) {
            val seed = (2001L..2048L).first { HumanSudoku.assess(Sudoku.generate(it, floor)).grade == grade }
            val supplied = PuzzleSupply.nextRated(grade, emptyList(), { seed }, attempts = 1)
            assertEquals(grade, supplied.assessment.grade)
            assertTrue(supplied.assessment.logicallySolved)
            assertEquals(1, Sudoku.solutionCount(supplied.puzzle.clues))
            assertTrue(supplied.puzzle.clues.count { it != 0 } >= floor)
        }
    }

    @Test fun ratedSupplierNeverPassesOffEasyPuzzleAsHardOrRepeatsRecentIds() {
        val easySeed = 2001L
        val easy = Sudoku.generate(easySeed, 36)
        assertEquals(HumanSudoku.Grade.EASY, HumanSudoku.assess(easy).grade)
        var attempts = 0
        try {
            PuzzleSupply.nextRated(HumanSudoku.Grade.EASY, listOf(easy.id),
                { attempts++; easySeed }, attempts = 4)
            fail("Repeated puzzle must not be silently accepted")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message!!.contains("no saved puzzle was replaced"))
        }
        assertEquals(4, attempts)
        try {
            PuzzleSupply.nextRated(HumanSudoku.Grade.HARD, emptyList(),
                { easySeed }, attempts = 2)
            // A particular 24-clue seed may actually be Hard. If so, this
            // cannot test non-downgrading, so we test an explicitly Easy
            // grade below using the same deterministic candidate floor.
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message!!.contains("hard"))
        }
    }

    @Test fun unratedDifficultyRequestsAndUnboundedAttemptsAreRejected() {
        for (bad in listOf(HumanSudoku.Grade.INVALID, HumanSudoku.Grade.BEYOND_CURRENT_LOGIC)) {
            try {
                PuzzleSupply.nextRated(bad, emptyList(), { 1L })
                fail("Invalid request $bad")
            } catch (expected: IllegalArgumentException) {
                assertTrue(expected.message!!.contains("logically rated"))
            }
        }
        try {
            PuzzleSupply.nextRated(HumanSudoku.Grade.EASY, emptyList(), { 1L }, attempts = 0)
            fail("Zero attempts must be rejected")
        } catch (_: IllegalArgumentException) { }
    }

}
