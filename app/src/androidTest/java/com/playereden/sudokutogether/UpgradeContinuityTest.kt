package com.playereden.sudokutogether

import android.content.pm.LauncherApps
import android.os.Process
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.playereden.sudokutogether.core.SOLO
import com.playereden.sudokutogether.core.SudokuAssistance
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.MessageDigest

/** Seed the original QA APK, install -r the candidate, then run verify. */
@RunWith(AndroidJUnit4::class)
class UpgradeContinuityTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val checkpoint get() = File(context.getExternalFilesDir("qa-upgrade"), "checkpoint.json")
    private val baselineVersion get() = InstrumentationRegistry.getArguments()
        .getString("upgradeFromVersion", "18").toLong()
    private val candidateVersion get() = InstrumentationRegistry.getArguments()
        .getString("upgradeToVersion", "19").toLong()
    private val installedVersion get() = PackageInfoCompat.getLongVersionCode(
        context.packageManager.getPackageInfo(context.packageName, 0)
    )
    private lateinit var model: GameViewModel
    @Before fun ready() {
        compose.runOnUiThread { model = ViewModelProvider(compose.activity)[GameViewModel::class.java] }
        compose.waitUntil(30000) { !model.state.value.busy }
        EmulatorSystemDialogs.dismiss()
    }
    private fun profileHash(): String = MessageDigest.getInstance("SHA-256")
        .digest(File(context.filesDir, "local-player.json").readBytes()).joinToString("") { "%02x".format(it) }
    @Test fun seedSavedPuzzleAndPreferencesBeforeUpgrade() {
        assertEquals("Seed must run against the specified original build", baselineVersion, installedVersion)
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
            val hint = SudokuAssistance.nextHint(snapshot.puzzle, snapshot.cells.map { it.value })!!
            compose.runOnUiThread { model.select(hint.cell);model.number(hint.digit) }
            compose.waitUntil(20000) { model.state.value.pendingId == null && model.state.value.snapshot!!.cells[hint.cell].value == hint.digit }
        }
        val recordId = model.state.value.profile.records.last().id
        compose.runOnUiThread {
            model.chooseCosmetic("emblem", "leaf")
            if (baselineVersion >= 19) {
                model.chooseCosmetic("portrait", "woodland_rabbit")
                model.chooseCosmetic("font", "serif")
            }
            model.home()
            model.solo(false)
        }
        compose.waitUntil(60000) { model.state.value.page == Page.PLAY && model.state.value.canEdit }
        val snapshot = model.state.value.snapshot!!
        val entry = SudokuAssistance.nextHint(snapshot.puzzle, snapshot.cells.map { it.value })!!
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
        val dailyDate = if (baselineVersion >= 19) seedDailyProgress() else null
        if (baselineVersion >= 19) compose.runOnUiThread { model.setRequireRoomCode(true) }
        compose.runOnUiThread { model.host(false) }
        compose.waitUntil(60000) { model.state.value.page == Page.PLAY && model.state.value.role == com.playereden.sudokutogether.core.HOST && model.state.value.snapshot != null && !model.state.value.busy }
        val hostId = model.state.value.snapshot!!.sessionId
        assertTrue("The original saved room requires a code", model.state.value.roomCode.isNotBlank())
        compose.runOnUiThread { model.home() }
        compose.waitUntil(15000) { model.state.value.page == Page.HOME && !model.state.value.busy }
        val state = model.state.value
        val solo = state.profile.activeSolo!!
        assertEquals(if (baselineVersion >= 19) "woodland_rabbit" else "", state.appearance.portrait)
        val data = JSONObject().put("profileSha256", profileHash()).put("playerId", state.profile.playerId)
            .put("seedVersion", installedVersion).put("soloId", solo.id).put("cell", cell)
            .put("notes", solo.cells[cell].notes).put("soloElapsedMs", solo.elapsedMs)
            .put("entryCell",entry.cell).put("entryValue",entry.digit)
            .put("recordId",recordId).put("recordCount",state.profile.records.size)
            .put("emblem", state.appearance.emblem).put("portrait", state.appearance.portrait)
            .put("font", state.appearance.font).put("hostId", hostId)
        if (dailyDate != null) {
            val daily = state.profile.dailyProgress.single { it.date == dailyDate }.solo
            data.put("dailyDate", dailyDate).put("dailyId", daily.id)
                .put("dailyCell", daily.history.single().cell).put("dailyElapsedMs", daily.elapsedMs)
            val dailyCell = daily.cells[daily.history.single().cell]
            data.put("dailyNotes", dailyCell.notes)
            assertTrue(dailyCell.notes != 0)
        }
        checkpoint.parentFile!!.mkdirs(); checkpoint.writeText(data.toString())
        assertTrue(data.getInt("notes") != 0)
    }
    @Test fun verifySavedPuzzleAndPreferencesAfterUpgrade() {
        assertEquals("Verify must run against the specified update", candidateVersion, installedVersion)
        val launcher = context.getSystemService(LauncherApps::class.java)
            .getActivityList(context.packageName, Process.myUserHandle())
            .single { it.componentName.className == MainActivity::class.java.name }
        assertEquals("Sudoku", launcher.label.toString())
        assertEquals("GridMates: Sudoku QA", context.packageManager.getApplicationLabel(context.applicationInfo).toString())
        assertTrue("The original APK seed must run first", checkpoint.isFile)
        val old = JSONObject(checkpoint.readText()); val state = model.state.value
        assertEquals(baselineVersion, old.getLong("seedVersion"))
        assertTrue(installedVersion > old.getLong("seedVersion"))
        assertEquals("The complete persisted profile is unchanged", old.getString("profileSha256"), profileHash())
        assertEquals(old.getString("playerId"), state.profile.playerId)
        assertEquals(old.getString("soloId"), state.profile.activeSolo!!.id)
        assertEquals("Upgrade player", state.name)
        assertEquals("leaf", state.appearance.emblem)
        assertEquals(old.getString("emblem"), state.appearance.emblem)
        assertEquals(old.getString("portrait"), state.appearance.portrait)
        assertEquals(old.getString("font"), state.appearance.font)
        assertEquals("Remembered room-code preference", baselineVersion >= 19, state.requireRoomCode)
        assertTrue(state.uniformNumbers); assertFalse(state.touchFeedback); assertTrue(state.autoCleanNotes)
        assertEquals(old.getInt("notes"), state.profile.activeSolo!!.cells[old.getInt("cell")].notes)
        assertEquals(old.getInt("entryValue"),state.profile.activeSolo!!.cells[old.getInt("entryCell")].value)
        assertEquals(old.getInt("recordCount"),state.profile.records.size)
        assertTrue(state.profile.records.any { it.id == old.getString("recordId") })
        compose.runOnUiThread { model.solo(true) }
        compose.waitUntil(15000) { model.state.value.page == Page.PLAY && model.state.value.canEdit }
        assertEquals(old.getString("soloId"), model.state.value.snapshot!!.sessionId)
        assertEquals(old.getLong("soloElapsedMs"), model.state.value.soloElapsedMs)
        assertEquals(old.getInt("cell"), model.state.value.snapshot!!.undoTargets[SOLO])
        assertEquals(old.getInt("notes"), model.state.value.snapshot!!.cells[old.getInt("cell")].notes)
        assertEquals(old.getInt("entryValue"),model.state.value.snapshot!!.cells[old.getInt("entryCell")].value)
        if (old.has("dailyDate")) {
            compose.runOnUiThread { model.home(); model.openDaily(old.getString("dailyDate")) }
            compose.waitUntil(15000) { model.state.value.page == Page.PLAY && model.state.value.canEdit }
            val daily = model.state.value.snapshot!!
            val cell = old.getInt("dailyCell")
            assertEquals(old.getString("dailyId"), daily.sessionId)
            assertEquals(old.getLong("dailyElapsedMs"), model.state.value.soloElapsedMs)
            assertEquals(old.getInt("dailyNotes"), daily.cells[cell].notes)
            assertEquals(cell, daily.undoTargets[SOLO])
        }
        compose.runOnUiThread { model.home(); model.host(true) }
        compose.waitUntil(30000) { model.state.value.page == Page.PLAY && model.state.value.snapshot != null && !model.state.value.busy }
        assertEquals(old.getString("hostId"), model.state.value.snapshot!!.sessionId)
        assertTrue("A saved room stays code-protected when it resumes", model.state.value.roomCode.isNotBlank())
        compose.runOnUiThread {
            model.home(); model.setName("Player"); model.setUniformNumbers(false)
            model.setTouchFeedback(true); model.setAutoCleanNotes(false)
        }
    }

    private fun seedDailyProgress(): String {
        compose.runOnUiThread { model.daily() }
        compose.waitUntil(60000) { model.state.value.page == Page.PLAY && model.state.value.canEdit }
        val state = model.state.value
        val date = checkNotNull(state.dailyDate)
        val cell = state.snapshot!!.puzzle.clues.indexOf(0)
        compose.runOnUiThread { model.select(cell); model.toggleNotes(); model.number(7) }
        compose.waitUntil(20000) {
            model.state.value.pendingId == null && model.state.value.snapshot!!.cells[cell].notes != 0
        }
        compose.runOnUiThread { model.home() }
        compose.waitUntil(15000) { model.state.value.page == Page.HOME && !model.state.value.busy }
        return date
    }
}
