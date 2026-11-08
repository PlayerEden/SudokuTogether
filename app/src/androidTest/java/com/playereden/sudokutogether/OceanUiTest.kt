package com.playereden.sudokutogether

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.playereden.sudokutogether.core.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OceanUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun oceanFilterShowsLockedProgressAndLetsThePlayerEquipOctopus() {
        val data = LocalPlayerData(records = listOf(GameRecord("one", SOLO, "SOLVED", "p", 1000, 1, 1000)))
        val appearance = mutableStateOf(Appearance())
        compose.setContent {
            SudokuTogetherTheme {
                SudokuApp(GameUiState(page = Page.PROFILE, profile = data, appearance = appearance.value),
                    polishActions(cosmetic = { group, id ->
                        assertEquals("portrait", group)
                        appearance.value = appearance.value.copy(portrait = id)
                    }), {})
            }
        }
        compose.onNodeWithTag("nav_collection").performClick()
        compose.onNodeWithText("Ocean").performScrollTo().performClick()
        compose.onNodeWithText("1 of 6 available").assertIsDisplayed()
        compose.onNodeWithTag("portrait_woodland_owl").assertDoesNotExist()
        compose.onNodeWithTag("cosmetic_emblem_tile").assertDoesNotExist()
        compose.onNodeWithTag("portrait_ocean_seal").performScrollTo().performClick()
        compose.onNodeWithText("Ocean player icon").assertIsDisplayed()
        compose.onNodeWithText("Complete 18 puzzles").assertIsDisplayed()
        compose.onNodeWithText("1 of 18 completed").assertIsDisplayed()
        compose.onNodeWithText("Use icon").assertDoesNotExist()
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithTag("portrait_ocean_octopus").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("", appearance.value.portrait) }
        compose.onNodeWithText("Use icon").performClick()
        compose.onNodeWithTag("portrait_ocean_octopus").assertIsSelected()
        compose.onNodeWithText("Octopus · Ocean").performScrollTo().assertIsDisplayed()
    }
}
