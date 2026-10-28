package com.playereden.sudokutogether.core

import kotlinx.serialization.Serializable
import java.util.UUID

const val SOLO = "solo"
const val MAX_RECORDS = 10_000

@Serializable data class SoloUndo(val cell: Int, val before: Cell)

@Serializable data class SoloProgress(
    val id: String,
    val puzzle: Puzzle,
    val cells: List<Cell>,
    val startedAtMs: Long,
    val elapsedMs: Long = 0,
    val moves: Int = 0,
    val history: List<SoloUndo> = emptyList(),
    val finishedAtMs: Long? = null
) {
    val completed: Boolean get() = finishedAtMs != null
    fun snapshot(): Snapshot = Snapshot(
        sessionId = id, epoch = id, puzzle = puzzle, cells = cells,
        revision = moves.toLong(), completed = completed,
        hostName = "Solo", undoTargets = if (history.isEmpty()) emptyMap() else mapOf(SOLO to history.last().cell)
    )
}

@Serializable data class GameRecord(
    val id: String,
    val mode: String,
    val result: String,
    val puzzleId: String,
    val elapsedMs: Long,
    val moves: Int,
    val finishedAtMs: Long,
    val difficulty: String? = null
)

@Serializable data class LocalPlayerData(
    val schema: Int = 1,
    val playerId: String = UUID.randomUUID().toString(),
    val activeSolo: SoloProgress? = null,
    val records: List<GameRecord> = emptyList()
)

data class PersonalStats(
    val soloWins: Int,
    val soloBestMs: Long?,
    val soloTotalMs: Long,
    val versusWins: Int,
    val versusLosses: Int,
    val versusDraws: Int,
    val coopCompletions: Int
)

object PersonalPlay {
    fun newSolo(puzzle: Puzzle, timeMs: Long): SoloProgress {
        require(Sudoku.solutionCount(puzzle.clues) == 1)
        return SoloProgress(UUID.randomUUID().toString(), puzzle, puzzle.clues.map { Cell(it) }, timeMs)
    }

    fun edit(s: SoloProgress, cell: Int, kind: Kind, number: Int = 0, enabled: Boolean = true, elapsedMs: Long, nowMs: Long = System.currentTimeMillis()): SoloProgress? {
        if (s.completed || cell !in 0..80 || s.puzzle.clues[cell] != 0 || elapsedMs < s.elapsedMs) return null
        if (kind in listOf(Kind.NOTE, Kind.VALUE) && number !in 1..9) return null
        val before = s.cells[cell]
        val nextMove = s.moves + 1
        val revision = nextMove.toLong()
        val history: List<SoloUndo>
        val after: Cell
        if (kind == Kind.UNDO) {
            val last = s.history.lastOrNull() ?: return null
            if (last.cell != cell) return null
            history = s.history.dropLast(1)
            after = last.before.copy(revision = revision)
        } else {
            after = when (kind) {
                Kind.VALUE -> Cell(number, revision = revision)
                Kind.CLEAR -> Cell(revision = revision)
                Kind.NOTE -> {
                    if (before.value != 0) return null
                    val bit = 1 shl (number - 1)
                    before.copy(notes = if (enabled) before.notes or bit else before.notes and bit.inv(), revision = revision)
                }
                Kind.UNDO -> error("Handled earlier")
            }
            if (after.value == before.value && after.notes == before.notes) return null
            history = (s.history + SoloUndo(cell, before)).takeLast(128)
        }
        val cells = s.cells.toMutableList().also { it[cell] = after }
        val finished = if (Sudoku.complete(s.puzzle, cells.map { it.value })) nowMs else null
        return s.copy(cells = cells, history = history, moves = nextMove, elapsedMs = elapsedMs, finishedAtMs = finished)
    }

    fun withElapsed(s: SoloProgress, elapsedMs: Long): SoloProgress =
        if (s.completed) s else s.copy(elapsedMs = maxOf(elapsedMs, s.elapsedMs))

    fun addCompletedSolo(data: LocalPlayerData, finished: SoloProgress): LocalPlayerData {
        require(finished.completed && Sudoku.complete(finished.puzzle, finished.cells.map { it.value }))
        if (data.records.any { it.id == finished.id }) return data.copy(activeSolo = finished)
        val record = GameRecord(finished.id, SOLO, "SOLVED", finished.puzzle.id,
            finished.elapsedMs, finished.moves, finished.finishedAtMs!!, finished.puzzle.difficulty)
        return data.copy(activeSolo = finished, records = (data.records + record).takeLast(MAX_RECORDS))
    }

    fun stats(data: LocalPlayerData): PersonalStats {
        val solo = data.records.filter { it.mode == SOLO && it.result == "SOLVED" }
        return PersonalStats(
            soloWins = solo.size,
            soloBestMs = solo.minOfOrNull { it.elapsedMs },
            soloTotalMs = solo.sumOf { it.elapsedMs },
            versusWins = data.records.count { it.mode == "versus" && it.result == "WIN" },
            versusLosses = data.records.count { it.mode == "versus" && it.result == "LOSS" },
            versusDraws = data.records.count { it.mode == "versus" && it.result == "DRAW" },
            coopCompletions = data.records.count { it.mode == "coop" && it.result == "SOLVED" }
        )
    }
}
