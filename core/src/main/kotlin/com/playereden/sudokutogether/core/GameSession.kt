package com.playereden.sudokutogether.core

import java.security.MessageDigest
import java.util.UUID

fun cleanName(name: String, fallback: String): String = name.filterNot { it.isISOControl() }.trim().take(24).ifBlank { fallback }

class GameSession(initial: SavedSession, private val persist: (SavedSession) -> Unit = {}) {
    private var state = initial
    private var hostPresent = true
    private var guestPresent = false
    private fun eligible(s: SavedSession, player: String): EditRecord? = s.history.lastOrNull {
        it.player == player && !it.undone && (if (s.snapshot.mode == VERSUS && player == GUEST) s.snapshot.guestCells else s.snapshot.cells)[it.cell].revision == it.afterRevision
    }
    private fun view(s: SavedSession): Snapshot {
        val snapshot = s.snapshot
        return snapshot.copy(
            undoTargets = listOf(HOST, GUEST).mapNotNull { p -> eligible(s, p)?.let { p to it.cell } }.toMap(),
            hostProgress = snapshot.cells.count { it.value != 0 },
            guestProgress = if (snapshot.mode == VERSUS) snapshot.guestCells.count { it.value != 0 } else 0
        )
    }
    @Synchronized fun snapshot(): Snapshot = view(state)
    // Never send either competitor's actual answers to their opponent.
    @Synchronized fun snapshotForGuest(): Snapshot {
        val visible = view(state)
        return if (visible.mode != VERSUS) visible else visible.copy(
            cells = visible.guestCells, guestCells = emptyList(),
            undoTargets = visible.undoTargets.filterKeys { it == GUEST }
        )
    }
    @Synchronized fun saved(): SavedSession = state.copy(snapshot = view(state))
    @Synchronized fun setPresence(host: Boolean = true, guest: Boolean = true) { hostPresent = host; guestPresent = guest }
    @Synchronized fun editable(): Boolean = hostPresent && (if (state.snapshot.mode == VERSUS) state.guest != null && guestPresent else state.guest == null || guestPresent)
    private fun commit(next: SavedSession) {
        val canonical = next.copy(snapshot = view(next))
        persist(canonical); state = canonical
    }
    @Synchronized fun submit(player: String, operation: Operation): Outcome {
        fun result(ok: Boolean, code: ResultCode) = Outcome(ok, code, snapshot(), operation.id)
        val s = state.snapshot
        if (operation.sessionId != s.sessionId || operation.epoch != s.epoch) return result(false, ResultCode.WRONG_GAME)
        if (player !in listOf(HOST, GUEST) || (player == GUEST && state.guest == null) || operation.id.isBlank() || operation.id.length > 128) return result(false, ResultCode.INVALID)
        state.recent.firstOrNull { it.operation.id == operation.id }?.let {
            val same = it.player == player && it.operation == operation
            return result(same, if (same) ResultCode.DUPLICATE else ResultCode.INVALID)
        }
        if (!editable()) return result(false, ResultCode.PAUSED)
        if (s.completed) return result(false, ResultCode.COMPLETE)
        val i = operation.cell
        if (i !in 0..80) return result(false, ResultCode.INVALID)
        if (s.puzzle.clues[i] != 0) return result(false, ResultCode.GIVEN)
        val board = if (s.mode == VERSUS && player == GUEST) s.guestCells else s.cells
        val before = board[i]
        if (operation.expectedCellRevision != before.revision) return result(false, ResultCode.STALE)
        if (operation.kind in listOf(Kind.VALUE, Kind.NOTE) && operation.number !in 1..9) return result(false, ResultCode.INVALID)
        val revision = s.revision + 1
        var history = state.history
        val after = when (operation.kind) {
            Kind.VALUE -> Cell(operation.number, 0, revision)
            Kind.CLEAR -> Cell(revision = revision)
            Kind.NOTE -> {
                if (before.value != 0) return result(false, ResultCode.INVALID)
                val bit = 1 shl (operation.number - 1)
                before.copy(notes = if (operation.enabled) before.notes or bit else before.notes and bit.inv(), revision = revision)
            }
            Kind.UNDO -> {
                val record = eligible(state, player) ?: return result(false, ResultCode.NO_UNDO)
                if (record.cell != i) return result(false, ResultCode.NO_UNDO)
                // The restored state may be an earlier edit's result. Carry its
                // lineage to the fresh revision, so another safe Undo is possible.
                val predecessor = history.indexOfLast { !it.undone && it.cell == i && (s.mode != VERSUS || it.player == player) && it.afterRevision == record.before.revision }
                history = history.mapIndexed { index, edit ->
                    when { edit.id == record.id -> edit.copy(undone = true); index == predecessor -> edit.copy(afterRevision = revision); else -> edit }
                }
                record.before.copy(revision = revision)
            }
        }
        if (operation.kind != Kind.UNDO) history = (history + EditRecord(operation.id, player, i, before, revision)).takeLast(256)
        val cells = board.toMutableList().also { it[i] = after }.toList()
        val solved = Sudoku.complete(s.puzzle, cells.map { it.value })
        val racing = s.mode == VERSUS
        val nextSnapshot = s.copy(
            cells = if (racing && player == GUEST) s.cells else cells,
            guestCells = if (racing && player == GUEST) cells else s.guestCells,
            revision = revision,
            winner = if (racing && solved) player else s.winner,
            completed = solved
        )
        val next = state.copy(snapshot = nextSnapshot,
            history = history, recent = (state.recent + AcceptedOperation(player, operation)).takeLast(128))
        try { commit(next) } catch (_: Exception) { return result(false, ResultCode.SAVE_FAILED) }
        return result(true, ResultCode.ACCEPTED)
    }
    @Synchronized fun admitGuest(name: String, token: String? = null): GuestIdentity {
        val current = state.guest
        if (current != null) {
            require(token != null && MessageDigest.isEqual(current.token.toByteArray(), token.toByteArray())) { "This game already has a partner. Use the same phone to rejoin, or ask the host to replace the partner." }
            return current
        }
        require(token == null) { "That saved invitation is no longer valid. Join again with the room code." }
        val identity = GuestIdentity(cleanName(name, "Partner"), UUID.randomUUID().toString() + UUID.randomUUID().toString())
        commit(state.copy(guest = identity, snapshot = state.snapshot.copy(guestName = identity.name,
            raceStartedAtMs = if (state.snapshot.mode == VERSUS) System.currentTimeMillis() else null)))
        return identity
    }
    @Synchronized fun replaceGuest() {
        commit(state.copy(guest = null, snapshot = state.snapshot.copy(guestName = null), history = state.history.filter { it.player != GUEST }, recent = state.recent.filter { it.player != GUEST }))
        guestPresent = false
    }
    @Synchronized fun newPuzzle(puzzle: Puzzle) {
        require(Sudoku.solutionCount(puzzle.clues) == 1) { "Puzzle must have exactly one solution." }
        commit(state.copy(snapshot = state.snapshot.copy(epoch = UUID.randomUUID().toString(), puzzle = puzzle,
            cells = puzzle.clues.map { Cell(it) }, guestCells = if (state.snapshot.mode == VERSUS) puzzle.clues.map { Cell(it) } else emptyList(),
            winner = null, raceStartedAtMs = if (state.snapshot.mode == VERSUS && state.guest != null) System.currentTimeMillis() else null,
            hostProgress = 0, guestProgress = 0, revision = 0, completed = false), history = emptyList(), recent = emptyList()))
    }
    companion object {
        fun create(puzzle: Puzzle, hostName: String = "Host", persist: (SavedSession) -> Unit = {}, mode: String = COOP): GameSession {
            require(Sudoku.solutionCount(puzzle.clues) == 1) { "Puzzle must have exactly one solution." }
            require(mode == COOP || mode == VERSUS)
            val initial = SavedSession(snapshot = Snapshot(UUID.randomUUID().toString(), UUID.randomUUID().toString(), puzzle,
                puzzle.clues.map { Cell(it) }, hostName = cleanName(hostName, "Host"), mode = mode,
                guestCells = if (mode == VERSUS) puzzle.clues.map { Cell(it) } else emptyList()))
            persist(initial); return GameSession(initial, persist)
        }
    }
}
