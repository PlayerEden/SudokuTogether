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
