package com.playereden.sudokutogether

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

/** Player-visible navigation/layout contracts, exercised through the real Activity. */
@RunWith(AndroidJUnit4::class)
class GameConvenienceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun ready() {
        compose.waitUntil(30000) {
            compose.onAllNodes(hasText("New puzzle") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
        }
        EmulatorSystemDialogs.dismiss()
        compose.waitUntil(10000) {
            if (!compose.activity.hasWindowFocus()) EmulatorSystemDialogs.dismiss()
            compose.activity.hasWindowFocus()
        }
    }

    private fun newSolo() {
        compose.onNodeWithTag("difficulty_EASY").performScrollTo().performClick()
        compose.onNodeWithText("New puzzle").performScrollTo().performClick()
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithText("Start new").performClick()
        compose.waitUntil(30000) { compose.onAllNodesWithTag("input_dock").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun resumeIsVisibleWithoutScrolling() {
        newSolo()
        val before = compose.onNodeWithTag("cell_0").fetchSemanticsNode().config[
            androidx.compose.ui.semantics.SemanticsProperties.ContentDescription]
        compose.onNodeWithText("Home").performClick()
        compose.onNodeWithText("Continue").assertIsDisplayed().performClick()
        compose.waitUntil(30000) { compose.onAllNodesWithTag("input_dock").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("cell_0").assertContentDescriptionEquals(*before.toTypedArray())
    }

    @Test fun journalDestinationsAreOneTapAway() {
        compose.onNodeWithTag("nav_collection").assertIsDisplayed().performClick()
        compose.onNodeWithText("Player icons").assertIsSelected()
        compose.onNodeWithText("Numbers").performScrollTo().performClick()
        compose.onNodeWithText("Number colors").assertExists()
        compose.onNodeWithText("Number fonts").assertExists()
        compose.onNodeWithText("Boards").performScrollTo().performClick()
        compose.onNodeWithText("Board styles").assertExists()
        compose.onNodeWithTag("nav_collection").assertIsSelected()
        compose.onNodeWithTag("nav_settings").assertIsDisplayed().performClick()
        compose.onNodeWithText("Touch feedback").assertExists()
        compose.onNodeWithText("Clean pencil marks").assertExists()
        compose.onNodeWithTag("nav_settings").assertIsSelected()
        compose.onNodeWithTag("nav_records").performClick()
        compose.onNodeWithText("Game history").performScrollTo().performClick()
        compose.onNodeWithText("DAILY HISTORY").assertExists()
        compose.onNodeWithTag("nav_play").performClick()
        compose.onNodeWithText("New puzzle").assertExists()
    }

    @Test fun compactBoardAndKeypadAreVisibleTogether() {
        newSolo()
        val first = compose.onNodeWithTag("cell_0").getUnclippedBoundsInRoot()
        val last = compose.onNodeWithTag("cell_80").getUnclippedBoundsInRoot()
        val dock = compose.onNodeWithTag("input_dock").getUnclippedBoundsInRoot()
        assertTrue("The last row must fit above the keypad: $last / $dock", last.bottom <= dock.top)
        assertTrue("The first row must be on screen: $first", first.top.value >= 0)
        compose.onNodeWithTag("cell_80").assertIsDisplayed()
        compose.onNodeWithTag("number_9").assertIsDisplayed()
    }
}
