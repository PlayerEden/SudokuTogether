package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test

class HumanSudokuTest {
    private val classic = Puzzle("classic-reference", (
        "530070000" +
        "600195000" +
        "098000060" +
        "800060003" +
        "400803001" +
        "700020006" +
        "060000280" +
        "000419005" +
        "000080079").map { it.digitToInt() })

    @Test fun logicallySolvesKnownClassicPuzzleWithoutGuessing() {
        val report = HumanSudoku.assess(classic)
        assertTrue(report.uniqueSolution)
        assertTrue(report.logicallySolved)
        assertEquals(HumanSudoku.Grade.EASY, report.grade)
        assertEquals(0, report.emptyRemaining)
        assertEquals(classic.clues.count { it == 0 }, report.placements)
        assertTrue(report.steps.isNotEmpty())
        assertTrue(report.steps.all {
            it.technique == HumanSudoku.Technique.NAKED_SINGLE ||
            it.technique == HumanSudoku.Technique.HIDDEN_SINGLE
        })
    }

    @Test fun rejectsContradictoryAndAmbiguousPuzzles() {
        val invalid = Puzzle("invalid", List(81) { if (it == 0 || it == 1) 5 else 0 })
        val ambiguous = Puzzle("ambiguous", List(81) { 0 })
        for (puzzle in listOf(invalid, ambiguous, Puzzle("short", listOf(1, 2)))) {
            val report = HumanSudoku.assess(puzzle)
            assertEquals(HumanSudoku.Grade.INVALID, report.grade)
            assertFalse(report.uniqueSolution)
            assertFalse(report.logicallySolved)
        }
    }

    @Test fun techniqueTraceIsRepeatableAndOriginalBoardNeverChanges() {
        for (seed in 1L..16L) {
            val p = Sudoku.generate(seed)
            val unchanged = p.clues.toList()
            val first = HumanSudoku.assess(p)
            val second = HumanSudoku.assess(p)
            assertEquals(first, second)
            assertEquals(unchanged, p.clues)
            assertTrue(first.uniqueSolution)
            assertNotEquals(HumanSudoku.Grade.INVALID, first.grade)
            if (first.logicallySolved) {
                assertEquals(0, first.emptyRemaining)
                assertEquals(81 - p.clues.count { it != 0 }, first.placements)
                assertTrue(first.grade in listOf(
                    HumanSudoku.Grade.EASY, HumanSudoku.Grade.MEDIUM, HumanSudoku.Grade.HARD))
            } else {
                assertEquals(HumanSudoku.Grade.BEYOND_CURRENT_LOGIC, first.grade)
                assertTrue(first.emptyRemaining > 0)
            }
            first.steps.forEach { step ->
                assertTrue(step.cells.isNotEmpty())
                assertTrue(step.cells.all { it in 0..80 })
                assertTrue(step.digits.isNotEmpty())
                assertTrue(step.digits.all { it in 1..9 })
                if (step.technique == HumanSudoku.Technique.NAKED_SINGLE ||
                    step.technique == HumanSudoku.Technique.HIDDEN_SINGLE) {
                    assertEquals(1, step.cells.size)
                    assertEquals(1, step.digits.size)
                    assertEquals(0, p.clues[step.cells.single()])
                }
            }
        }
    }

    @Test fun calibratesCandidateGenerationClueFloorsWithoutChangingLegacySeedOutput() {
        val seeds = (2001L..2048L).toList()
        for (floor in listOf(36, 32, 29, 26, 24)) {
            val started = System.nanoTime()
            val sample = seeds.map { seed ->
                val puzzle = Sudoku.generate(seed, floor)
                assertEquals(1, Sudoku.solutionCount(puzzle.clues))
                if (floor == 36) assertEquals(Sudoku.generate(seed), puzzle)
                puzzle to HumanSudoku.assess(puzzle)
            }
            val distribution = sample.groupingBy { it.second.grade }.eachCount()
            val clueCounts = sample.map { it.first.clues.count { x -> x != 0 } }
            val elapsedMs = (System.nanoTime() - started) / 1_000_000
            println("SUDOKU_V08B_FLOOR_$floor=" + distribution.toSortedMap(compareBy { it.ordinal }) +
                ", clues_min=${clueCounts.minOrNull()}, clues_max=${clueCounts.maxOrNull()}, " +
                "millis=$elapsedMs")
            assertEquals(seeds.size, sample.size)
            assertEquals(0, distribution[HumanSudoku.Grade.INVALID] ?: 0)
            assertTrue(clueCounts.all { it >= floor })
        }
    }

    @Test fun reportCalibratesCurrent36ClueGeneratorWithoutClaimingAllTiersAvailable() {
        val distribution = (101L..148L).map { HumanSudoku.assess(Sudoku.generate(it)) }
            .groupingBy { it.grade }.eachCount()
        println("SUDOKU_V08B_CALIBRATION_48=" + distribution.toSortedMap(compareBy { it.ordinal }))
        assertEquals(48, distribution.values.sum())
        assertEquals(0, distribution[HumanSudoku.Grade.INVALID] ?: 0)
        assertTrue(distribution.keys.isNotEmpty())
    }
}
