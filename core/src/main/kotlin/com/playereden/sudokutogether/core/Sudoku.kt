package com.playereden.sudokutogether.core

import java.security.MessageDigest
import kotlin.random.Random

object Sudoku {
    fun generate(seed: Long, minClues: Int = 36): Puzzle {
        require(minClues in 24..45) { "Calibration clue floor must be between 24 and 45" }
        val random = Random(seed)
        fun order(): List<Int> = (0..2).shuffled(random).flatMap { band -> (0..2).shuffled(random).map { band * 3 + it } }
        val rows = order(); val cols = order(); val digits = (1..9).shuffled(random)
        val clues = rows.flatMap { r -> cols.map { c -> digits[(r * 3 + r / 3 + c) % 9] } }.toMutableList()
        var remaining = 81
        for (i in (0..80).shuffled(random)) {
            if (remaining <= minClues) break
            val old = clues[i]; clues[i] = 0
            if (solutionCount(clues) == 1) remaining-- else clues[i] = old
        }
        val hash = MessageDigest.getInstance("SHA-256").digest(clues.joinToString("").toByteArray()).take(8).joinToString("") { "%02x".format(it) }
        return Puzzle("standard-$hash", clues.toList())
    }
    fun solutionCount(clues: List<Int>, limit: Int = 2): Int {
        if (clues.size != 81 || clues.any { it !in 0..9 } || limit < 1 || conflicts(clues).isNotEmpty()) return 0
        val board = clues.toIntArray(); val rows = IntArray(9); val cols = IntArray(9); val boxes = IntArray(9)
        for (i in 0..80) if (board[i] != 0) {
            val bit = 1 shl board[i]; rows[i / 9] = rows[i / 9] or bit; cols[i % 9] = cols[i % 9] or bit
            val box = i / 27 * 3 + i % 9 / 3; boxes[box] = boxes[box] or bit
        }
        var found = 0
        fun search() {
            if (found >= limit) return
            var best = -1; var candidates = 0; var least = 10
            for (i in 0..80) if (board[i] == 0) {
                val mask = 0x3fe and (rows[i / 9] or cols[i % 9] or boxes[i / 27 * 3 + i % 9 / 3]).inv()
                val count = Integer.bitCount(mask)
                if (count == 0) return
                if (count < least) { best = i; candidates = mask; least = count; if (count == 1) break }
            }
            if (best == -1) { found++; return }
            val r = best / 9; val c = best % 9; val box = best / 27 * 3 + c / 3
            while (candidates != 0 && found < limit) {
                val bit = candidates and -candidates; candidates = candidates xor bit
                board[best] = Integer.numberOfTrailingZeros(bit)
                rows[r] = rows[r] or bit; cols[c] = cols[c] or bit; boxes[box] = boxes[box] or bit
                search()
                rows[r] = rows[r] xor bit; cols[c] = cols[c] xor bit; boxes[box] = boxes[box] xor bit; board[best] = 0
            }
        }
        search(); return found
    }
    fun conflicts(values: List<Int>): Set<Int> {
        if (values.size != 81) return emptySet()
        val result = mutableSetOf<Int>()
        fun check(indices: List<Int>) {
            indices.filter { values[it] in 1..9 }.groupBy { values[it] }.values.filter { it.size > 1 }.forEach { result.addAll(it) }
        }
        for (n in 0..8) { check((0..8).map { n * 9 + it }); check((0..8).map { it * 9 + n }) }
        for (box in 0..8) check((0..8).map { (box / 3 * 3 + it / 3) * 9 + box % 3 * 3 + it % 3 })
        return result
    }
    fun complete(puzzle: Puzzle, values: List<Int>): Boolean = puzzle.clues.size == 81 && values.size == 81 &&
        values.all { it in 1..9 } && puzzle.clues.indices.all { puzzle.clues[it] == 0 || puzzle.clues[it] == values[it] } && conflicts(values).isEmpty()
}
