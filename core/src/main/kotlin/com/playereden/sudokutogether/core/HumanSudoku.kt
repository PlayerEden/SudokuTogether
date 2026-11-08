package com.playereden.sudokutogether.core

/**
 * A deterministic, deduction-only Sudoku solver.
 *
 * This engine does not guess, search, backtrack, or look at the solution while
 * making moves. Uniqueness is checked separately before grading an original
 * puzzle. Difficulty bands are provisional technique ceilings until real
 * generation benchmarks and player calibration are complete.
 */
object HumanSudoku {
    enum class Technique(val label: String) {
        NAKED_SINGLE("Only one digit fits"),
        HIDDEN_SINGLE("Only one square fits"),
        LOCKED_CANDIDATE("Locked candidate"),
        NAKED_PAIR("Naked pair"),
        HIDDEN_PAIR("Hidden pair"),
        X_WING("X-Wing"),
        SOLUTION_REVEAL("One-number reveal")
    }
    enum class Grade { EASY, MEDIUM, HARD, BEYOND_CURRENT_LOGIC, INVALID }

    data class Step(
        val technique: Technique,
        /** For placements: exactly one target; for eliminations: affected targets. */
        val cells: List<Int>,
        /** Placed digit or logically removed candidates. */
        val digits: List<Int>,
        val reason: String
    )

    data class Assessment(
        val grade: Grade,
        val logicallySolved: Boolean,
        val uniqueSolution: Boolean,
        val steps: List<Step>,
        val emptyRemaining: Int
    ) {
        val placements: Int get() = steps.count {
            it.technique == Technique.NAKED_SINGLE || it.technique == Technique.HIDDEN_SINGLE
        }
        val highestTechnique: Technique? get() = steps.maxByOrNull { it.technique.ordinal }?.technique
    }

    private const val ALL = 0x3FE // bits 1 through 9
    private const val COACH_SEARCH_NODES = 200_000
    private val units: List<IntArray> = buildList {
        for (r in 0..8) add(IntArray(9) { r * 9 + it })
        for (c in 0..8) add(IntArray(9) { it * 9 + c })
        for (b in 0..8) add(IntArray(9) {
            (b / 3 * 3 + it / 3) * 9 + (b % 3 * 3 + it % 3)
        })
    }
    private val peers: List<IntArray> = (0..80).map { cell ->
        units.filter { cell in it }.flatMap { it.asIterable() }
            .filter { it != cell }.distinct().sorted().toIntArray()
    }

    private fun box(cell: Int) = cell / 27 * 3 + cell % 9 / 3
    private fun digits(mask: Int) = (1..9).filter { mask and (1 shl it) != 0 }
    private fun single(mask: Int) = mask != 0 && mask and (mask - 1) == 0

    private fun candidatePositions(unit: IntArray, candidates: IntArray, digitBit: Int): Int =
        unit.indices.fold(0) { positions, index ->
            if (candidates[unit[index]] and digitBit != 0) positions or (1 shl index) else positions
        }

    /** Safe for generator calibration; never mutates the input puzzle. */
    fun assess(puzzle: Puzzle): Assessment {
        val givens = puzzle.clues
        if (!hasUniqueSolution(givens)) {
            return Assessment(Grade.INVALID, false, false, emptyList(), givens.count { it == 0 })
        }
        return solve(givens)
    }

    internal fun coachSteps(visible: List<Int>): List<SudokuAssistance.CoachStep> {
        val firstEmpty = visible.indexOf(0)
        if (firstEmpty < 0) return emptyList()
        // Validate before offering deductions. Keep the solution out of the deduction engine.
        val solution = Sudoku.uniqueSolutionWithin(visible, COACH_SEARCH_NODES) ?: return emptyList()
        val proof = mutableListOf<SudokuAssistance.CoachStep>()
        solve(visible, proof)
        if (proof.lastOrNull()?.eliminations?.isEmpty() != true) {
            val target = proof.lastOrNull()?.targetCells?.first() ?: firstEmpty
            proof.add(SudokuAssistance.CoachStep(
                Technique.SOLUTION_REVEAL, emptyList(), listOf(target), listOf(solution[target]),
                "The Coach ran out of deduction steps. You can reveal one number to keep going.",
                emptyMap()
            ))
        }
        return proof.toList()
    }

    private fun hasUniqueSolution(grid: List<Int>): Boolean =
        grid.size == 81 && grid.all { it in 0..9 } && Sudoku.conflicts(grid).isEmpty() &&
            Sudoku.solutionCount(grid) == 1

    private fun unitName(unitIndex: Int): String = when (unitIndex) {
        in 0..8 -> "row ${unitIndex + 1}"
        in 9..17 -> "column ${unitIndex - 8}"
        else -> "box ${unitIndex - 17}"
    }

