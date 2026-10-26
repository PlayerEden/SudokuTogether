package com.playereden.sudokutogether.core

import kotlinx.serialization.Serializable
import java.util.UUID

const val HOST = "host"
const val GUEST = "guest"
const val COOP = "coop"
const val VERSUS = "versus"
@Serializable data class Puzzle(val id: String, val clues: List<Int>)
@Serializable data class Cell(val value: Int = 0, val notes: Int = 0, val revision: Long = 0)
@Serializable data class Snapshot(
    val sessionId: String, val epoch: String, val puzzle: Puzzle, val cells: List<Cell>,
    val revision: Long = 0, val completed: Boolean = false, val hostName: String = "Host",
    val guestName: String? = null, val undoTargets: Map<String, Int> = emptyMap(),
    val mode: String = COOP, val guestCells: List<Cell> = emptyList(),
    val winner: String? = null, val raceStartedAtMs: Long? = null,
    val hostProgress: Int = 0, val guestProgress: Int = 0
)
@Serializable enum class Kind { VALUE, NOTE, CLEAR, UNDO }
@Serializable data class Operation(
    val sessionId: String, val epoch: String, val cell: Int, val expectedCellRevision: Long,
    val kind: Kind, val number: Int = 0, val enabled: Boolean = true, val id: String = UUID.randomUUID().toString()
)
@Serializable enum class ResultCode { ACCEPTED, DUPLICATE, STALE, GIVEN, INVALID, WRONG_GAME, PAUSED, COMPLETE, NO_UNDO, SAVE_FAILED }
@Serializable data class Outcome(val accepted: Boolean, val code: ResultCode, val snapshot: Snapshot, val operationId: String)
@Serializable data class EditRecord(val id: String, val player: String, val cell: Int, val before: Cell, val afterRevision: Long, val undone: Boolean = false)
@Serializable data class AcceptedOperation(val player: String, val operation: Operation)
@Serializable data class GuestIdentity(val name: String, val token: String)
@Serializable data class SavedSession(
    val schema: Int = 1, val snapshot: Snapshot, val history: List<EditRecord> = emptyList(),
    val recent: List<AcceptedOperation> = emptyList(), val guest: GuestIdentity? = null
)
