package com.playereden.sudokutogether

import android.content.SharedPreferences
import android.system.Os
import android.system.OsConstants
import android.util.AtomicFile
import com.playereden.sudokutogether.core.Appearance
import com.playereden.sudokutogether.core.Cosmetic
import com.playereden.sudokutogether.core.Cosmetics
import com.playereden.sudokutogether.core.GameRecord
import com.playereden.sudokutogether.core.LocalPlayerData
import com.playereden.sudokutogether.core.PuzzleSupply
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import java.io.File
import java.io.IOException

/** Run on the same worker as the other local save operations, with settings/room actions paused. */
internal class ProgressBackupStore(
    profileFile: File, private val preferences: SharedPreferences, private val settingsFile: File? = null
) {
    private val pending = File(profileFile.path + ".restore")
    private val staging = AtomicFile(File(pending.path + ".staging"))
    private val committed = File(pending.path + ".committed")
    private val journalDirectory = pending.absoluteFile.parentFile ?: error("Missing restore directory.")

    fun export(player: LocalPlayerStore, appVersion: String): ByteArray = synchronized(player) {
        storageOperation {
            checkReady(player)
            val profile = player.current()
            ProgressBackup.encode(profile, readSettings(profile.records), appVersion)
        }
    }

    fun restore(player: LocalPlayerStore, preview: BackupPreview): LocalPlayerData = synchronized(player) {
        storageOperation {
            checkReady(player)
            val previous = player.current()
            val snapshot = RecoverySnapshot(JOURNAL_SCHEMA, previous.playerId, BackupProgress(previous), preferencesSnapshot())
            val next = preview.progress.forInstallation(previous.playerId)
            val newSettings = preview.settings.copy(
                appearance = Cosmetics.accessible(preview.settings.appearance, next.records)
            ).preferenceValues()
            prepareJournal(snapshot)
            try {
                player.replaceForRestore(next, rollback = false)
                writePreferences(newSettings)
                syncStores()
                Os.rename(pending.path, committed.path)
            } catch (failure: Exception) {
                try {
                    rollback(player, snapshot)
                } catch (recoveryFailure: Exception) {
                    failure.addSuppressed(recoveryFailure)
                    throw ProgressBackupException(ProgressBackupException.Reason.RECOVERY_REQUIRED, failure)
                }
                throw ProgressBackupException(ProgressBackupException.Reason.STORAGE, failure)
            }
            // The rename is the commit point. If its durability is uncertain, leave the evidence for startup.
            try { syncDirectory(journalDirectory) } catch (failure: Exception) {
                throw ProgressBackupException(ProgressBackupException.Reason.RECOVERY_REQUIRED, failure)
            }
            discardCommitted()
            player.current()
        }
    }

    /** Call before publishing the profile or settings on startup. A journal is never exported. */
    fun recoverPendingRestore(player: LocalPlayerStore): Boolean = synchronized(player) {
        try {
            if (pending.exists()) {
                rollback(player, readJournal(pending))
                discardCommitted()
                true
            } else {
                if (committed.exists()) {
                    // Only a verified pair can have reached the committed name.
                    player.verifyRestoredProfile()
                    preferencesSnapshot()
                    syncDirectory(journalDirectory)
                    discardCommitted()
                }
                false
            }
        } catch (failure: Exception) {
            throw ProgressBackupException(ProgressBackupException.Reason.RECOVERY_REQUIRED, failure)
        }
    }

    private fun checkReady(player: LocalPlayerStore) {
        if (pending.exists()) throw ProgressBackupException(ProgressBackupException.Reason.RECOVERY_REQUIRED)
        if (!player.canSave) throw ProgressBackupException(ProgressBackupException.Reason.STORAGE)
    }

    private fun prepareJournal(snapshot: RecoverySnapshot) {
        try {
            writeJournal(snapshot)
        } catch (failure: Exception) {
            // Neither the profile nor preferences has been touched if journal preparation fails.
            try {
                deleteJournal()
                syncDirectory(journalDirectory)
            } catch (cleanupFailure: Exception) {
                failure.addSuppressed(cleanupFailure)
                throw ProgressBackupException(ProgressBackupException.Reason.RECOVERY_REQUIRED, failure)
            }
            throw failure
        }
    }

    private fun writeJournal(snapshot: RecoverySnapshot) {
        val bytes = Json.encodeToString(snapshot).toByteArray(Charsets.UTF_8)
        require(bytes.size <= JOURNAL_MAX_BYTES)
        // Android's older AtomicFile writes the first base file directly. Only publish a
        // verified staging file, so a torn initial write cannot be mistaken for recovery.
        writeVerifiedAtomic(staging, bytes)
        if (readJournal(staging.baseFile) != snapshot) throw IOException("The restore recovery snapshot could not be verified.")
        Os.rename(staging.baseFile.path, pending.path)
        syncDirectory(journalDirectory)
    }

    private fun readJournal(file: File): RecoverySnapshot {
        val text = file.inputStream().use { it.readBackupBytes(JOURNAL_MAX_BYTES) }.decodeToString(throwOnInvalidSequence = true)
        return Json.decodeFromString<RecoverySnapshot>(text).also { snapshot ->
            require(snapshot.schema == JOURNAL_SCHEMA)
            validateLocalPlayerData(snapshot.progress.forInstallation(snapshot.playerId))
            require(snapshot.preferences.all { (key, value) ->
                val expected = defaultPreferences[key] ?: return@all false
                if (expected.isString) value.isString else !value.isString && value.booleanOrNull != null
            })
        }
    }

    private fun rollback(player: LocalPlayerStore, snapshot: RecoverySnapshot) {
        // Refresh the last-good profile as well, so it cannot resurrect the rejected import.
        player.replaceForRestore(snapshot.progress.forInstallation(snapshot.playerId), rollback = true)
        writePreferences(snapshot.preferences)
        syncStores()
        deleteJournal()
        syncDirectory(journalDirectory)
    }

    private fun syncStores() {
        settingsFile?.let { file ->
            if (File(file.path + ".bak").exists() || (!file.isFile && preferences.all.isNotEmpty())) {
                throw IOException("Restored settings were not committed on disk.")
            }
            file.parentFile?.takeIf(File::isDirectory)?.let(::syncDirectory)
        }
        syncDirectory(journalDirectory)
    }

    private fun syncDirectory(directory: File) {
        val fd = Os.open(directory.path, OsConstants.O_RDONLY, 0)
        try { Os.fsync(fd) } finally { Os.close(fd) }
    }

    private fun deleteJournal() {
        pending.delete()
        if (pending.exists()) throw IOException("Could not clear the restore recovery snapshot.")
        runCatching { staging.delete() }
    }

    private fun discardCommitted() { runCatching { committed.delete() } }

    private fun preferencesSnapshot(): Map<String, JsonPrimitive> {
        val all = preferences.all
        return buildMap {
            for ((key, expected) in defaultPreferences) {
                if (!all.containsKey(key)) continue
                val value = all[key]
                put(key, when {
                    expected.isString && value is String -> JsonPrimitive(value)
                    !expected.isString && value is Boolean -> JsonPrimitive(value)
                    else -> throw IOException("An existing progress preference is unreadable.")
                })
            }
        }
    }

    private fun writePreferences(values: Map<String, JsonPrimitive>) {
        val editor = preferences.edit()
        for ((key, expected) in defaultPreferences) {
            val value = values[key]
            when {
                value == null -> editor.remove(key)
                expected.isString -> editor.putString(key, value.content)
                else -> editor.putBoolean(key, value.content.toBooleanStrict())
            }
        }
        // commit() waits for the disk write, then reload the values the app will actually use.
        if (!editor.commit() || preferencesSnapshot() != values) throw IOException("Restored settings could not be reloaded.")
    }

    private fun readSettings(records: List<GameRecord>): BackupSettings {
        val values = defaultPreferences + preferencesSnapshot()
        fun value(key: String) = values.getValue(key).content
        fun cosmetic(key: String, group: List<Cosmetic>): String =
            value(key).takeIf { id -> group.any { it.id == id } } ?: defaultPreferences.getValue(key).content
        val appearance = Appearance(cosmetic("cosmeticInk", Cosmetics.inks), cosmetic("cosmeticBoard", Cosmetics.boards),
            cosmetic("cosmeticFont", Cosmetics.fonts), cosmetic("cosmeticEmblem", Cosmetics.emblems),
            cosmetic("cosmeticPortrait", Cosmetics.portraits))
        return BackupSettings(
            name = cleanPlayerName(value("name")),
            appearance = Cosmetics.accessible(appearance, records),
            uniformNumbers = value("uniformNumbers").toBooleanStrict(),
            touchFeedback = value("touchFeedback").toBooleanStrict(),
            autoCleanNotes = value("autoCleanNotes").toBooleanStrict(),
            requireRoomCode = value("requireRoomCode").toBooleanStrict(),
            difficulty = value("difficulty").takeIf(ProgressBackup::isDifficulty) ?: "EASY",
            recentPuzzleIds = value("recentPuzzleIds").split('|').filter(ProgressBackup::isRecentPuzzleId)
                .asReversed().distinct().take(PuzzleSupply.RECENT_LIMIT).asReversed()
        )
    }

    private inline fun <T> storageOperation(block: () -> T): T = try {
        block()
    } catch (failure: ProgressBackupException) {
        throw failure
    } catch (failure: Exception) {
        throw ProgressBackupException(ProgressBackupException.Reason.STORAGE, failure)
    }

    @Serializable
    private data class RecoverySnapshot(
        val schema: Int, val playerId: String, val progress: BackupProgress, val preferences: Map<String, JsonPrimitive>
    )

    private companion object {
        const val JOURNAL_SCHEMA = 1
        const val JOURNAL_MAX_BYTES = LOCAL_PROFILE_MAX_BYTES.toInt() + 1024 * 1024
        val defaultPreferences = BackupSettings().preferenceValues()

        fun BackupSettings.preferenceValues() = mapOf(
            "name" to JsonPrimitive(name),
            "cosmeticInk" to JsonPrimitive(appearance.ink),
            "cosmeticBoard" to JsonPrimitive(appearance.board),
            "cosmeticFont" to JsonPrimitive(appearance.font),
            "cosmeticEmblem" to JsonPrimitive(appearance.emblem),
            "cosmeticPortrait" to JsonPrimitive(appearance.portrait),
            "uniformNumbers" to JsonPrimitive(uniformNumbers),
            "touchFeedback" to JsonPrimitive(touchFeedback),
            "autoCleanNotes" to JsonPrimitive(autoCleanNotes),
            "requireRoomCode" to JsonPrimitive(requireRoomCode),
            "difficulty" to JsonPrimitive(difficulty),
            "recentPuzzleIds" to JsonPrimitive(recentPuzzleIds.joinToString("|"))
        )
    }
}
