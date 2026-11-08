package com.playereden.sudokutogether

import com.playereden.sudokutogether.core.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate

internal class ProgressBackupException(val reason: Reason, cause: Throwable? = null) : IOException(
    when (reason) {
        Reason.TOO_LARGE -> "This backup is too large to read safely."
        Reason.INVALID -> "This file is not a valid GridMates progress backup."
        Reason.UNSUPPORTED_VERSION -> "This GridMates backup uses an unsupported format version."
        Reason.STORAGE -> "The backup could not be read or saved."
        Reason.RECOVERY_REQUIRED -> "A previous restore needs recovery before progress can be changed."
    }, cause
) {
    enum class Reason { TOO_LARGE, INVALID, UNSUPPORTED_VERSION, STORAGE, RECOVERY_REQUIRED }
}

@Serializable
internal data class BackupSettings(
    @Required val name: String = "Player",
    @Required val appearance: Appearance = Appearance(),
    @Required val uniformNumbers: Boolean = false,
    @Required val touchFeedback: Boolean = true,
    @Required val autoCleanNotes: Boolean = false,
    @Required val requireRoomCode: Boolean = false,
    @Required val difficulty: String = "EASY",
    @Required val recentPuzzleIds: List<String> = emptyList()
)

@Serializable
internal data class BackupProgress(
    val activeSolo: SoloProgress?, val dailyProgress: List<DailyProgress>, val records: List<GameRecord>
) {
    constructor(profile: LocalPlayerData) : this(profile.activeSolo, profile.dailyProgress, profile.records)
    fun forInstallation(playerId: String) = LocalPlayerData(
        playerId = playerId, activeSolo = activeSolo, dailyProgress = dailyProgress, records = records
    )
}

internal class BackupPreview internal constructor(
    val appVersion: String,
    val exportedAtMs: Long,
    internal val progress: BackupProgress,
    internal val settings: BackupSettings
) {
    val hasSolo get() = progress.activeSolo != null
    val dailyCount get() = progress.dailyProgress.size
    val recordCount get() = progress.records.size
}

@Serializable
private data class BackupEnvelope(
    val format: String, val schema: Int, val exportedAtMs: Long, val appVersion: String,
    val progress: BackupProgress, val settings: BackupSettings
)

