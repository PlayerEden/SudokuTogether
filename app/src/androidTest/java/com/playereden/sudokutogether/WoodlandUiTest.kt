package com.playereden.sudokutogether

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.playereden.sudokutogether.core.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class WoodlandUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun collectionOffersAStarterAnimalAndKeepsLockedRequirementsVisible() {
        var selected: String? = null
        compose.setContent { SudokuTogetherTheme { SudokuApp(GameUiState(page = Page.PROFILE), polishActions(cosmetic = { group, id -> selected = "$group:$id" }), {}) } }
        compose.onNodeWithTag("nav_collection").performClick()
        compose.onNodeWithText("Woodland").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("portrait_woodland_owl").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("portrait:woodland_owl", selected) }
        compose.onNodeWithTag("portrait_woodland_rabbit").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Complete 1 puzzle").assertIsDisplayed()
    }
    @Test fun anAssistedCompletedPuzzleUnlocksTheRabbitAndOldEmblemsRemain() {
        val data = LocalPlayerData(records = listOf(GameRecord("one",SOLO,"SOLVED","p",1000,1,1000,assisted = true,hintsUsed = 1)))
        var selected: String? = null
        compose.setContent { SudokuTogetherTheme { SudokuApp(GameUiState(page = Page.PROFILE,profile = data),polishActions(cosmetic = { group,id -> selected = "$group:$id" }),{}) } }
        compose.onNodeWithTag("nav_collection").performClick()
        compose.onNodeWithTag("portrait_woodland_rabbit").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("portrait:woodland_rabbit", selected) }
        compose.onNodeWithText("Player emblems").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("First Sprout").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("emblem:leaf", selected) }
    }
}