    private fun solve(
        givens: List<Int>,
        proof: MutableList<SudokuAssistance.CoachStep>? = null
    ): Assessment {
        val board = givens.toIntArray()
        val candidates = IntArray(81) { cell ->
            if (board[cell] != 0) 0 else {
                var seen = 0
                for (peer in peers[cell]) if (board[peer] != 0) seen = seen or (1 shl board[peer])
                ALL and seen.inv()
            }
        }
        val steps = mutableListOf<Step>()

        fun placed(cell: Int, digit: Int, technique: Technique, reason: String, unitIndex: Int? = null) {
            val target = listOf(cell)
            val placedDigit = listOf(digit)
            if (proof != null) {
                val source = if (unitIndex == null) {
                    peers[cell].filter { board[it] != 0 }.distinctBy { board[it] }
                } else {
                    // Occupied squares and matching peers are board evidence. Any other
                    // missing candidate is explained by an earlier elimination in the proof.
                    units[unitIndex].filter { it != cell }.mapNotNull { other ->
                        if (board[other] != 0) other else peers[other].firstOrNull { board[it] == digit }
                    }.distinct().sorted()
                }
                val location = "Row ${cell / 9 + 1}, column ${cell % 9 + 1}"
                val explanation = if (unitIndex == null) {
                    "$location has only one candidate left: $digit."
                } else {
                    "$location is the only place for $digit in ${unitName(unitIndex)}."
                }
                proof.add(SudokuAssistance.CoachStep(technique, source, target, placedDigit,
                    explanation, emptyMap()))
            }
            board[cell] = digit
            candidates[cell] = 0
            val bit = (1 shl digit).inv()
            for (peer in peers[cell]) if (board[peer] == 0) {
                candidates[peer] = candidates[peer] and bit
            }
            steps.add(Step(technique, target, placedDigit, reason))
        }

        fun eliminate(
            targets: Iterable<Int>, mask: Int, technique: Technique, reason: String,
            source: List<Int>, sourceUnit: Int, targetUnit: Int = sourceUnit
        ): Boolean {
            val affected = targets.filter { board[it] == 0 && candidates[it] and mask != 0 }
                .distinct().sorted()
            if (affected.isEmpty()) return false
            val removed = if (technique == Technique.HIDDEN_PAIR) {
                digits(affected.fold(0) { result, cell -> result or (candidates[cell] and mask) })
            } else digits(mask)
            if (proof != null) {
                val eliminations = affected.associateWith { digits(candidates[it] and mask) }
                val support = if (technique == Technique.HIDDEN_PAIR) digits(ALL and mask.inv()) else removed
                val explanation = when (technique) {
                    Technique.NAKED_PAIR ->
                        "Two cells in ${unitName(sourceUnit)} share the candidates ${removed.joinToString(" and ")}. " +
                            "Remove those digits from the other cells in that unit."
                    Technique.HIDDEN_PAIR ->
                        "In ${unitName(sourceUnit)}, ${support.joinToString(" and ")} can only go in these two squares. " +
                            "Keep those two candidates and cross off any others in the two squares."
                    Technique.LOCKED_CANDIDATE ->
                        "In ${unitName(sourceUnit)}, ${removed.single()} can only go in ${unitName(targetUnit)}. " +
                            "Remove it from candidates elsewhere in ${unitName(targetUnit)}."
                    else -> reason
                }
                proof.add(SudokuAssistance.CoachStep(technique, source, affected, support,
                    explanation, eliminations))
            }
            for (cell in affected) candidates[cell] = candidates[cell] and mask.inv()
            steps.add(Step(technique, affected, removed, reason))
            return true
        }

        // One logical step per iteration. Scan in a consistent order so a
        // difficulty result and future hint explanation are reproducible.
        while (steps.size < 1024) {
            if (board.none { it == 0 }) break
            if ((0..80).any { board[it] == 0 && candidates[it] == 0 }) break

            val naked = (0..80).firstOrNull { board[it] == 0 && single(candidates[it]) }
            if (naked != null) {
                placed(naked, Integer.numberOfTrailingZeros(candidates[naked]),
                    Technique.NAKED_SINGLE, "Only one digit fits this square")
                if (proof != null) break
                continue
            }

            var moved = false
            for ((unitIndex, unit) in units.withIndex()) {
                if (moved) break
                for (digit in 1..9) {
                    val bit = 1 shl digit
                    if (unit.any { board[it] == digit }) continue
                    val locations = unit.filter { board[it] == 0 && candidates[it] and bit != 0 }
                    if (locations.size == 1) {
                        placed(locations.single(), digit, Technique.HIDDEN_SINGLE,
                            "Only place for $digit in unit ${unitIndex + 1}", unitIndex)
                        moved = true
                        break
                    }
                }
            }
            if (moved) {
                if (proof != null) break
                continue
            }

            // Pointing: candidates of a digit in one 3x3 box lie in the same row/column.
            for (b in 0..8) {
                if (moved) break
                val square = units[18 + b]
                for (digit in 1..9) {
                    val bit = 1 shl digit
                    val positions = square.filter { board[it] == 0 && candidates[it] and bit != 0 }
                    if (positions.size < 2) continue
                    val row = positions.first() / 9
                    if (positions.all { it / 9 == row } &&
                        eliminate(units[row].asIterable().filter { box(it) != b }, bit,
                            Technique.LOCKED_CANDIDATE, "Box ${b + 1} confines $digit to row ${row + 1}",
                            positions, 18 + b, row)) {
                        moved = true; break
                    }
                    val col = positions.first() % 9
                    if (positions.all { it % 9 == col } &&
                        eliminate(units[9 + col].asIterable().filter { box(it) != b }, bit,
                            Technique.LOCKED_CANDIDATE, "Box ${b + 1} confines $digit to column ${col + 1}",
                            positions, 18 + b, 9 + col)) {
                        moved = true; break
                    }
                }
            }
            if (moved) continue

            // Claiming: a row/column's candidates all fall within one box.
            for (unitIndex in 0..17) {
                if (moved) break
                val unit = units[unitIndex]
                for (digit in 1..9) {
                    val bit = 1 shl digit
                    val positions = unit.filter { board[it] == 0 && candidates[it] and bit != 0 }
                    if (positions.size < 2) continue
                    val box = box(positions.first())
                    if (positions.all { box(it) == box } &&
                        eliminate(units[18 + box].asIterable().filter { it !in unit }, bit,
                            Technique.LOCKED_CANDIDATE, "Line ${unitIndex + 1} confines $digit to box ${box + 1}",
                            positions, unitIndex, 18 + box)) {
                        moved = true; break
                    }
                }
            }
            if (moved) continue

            // Naked pair: exactly two cells in the same unit share the same two candidates.
            for ((unitIndex, unit) in units.withIndex()) {
                if (moved) break
                val pairMasks = unit.filter { board[it] == 0 && Integer.bitCount(candidates[it]) == 2 }
                    .groupBy { candidates[it] }
                for ((mask, pair) in pairMasks) {
                    if (pair.size != 2) continue
                    if (eliminate(unit.asIterable().filter { it !in pair }, mask,
                            Technique.NAKED_PAIR,
                            "Two cells reserve digits ${digits(mask).joinToString("/")} in unit ${unitIndex + 1}",
                            pair, unitIndex)) {
                        moved = true; break
                    }
                }
            }
            if (moved) continue
            // Additional Coach techniques leave puzzle supply and difficulty grading alone.
            if (proof == null) break

            hiddenPairs@ for ((unitIndex, unit) in units.withIndex()) {
                val positions = IntArray(10) { digit -> candidatePositions(unit, candidates, 1 shl digit) }
                for (first in 1..8) {
                    if (Integer.bitCount(positions[first]) != 2) continue
                    for (second in first + 1..9) {
                        if (positions[first] != positions[second]) continue
                        val pairMask = (1 shl first) or (1 shl second)
                        val pair = unit.filter { candidates[it] and pairMask == pairMask }
                        if (eliminate(pair, ALL and pairMask.inv(), Technique.HIDDEN_PAIR,
                                "$first and $second are confined to two squares", pair, unitIndex)) {
                            moved = true
                            break@hiddenPairs
                        }
                    }
                }
            }
            if (moved) continue

            // The two base rows (or columns) must each use the digit in the same two cover lines.
            xWings@ for (baseStart in listOf(0, 9)) {
                val coverStart = 9 - baseStart
                val (baseName, coverName) = if (baseStart == 0) "rows" to "columns" else "columns" to "rows"
                for (digit in 1..9) {
                    val bit = 1 shl digit
                    val positions = IntArray(9) { candidatePositions(units[baseStart + it], candidates, bit) }
                    for (first in 0..7) {
                        val mask = positions[first]
                        if (Integer.bitCount(mask) != 2) continue
                        for (second in first + 1..8) {
                            if (positions[second] != mask) continue
                            val cover = (0..8).filter { mask and (1 shl it) != 0 }
                            val corners = cover.flatMap {
                                listOf(units[baseStart + first][it], units[baseStart + second][it])
                            }.sorted()
                            val targets = cover.flatMap { units[coverStart + it].asIterable() }
                                .filter { it !in corners }
                            val coverNumbers = cover.joinToString(" and ") { (it + 1).toString() }
                            val explanation = "In $baseName ${first + 1} and ${second + 1}, " +
                                "$digit can only go in $coverName $coverNumbers. " +
                                "Those $coverName use their $digit in the shaded squares. " +
                                "Cross off $digit elsewhere in those $coverName."
                            if (eliminate(targets, bit, Technique.X_WING, explanation, corners,
                                    baseStart + first)) {
                                moved = true
                                break@xWings
                            }
                        }
                    }
                }
            }
            if (!moved) break
        }

        val solved = board.none { it == 0 } && Sudoku.complete(Puzzle("grade", givens), board.toList())
        val grade = if (!solved) Grade.BEYOND_CURRENT_LOGIC else when {
            steps.any { it.technique == Technique.NAKED_PAIR } -> Grade.HARD
            steps.any { it.technique == Technique.LOCKED_CANDIDATE } -> Grade.MEDIUM
            else -> Grade.EASY
        }
        return Assessment(grade, solved, true, steps.toList(), board.count { it == 0 })
    }
}
