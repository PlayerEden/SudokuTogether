package com.playereden.sudokutogether

import android.util.AtomicFile
import com.playereden.sudokutogether.core.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Offline-only profile. An AtomicFile commit contains both the finished game
 * and its record, so interruption cannot count one completion twice.
 */
class LocalPlayerStore(file: File) {
    private val atomic = AtomicFile(file)
    private val json = Json { encodeDefaults = true }
    val recoveryWarning: String?
    private var data: LocalPlayerData

    init {
        val attempt = runCatching { load() }
        if (attempt.isSuccess) {
            data = attempt.getOrThrow()
            recoveryWarning = null
        } else {
            // Keep a copy of unreadable data before allowing new offline play.
            val source = atomic.baseFile
            runCatching {
                if (source.isFile) source.copyTo(File(source.path + ".unreadable"), overwrite = true)
            }
            data = LocalPlayerData()
            recoveryWarning = "Your old local records could not be read. An unreadable copy was kept in private app storage."
        }
    }

    private fun load(): LocalPlayerData {
        if (!atomic.baseFile.exists() && !File(atomic.baseFile.path + ".bak").exists()) return LocalPlayerData()
        val saved = json.decodeFromString<LocalPlayerData>(atomic.openRead().use { it.readBytes().toString(Charsets.UTF_8) })
        require(saved.schema == 1 && saved.playerId.isNotBlank() && saved.records.size <= MAX_RECORDS)
        saved.activeSolo?.let { solo ->
            require(solo.cells.size == 81 && solo.puzzle.clues.size == 81 && solo.elapsedMs >= 0)
            require(solo.history.size <= 128 && solo.cells.all { it.value in 0..9 && it.notes in 0..511 })
            require(solo.puzzle.clues.indices.all { index -> solo.puzzle.clues[index] == 0 || solo.cells[index].value == solo.puzzle.clues[index] })
            require(solo.completed == Sudoku.complete(solo.puzzle, solo.cells.map { it.value }))
        }
        return saved
    }

    @Synchronized fun current(): LocalPlayerData = data

    @Synchronized fun update(transform: (LocalPlayerData) -> LocalPlayerData): LocalPlayerData {
        val next = transform(data)
        require(next.schema == 1 && next.playerId == data.playerId)
        val stream = atomic.startWrite()
        try {
            stream.write(json.encodeToString(next).toByteArray(Charsets.UTF_8))
            atomic.finishWrite(stream)
            data = next
        } catch (error: Exception) {
            atomic.failWrite(stream)
            throw error
        }
        return data
    }
}