@OptIn(ExperimentalSerializationApi::class)
internal object ProgressBackup {
    private const val FORMAT = "gridmates-sudoku-progress"
    private const val SCHEMA = 1
    // Includes the full game retention limits, even when Daily boards have long undo histories.
    const val MAX_BYTES = 32 * 1024 * 1024
    private const val MAX_ELAPSED_MS = Long.MAX_VALUE / MAX_RECORDS
    private val difficulties = setOf("EASY", "MEDIUM", "HARD")
    private val identifier = Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,127}")
    private val recordIdentifier = Regex("[A-Za-z0-9][A-Za-z0-9_:-]{0,255}")
    private val appVersionPattern = Regex("[A-Za-z0-9][A-Za-z0-9._+-]{0,63}")
    private val recentPuzzleId = Regex("standard-[a-f0-9]{16}")

    fun isDifficulty(value: String) = value in difficulties
    fun isRecentPuzzleId(value: String) = recentPuzzleId.matches(value)

    fun encode(
        profile: LocalPlayerData, settings: BackupSettings, appVersion: String,
        exportedAtMs: Long = System.currentTimeMillis()
    ): ByteArray = invalidOnFailure {
        require(profile.schema == 1)
        val payload = BackupEnvelope(FORMAT, SCHEMA, exportedAtMs, appVersion, BackupProgress(profile), settings)
        validate(payload)
        val bytes = ByteArrayOutputStream()
        Json.encodeToStream(payload, bytes)
        if (bytes.size() > MAX_BYTES) throw ProgressBackupException(ProgressBackupException.Reason.TOO_LARGE)
        bytes.toByteArray()
    }

    fun decode(input: InputStream): BackupPreview {
        val bytes = try {
            input.readBackupBytes(MAX_BYTES)
        } catch (error: ProgressBackupException) {
            throw error
        } catch (error: IOException) {
            throw ProgressBackupException(ProgressBackupException.Reason.STORAGE, error)
        }
        return invalidOnFailure {
            fun reader() = InputStreamReader(bytes.inputStream(), Charsets.UTF_8.newDecoder())
            val (format, schema) = StrictBackupJson.header(reader())
            require(format == FORMAT && schema > 0)
            if (schema != SCHEMA) throw ProgressBackupException(ProgressBackupException.Reason.UNSUPPORTED_VERSION)
            StrictBackupJson.validate(reader(), BackupEnvelope.serializer().descriptor)
            val backup = Json.decodeFromStream<BackupEnvelope>(bytes.inputStream())
            validate(backup)
            BackupPreview(backup.appVersion, backup.exportedAtMs, backup.progress, backup.settings)
        }
    }

    private fun validate(backup: BackupEnvelope) {
        require(backup.exportedAtMs >= 0 && appVersionPattern.matches(backup.appVersion))
        validateLocalPlayerData(backup.progress.forInstallation("portable"))
        val settings = backup.settings
        require(settings.name.length <= 24 && settings.name.none { it.isISOControl() } &&
            Charsets.UTF_8.newEncoder().canEncode(settings.name))
        require(Cosmetics.valid(settings.appearance) && isDifficulty(settings.difficulty))
        require(settings.recentPuzzleIds.size <= PuzzleSupply.RECENT_LIMIT &&
            settings.recentPuzzleIds.toSet().size == settings.recentPuzzleIds.size &&
            settings.recentPuzzleIds.all(::isRecentPuzzleId))

        val records = backup.progress.records.associateBy { it.id }
        require(records.size == backup.progress.records.size)
        for (record in records.values) {
            require(recordIdentifier.matches(record.id) && identifier.matches(record.puzzleId) &&
                record.elapsedMs <= MAX_ELAPSED_MS &&
                (record.hintsUsed == 0 || record.assisted) &&
                (record.difficulty == null || record.difficulty in difficulties))
            require(when (record.mode) {
                SOLO, COOP -> record.result == "SOLVED" && record.dailyDate == null
                VERSUS -> record.result in listOf("WIN", "LOSS", "DRAW") && record.dailyDate == null
                DAILY -> record.result == "SOLVED" && record.difficulty == DailyChallenge.DIFFICULTY &&
                    record.dailyDate?.let { LocalDate.parse(it).toString() == it } == true
                else -> false
            })
        }

        val progressIds = mutableSetOf<String>()
        fun validateBoard(solo: SoloProgress, date: String?) {
            require(identifier.matches(solo.id) && progressIds.add(solo.id) && identifier.matches(solo.puzzle.id))
            require(solo.moves < Int.MAX_VALUE && solo.elapsedMs <= MAX_ELAPSED_MS &&
                (solo.puzzle.difficulty == null || solo.puzzle.difficulty in difficulties))
            require(solo.cells.all { it.owner == null } && solo.history.all { undo ->
                undo.before.owner == null && undo.relatedBefore.values.all { it.owner == null }
            })
            if (date == null) {
                require(Sudoku.hasUniqueSolutionWithin(solo.puzzle.clues, maxNodes = 100_000))
            } else {
                require(solo.puzzle == DailyChallenge.puzzle(date))
            }
            records[solo.id]?.let { record ->
                require(solo.completed && record.mode == (if (date == null) SOLO else DAILY) &&
                    record.dailyDate == date && record.puzzleId == solo.puzzle.id && record.elapsedMs == solo.elapsedMs &&
                    record.moves == solo.moves && record.finishedAtMs == solo.finishedAtMs && record.difficulty == solo.puzzle.difficulty &&
                    record.assisted == solo.assisted && record.hintsUsed == solo.hintsUsed && record.checksUsed == solo.checksUsed)
            }
        }
        backup.progress.activeSolo?.let { validateBoard(it, null) }
        backup.progress.dailyProgress.forEach { validateBoard(it.solo, it.date) }
    }

    private inline fun <T> invalidOnFailure(block: () -> T): T = try {
        block()
    } catch (error: ProgressBackupException) {
        throw error
    } catch (error: Exception) {
        // Malformed syntax, fields, types, dates or UTF-8 all fail before preview.
        throw ProgressBackupException(ProgressBackupException.Reason.INVALID, error)
    }
}

internal fun InputStream.readBackupBytes(limit: Int): ByteArray {
    val bytes = ByteArrayOutputStream()
    val chunk = ByteArray(8192)
    while (true) {
        val count = read(chunk, 0, minOf(chunk.size, limit - bytes.size() + 1))
        if (count < 0) return bytes.toByteArray()
        if (count == 0) {
            val next = read()
            if (next < 0) return bytes.toByteArray()
            if (bytes.size() == limit) throw ProgressBackupException(ProgressBackupException.Reason.TOO_LARGE)
            bytes.write(next)
        } else {
            if (count > limit - bytes.size()) throw ProgressBackupException(ProgressBackupException.Reason.TOO_LARGE)
            bytes.write(chunk, 0, count)
        }
    }
}
