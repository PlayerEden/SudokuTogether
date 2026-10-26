package com.playereden.sudokutogether

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaySmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun hostCanEnterUndoAndSharePencilMarksWithoutInternet() {
        compose.onNodeWithText("Host a puzzle").assertExists().performClick()
        compose.waitUntil(20000) { compose.onAllNodesWithTag("cell_0").fetchSemanticsNodes().isNotEmpty() }
        val empty = compose.onAllNodes(hasContentDescription("empty", substring = true)).fetchSemanticsNodes().first()
        val tag = empty.config[androidx.compose.ui.semantics.SemanticsProperties.TestTag]
        compose.onNodeWithTag(tag).performClick()
        compose.onNodeWithTag("number_9").performClick()
        compose.waitUntil(10000) { compose.onAllNodes(hasContentDescription("value 9", substring = true)).fetchSemanticsNodes().any { it.id == empty.id } }
        compose.onNodeWithText("Undo").performClick()
        compose.waitUntil(10000) { compose.onNodeWithTag(tag).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ContentDescription].any { it.contains("empty") } }
        compose.onNodeWithText("Notes").performClick()
        compose.onNodeWithTag("number_3").performClick()
        compose.waitUntil(10000) { compose.onNodeWithTag(tag).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ContentDescription].any { it.contains("notes 3") } }
        compose.onNodeWithText("Erase").performClick()
        compose.waitUntil(10000) { compose.onNodeWithTag(tag).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ContentDescription].any { it.endsWith("empty") } }
        compose.onNodeWithText("Room code", substring = true).assertExists()
    }
}
