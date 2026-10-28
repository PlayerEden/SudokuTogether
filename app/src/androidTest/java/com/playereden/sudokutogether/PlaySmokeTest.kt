package com.playereden.sudokutogether

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.text.TextLayoutResult
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class PlaySmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun profileCollectionDisplaysUnlockChoices() {
        compose.onNodeWithText("My profile & records").performScrollTo().performClick()
        compose.onNodeWithText("YOUR COLLECTION").assertExists()
        compose.onNodeWithText("Number colors").assertExists()
        compose.onNodeWithText("Board styles").assertExists()
        compose.onNodeWithText("Number fonts").assertExists()
        compose.onNodeWithText("Player emblems").assertExists()
        compose.onNodeWithText("ACHIEVEMENTS").assertExists()
        compose.onNodeWithText("Classic number display").assertExists()
        compose.onNodeWithText("Touch feedback").assertExists()
        compose.onNodeWithText("Clean pencil marks").assertExists()
        compose.onNodeWithText("First Grid").assertExists()
        assertTrue(compose.onAllNodesWithText("Forest", substring = true).fetchSemanticsNodes().isNotEmpty())
    }

    @Test fun homePresentsThreeDistinctModesAndNativeRuleSheet() {
        compose.onNodeWithText("Help").performClick()
        compose.onNodeWithText("How to play Sudoku").assertExists()
        compose.onNodeWithText("Fill each row", substring = true).assertExists()
        compose.onNodeWithText("Got it").performClick()
        compose.onNodeWithText("Co-op").performClick()
        compose.onNodeWithText("Join a puzzle").assertExists()
        compose.onNodeWithText("Versus", substring = false).performClick()
        compose.onNodeWithText("Host a versus race").assertExists()
        compose.onNodeWithText("Solo").performClick()
        compose.onNodeWithText("New solo puzzle").assertExists()
    }

    @Test fun selectedDifficultyAppearsOnTheActualPuzzle() {
        compose.onNodeWithTag("difficulty_MEDIUM").assertExists().performClick()
        compose.onNodeWithTag("difficulty_MEDIUM").assertIsSelected()
        compose.onNodeWithText("New solo puzzle").performClick()
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Start new").performClick()
        }
        compose.waitUntil(20000) {
            compose.onAllNodesWithTag("cell_0").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Medium", substring = true).assertExists()
        compose.onNodeWithTag("input_dock").assertIsDisplayed()
    }

    @Test fun dailyChallengeDoesNotReplaceNormalSoloProgress() {
        compose.onNodeWithText("New solo puzzle").performClick()
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithText("Start new").performClick()
        compose.waitUntil(20000) {
            compose.onAllNodesWithTag("input_dock").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Home").performClick()
        compose.onNodeWithText("Leave room").performClick()
        compose.onNodeWithText("Resume solo puzzle").assertExists()

        compose.onNodeWithTag("daily_challenge").performScrollTo().performClick()
        compose.waitUntil(20000) {
            compose.onAllNodesWithText("Daily", substring = false).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Daily", substring = false).assertExists()
        compose.onNodeWithTag("input_dock").assertIsDisplayed()
        compose.onNodeWithText("Home").performClick()
        compose.onNodeWithText("Leave room").performClick()
        compose.onNodeWithText("Continue today's challenge").assertExists()
        compose.onNodeWithText("Resume solo puzzle").assertExists()

        compose.onNodeWithText("My profile & records").performScrollTo().performClick()
        compose.onNodeWithText("DAILY HISTORY").assertExists()
        val date = com.playereden.sudokutogether.core.DailyChallenge.today()
        compose.onNodeWithTag("daily_history_$" + "{date}").performScrollTo().performClick()
        compose.waitUntil(20000) {
            compose.onAllNodesWithText("Daily", substring = false).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Daily", substring = false).assertExists()
        compose.onNodeWithTag("input_dock").assertIsDisplayed()
    }

    @Test fun soloHintsExplainOneDeductionWithoutPlayingIt() {
        compose.onNodeWithText("New solo puzzle").performClick()
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Start new").performClick()
        }
        compose.waitUntil(20000) { compose.onAllNodesWithText("Hint").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Hint").performClick()
        compose.waitUntil(20000) {
            compose.onAllNodesWithText("Your next deduction").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Suggested number:", substring = true).assertExists()
        compose.onNodeWithText("Nothing was entered for you.", substring = true).assertExists()
        compose.onNodeWithText("Back to puzzle").performClick()
        compose.onNodeWithTag("input_dock").assertIsDisplayed()
    }

    @Test fun hostedRoomHasCompactChatSheet() {
        compose.onNodeWithText("Co-op").performClick()
        compose.onNodeWithText("Host a puzzle").performClick()
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
    }

    @Test fun soloPlayCanResumeAndOpenLocalProfile() {
        compose.onNodeWithText("New solo puzzle").performClick()
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
        compose.onNodeWithText("Leave room").performClick()
        compose.onNodeWithText("Resume solo puzzle").assertExists()
        compose.onNodeWithText("My profile & records").performScrollTo().performClick()
        compose.onNodeWithText("Your records").assertExists()
        compose.onNodeWithText("Best unassisted:", substring = true).assertExists()
        compose.onNodeWithText("Offline profile", substring = true).assertExists()
    }

    @Test fun raceHostWaitsForSecondPlayerBeforeAllowingInput() {
        compose.onNodeWithText("Versus", substring = false).performClick()
        compose.onNodeWithText("Host a versus race").performClick()
        compose.waitUntil(20000) { compose.onAllNodesWithTag("cell_0").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Versus").assertExists()
        compose.onNodeWithText("Waiting for your rival", substring = true).assertExists()
        compose.onNodeWithText("Notes").assertIsNotEnabled()
    }

    @Test fun hostCanEnterUndoAndSharePencilMarksWithoutInternet() {
        compose.onNodeWithText("Co-op").performClick()
        compose.onNodeWithText("Host a puzzle").assertExists().performClick()
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
        compose.onNodeWithText("Room code", substring = true).assertExists()
    }
}
