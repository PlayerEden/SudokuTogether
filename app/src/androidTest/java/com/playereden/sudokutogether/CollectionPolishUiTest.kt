package com.playereden.sudokutogether

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.playereden.sudokutogether.core.Appearance
import com.playereden.sudokutogether.core.GameRecord
import com.playereden.sudokutogether.core.LocalPlayerData
import com.playereden.sudokutogether.core.VERSUS
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CollectionPolishUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun compactProfileActionsEditTheNameAndOpenPlayerIconsAtLargeText() {
        val state = mutableStateOf(GameUiState(page = Page.PROFILE, name = "Alexandria Puzzle Friend"))
        val actions = polishActions().copy(name = { state.value = state.value.copy(name = it) })
        compose.setContent {
            SudokuTogetherTheme {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                    Box(Modifier.width(320.dp).fillMaxHeight()) { SudokuApp(state.value, actions, {}) }
                }
            }
        }

        val edit = compose.onNodeWithContentDescription("Edit name").assertIsDisplayed()
        val editBounds = edit.getUnclippedBoundsInRoot()
        assertTrue(editBounds.right - editBounds.left >= 48.dp)
        edit.performClick()
        compose.onNodeWithTag("player_name").performTextReplacement("Puzzle partner")
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithText("Puzzle partner").assertIsDisplayed()

        compose.onNodeWithText("Change icon").performScrollTo().performClick()
        compose.onNodeWithTag("nav_collection").assertIsSelected()
        compose.onNodeWithText("Player icons").assertIsSelected()
        compose.onNodeWithTag("cosmetic_emblem_tile").performScrollTo().assertIsDisplayed()
    }

    @Test fun equippedAndAvailableIconsAppearAheadOfLockedIcons() {
        val data = LocalPlayerData(records = listOf(GameRecord("race", VERSUS, "WIN", "p", 1000, 1, 1000)))
        val state = mutableStateOf(GameUiState(page = Page.PROFILE, profile = data, appearance = Appearance(portrait = "woodland_rabbit")))
        compose.setContent { SudokuTogetherTheme { SudokuApp(state.value, polishActions(), {}) } }

        compose.onNodeWithTag("nav_collection").performClick()
        compose.onNodeWithText("6 of 24 available").assertIsDisplayed()
        val equipped = compose.onNodeWithTag("portrait_woodland_rabbit").assertIsSelected()
        val starter = compose.onNodeWithTag("cosmetic_emblem_tile")
        val lastAvailable = compose.onNodeWithTag("portrait_ocean_octopus")
        val firstLocked = compose.onNodeWithTag("cosmetic_emblem_leaf")
        val equippedBounds = equipped.getUnclippedBoundsInRoot()
        val starterBounds = starter.getUnclippedBoundsInRoot()
        assertTrue(
            "The equipped icon should come first in the gallery",
            equippedBounds.top < starterBounds.top ||
                (equippedBounds.top == starterBounds.top && equippedBounds.left < starterBounds.left)
        )
        assertTrue("Locked icons should be below the available icons",
            lastAvailable.getUnclippedBoundsInRoot().bottom <= firstLocked.getUnclippedBoundsInRoot().top)

        compose.runOnIdle { state.value = state.value.copy(profile = data.copy(records = emptyList())) }
        compose.onNodeWithText("5 of 24 available").assertIsDisplayed()
        equipped.assertIsSelected().assertIsDisplayed()
        assertTrue(equipped.getUnclippedBoundsInRoot().bottom <= firstLocked.getUnclippedBoundsInRoot().top)
    }

    @Test fun numbersLetsPlayersBrowseFontsSeparatelyFromColors() {
        compose.setContent { SudokuTogetherTheme { SudokuApp(GameUiState(page = Page.PROFILE), polishActions(), {}) } }

        compose.onNodeWithTag("nav_collection").performClick()
        compose.onNodeWithText("Numbers").performClick()
        compose.onNodeWithText("Number colors").assertIsSelected()
        compose.onNodeWithTag("cosmetic_ink_forest").assertIsSelected()
        compose.onNodeWithTag("cosmetic_font_clear").assertDoesNotExist()
        compose.onNodeWithText("Number fonts").performClick().assertIsSelected()
        compose.onNodeWithTag("cosmetic_font_clear").assertIsSelected()
        compose.onNodeWithTag("cosmetic_ink_forest").assertDoesNotExist()
    }
}
