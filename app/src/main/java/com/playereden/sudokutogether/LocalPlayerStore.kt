package com.playereden.sudokutogether

import android.util.AtomicFile
import com.playereden.sudokutogether.core.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.util.UUID

/** A verified primary plus a last-good private recovery snapshot. Schema 1 stays compatible. */
class LocalPlayerStore(file: File) {
    private val atomic = AtomicFile(file)
    private val recovery = AtomicFile(File(file.path + ".recovery"))
    private val json = Json { encodeDefaults = true }
    val recoveryWarning: String?
    val canSave: Boolean
    private var needsRepair = false
    private var data: LocalPlayerData

    init {
        val primary = runCatching { read(atomic) }.getOrNull()
        if (primary != null) {
            data = primary; canSave = true; recoveryWarning = null
        } else {
            val backup = runCatching { read(recovery) }.getOrNull()
            if (backup != null) {
                data = backup
                canSave = !exists(atomic) || preserveDamagedPrimary()
                needsRepair = true
                recoveryWarning = if (canSave)
                    "Your last healthy local backup was recovered. Recent changes may be missing."
                else "Saved progress was recovered, but saving is paused because the damaged file could not be preserved. Check phone storage and reopen the app."
            } else if (!exists(atomic) && !exists(recovery)) {
                data = LocalPlayerData(); canSave = true; recoveryWarning = null
            } else {
                // Read-only placeholder for the recovery screen. It must never reach disk.
                data = LocalPlayerData(); canSave = false
                recoveryWarning = "Local progress could not be read. Your existing files are preserved and saving is paused. Reopen the app after checking phone storage."
            }
        }
    }

    private fun exists(file: AtomicFile): Boolean =
        listOf(file.baseFile, File(file.baseFile.path + ".bak"), File(file.baseFile.path + ".new")).any { it.exists() }

    private fun read(file: AtomicFile): LocalPlayerData? {
        if (!exists(file)) return null
        if (!file.baseFile.exists() && !File(file.baseFile.path + ".bak").exists())
            throw IOException("An interrupted profile write needs recovery.")
        val text = file.openRead().use {
            if (it.channel.size() > 64L * 1024 * 1024) throw IOException("Local progress is too large to read safely.")
            it.readBytes().toString(Charsets.UTF_8)
        }
        return json.decodeFromString<LocalPlayerData>(text).also(::validate)
    }

    private fun preserveDamagedPrimary(): Boolean = runCatching {
        val sources = listOf(atomic.baseFile, File(atomic.baseFile.path + ".bak"), File(atomic.baseFile.path + ".new"))
            .filter { it.exists() }
        sources.forEach { source ->
            if (!source.isFile || source.length() > 64L * 1024 * 1024) throw IOException("Cannot preserve damaged progress.")
            val archive = AtomicFile(File(source.path + ".unreadable-" + UUID.randomUUID()))
            writeVerified(archive, source.readBytes())
        }
        true
    }.getOrDefault(false)

    private fun writeVerified(file: AtomicFile, bytes: ByteArray) {
        val stream = file.startWrite()
        try {
            stream.write(bytes)
            file.finishWrite(stream)
            // Some framework versions log file failures instead of throwing.
            val verified = file.openRead().use { input ->
                input.channel.size() == bytes.size.toLong() && input.readBytes().contentEquals(bytes)
            }
            if (!verified) throw IOException("The local save could not be verified.")
        } catch (error: Exception) {
            file.failWrite(stream)
            throw if (error is IOException) error else IOException("The local save failed.", error)
        }
    }

    private fun validate(saved: LocalPlayerData) {
        require(saved.schema == 1 && saved.playerId.length in 1..128 && saved.records.size <= MAX_RECORDS)
        fun validCell(cell: Cell): Boolean = cell.value in 0..9 && cell.notes in 0..511 &&
            cell.revision >= 0 && (cell.value == 0 || cell.notes == 0)
        fun validateProgress(solo: SoloProgress) {
            require(solo.id.isNotBlank() && solo.cells.size == 81 && solo.puzzle.clues.size == 81 && solo.elapsedMs >= 0 && solo.moves >= 0)
            require(solo.puzzle.clues.all { it in 0..9 } && Sudoku.conflicts(solo.puzzle.clues).isEmpty())
            require(solo.history.size <= 128 && solo.cells.all(::validCell) && solo.hintsUsed in 0..9999)
            require(solo.history.all { undo -> undo.cell in 0..80 && validCell(undo.before) &&
                undo.relatedBefore.size <= 20 && undo.relatedBefore.all { (i, previous) -> i in 0..80 && validCell(previous) } })
            require(solo.puzzle.clues.indices.all { i -> solo.puzzle.clues[i] == 0 || solo.cells[i].value == solo.puzzle.clues[i] })
            require(solo.completed == Sudoku.complete(solo.puzzle, solo.cells.map { it.value }))
        }
        saved.activeSolo?.let(::validateProgress)
        require(saved.dailyProgress.size <= DailyChallenge.MAX_SAVED_DAYS)
        require(saved.dailyProgress.map { it.date }.toSet().size == saved.dailyProgress.size)
        saved.dailyProgress.forEach { daily ->
            require(runCatching { java.time.LocalDate.parse(daily.date).toString() == daily.date }.getOrDefault(false))
            require(daily.solo.puzzle.difficulty == DailyChallenge.DIFFICULTY)
            validateProgress(daily.solo)
        }
        require(saved.records.all { it.id.isNotBlank() && it.elapsedMs >= 0 && it.moves >= 0 && it.hintsUsed in 0..9999 })
    }

    @Synchronized fun current(): LocalPlayerData = data

    @Synchronized fun update(transform: (LocalPlayerData) -> LocalPlayerData): LocalPlayerData {
        if (!canSave) throw IOException("Existing local progress needs recovery. It has not been replaced.")
        val next = transform(data)
        require(next.playerId == data.playerId)
        validate(next)
        if (next == data && !needsRepair) return data
        writeVerified(recovery, json.encodeToString(data).toByteArray(Charsets.UTF_8))
        writeVerified(atomic, json.encodeToString(next).toByteArray(Charsets.UTF_8))
        data = next; needsRepair = false
        return data
    }
}
