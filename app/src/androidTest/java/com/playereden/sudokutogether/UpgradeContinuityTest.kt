package com.playereden.sudokutogether

import android.content.pm.LauncherApps
import android.os.Process
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.MessageDigest

/** Run seed on the original v0.11A APK, install -r the candidate, then run verify. */
@RunWith(AndroidJUnit4::class)
class UpgradeContinuityTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val checkpoint get() = File(context.getExternalFilesDir("qa-upgrade"), "checkpoint.json")
    private lateinit var model: GameViewModel
    @Before fun ready() {
        compose.runOnUiThread { model = ViewModelProvider(compose.activity)[GameViewModel::class.java] }
        compose.waitUntil(30000) { !model.state.value.busy }
        EmulatorSystemDialogs.dismiss()
    }
    private fun profileHash(): String = MessageDigest.getInstance("SHA-256")
        .digest(File(context.filesDir, "local-player.json").readBytes()).joinToString("") { "%02x".format(it) }
    @Test fun seedSavedPuzzleAndPreferencesBeforeUpgrade() {
        assertFalse("Seed requires an original-version fixture: " + model.state.value.saveIssue,
            model.state.value.localSaveBlocked)
        compose.runOnUiThread {
            model.setName("Upgrade player"); model.setUniformNumbers(true); model.setTouchFeedback(false)
            model.setAutoCleanNotes(true); model.setDifficulty("EASY"); model.solo(false)
        }
        compose.waitUntil(60000) { model.state.value.page == Page.PLAY && model.state.value.canEdit }
        // Prepare a real completed history record through the original app's
        // unchanged public actions, without constructors added by the update.
        var moves = 0
        while (!model.state.value.snapshot!!.completed) {
            check(moves++ < 81)
            val snapshot = model.state.value.snapshot!!
            val hint = com.playereden.sudokutogether.core.SudokuAssistance.nextHint(snapshot.puzzle,snapshot.cells.map { it.value })!!
            compose.runOnUiThread { model.select(hint.cell);model.number(hint.digit) }
            compose.waitUntil(20000) { model.state.value.pendingId == null && model.state.value.snapshot!!.cells[hint.cell].value == hint.digit }
        }
        val recordId = model.state.value.profile.records.last().id
        compose.runOnUiThread { model.home();model.solo(false) }
        compose.waitUntil(60000) { model.state.value.page == Page.PLAY && model.state.value.canEdit }
        val snapshot = model.state.value.snapshot!!
        val entry = com.playereden.sudokutogether.core.SudokuAssistance.nextHint(snapshot.puzzle,snapshot.cells.map { it.value })!!
        compose.runOnUiThread { model.select(entry.cell);model.number(entry.digit) }
        compose.waitUntil(20000) { model.state.value.pendingId == null && model.state.value.snapshot!!.cells[entry.cell].value == entry.digit }
        val cell = model.state.value.snapshot!!.puzzle.clues.indices.first {
            it != entry.cell && model.state.value.snapshot!!.puzzle.clues[it] == 0
        }
        assertTrue(cell >= 0)
        compose.runOnUiThread { model.select(cell); if (!model.state.value.notes) model.toggleNotes(); model.number(7) }
        compose.waitUntil(15000) { model.state.value.pendingId == null && model.state.value.snapshot!!.cells[cell].notes != 0 }
        compose.runOnUiThread { model.home() }
        compose.waitUntil(15000) { model.state.value.page == Page.HOME && !model.state.value.busy }
        val state = model.state.value
        val data = JSONObject().put("profileSha256", profileHash()).put("playerId", state.profile.playerId)
            .put("soloId", state.profile.activeSolo!!.id).put("cell", cell)
            .put("notes", state.profile.activeSolo!!.cells[cell].notes)
            .put("entryCell",entry.cell).put("entryValue",entry.digit)
            .put("recordId",recordId).put("recordCount",state.profile.records.size)
        checkpoint.parentFile!!.mkdirs(); checkpoint.writeText(data.toString())
        assertTrue(data.getInt("notes") != 0)
    }
    @Test fun verifySavedPuzzleAndPreferencesAfterUpgrade() {
        val launcher = context.getSystemService(LauncherApps::class.java)
            .getActivityList(context.packageName, Process.myUserHandle())
            .single { it.componentName.className == MainActivity::class.java.name }
        assertEquals("Sudoku", launcher.label.toString())
        assertEquals("GridMates: Sudoku QA", context.packageManager.getApplicationLabel(context.applicationInfo).toString())
        assertTrue("The original APK seed must run first", checkpoint.isFile)
        val old = JSONObject(checkpoint.readText()); val state = model.state.value
        assertEquals("The complete persisted profile is unchanged", old.getString("profileSha256"), profileHash())
        assertEquals(old.getString("playerId"), state.profile.playerId)
        assertEquals(old.getString("soloId"), state.profile.activeSolo!!.id)
        assertEquals("Upgrade player", state.name)
        assertTrue(state.uniformNumbers); assertFalse(state.touchFeedback); assertTrue(state.autoCleanNotes)
        assertEquals(old.getInt("notes"), state.profile.activeSolo!!.cells[old.getInt("cell")].notes)
        assertEquals(old.getInt("entryValue"),state.profile.activeSolo!!.cells[old.getInt("entryCell")].value)
        assertEquals(old.getInt("recordCount"),state.profile.records.size)
        assertTrue(state.profile.records.any { it.id == old.getString("recordId") })
        compose.runOnUiThread { model.solo(true) }
        compose.waitUntil(15000) { model.state.value.page == Page.PLAY && model.state.value.canEdit }
        assertEquals(old.getString("soloId"), model.state.value.snapshot!!.sessionId)
        assertEquals(old.getInt("notes"), model.state.value.snapshot!!.cells[old.getInt("cell")].notes)
        assertEquals(old.getInt("entryValue"),model.state.value.snapshot!!.cells[old.getInt("entryCell")].value)
        compose.runOnUiThread {
            model.home(); model.setName("Player"); model.setUniformNumbers(false)
            model.setTouchFeedback(true); model.setAutoCleanNotes(false)
        }
    }
}
