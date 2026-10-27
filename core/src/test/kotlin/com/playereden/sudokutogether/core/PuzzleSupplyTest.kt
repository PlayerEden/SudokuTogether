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
}
