package com.playereden.sudokutogether

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import com.playereden.sudokutogether.core.Appearance
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NumberFontPreviewTest {
    @get:Rule val compose = createComposeRule()

    @Test fun collectionPreviewFollowsClassicNumberDisplay() {
        val state = mutableStateOf(GameUiState(
            page = Page.PROFILE,
            appearance = Appearance(ink = "rose", board = "linen", font = "serif"),
            uniformNumbers = true
        ))
        compose.setContent { SudokuTogetherTheme { SudokuApp(state.value, polishActions(), {}) } }
        compose.onNodeWithTag("nav_collection").performClick()
        compose.onNodeWithText("Forest · Linen · Clear").performScrollTo().assertIsDisplayed()
        assertEquals(fontFor("clear"), previewDigit("1").layoutInput.style.fontFamily)
        val clue = previewDigit("2").layoutInput.style
        assertEquals(fontFor("clear"), clue.fontFamily)
        assertEquals(FontWeight.Bold, clue.fontWeight)

        compose.runOnIdle { state.value = state.value.copy(uniformNumbers = false) }
        compose.onNodeWithText("Rose · Linen · Bookish").performScrollTo().assertIsDisplayed()
        assertEquals(fontFor("serif"), previewDigit("1").layoutInput.style.fontFamily)
        assertEquals(fontFor("clear"), previewDigit("2").layoutInput.style.fontFamily)
    }

    private fun previewDigit(digit: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(digit, useUnmergedTree = true)
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        return results.single()
    }
}
