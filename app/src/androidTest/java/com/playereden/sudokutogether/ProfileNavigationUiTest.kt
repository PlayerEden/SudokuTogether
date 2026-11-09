package com.playereden.sudokutogether

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class ProfileNavigationUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun homeAvatarOpensProfileWithReachableRecordsAndAchievementsAtLargeText() {
        val name = "Alexandria Puzzle Friend"
        val state = mutableStateOf(GameUiState(name = name))
        val actions = polishActions().copy(profile = { state.value = state.value.copy(page = Page.PROFILE) })
        compose.setContent {
            SudokuTogetherTheme {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                    Box(Modifier.width(320.dp).fillMaxHeight()) { SudokuApp(state.value, actions, {}) }
                }
            }
        }

        compose.onNodeWithContentDescription("My profile & records").performClick()
        compose.onNodeWithTag("nav_records").assertIsSelected().assertContentDescriptionEquals("Profile")
        val layout = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(name).performScrollTo().assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layout) }
        assertFalse(layout.single().hasVisualOverflow)
        compose.onNodeWithText("Game history").performScrollTo().performClick()
        compose.onNodeWithText("DAILY HISTORY").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to Profile").performScrollTo().performClick()
        compose.onNodeWithText("View achievements").performScrollTo().performClick()
        compose.onNodeWithText("First Grid").performScrollTo().assertIsDisplayed()
    }
}
