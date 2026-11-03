package com.playereden.sudokutogether

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

/** Run seed on the original v0.10D APK, install -r the candidate, then run verify. */
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
        compose.runOnUiThread {
            model.setName("Upgrade player"); model.setUniformNumbers(true); model.setTouchFeedback(false)
            model.setAutoCleanNotes(true); model.setDifficulty("EASY"); model.solo(false)
        }
        compose.waitUntil(60000) { model.state.value.page == Page.PLAY && model.state.value.canEdit }
        val cell = model.state.value.snapshot!!.puzzle.clues.indexOfFirst { it == 0 }
        assertTrue(cell >= 0)
        compose.runOnUiThread { model.select(cell); if (!model.state.value.notes) model.toggleNotes(); model.number(7) }
        compose.waitUntil(15000) { model.state.value.pendingId == null && model.state.value.snapshot!!.cells[cell].notes != 0 }
        compose.runOnUiThread { model.home() }
        compose.waitUntil(15000) { model.state.value.page == Page.HOME && !model.state.value.busy }
        val state = model.state.value
        val data = JSONObject().put("profileSha256", profileHash()).put("playerId", state.profile.playerId)
            .put("soloId", state.profile.activeSolo!!.id).put("cell", cell)
            .put("notes", state.profile.activeSolo!!.cells[cell].notes)
        checkpoint.parentFile!!.mkdirs(); checkpoint.writeText(data.toString())
        assertTrue(data.getInt("notes") != 0)
    }
    @Test fun verifySavedPuzzleAndPreferencesAfterUpgrade() {
        assertTrue("The original APK seed must run first", checkpoint.isFile)
        val old = JSONObject(checkpoint.readText()); val state = model.state.value
        assertEquals("The complete persisted profile is unchanged", old.getString("profileSha256"), profileHash())
        assertEquals(old.getString("playerId"), state.profile.playerId)
        assertEquals(old.getString("soloId"), state.profile.activeSolo!!.id)
        assertEquals("Upgrade player", state.name)
        assertTrue(state.uniformNumbers); assertFalse(state.touchFeedback); assertTrue(state.autoCleanNotes)
        assertEquals(old.getInt("notes"), state.profile.activeSolo!!.cells[old.getInt("cell")].notes)
        compose.runOnUiThread { model.solo(true) }
        compose.waitUntil(15000) { model.state.value.page == Page.PLAY && model.state.value.canEdit }
        assertEquals(old.getString("soloId"), model.state.value.snapshot!!.sessionId)
        assertEquals(old.getInt("notes"), model.state.value.snapshot!!.cells[old.getInt("cell")].notes)
        compose.runOnUiThread {
            model.home(); model.setName("Player"); model.setUniformNumbers(false)
            model.setTouchFeedback(true); model.setAutoCleanNotes(false)
        }
    }
}
