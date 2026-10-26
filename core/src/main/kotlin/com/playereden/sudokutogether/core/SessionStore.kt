package com.playereden.sudokutogether.core

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.AtomicMoveNotSupportedException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed class LoadResult {
    data object Missing : LoadResult()
    data class Loaded(val data: SavedSession) : LoadResult()
    data class Recovered(val data: SavedSession) : LoadResult()
    data class Invalid(val message: String) : LoadResult()
}
interface SessionStore { fun load(): LoadResult; fun save(state: SavedSession) }
class AtomicSessionStore(private val file: File) : SessionStore {
    private val backup = File(file.path + ".backup")
    private val json = Json { encodeDefaults = true }
    private fun read(source: File): SavedSession? = try {
        if (!source.isFile || source.length() > 512 * 1024) null
        else json.decodeFromString<SavedSession>(source.readText()).also { validateSaved(it) }
    } catch (_: Exception) { null }

    @Synchronized override fun load(): LoadResult {
        read(file)?.let { return LoadResult.Loaded(it) }
        read(backup)?.let { return LoadResult.Recovered(it) }
        return if (!file.exists() && !backup.exists()) LoadResult.Missing else LoadResult.Invalid("The saved puzzle could not be read. It has been kept on this phone; starting a new game requires confirmation.")
    }

    private fun replace(source: File, target: File) {
        try { Files.move(source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING) }
        catch (_: AtomicMoveNotSupportedException) { Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING) }
    }
    private fun write(target: File, text: String) {
        val temporary = File(target.path + ".tmp")
        FileOutputStream(temporary).use { out -> out.write(text.toByteArray(Charsets.UTF_8)); out.fd.sync() }
        replace(temporary, target)
    }
    @Synchronized override fun save(state: SavedSession) {
        validateSaved(state)
        val parent = file.absoluteFile.parentFile!!
        check(parent.isDirectory || parent.mkdirs()) { "Saved puzzle storage is unavailable." }
        read(file)?.let { write(backup, json.encodeToString(it)) }
        write(file, json.encodeToString(state))
    }
}

fun validateSnapshot(s: Snapshot) {
    require(s.sessionId.isNotBlank() && s.sessionId.length <= 128 && s.epoch.isNotBlank() && s.epoch.length <= 128)
    require(s.puzzle.id.length in 1..128 && Sudoku.solutionCount(s.puzzle.clues) == 1)
    require(s.cells.size == 81 && s.revision >= 0)
    require(s.hostName.length in 1..24 && (s.guestName == null || s.guestName.length in 1..24))
    s.cells.forEachIndexed { i, c ->
        require(c.value in 0..9 && c.notes in 0..511 && c.revision in 0..s.revision)
        require(c.value == 0 || c.notes == 0)
        if (s.puzzle.clues[i] != 0) require(c.value == s.puzzle.clues[i] && c.notes == 0)
    }
    require(s.mode == COOP || s.mode == VERSUS)
    if (s.mode == COOP) {
        require(s.guestCells.isEmpty() && s.winner == null)
        require(s.completed == Sudoku.complete(s.puzzle, s.cells.map { it.value }))
    } else {
        require(s.guestCells.isEmpty() || s.guestCells.size == 81)
        s.guestCells.forEachIndexed { i, c ->
            require(c.value in 0..9 && c.notes in 0..511 && c.revision in 0..s.revision)
            require(c.value == 0 || c.notes == 0)
            if (s.puzzle.clues[i] != 0) require(c.value == s.puzzle.clues[i])
        }
        require(s.winner == null || s.winner == HOST || s.winner == GUEST)
        require(s.completed == (s.winner != null))
        require(s.raceStartedAtMs == null || s.raceStartedAtMs > 0)
        if (s.winner == HOST && s.guestCells.isNotEmpty())
            require(Sudoku.complete(s.puzzle, s.cells.map { it.value }))
        if (s.winner == GUEST && s.guestCells.isNotEmpty())
            require(Sudoku.complete(s.puzzle, s.guestCells.map { it.value }))
    }
    require(s.hostProgress in 0..81 && s.guestProgress in 0..81)
    require(s.undoTargets.all { (p, i) -> p in listOf(HOST, GUEST) && i in 0..80 })
}

fun validateSaved(s: SavedSession) {
    require(s.schema == 1)
    validateSnapshot(s.snapshot)
    require(s.history.size <= 256 && s.recent.size <= 128)
    require(s.history.all { it.player in listOf(HOST, GUEST) && it.cell in 0..80 && it.id.length in 1..128 &&
        it.afterRevision in 1..s.snapshot.revision && it.before.revision in 0 until it.afterRevision && it.before.value in 0..9 && it.before.notes in 0..511 })
    require(s.recent.all { it.player in listOf(HOST, GUEST) && it.operation.cell in 0..80 && it.operation.id.length in 1..128 && it.operation.epoch == s.snapshot.epoch && it.operation.sessionId == s.snapshot.sessionId })
    require((s.guest == null && s.snapshot.guestName == null) || (s.guest != null && s.guest.name == s.snapshot.guestName && s.guest.token.length in 32..128))
}
