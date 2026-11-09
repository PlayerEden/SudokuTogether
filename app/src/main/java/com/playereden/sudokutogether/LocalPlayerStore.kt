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
    var recoveryWarning: String?
        private set
    var canSave: Boolean
        private set
    private var needsRepair = false
    private var data: LocalPlayerData

    init {
        // AtomicFile.openRead reconciles/deletes sidecars, even before decoding.
        // Inspect committed candidates without changing any source bytes first.
        val primary = runCatching { read(atomic) }.getOrNull()
        val backup = runCatching { read(recovery) }.getOrNull()
        val loaded = primary ?: backup
        if (loaded != null) {
            data = loaded
            val preserve = buildList {
                if (primary == null || hasSidecars(atomic)) addAll(sources(atomic))
                if (backup == null || hasSidecars(recovery)) addAll(sources(recovery))
            }
            canSave = preserveSources(preserve)
            needsRepair = primary == null || preserve.isNotEmpty()
            recoveryWarning = when {
                !canSave -> "Saved progress was recovered, but saving is paused because the damaged file could not be preserved. Check phone storage and reopen the app."
                primary == null -> "Your last healthy local backup was recovered. Recent changes may be missing."
                hasSidecars(atomic) -> "An interrupted local save was recovered. Recent changes may be missing."
                else -> null
            }
        } else if (!exists(atomic) && !exists(recovery)) {
            data = LocalPlayerData(); canSave = true; recoveryWarning = null
        } else {
            // Read-only placeholder. No source file has been reconciled or reset.
            data = LocalPlayerData(); canSave = false
            recoveryWarning = "Local progress could not be read. Your existing files are preserved and saving is paused. Reopen the app after checking phone storage."
        }
    }

    private fun sources(file: AtomicFile): List<File> =
        listOf(file.baseFile, File(file.baseFile.path + ".bak"), File(file.baseFile.path + ".new")).filter { it.exists() }
    private fun exists(file: AtomicFile): Boolean = sources(file).isNotEmpty()
    private fun hasSidecars(file: AtomicFile): Boolean = sources(file).any { it != file.baseFile }

    private fun read(file: AtomicFile): LocalPlayerData? {
        if (!exists(file)) return null
        // Legacy .bak is the authoritative pre-transaction save; .new is never committed.
        val legacyBackup = File(file.baseFile.path + ".bak")
        val candidate = if (legacyBackup.exists()) legacyBackup else file.baseFile
        val text = candidate.inputStream().use {
            if (it.channel.size() > LOCAL_PROFILE_MAX_BYTES) throw IOException("Local progress is too large to read safely.")
            it.readBytes().toString(Charsets.UTF_8)
        }
        return json.decodeFromString<LocalPlayerData>(text).also(::validateLocalPlayerData)
    }

    private fun preserveSources(sources: List<File>): Boolean = runCatching {
        sources.forEach { source ->
            if (!source.isFile || source.length() > LOCAL_PROFILE_MAX_BYTES) throw IOException("Cannot preserve damaged progress.")
            val archive = AtomicFile(File(source.path + ".unreadable-" + UUID.randomUUID()))
            writeVerifiedAtomic(archive, source.readBytes())
        }
        true
    }.getOrDefault(false)

    @Synchronized fun current(): LocalPlayerData = data

    @Synchronized fun update(transform: (LocalPlayerData) -> LocalPlayerData): LocalPlayerData {
        if (!canSave) throw IOException("Existing local progress needs recovery. It has not been replaced.")
        val next = transform(data)
        require(next.playerId == data.playerId)
        validateLocalPlayerData(next)
        if (next == data && !needsRepair) return data
        writeProfile(recovery, data)
        writeProfile(atomic, next)
        data = next; needsRepair = false
        return data
    }

    @Synchronized internal fun replaceForRestore(next: LocalPlayerData, rollback: Boolean) {
        if (!canSave && (!rollback || !preserveSources(sources(atomic) + sources(recovery)))) {
            throw IOException("Existing local progress needs recovery. It has not been replaced.")
        }
        // Only replaying our private journal can recover an identity that had not yet been saved.
        require(rollback || next.playerId == data.playerId)
        validateLocalPlayerData(next)
        // The separate transaction journal protects the previous pair. Both normal
        // profile copies must agree with the settings after the restore commits.
        writeProfile(recovery, next)
        writeProfile(atomic, next)
        if (read(atomic) != next || read(recovery) != next) throw IOException("Restored progress could not be reloaded.")
        data = next
        needsRepair = false
        canSave = true
        recoveryWarning = null
    }

    @Synchronized internal fun verifyRestoredProfile() {
        val persisted = runCatching { read(atomic) }.getOrNull() ?: read(recovery)
        if (!canSave || persisted != data) throw IOException("Restored progress could not be reloaded.")
    }

    private fun writeProfile(file: AtomicFile, saved: LocalPlayerData) =
        writeVerifiedAtomic(file, json.encodeToString(saved).toByteArray(Charsets.UTF_8))
}

internal const val LOCAL_PROFILE_MAX_BYTES = 64L * 1024 * 1024

internal fun writeVerifiedAtomic(file: AtomicFile, bytes: ByteArray) {
    val stream = file.startWrite()
    try {
        stream.write(bytes)
        stream.fd.sync()
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

internal fun validateLocalPlayerData(saved: LocalPlayerData) {
    require(saved.schema == 1 && saved.playerId.length in 1..128 && saved.records.size <= MAX_RECORDS)
    fun validCell(cell: Cell): Boolean = cell.value in 0..9 && cell.notes in 0..511 &&
        cell.revision >= 0 && (cell.value == 0 || cell.notes == 0)
    fun validateProgress(solo: SoloProgress) {
        require(solo.id.isNotBlank() && solo.cells.size == 81 && solo.puzzle.clues.size == 81 && solo.elapsedMs >= 0 && solo.moves >= 0)
        require(solo.puzzle.clues.all { it in 0..9 } && Sudoku.conflicts(solo.puzzle.clues).isEmpty())
        require(solo.history.size <= 128 && solo.cells.all { validCell(it) && it.revision <= solo.moves } &&
            solo.hintsUsed in 0..9999 && solo.checksUsed in 0..9999)
        require(solo.history.all { undo ->
            undo.cell in 0..80 && solo.puzzle.clues[undo.cell] == 0 && validCell(undo.before) &&
                undo.before.revision < solo.moves && undo.relatedBefore.size <= 20 &&
                undo.relatedBefore.all { (i, previous) ->
                    i in 0..80 && i != undo.cell && solo.puzzle.clues[i] == 0 &&
                        SudokuAssistance.isPeer(i, undo.cell) && validCell(previous) &&
                        previous.value == 0 && previous.revision < solo.moves
                }
        })
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
    require(saved.records.all { it.id.isNotBlank() && it.elapsedMs >= 0 && it.moves >= 0 &&
        it.hintsUsed in 0..9999 && it.checksUsed in 0..9999 && (it.checksUsed == 0 || it.assisted) })
}
