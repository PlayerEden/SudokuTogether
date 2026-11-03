package com.playereden.sudokutogether

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.playereden.sudokutogether.core.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream
import org.json.JSONObject

/** Controlled fixtures exercise the real Activity, ViewModel and private store. */
@RunWith(AndroidJUnit4::class)
class DailyFinishUiTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private var scenario: ActivityScenario<MainActivity>? = null
    private val fixtureBackup get() = File(context.cacheDir, "v010c-ui-fixture-original")
    private val oldDate = "2026-09-01"
    private val recentDate = "2026-10-31"
    private fun shell(command: String): String = instrumentation.uiAutomation.executeShellCommand(command).use { descriptor ->
        FileInputStream(descriptor.fileDescriptor).use { it.readBytes().toString(Charsets.UTF_8) }
    }
    @Before fun seed() {
        restoreAbandonedFixture()
        val originals = context.filesDir.listFiles().orEmpty().filter { it.name.startsWith("local-player.json") && it.isFile }
            .associate { it.name to it.readBytes() }
        val oldFont = Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE, 1f)
        val oldSize = Regex("Override size: (\\d+x\\d+)").find(shell("wm size"))?.groupValues?.get(1)
        check(fixtureBackup.mkdirs())
        originals.forEach { (name, bytes) ->
            val backup = File(fixtureBackup, name); backup.writeBytes(bytes)
            check(backup.readBytes().contentEquals(bytes))
        }
        File(fixtureBackup, "manifest.json").writeText(JSONObject().put("font", oldFont.toDouble())
            .put("size", oldSize ?: "reset").toString())
        context.filesDir.listFiles().orEmpty().filter { it.name.startsWith("local-player.json") }.forEach { check(it.delete()) }
        shell("settings put system font_scale 1.0")
        val digits = "123456789456789123789123456234567891567891234891234567345678912678912345912345678".map { it.digitToInt() }
        val puzzle = Puzzle("finish-fixture", digits.mapIndexed { i, n -> if (i == 0) 0 else n }, "EASY")
        val active = PersonalPlay.newSolo(puzzle, 1000L).copy(id = "fixture-active", elapsedMs = 4500L, hintsUsed = 1)
        val days = (1..31).map { day -> DailyProgress("2026-10-${day.toString().padStart(2, '0')}",
            active.copy(id = "fixture-day-$day", cells = digits.map { Cell(it) }, finishedAtMs = 5500L, moves = 1))
        } + DailyProgress(oldDate, active.copy(id = "fixture-old-daily"))
        val records = days.filter { it.solo.completed }.map { day -> GameRecord(day.solo.id, DAILY, "SOLVED", puzzle.id,
            4500L, 1, 5500L, "EASY", assisted = true, hintsUsed = 1, dailyDate = day.date) }
        File(context.filesDir, "local-player.json").writeText(Json.encodeToString(LocalPlayerData(
            playerId = "screenshot-fixture", activeSolo = active, dailyProgress = days, records = records)))
    }
    @After fun restore() { scenario?.close(); restoreAbandonedFixture() }
    private fun restoreAbandonedFixture() {
        val manifest = File(fixtureBackup, "manifest.json")
        if (!manifest.isFile) return
        val settings = JSONObject(manifest.readText())
        val backups = fixtureBackup.listFiles().orEmpty().filter { it.name.startsWith("local-player.json") }
        backups.forEach { backup ->
            val restored = File(context.filesDir, backup.name); backup.copyTo(restored, overwrite = true)
            check(restored.readBytes().contentEquals(backup.readBytes()))
        }
        val names = backups.map { it.name }.toSet()
        context.filesDir.listFiles().orEmpty().filter { it.name.startsWith("local-player.json") && it.name !in names }.forEach { check(it.delete()) }
        shell("settings put system font_scale ${settings.getDouble("font")}")
        shell("wm size ${settings.getString("size")}")
        check(fixtureBackup.deleteRecursively())
    }
    private fun launch() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        EmulatorSystemDialogs.dismiss()
        compose.waitUntil(30000) { compose.onAllNodes(hasTestTag("nav_records") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(10000) {
            var focused = false
            scenario!!.onActivity { focused = it.hasWindowFocus() }
            if (!focused) EmulatorSystemDialogs.dismiss()
            focused
        }
    }
    private fun capture(name: String) {
        val file = File(context.getExternalFilesDir("screenshots"), name)
        scenario!!.onActivity { activity ->
            // Retain the current recreated Activity, then capture outside the main thread.
            captureActivity = activity
        }
        NativeWindowCapture.save(requireNotNull(captureActivity), file)
    }
    private var captureActivity: MainActivity? = null
    private fun profile() = compose.onNodeWithTag("nav_records").performClick()
    @Test fun anOlderDailyKeepsItsFinishDetailsAfterItsReviewBoardIsPruned() {
        launch(); profile()
        compose.onNodeWithTag("daily_history_$recentDate").performScrollTo().assertIsDisplayed()
        capture("10-daily-history-normal.png")
        compose.onNodeWithText("Show all 32").performScrollTo().performClick()
        compose.onNodeWithTag("daily_history_$oldDate").performScrollTo().performClick()
        compose.waitUntil(20000) { compose.onAllNodesWithTag("cell_0").fetchSemanticsNodes().isNotEmpty() }
        capture("11-old-daily-playing.png")
        compose.onNodeWithTag("cell_0").performClick().assertIsSelected()
        compose.onNodeWithTag("number_1").assertIsEnabled().performClick()
        try {
            compose.waitUntil(30000) { compose.onAllNodesWithText("Daily challenge complete", substring = false).fetchSemanticsNodes().isNotEmpty() }
        } catch (error: Throwable) {
            scenario!!.onActivity {
                val state = androidx.lifecycle.ViewModelProvider(it)[GameViewModel::class.java].state.value
                println("SUDOKU_DAILY_DIAGNOSTIC: page=${state.page}, busy=${state.busy}, selected=${state.selected}, cell=${state.snapshot?.cells?.firstOrNull()?.value}, complete=${state.snapshot?.completed}, message=${state.message}")
            }
            throw error
        }
        compose.onAllNodesWithText("Today's challenge complete", substring = true).assertCountEquals(0)
        compose.onNodeWithText("Daily challenge complete", substring = false).performScrollTo()
        compose.onNodeWithText("Assisted", substring = true).assertIsDisplayed()
        compose.onNodeWithText("1 logical hint used").assertIsDisplayed()
        capture("12-old-daily-finish-normal.png")
        scenario!!.recreate()
        compose.onNodeWithText("1 logical hint used").performScrollTo().assertIsDisplayed()
        val saved = LocalPlayerStore(File(context.filesDir, "local-player.json")).current()
        assertEquals(31, saved.dailyProgress.size); assertNull(DailyChallenge.find(saved, oldDate))
        assertEquals(1, saved.records.count { it.id == "fixture-old-daily" })
        shell("settings put system font_scale 2.0")
        compose.onNodeWithText("Back home").performScrollTo().assertIsDisplayed()
        capture("18-old-daily-finish-large.png")
        scenario!!.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        compose.waitUntil(15000) { context.resources.configuration.screenWidthDp > context.resources.configuration.screenHeightDp }
        EmulatorSystemDialogs.dismiss()
        compose.onNodeWithText("1 logical hint used").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Back home").performScrollTo().assertIsDisplayed()
        capture("19-old-daily-finish-large-landscape.png")
        compose.onNodeWithText("Back home").performClick()
        compose.onNodeWithText("Continue").assertExists()
    }
    @Test fun historyDatesAndActionsFitAt200PercentOnACompactPhoneAndLandscape() {
        shell("wm size 640x1136"); shell("settings put system font_scale 2.0")
        launch(); profile()
        compose.onNodeWithTag("daily_history_$recentDate").performScrollTo().assertIsDisplayed()
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(recentDate, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val layout = layouts.single(); assertEquals(1, layout.lineCount)
        for (i in recentDate.indices) {
            val glyph = layout.getBoundingBox(i)
            assertTrue("Every date digit fits", glyph.left >= -1f && glyph.top >= -1f &&
                glyph.right <= layout.size.width + 1f && glyph.bottom <= layout.size.height + 1f)
        }
        assertTrue(compose.onNodeWithTag("daily_history_$recentDate").fetchSemanticsNode().boundsInRoot.height >=
            48 * context.resources.displayMetrics.density)
        capture("13-daily-history-large-compact.png")
        scenario!!.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        compose.waitUntil(15000) { context.resources.configuration.screenWidthDp > context.resources.configuration.screenHeightDp }
        EmulatorSystemDialogs.dismiss()
        compose.onNodeWithTag("daily_history_$recentDate").performScrollTo().assertIsDisplayed()
        capture("14-daily-history-large-landscape.png")
        compose.onAllNodesWithText("DAILY · SOLVED · Easy").onFirst().performScrollTo().assertIsDisplayed()
        capture("15-recent-records-large-landscape.png")
    }
    @Test fun soloFinishMetadataAndReplayRemainReachableWithLargeText() {
        shell("wm size 640x1136"); shell("settings put system font_scale 2.0")
        launch(); compose.onNodeWithText("Continue").performScrollTo().performClick()
        compose.waitUntil(20000) { compose.onAllNodesWithTag("cell_0").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("cell_0").performScrollTo().performClick().assertIsSelected()
        // At compact 200% text the whole play screen scrolls. Bring the dock
        // into view before injecting a real touch at its coordinates.
        println("SUDOKU_SOLO_DOCK_BEFORE_SCROLL: ${compose.onNodeWithTag("number_1").fetchSemanticsNode().boundsInRoot}")
        compose.onNodeWithTag("number_1").performScrollTo().assertIsDisplayed().performClick()
        compose.waitUntil(20000) { compose.onAllNodesWithText("Puzzle completed").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Puzzle completed").performScrollTo(); capture("16-solo-finish-large-metadata.png")
        compose.onNodeWithText("1 logical hint used").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("New puzzle").performScrollTo().assertIsDisplayed()
        capture("17-solo-finish-large-actions.png")
        val finished = LocalPlayerStore(File(context.filesDir, "local-player.json")).current().activeSolo!!
        assertTrue(finished.completed); assertTrue(finished.elapsedMs >= 4500L)
    }
}
