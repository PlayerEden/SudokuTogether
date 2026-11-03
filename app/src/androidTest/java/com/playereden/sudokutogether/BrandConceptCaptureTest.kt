package com.playereden.sudokutogether

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.playereden.sudokutogether.core.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** The production Home and navigation, with a controlled valid puzzle for artwork comparison. */
@RunWith(AndroidJUnit4::class)
class BrandConceptCaptureTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val actions = GameActions(name = {}, host = {}, showJoin = {}, home = {}, join = { _, _ -> },
        select = {}, number = {}, toggleNotes = {}, erase = {}, undo = {}, replacePartner = {}, newPuzzle = {},
        solo = {}, profile = {}, hostRace = {}, sendChat = {}, chatVisible = {}, cosmetic = { _, _ -> },
        uniformNumbers = {}, touchFeedback = {}, difficulty = {}, hint = {}, dismissHint = {}, autoCleanNotes = {}, daily = {}, openDaily = {})
    @Test fun capturesThreeOriginalMarksOnTheSameHomeScreen() {
        var seed = 1964L
        val puzzle = PuzzleSupply.nextRated(HumanSudoku.Grade.EASY, emptyList(), { seed++ }).puzzle
        val progress = PersonalPlay.newSolo(puzzle, 1000L).copy(elapsedMs = 112000L)
        val state = GameUiState(page = Page.HOME, role = SOLO, resumeSolo = true,
            profile = LocalPlayerData(activeSolo = progress))
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            EmulatorSystemDialogs.dismiss()
            for ((index, style) in GridMatesMarkStyle.entries.withIndex()) {
                scenario.onActivity { activity -> activity.setContent {
                    SudokuTogetherTheme {
                        Surface(color = Cream) {
                            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                                Box(Modifier.weight(1f).fillMaxWidth()) {
                                    HomeScreen(state, actions, {}, {}, {}, {}, {}, style)
                                }
                                GameNavigation(Page.HOME, JournalTab.RECORDS, {}, {})
                            }
                        }
                    }
                } }
                compose.onNodeWithText("GridMates").assertIsDisplayed()
                compose.onNodeWithText("Continue").assertIsDisplayed()
                compose.onNodeWithText("New puzzle").assertIsDisplayed()
                var activity: MainActivity? = null
                scenario.onActivity { activity = it }
                NativeWindowCapture.save(requireNotNull(activity), File(context.getExternalFilesDir("screenshots"), "gridmates-concept-${index + 1}.png"))
            }
        }
    }
}
