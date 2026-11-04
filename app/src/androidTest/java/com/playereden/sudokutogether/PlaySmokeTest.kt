package com.playereden.sudokutogether

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.ViewModelProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.text.TextLayoutResult
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class PlaySmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun profileCollectionDisplaysUnlockChoices() {
        compose.onNodeWithTag("nav_collection").performClick()
        compose.onNodeWithTag("nav_collection").assertIsSelected()
        compose.onNodeWithText("Number colors").assertExists()
        compose.onNodeWithText("Board styles").assertExists()
        compose.onNodeWithText("Number fonts").assertExists()
        compose.onNodeWithText("Player emblems").assertExists()
        compose.onNodeWithText("ACHIEVEMENTS").assertExists()
        compose.onNodeWithText("First Grid").assertExists()
        assertTrue(compose.onAllNodesWithText("Forest", substring = true).fetchSemanticsNodes().isNotEmpty())
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithText("Classic number display").assertExists()
        compose.onNodeWithText("Touch feedback").assertExists()
        compose.onNodeWithText("Clean pencil marks").assertExists()
    }

    private fun awaitWindowFocus() {
        compose.waitUntil(10000) {
            if (!compose.activity.hasWindowFocus()) EmulatorSystemDialogs.dismiss()
            compose.activity.hasWindowFocus()
        }
    }
    @Before fun awaitProfileAndWindow() {
        compose.waitUntil(30000) { compose.onAllNodes(hasTestTag("nav_records") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        EmulatorSystemDialogs.dismiss()
        awaitWindowFocus()
    }
    @Test fun homePresentsThreeDistinctModesAndNativeRuleSheet() {
        compose.onNodeWithContentDescription("How to play Sudoku").performScrollTo().assertIsDisplayed()
        EmulatorSystemDialogs.dismiss()
        awaitWindowFocus()
        compose.onNodeWithContentDescription("How to play Sudoku").performClick()
        compose.onNodeWithText("How to play Sudoku").assertExists()
        compose.onNodeWithText("Fill each row", substring = true).assertExists()
        compose.onNodeWithText("Got it").performClick()
        compose.onNodeWithText("Co-op").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Join room").assertExists()
        compose.onNodeWithText("Versus", substring = false).performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Host race").assertExists()
        compose.onNodeWithText("Solo").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("New puzzle").assertExists()
    }

    @Test fun selectedDifficultyAppearsOnTheActualPuzzle() {
        compose.onNodeWithTag("difficulty_MEDIUM").assertExists().performClick()
        compose.onNodeWithTag("difficulty_MEDIUM").assertIsSelected()
        compose.onNodeWithText("New puzzle").performScrollTo().assertIsDisplayed().performClick()
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Start new").performClick()
        }
        try {
            compose.waitUntil(60000) { compose.onAllNodesWithTag("cell_0").fetchSemanticsNodes().isNotEmpty() }
        } catch (error: Throwable) {
            val state = androidx.lifecycle.ViewModelProvider(compose.activity)[GameViewModel::class.java].state.value
            println("SUDOKU_DIFFICULTY_DIAGNOSTIC: page=${state.page}, busy=${state.busy}, difficulty=${state.selectedDifficulty}, snapshot=${state.snapshot != null}, message=${state.message}")
            throw error
        }
        compose.onNodeWithText("Medium", substring = true).assertExists()
        compose.onNodeWithTag("input_dock").assertIsDisplayed()
    }

    @Test fun dailyChallengeDoesNotReplaceNormalSoloProgress() {
        compose.onNodeWithText("New puzzle").performScrollTo().assertIsDisplayed().performClick()
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithText("Start new").performClick()
        compose.waitUntil(20000) { compose.onAllNodesWithTag("input_dock").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Home").performClick()
        compose.onNodeWithText("Continue").assertExists()

        compose.onNodeWithTag("daily_challenge").performScrollTo().performClick()
        compose.waitUntil(20000) {
            compose.onAllNodesWithTag("input_dock").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Daily", substring = false).assertExists()
        compose.onNodeWithTag("input_dock").assertIsDisplayed()
        compose.onNodeWithText("Home").performClick()
        compose.onNodeWithText("In progress").assertExists()
        compose.onNodeWithText("Continue").assertExists()

        compose.onNodeWithTag("nav_records").performClick()
        compose.onNodeWithText("DAILY HISTORY").assertExists()
        val date = com.playereden.sudokutogether.core.DailyChallenge.today()
        compose.onNodeWithTag("daily_history_" + date).performScrollTo().performClick()
        compose.waitUntil(20000) {
            compose.onAllNodesWithTag("input_dock").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Daily", substring = false).assertExists()
        compose.onNodeWithTag("input_dock").assertIsDisplayed()
    }

    @Test fun soloHintsExplainOneDeductionWithoutPlayingIt() {
        compose.onNodeWithText("New puzzle").performScrollTo().assertIsDisplayed().performClick()
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Start new").performClick()
        }
        compose.waitUntil(20000) { compose.onAllNodesWithText("Hint").fetchSemanticsNodes().isNotEmpty() }
        lateinit var model: GameViewModel
        compose.runOnUiThread { model = ViewModelProvider(compose.activity)[GameViewModel::class.java] }
        val before = model.state.value.snapshot!!.cells.map { it.copy() }
        val hintsBefore = model.state.value.profile.activeSolo!!.hintsUsed
        compose.onNodeWithText("Hint").performClick()
        compose.waitUntil(20000) {
            compose.onAllNodesWithText("Your next deduction").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(model.state.value.hint!!.explanation).assertExists()
        assertEquals(before,model.state.value.snapshot!!.cells)
        assertEquals(hintsBefore+1,model.state.value.profile.activeSolo!!.hintsUsed)
        compose.onNodeWithText("Back to puzzle").performClick()
        compose.onNodeWithTag("input_dock").assertIsDisplayed()
    }

    @Test fun hostedRoomHasCompactChatSheet() {
        compose.onNodeWithText("Co-op").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Host room").performScrollTo().assertIsDisplayed().performClick()
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Start new").performClick()
        }
        compose.waitUntil(20000) { compose.onAllNodesWithTag("cell_0").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Copy code").assertExists()
        compose.onNodeWithTag("input_dock").assertIsDisplayed()
        compose.onNodeWithText("Chat").performClick()
        compose.onNodeWithText("Room chat").assertExists()
        compose.onNodeWithText("Message your partner").assertExists()
        compose.onNodeWithText("Send").assertExists()
        compose.waitForIdle()
        compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss), useUnmergedTree = true)
            .onFirst().performSemanticsAction(SemanticsActions.Dismiss) { it() }
        compose.waitUntil(20000) { compose.onAllNodesWithText("Room chat").fetchSemanticsNodes().isEmpty() }
        compose.waitForIdle()
    }

    @Test fun soloPlayCanResumeAndOpenLocalProfile() {
        compose.onNodeWithText("New puzzle").performScrollTo().assertIsDisplayed().performClick()
        // Another smoke test can leave a partially solved local puzzle.
        // Starting fresh asks for confirmation so we don't discard progress.
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Start new").performClick()
        }
        compose.waitUntil(20000) { compose.onAllNodesWithTag("cell_0").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("input_dock").assertIsDisplayed()
        compose.onNodeWithTag("number_9").assertIsDisplayed()
        compose.onNodeWithText("Solo").assertExists()
        compose.onNodeWithText("Home").performClick()
        compose.onNodeWithText("Continue").assertExists()
        compose.onNodeWithTag("nav_records").performClick()
        compose.onNodeWithTag("nav_records").assertIsSelected()
        compose.onNodeWithText("Best unassisted:", substring = true).assertExists()
    }

    @Test fun raceHostWaitsForSecondPlayerBeforeAllowingInput() {
        compose.onNodeWithText("Versus", substring = false).performScrollTo().performClick()
        // Keep this pause-rule test independent of another test's difficulty preference.
        compose.onNodeWithTag("difficulty_EASY").performScrollTo().assertIsEnabled().performClick()
        compose.onNodeWithText("Host race").performScrollTo().assertIsEnabled().performClick()
        // Like co-op, a prior run can leave a saved race that needs explicit replacement.
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Start new").performClick()
        }
        compose.waitUntil(20000) { compose.onAllNodesWithTag("cell_0").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Versus").assertExists()
        compose.onNodeWithText("Waiting for your rival", substring = true).assertExists()
        compose.onNodeWithText("Notes").assertIsNotEnabled()
    }

    @Test fun hostCanEnterUndoAndSharePencilMarksWithoutInternet() {
        compose.onNodeWithText("Co-op").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Host room").performScrollTo().assertIsDisplayed().performClick()
        // A prior emulator test may leave a saved host room on this device.
        // The expected confirmation is part of the real user flow.
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Start new").performClick()
        }
        compose.waitUntil(20000) { compose.onAllNodesWithTag("cell_0").fetchSemanticsNodes().isNotEmpty() }
        val empty = compose.onAllNodes(hasContentDescription("empty", substring = true)).fetchSemanticsNodes().first()
        val tag = empty.config[androidx.compose.ui.semantics.SemanticsProperties.TestTag]
        compose.onNodeWithTag(tag).performClick()
        compose.onNodeWithTag("number_9").assertExists().performClick()
        compose.waitUntil(10000) { compose.onAllNodes(hasContentDescription("value 9", substring = true)).fetchSemanticsNodes().any { it.id == empty.id } }
        compose.onNodeWithText("Undo").performClick()
        compose.waitUntil(10000) { compose.onNodeWithTag(tag).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ContentDescription].any { it.contains("empty") } }
        compose.onNodeWithText("Notes").performClick()
        compose.onNodeWithTag("number_3").performClick()
        compose.waitUntil(10000) { compose.onNodeWithTag(tag).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ContentDescription].any { it.contains("notes 3") } }
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNode(hasText("3") and hasAnyAncestor(hasTestTag(tag)), useUnmergedTree = true)
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val layout = layouts.single()
        val glyph = layout.getBoundingBox(0)
        assertTrue("Pencil glyph must fit: glyph=$glyph, box=${layout.size}",
            glyph.left >= -1f && glyph.top >= -1f && glyph.right <= layout.size.width + 1f && glyph.bottom <= layout.size.height + 1f)
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10000) { compose.onNodeWithTag(tag).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ContentDescription].any { it.contains("notes 3") } }
        compose.onNodeWithText("Erase").performClick()
        compose.waitUntil(10000) { compose.onNodeWithTag(tag).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ContentDescription].any { it.endsWith("empty") } }
        compose.onNodeWithContentDescription("Room code", substring = true).assertExists()
    }
}
