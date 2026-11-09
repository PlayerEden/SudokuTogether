package com.playereden.sudokutogether

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.playereden.sudokutogether.core.*
import kotlinx.serialization.json.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.InputStream
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ProgressBackupTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val settingsName = "progress-backup-test-${UUID.randomUUID()}"
    private lateinit var root: File
    private lateinit var preferences: SharedPreferences
    private val profileFile get() = File(root, "local-player.json")
    private val recentId = "standard-0123456789abcdef"
    private val requestedAppearance = Appearance("cobalt", "linen", "serif", "leaf", "woodland_cat")
    private val expectedAppearance = Appearance("cobalt", "classic", "serif", "leaf")

    @Before fun prepare() {
        root = File(context.cacheDir, settingsName).also { check(it.mkdirs()) }
        preferences = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE)
    }

    @After fun clean() {
        context.deleteSharedPreferences(settingsName)
        root.deleteRecursively()
    }

    @Test fun portableRestoreKeepsDestinationIdentityAndRoomsAndUsesImportedResultsForStyles() {
        val source = LocalPlayerStore(File(root, "source.json"))
        val incoming = source.update { fixture(it.playerId) }
        check(preferences.edit().putString("name", "River").putString("cosmeticInk", "cobalt")
            .putString("cosmeticBoard", "linen").putString("cosmeticFont", "serif")
            .putString("cosmeticEmblem", "leaf").putString("cosmeticPortrait", "woodland_cat")
            .putBoolean("uniformNumbers", true).putBoolean("touchFeedback", false)
            .putBoolean("autoCleanNotes", true).putBoolean("requireRoomCode", true)
            .putString("difficulty", "HARD").putString("recentPuzzleIds", recentId)
            .putString("lastAddress", "192.0.2.42:9876").putString("guestToken", "private-token").commit())
        val bytes = ProgressBackupStore(File(root, "source.json"), preferences).export(source, "0.11.0-dev4-qa")
        val text = bytes.toString(Charsets.UTF_8)
        for (privateValue in listOf(incoming.playerId, "playerId", "lastAddress", "192.0.2.42", "private-token")) {
            assertFalse("The portable file contains $privateValue", text.contains(privateValue))
        }

        val destination = LocalPlayerStore(profileFile)
        val original = destination.update { it.copy(records = (1..5).map { n -> solved("original-$n") }) }
        val rooms = listOf("host-session.json", "host-race-session.json", "guest-session.json")
            .map { File(root, it).also { file -> file.writeText("saved room ${file.name}") } }
        check(preferences.edit().putString("name", "Destination")
            .putString("lastAddress", "192.0.2.11:4321").putString("guestToken", "destination-token").commit())
        val untouchedProfile = profileFile.readBytes()
        val untouchedPreferences = preferences.all

        val exported = ProgressBackup.decode(bytes.inputStream())
        assertEquals(expectedAppearance, exported.settings.appearance)
        val unearnedRequest = Json.parseToJsonElement(text).jsonObject
            .withField(JsonPrimitive("linen"), "settings", "appearance", "board")
            .withField(JsonPrimitive("woodland_cat"), "settings", "appearance", "portrait")
        val preview = ProgressBackup.decode(unearnedRequest.toString().byteInputStream())
        assertEquals("0.11.0-dev4-qa", preview.appVersion)
        assertTrue(preview.hasSolo)
        assertEquals(1, preview.dailyCount)
        assertEquals(4, preview.recordCount)
        assertArrayEquals(untouchedProfile, profileFile.readBytes())
        assertEquals(untouchedPreferences, preferences.all)

        val restored = ProgressBackupStore(profileFile, preferences).restore(destination, preview)
        assertEquals(incoming.copy(playerId = original.playerId), restored)
        assertEquals(restored, LocalPlayerStore(profileFile).current())
        assertEquals("River", preferences.getString("name", null))
        assertEquals(expectedAppearance, readAppearance())
        assertTrue(preferences.getBoolean("uniformNumbers", false))
        assertFalse(preferences.getBoolean("touchFeedback", true))
        assertTrue(preferences.getBoolean("autoCleanNotes", false))
        assertTrue(preferences.getBoolean("requireRoomCode", false))
        assertEquals("HARD", preferences.getString("difficulty", null))
        assertEquals(recentId, preferences.getString("recentPuzzleIds", null))
        assertEquals("192.0.2.11:4321", preferences.getString("lastAddress", null))
        assertEquals("destination-token", preferences.getString("guestToken", null))
        rooms.forEach { assertEquals("saved room ${it.name}", it.readText()) }
    }

    @Test fun exportUsesCurrentDefaultsForRetiredStyleAndDifficultyPreferences() {
        val player = LocalPlayerStore(profileFile)
        check(preferences.edit().putString("cosmeticBoard", "old-tile-style")
            .putString("cosmeticEmblem", "forest").putString("difficulty", "EXPERT")
            .putString("recentPuzzleIds", "bad|$recentId").commit())

        val bytes = ProgressBackupStore(profileFile, preferences).export(player, "0.11.0-dev4-qa")
        val preview = ProgressBackup.decode(bytes.inputStream())
        assertEquals(Appearance(), preview.settings.appearance)
        assertEquals("EASY", preview.settings.difficulty)
        assertEquals(listOf(recentId), preview.settings.recentPuzzleIds)
    }

    @Test fun invalidSchemaTypesAndUnexpectedDataAreRejectedBeforeRestore() {
        val valid = backupJson()
        val malformed = listOf(
            valid.withField(JsonPrimitive("1"), "schema").toString(),
            valid.withField(JsonPrimitive(1), "settings", "name").toString(),
            valid.withField(JsonPrimitive("true"), "settings", "touchFeedback").toString(),
            valid.withField(JsonPrimitive("3"), "progress", "activeSolo", "moves").toString(),
            valid.withField(JsonPrimitive("192.0.2.42:9876"), "settings", "lastAddress").toString(),
            valid.withField(JsonPrimitive("imported-player"), "progress", "playerId").toString(),
            valid.withField(JsonArray(listOf(JsonPrimitive("192.0.2.42:9876"))), "settings", "recentPuzzleIds").toString(),
            valid.toString().replace("\"schema\":1", "\"schema\":1,\"schema\":1"),
            valid.toString() + "{}"
        )
        malformed.forEachIndexed { index, raw ->
            assertEquals("Malformed backup $index", ProgressBackupException.Reason.INVALID,
                assertThrows(ProgressBackupException::class.java) { ProgressBackup.decode(raw.byteInputStream()) }.reason)
        }
        val future = valid.withField(JsonPrimitive(2), "schema").toString()
        assertEquals(ProgressBackupException.Reason.UNSUPPORTED_VERSION,
            assertThrows(ProgressBackupException::class.java) { ProgressBackup.decode(future.byteInputStream()) }.reason)
        val brokenUtf8 = valid.toString().toByteArray() + byteArrayOf(0xc3.toByte(), 0x28)
        assertEquals(ProgressBackupException.Reason.INVALID,
            assertThrows(ProgressBackupException::class.java) { ProgressBackup.decode(brokenUtf8.inputStream()) }.reason)
    }

    @Test fun inputLargerThanTheBackupLimitIsRejectedWhileReading() {
        val input = object : InputStream() {
            private var remaining = ProgressBackup.MAX_BYTES + 1
            override fun read(): Int = if (remaining-- > 0) ' '.code else -1
            override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
                if (remaining <= 0) return -1
                val count = minOf(remaining, length)
                bytes.fill(' '.code.toByte(), offset, offset + count)
                remaining -= count
                return count
            }
        }
        assertEquals(ProgressBackupException.Reason.TOO_LARGE,
            assertThrows(ProgressBackupException::class.java) { ProgressBackup.decode(input) }.reason)
    }

    @Test fun impossibleUndoAndInconsistentCompletedRecordsAreRejected() {
        val valid = backupJson()
        val brokenUndo = valid.withField(JsonArray(listOf(JsonObject(mapOf(
            "cell" to JsonPrimitive(0), "before" to JsonObject(mapOf("value" to JsonPrimitive(99)))
        )))), "progress", "activeSolo", "history")
        val activeRecord = valid.getValue("progress").jsonObject.getValue("records").jsonArray[0].jsonObject
            .withField(JsonPrimitive("unfinished-solo"), "id")
        val badRecord = valid.withField(JsonArray(listOf(activeRecord)), "progress", "records")
        val duplicateRecord = valid.withField(JsonArray(listOf(activeRecord, activeRecord)), "progress", "records")
        listOf(brokenUndo, badRecord, duplicateRecord).forEach { data ->
            assertEquals(ProgressBackupException.Reason.INVALID,
                assertThrows(ProgressBackupException::class.java) { ProgressBackup.decode(data.toString().byteInputStream()) }.reason)
        }
    }

    @Test fun oldProgressWithDefaultedFieldsCanBeExportedAndRestored() {
        val valid = backupJson()
        val progress = valid.getValue("progress").jsonObject
        val solo = progress.getValue("activeSolo").jsonObject
        val legacyUndo = JsonObject(mapOf("cell" to JsonPrimitive(0), "before" to JsonObject(emptyMap())))
        val legacySolo = JsonObject(solo.withField(JsonArray(listOf(legacyUndo)), "history") -
            setOf("checksUsed", "hintsUsed", "usedNoteAssist"))
        val legacy = valid.withField(legacySolo, "progress", "activeSolo")
        val backup = ProgressBackup.decode(legacy.toString().byteInputStream())
        val destination = LocalPlayerStore(profileFile)
        val originalId = destination.current().playerId
        ProgressBackupStore(profileFile, preferences).restore(destination, backup)
        val reloaded = LocalPlayerStore(profileFile).current()
        val restoredSolo = requireNotNull(reloaded.activeSolo)
        assertEquals(originalId, reloaded.playerId)
        assertEquals(listOf(SoloUndo(0, Cell())), restoredSolo.history)
        assertEquals(0, restoredSolo.checksUsed)
    }

    @Test fun preferencesWriteFailureRollsBackTheProfileAndExactSettings() {
        val player = LocalPlayerStore(profileFile)
        val original = player.update { it.copy(records = listOf(solved("old-solve"))) }
        check(preferences.edit().putString("name", "Before").putBoolean("touchFeedback", true)
            .putString("lastAddress", "192.0.2.10:9000").commit())
        val originalPreferences = preferences.all
        val failing = onFirstPreferenceCommit { false }

        val error = assertThrows(ProgressBackupException::class.java) {
            ProgressBackupStore(profileFile, failing).restore(player, incomingPreview())
        }

        assertEquals(ProgressBackupException.Reason.STORAGE, error.reason)
        assertEquals(original, player.current())
        assertEquals(original, LocalPlayerStore(profileFile).current())
        assertEquals(originalPreferences, preferences.all)
        assertFalse(ProgressBackupStore(profileFile, preferences).recoverPendingRestore(player))
        profileFile.writeText("broken primary after rollback")
        assertEquals(original, LocalPlayerStore(profileFile).current())
    }

    @Test fun interruptionRollsBackBothStoresOnNextOpen() {
        val player = LocalPlayerStore(profileFile)
        val original = player.update { it.copy(records = listOf(solved("original-solve"))) }
        check(preferences.edit().putString("name", "Before interruption").commit())
        val originalPreferences = preferences.all
        val interrupted = onFirstPreferenceCommit { throw TestInterruption() }

        assertThrows(TestInterruption::class.java) {
            ProgressBackupStore(profileFile, interrupted).restore(player, incomingPreview())
        }
        assertEquals("River", preferences.getString("name", null))
        val reopened = LocalPlayerStore(profileFile)
        assertEquals("unfinished-solo", reopened.current().activeSolo?.id)

        val backupStore = ProgressBackupStore(profileFile, preferences)
        assertTrue(backupStore.recoverPendingRestore(reopened))
        assertEquals(original, reopened.current())
        assertEquals(original, LocalPlayerStore(profileFile).current())
        assertEquals(originalPreferences, preferences.all)
        assertFalse(backupStore.recoverPendingRestore(reopened))
    }

    private fun fixture(playerId: String): LocalPlayerData {
        val digits = "123456789456789123789123456234567891567891234891234567345678912678912345912345678"
        val puzzle = Puzzle(recentId, digits.mapIndexed { i, digit -> if (i < 2) 0 else digit.digitToInt() }, "EASY")
        val solo = PersonalPlay.newSolo(puzzle, 1_000L).copy(id = "unfinished-solo", moves = 3, elapsedMs = 14_000L,
            hintsUsed = 1, usedNoteAssist = true, checksUsed = 1,
            cells = puzzle.clues.map { Cell(it) }.toMutableList().also {
                it[0] = Cell(value = 1, revision = 3)
                it[1] = Cell(notes = 2, revision = 3)
            }, history = listOf(SoloUndo(0, Cell(notes = 1, revision = 1), mapOf(1 to Cell(notes = 3, revision = 2)))))
        return LocalPlayerData(playerId = playerId, activeSolo = solo,
            dailyProgress = listOf(DailyProgress("2026-07-11",
                PersonalPlay.newSolo(DailyChallenge.puzzle("2026-07-11"), 1_000L).copy(id = "unfinished-daily"))),
            records = listOf(solved("solo-record"), solved("daily-record").copy(mode = DAILY, difficulty = "EASY", dailyDate = "2026-07-12"),
                solved("session-one:epoch-one:host").copy(mode = COOP),
                solved("session-two:epoch-two:guest").copy(mode = VERSUS, result = "WIN")))
    }

    @Test fun ambiguousSoloAndMismatchedDailyBoardsAreRejectedBeforePreview() {
        val json = backupJson()
        val blankClues = JsonArray(List(81) { JsonPrimitive(0) })
        val ambiguous = json.withField(blankClues, "progress", "activeSolo", "puzzle", "clues")
        val daily = json.getValue("progress").jsonObject.getValue("dailyProgress").jsonArray.single().jsonObject
        val wrongDate = json.withField(JsonArray(listOf(daily.withField(JsonPrimitive("2026-07-13"), "date"))),
            "progress", "dailyProgress")
        for (invalid in listOf(ambiguous, wrongDate)) {
            val error = assertThrows(ProgressBackupException::class.java) {
                ProgressBackup.decode(invalid.toString().byteInputStream())
            }
            assertEquals(ProgressBackupException.Reason.INVALID, error.reason)
        }
    }

    private fun solved(id: String) = GameRecord(id, SOLO, "SOLVED", recentId, 30_000L, 20, 31_000L)

    private fun backupJson() = Json.parseToJsonElement(incomingBytes().toString(Charsets.UTF_8)).jsonObject

    private fun incomingBytes() = ProgressBackup.encode(fixture("source-installation"),
        BackupSettings(name = "River", appearance = requestedAppearance, recentPuzzleIds = listOf(recentId)), "0.11.0-dev4-qa", 42L)

    private fun incomingPreview() = ProgressBackup.decode(incomingBytes().inputStream())

    private fun readAppearance() = Appearance(
        preferences.getString("cosmeticInk", "")!!, preferences.getString("cosmeticBoard", "")!!,
        preferences.getString("cosmeticFont", "")!!, preferences.getString("cosmeticEmblem", "")!!,
        preferences.getString("cosmeticPortrait", "")!!
    )

    private fun JsonObject.withField(value: JsonElement, vararg path: String): JsonObject {
        val name = path.first()
        val nested = if (path.size == 1) value else getValue(name).jsonObject.withField(value, *path.drop(1).toTypedArray())
        return JsonObject(this + (name to nested))
    }

    private fun onFirstPreferenceCommit(after: () -> Boolean): SharedPreferences = object : SharedPreferences by preferences {
        private var first = true
        override fun edit(): SharedPreferences.Editor {
            val real = preferences.edit()
            return object : SharedPreferences.Editor by real {
                override fun commit(): Boolean {
                    check(real.commit())
                    if (!first) return true
                    first = false
                    return after()
                }
            }
        }
    }

    private class TestInterruption : Error()
}
