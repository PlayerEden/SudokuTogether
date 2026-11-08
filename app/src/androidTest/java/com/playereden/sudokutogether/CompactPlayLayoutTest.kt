package com.playereden.sudokutogether

import android.content.pm.ActivityInfo
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.playereden.sudokutogether.core.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream

/** Exact presentation fixtures cover headers without requiring a second phone. */
@RunWith(AndroidJUnit4::class)
class CompactPlayLayoutTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private var scenario: ActivityScenario<MainActivity>? = null
    private var oldFont = 1f
    private var oldSize: String? = null
    private val actions = GameActions(name = {}, host = {}, showJoin = {}, home = {}, join = { _, _ -> },
        select = {}, number = {}, toggleNotes = {}, erase = {}, undo = {}, replacePartner = {}, newPuzzle = {},
        solo = {}, profile = {}, hostRace = {}, sendChat = {}, chatVisible = {}, cosmetic = { _, _ -> },
        uniformNumbers = {}, touchFeedback = {}, difficulty = {}, hint = {}, dismissHint = {}, autoCleanNotes = {}, daily = {}, openDaily = {})

    private fun shell(command: String) = instrumentation.uiAutomation.executeShellCommand(command).use { p ->
        FileInputStream(p.fileDescriptor).use { it.readBytes().toString(Charsets.UTF_8) }
    }
    @Before fun configure() {
        oldFont = Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE, 1f)
        oldSize = Regex("Override size: (\\d+x\\d+)").find(shell("wm size"))?.groupValues?.get(1)
        shell("settings put system font_scale 2.0"); shell("wm size 640x1136")
    }
    @After fun restore() {
        scenario?.close(); shell("settings put system font_scale $oldFont"); shell("wm size ${oldSize ?: "reset"}")
    }
    private fun fixture(role: String, mode: String = COOP): GameUiState {
        val digits = "123456789456789123789123456234567891567891234891234567345678912678912345912345678".map { it.digitToInt() }
        val puzzle = Puzzle("layout-fixture", digits.mapIndexed { i, n -> if (i == 0) 0 else n }, "EASY")
        return GameUiState(page = Page.PLAY, role = role, selected = 0, roomCode = "123456", unreadChat = 999,
            snapshot = Snapshot("layout", "layout-epoch", puzzle, puzzle.clues.map { Cell(it) }, mode = mode), soloElapsedMs = 4500L)
    }
    private fun launch(landscape: Boolean) {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario!!.onActivity { it.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        compose.waitUntil(20000) { (context.resources.configuration.screenWidthDp > context.resources.configuration.screenHeightDp) == landscape }
        EmulatorSystemDialogs.dismiss()
    }
    private fun capture(name: String) {
        val file = File(context.getExternalFilesDir("screenshots"), "v010d-compact-$name.png")
        var activity: MainActivity? = null
        scenario!!.onActivity { activity = it }
        NativeWindowCapture.save(requireNotNull(activity), file)
    }
    @Test fun activeCompactLandscapeAt200PercentKeepsBoardAndEveryToolReachable() {
        launch(true)
        scenario!!.onActivity { it.setContent { SudokuTogetherTheme { SudokuApp(fixture(SOLO), actions, {}) } } }
        compose.onNodeWithTag("input_dock").assertExists(); capture("landscape-before-scroll")
        for (cell in 0..80) compose.onNodeWithTag("cell_$cell").performScrollTo().assertIsDisplayed()
        for (tool in listOf("Notes", "Erase", "Undo", "Hint")) compose.onNodeWithText(tool).performScrollTo().assertIsDisplayed()
        for (number in 1..9) compose.onNodeWithTag("number_$number").performScrollTo().assertIsDisplayed()
        capture("landscape-controls")
    }
    @Test fun compactVersusLandscapeKeepsPresenceAboveTheKeypadBesideTheBoard() {
        launch(true)
        val state = fixture(HOST, VERSUS).let {
            it.copy(snapshot = requireNotNull(it.snapshot).copy(guestName = "Taylor"),
                status = RoomStatus(guestActive = true, reserved = true), connected = true)
        }
        scenario!!.onActivity { it.setContent { SudokuTogetherTheme { SudokuApp(state, actions, {}) } } }

        val presence = compose.onNodeWithTag("versus_presence").assertIsDisplayed().getUnclippedBoundsInRoot()
        val board = compose.onNodeWithTag("board_frame").assertIsDisplayed().getUnclippedBoundsInRoot()
        val dock = compose.onNodeWithTag("input_dock").getUnclippedBoundsInRoot()
        assertTrue(presence.left >= board.right)
        assertTrue(presence.bottom <= dock.top)
        capture("versus-landscape")
        compose.onNodeWithTag("number_9").performScrollTo().assertIsDisplayed()
    }
    @Test fun hostHeadersAt200PercentKeepCodeAndActionsVisibleWithUnreadChat() {
        launch(false)
        val shown = mutableStateOf(fixture(HOST))
        scenario!!.onActivity { it.setContent { SudokuTogetherTheme { SudokuApp(shown.value, actions, {}) } } }
        for (mode in listOf(COOP, VERSUS)) {
            compose.runOnIdle { shown.value = fixture(HOST, mode) }
            compose.onNodeWithTag("input_dock").assertExists(); capture("$mode-header")
            val bounds = compose.onRoot().getUnclippedBoundsInRoot()
            for (node in listOf(compose.onNodeWithText("Game details"),
                compose.onNodeWithText("Chat (999)"), compose.onNodeWithContentDescription("How to play Sudoku"))) {
                node.assertIsDisplayed()
                val rect = node.getUnclippedBoundsInRoot()
                assertTrue("Header must fit: $rect in $bounds", (rect.right - rect.left).value > 10 && rect.left >= bounds.left && rect.right <= bounds.right && rect.bottom <= bounds.bottom)
            }
        }
    }
}
