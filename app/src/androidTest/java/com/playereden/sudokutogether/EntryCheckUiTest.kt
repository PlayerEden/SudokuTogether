package com.playereden.sudokutogether

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.playereden.sudokutogether.core.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

/** Real user actions and saved results, with a private reversible fixture. */
@RunWith(AndroidJUnit4::class)
class EntryCheckUiTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val backup get() = File(context.cacheDir, "entry-check-ui-original")
    private var scenario: ActivityScenario<MainActivity>? = null
    private lateinit var model: GameViewModel
    private var activity: MainActivity? = null
    private val prefix get() = if (context.resources.configuration.fontScale > 1.5f) "large" else "normal"
    private val clues = "530070000600195000098000060800060003400803001700020006060000280000419005000080079".map { it.digitToInt() }

    @Before fun seed() {
        restoreOriginal()
        check(backup.mkdirs())
        val originals = context.filesDir.listFiles().orEmpty().filter { it.name.startsWith("local-player.json") && it.isFile }
        originals.forEach { it.copyTo(File(backup, it.name)) }
        File(backup, "manifest.json").writeText(JSONObject().put("ready", true).toString())
        val puzzle = Puzzle("coach-ui", clues, "EASY")
        writeProgress(PersonalPlay.edit(PersonalPlay.newSolo(puzzle, 1000L), 2, Kind.VALUE, 1,
            elapsedMs = 1000L, nowMs = 2000L)!!)
    }
    private fun writeProgress(progress: SoloProgress) {
        context.filesDir.listFiles().orEmpty().filter { it.name.startsWith("local-player.json") }.forEach { check(it.delete()) }
        File(context.filesDir, "local-player.json").writeText(Json.encodeToString(LocalPlayerData(
            playerId = "coach-ui-fixture", activeSolo = progress)))
    }
    private fun restoreOriginal() {
        if (!File(backup, "manifest.json").isFile) return
        val originals = backup.listFiles().orEmpty().filter { it.name.startsWith("local-player.json") }
        context.filesDir.listFiles().orEmpty().filter { it.name.startsWith("local-player.json") }.forEach { check(it.delete()) }
        originals.forEach { it.copyTo(File(context.filesDir, it.name), overwrite = true) }
        check(backup.deleteRecursively())
    }
    @After fun restore() { compose.mainClock.autoAdvance = true;scenario?.close();restoreOriginal() }
    private fun launch() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario!!.onActivity { activity = it;model = ViewModelProvider(it)[GameViewModel::class.java] }
        compose.waitUntil(30000) { !model.state.value.busy }
        compose.waitUntil(15000) {
            EmulatorSystemDialogs.dismiss()
            var focused = false;scenario!!.onActivity { focused = it.hasWindowFocus() };focused
        }
        compose.waitUntil(20000) {
            try { compose.onNodeWithText("Continue").isDisplayed() }
            catch (error: IllegalStateException) {
                if (!error.message.orEmpty().startsWith("No compose hierarchies")) throw error
                false
            }
        }
        compose.onNodeWithText("Continue").assertIsDisplayed().performClick()
        compose.waitUntil(30000) { model.state.value.canEdit }
    }
    private fun capture(name: String, dialog: Boolean = true) {
        EmulatorSystemDialogs.dismiss()
        compose.waitForIdle()
        val title = when (name) {
            "hint-unavailable" -> "No logical step"
            "entry-to-review" -> "Entry to review"
            "entries-checked" -> "No errors found"
            else -> null
        }
        if (title != null) compose.onNodeWithText(title).assertIsDisplayed()
        EmulatorSystemDialogs.dismiss()
        val target = File(context.getExternalFilesDir("screenshots"), "v011b-$prefix-$name.png")
        if (dialog) {
            compose.waitForIdle();instrumentation.uiAutomation.waitForIdle(300, 5000)
            val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
            target.parentFile!!.mkdirs();target.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG,100,it)) };bitmap.recycle()
        } else {
            val warmFrame = File(context.cacheDir, "coach-capture-frame.png")
            NativeWindowCapture.save(requireNotNull(activity),warmFrame)
            NativeWindowCapture.save(requireNotNull(activity),target)
            check(warmFrame.delete())
        }
    }
    private fun tap(text: String) {
        val node = compose.onNodeWithText(text)
        if (!node.isDisplayed()) node.performScrollTo()
        node.assertIsDisplayed().performClick()
    }
    private fun coachAction(text: String) {
        val button = compose.onNodeWithText(text)
        if (!button.isDisplayed()) button.performScrollTo()
        button.assertIsDisplayed()
        // Keep production IO intact, then deliver the test frame on Main.
        compose.mainClock.autoAdvance = false
        try {
            button.performClick()
            compose.waitUntil(20000) { !model.state.value.busy }
            compose.runOnUiThread { compose.mainClock.advanceTimeByFrame() }
        } finally { compose.mainClock.autoAdvance = true }
        compose.waitForIdle()
    }
    private fun hint() = coachAction("Hint")
    @Test fun anExplicitCheckFindsTheEntryAndEraseUndoAndResumePreserveIt() {
        launch();hint()
        compose.onNodeWithText("Check my work").assertIsDisplayed()
        assertEquals(0,model.state.value.profile.activeSolo!!.checksUsed)
        assertEquals(0,model.state.value.profile.activeSolo!!.hintsUsed)
        capture("hint-unavailable")
        coachAction("Check my work")
        compose.waitUntil(20000) { model.state.value.entryCheck != null }
        assertEquals(SudokuAssistance.CheckKind.INCORRECT_ENTRY,model.state.value.entryCheck!!.kind)
        assertEquals(1,model.state.value.snapshot!!.cells[2].value)
        assertEquals(1,LocalPlayerStore(File(context.filesDir,"local-player.json")).current().activeSolo!!.checksUsed)
        compose.onNodeWithText("Erase entry").assertIsDisplayed();capture("entry-to-review")
        tap("Erase entry")
        compose.waitUntil(15000) { model.state.value.pendingId == null && model.state.value.snapshot!!.cells[2].value == 0 }
        tap("Undo")
        compose.waitUntil(15000) { model.state.value.pendingId == null && model.state.value.snapshot!!.cells[2].value == 1 }
        assertEquals(1,model.state.value.profile.activeSolo!!.checksUsed)
        tap("Home");compose.waitUntil(15000) { model.state.value.page == Page.HOME }
        compose.onNodeWithText("Continue").performScrollTo().performClick()
        compose.waitUntil(15000) { model.state.value.canEdit }
        assertEquals(1,model.state.value.snapshot!!.cells[2].value)
        assertEquals(1,model.state.value.profile.activeSolo!!.checksUsed)
        compose.waitUntil(20000) { compose.onAllNodesWithTag("input_dock").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("input_dock").assertIsDisplayed()
        capture("checked-puzzle-resumed",dialog=false)
    }
    @Test fun aCheckedFinishDisplaysItsSavedCountAndAssistedStatus() {
        val digits = "123456789456789123789123456234567891567891234891234567345678912678912345912345678".map { it.digitToInt() }
        val puzzle = Puzzle("coach-finish",digits.mapIndexed { i,n -> if (i in listOf(0,1,9,10)) 0 else n },"EASY")
        writeProgress(PersonalPlay.edit(PersonalPlay.newSolo(puzzle,1000L),0,Kind.VALUE,1,elapsedMs=1000L,nowMs=2000L)!!)
        launch();hint();coachAction("Check my work")
        compose.waitUntil(20000) { model.state.value.entryCheck != null }
        assertEquals(SudokuAssistance.CheckKind.CORRECT_SO_FAR,model.state.value.entryCheck!!.kind)
        assertEquals(1,model.state.value.profile.activeSolo!!.checksUsed)
        assertEquals(1,model.state.value.profile.activeSolo!!.hintsUsed)
        capture("entries-checked")
        tap("Back to puzzle")
        for ((cell,digit) in listOf(1 to 2,9 to 4,10 to 5)) {
            instrumentation.runOnMainSync { model.select(cell);model.number(digit) }
            compose.waitUntil(15000) { model.state.value.pendingId == null && model.state.value.snapshot!!.cells[cell].value == digit }
        }
        compose.onNodeWithText("Puzzle completed").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("1 entry check used").performScrollTo().assertIsDisplayed()
        val record = model.state.value.profile.records.single()
        assertTrue(record.assisted);assertEquals(1,record.checksUsed);assertEquals(1,record.hintsUsed)
        capture("checked-finish",dialog=false)
    }
}
