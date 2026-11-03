package com.playereden.sudokutogether

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.playereden.sudokutogether.core.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class LocalPlayerStoreTest {
    private lateinit var root: File
    private val json = Json { encodeDefaults = true }
    @Before fun prepare() {
        root = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "profile-test-${UUID.randomUUID()}")
        check(root.mkdirs())
    }
    @After fun clean() { root.setWritable(true, true); root.deleteRecursively() }
    private fun target() = File(root, "player.json")
    private fun saved(): LocalPlayerData {
        val digits = "123456789456789123789123456234567891567891234891234567345678912678912345912345678".map { it.digitToInt() }
        val puzzle = Puzzle("recovery-fixture", digits.mapIndexed { i, n -> if (i == 0) 0 else n }, "EASY")
        val solo = PersonalPlay.newSolo(puzzle, 1000L).copy(cells = puzzle.clues.map { Cell(it) }.toMutableList().also {
            it[0] = Cell(notes = 4, revision = 1)
        }, moves = 1, elapsedMs = 1200L, hintsUsed = 1, history = listOf(SoloUndo(0, Cell())))
        return LocalPlayerData(playerId = "existing-player", activeSolo = solo)
    }

    @Test fun unreadableProfileCannotBeOverwrittenByANewEmptyProfile() {
        val file = target().also { it.writeText("broken original progress") }
        val store = LocalPlayerStore(file)
        assertThrows(IOException::class.java) { store.update { it.copy(records = emptyList()) } }
        assertEquals("broken original progress", file.readText())
    }

    @Test fun validBackupRecoversIdentityNotesAndHintsAndPreservesDamagedBytes() {
        val file = target().also { it.writeText("damaged-primary") }
        File(file.path + ".recovery").writeText(json.encodeToString(saved()))
        val store = LocalPlayerStore(file)
        assertEquals("existing-player", store.current().playerId)
        assertEquals(4, store.current().activeSolo!!.cells[0].notes)
        assertEquals(1, store.current().activeSolo!!.hintsUsed)
        assertNotNull(store.recoveryWarning)
        store.update { it }
        assertEquals(saved().playerId, json.decodeFromString<LocalPlayerData>(file.readText()).playerId)
        assertTrue(root.listFiles()!!.any { it.isFile && it.name.contains("unreadable") && it.readText() == "damaged-primary" })
    }

    @Test fun backupFailureDoesNotChangePrimaryOrInMemoryProgress() {
        val original = saved(); val file = target().also { it.writeText(json.encodeToString(original)) }
        val store = LocalPlayerStore(file)
        File(file.path + ".recovery").also { check(it.mkdir()); File(it, "blocker").writeText("keep") }
        assertThrows(IOException::class.java) { store.update { it.copy(activeSolo = null) } }
        assertEquals(original, store.current())
        assertEquals(original, json.decodeFromString<LocalPlayerData>(file.readText()))
    }

    @Test fun failedPrimaryRenameIsNeverReportedAsASuccessfulCommit() {
        val original = saved(); val file = target().also { it.writeText(json.encodeToString(original)) }
        val store = LocalPlayerStore(file)
        check(file.delete()); check(file.mkdir()); File(file, "blocker").writeText("keep")
        assertThrows(IOException::class.java) { store.update { it.copy(activeSolo = null) } }
        assertEquals(original, store.current())
        assertEquals("keep", File(file, "blocker").readText())
    }

    @Test fun recoveryDoesNotRewriteDamagedBytesWhenTheirPreservationFails() {
        val file = target().also { it.writeText("damaged-primary") }
        File(file.path + ".recovery").writeText(json.encodeToString(saved()))
        check(root.setWritable(false, false))
        try {
            val store = LocalPlayerStore(file)
            assertEquals("existing-player", store.current().playerId)
            assertThrows(IOException::class.java) { store.update { it.copy(activeSolo = null) } }
            assertEquals("damaged-primary", file.readText())
        } finally { root.setWritable(true, true) }
    }

    @Test fun impossibleUndoPayloadIsNotAcceptedAndWrittenBack() {
        val original = saved(); val invalid = original.copy(activeSolo = original.activeSolo!!.copy(
            history = listOf(SoloUndo(0, Cell(99)))))
        val text = json.encodeToString(invalid); val file = target().also { it.writeText(text) }
        val store = LocalPlayerStore(file)
        assertThrows(IOException::class.java) { store.update { it } }
        assertEquals(text, file.readText())
    }

    @Test fun oldProfilesWithoutNewFieldsKeepTheirIdentityAndCanStillSave() {
        val file = target().also { it.writeText("""{"schema":1,"playerId":"legacy-player","records":[]}""") }
        val store = LocalPlayerStore(file)
        assertEquals("legacy-player", store.current().playerId)
        store.update { it.copy(activeSolo = saved().activeSolo) }
        assertEquals(4, LocalPlayerStore(file).current().activeSolo!!.cells[0].notes)
        assertEquals("legacy-player", LocalPlayerStore(file).current().playerId)
    }
}
