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

    @Test fun unreadableAtomicSidecarsDoNotDestroyEitherOriginal() {
        for (suffix in listOf(".bak", ".new")) {
            val file = File(root, "sidecar-${suffix.drop(1)}.json").also { it.writeText("primary-$suffix") }
            val sidecar = File(file.path + suffix).also { it.writeText("sidecar-$suffix") }
            val store = LocalPlayerStore(file)
            assertFalse(store.canSave)
            assertEquals("primary-$suffix", file.readText())
            assertEquals("sidecar-$suffix", sidecar.readText())
        }
    }
    @Test fun recoveryPreservesEveryPrimaryAndRecoverySidecarBeforeReconciliation() {
        val file = target().also { it.writeText("damaged-base") }
        File(file.path + ".bak").writeText("damaged-bak")
        File(file.path + ".new").writeText("interrupted-new")
        File(file.path + ".recovery").writeText(json.encodeToString(saved()))
        File(file.path + ".recovery.new").writeText("interrupted-recovery-new")
        val store = LocalPlayerStore(file)
        assertTrue(store.canSave); store.update { it }
        val preserved = root.listFiles()!!.filter { it.isFile }.map { it.readText() }
        for (original in listOf("damaged-base", "damaged-bak", "interrupted-new", "interrupted-recovery-new"))
            assertTrue("Every source survives reconciliation: $original", original in preserved)
    }
    @Test fun failedSidecarPreservationLeavesEverySourceUntouched() {
        val file = target().also { it.writeText("damaged-base") }
        val sidecar = File(file.path + ".bak").also { it.writeText("damaged-bak") }
        File(file.path + ".recovery").writeText(json.encodeToString(saved()))
        check(root.setWritable(false, false))
        try {
            val store = LocalPlayerStore(file)
            assertFalse(store.canSave)
            assertEquals("damaged-base", file.readText()); assertEquals("damaged-bak", sidecar.readText())
        } finally { root.setWritable(true, true) }
    }
    @Test fun structurallyImpossibleUndoCannotEraseOtherEntries() {
        val digits = "123456789456789123789123456234567891567891234891234567345678912678912345912345678".map { it.digitToInt() }
        val puzzle = Puzzle("undo-fixture", digits.mapIndexed { i, n -> if (i in listOf(0, 1, 40)) 0 else n }, "EASY")
        val solo = PersonalPlay.newSolo(puzzle, 1000L).copy(moves = 1)
        val bad = listOf(SoloUndo(0, Cell(), mapOf(40 to Cell())), SoloUndo(0, Cell(), mapOf(0 to Cell())),
            SoloUndo(0, Cell(), mapOf(2 to Cell())), SoloUndo(0, Cell(), mapOf(1 to Cell(2))),
            SoloUndo(2, Cell()), SoloUndo(0, Cell(revision = 2)))
        for ((index, undo) in bad.withIndex()) {
            val invalid = LocalPlayerData(playerId = "undo-player", activeSolo = solo.copy(history = listOf(undo)))
            val text = json.encodeToString(invalid)
            val file = File(root, "undo-$index.json").also { it.writeText(text) }
            val store = LocalPlayerStore(file)
            assertFalse("Reject impossible Undo payload $index", store.canSave); assertEquals(text, file.readText())
        }
    }

    @Test fun legacyUndoWithoutRelatedPayloadCanStillBeUndoneAndSaved() {
        val original = saved()
        val text = Json.encodeToString(original)
        assertFalse(text.contains("relatedBefore"))
        val store = LocalPlayerStore(target().also { it.writeText(text) })
        assertTrue(store.canSave)
        store.update { data -> data.copy(activeSolo = PersonalPlay.edit(data.activeSolo!!, 0, Kind.UNDO,
            elapsedMs = 1200L, nowMs = 2000L)!!) }
        assertEquals(0, store.current().activeSolo!!.cells[0].notes)
    }

    @Test fun oldProfilesWithoutNewFieldsKeepTheirIdentityAndCanStillSave() {
        val file = target().also { it.writeText("""{"schema":1,"playerId":"legacy-player","records":[]}""") }
        val store = LocalPlayerStore(file)
        assertEquals("legacy-player", store.current().playerId)
        store.update { it.copy(activeSolo = saved().activeSolo) }
        assertEquals(4, LocalPlayerStore(file).current().activeSolo!!.cells[0].notes)
        assertEquals("legacy-player", LocalPlayerStore(file).current().playerId)
    }
    @Test fun invalidCheckCountersCannotReplaceExistingProgress() {
        for (count in listOf(-1,10000)) {
            val original=saved();val invalid=original.copy(activeSolo=original.activeSolo!!.copy(checksUsed=count))
            val text=Json.encodeToString(invalid);val file=File(root,"checks-$count.json").also { it.writeText(text) }
            assertFalse(LocalPlayerStore(file).canSave)
            assertEquals(text,file.readText())
        }
    }
    @Test fun recordedSolutionChecksCannotMasqueradeAsUnassisted() {
        val original=saved();val record=GameRecord("inconsistent-check",SOLO,"SOLVED","record",1000L,1,2000L,
            assisted=false,checksUsed=1)
        val text=Json.encodeToString(original.copy(records=listOf(record)));val file=target().also { it.writeText(text) }
        assertFalse(LocalPlayerStore(file).canSave)
        assertEquals(text,file.readText())
    }
}
