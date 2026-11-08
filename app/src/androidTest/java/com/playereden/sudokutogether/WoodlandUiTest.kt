package com.playereden.sudokutogether

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import com.playereden.sudokutogether.core.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class WoodlandUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun largeTextKeepsTheBasicIconActionAndLockedMilestonesReachable() {
        var selected: String? = null
        compose.setContent { SudokuTogetherTheme { CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
            Box(Modifier.width(320.dp).fillMaxHeight()) {
                SudokuApp(GameUiState(page = Page.PROFILE, appearance = Appearance(portrait = "woodland_owl")),
                    polishActions(cosmetic = { group, id -> selected = "$group:$id" }), {})
            }
        } } }
        compose.onNodeWithTag("nav_collection").performClick()
        compose.onNodeWithText("Woodland").performScrollTo()
        val titleLayout = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText("Woodland").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(titleLayout) }
        assertEquals("The collection title must stay whole at large text", 1, titleLayout.single().lineCount)
        compose.onNodeWithText("Use basic icon").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("emblem:tile", selected) }
        compose.onNodeWithTag("portrait_woodland_cat").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Complete 15 puzzles").assertIsDisplayed()
    }
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
        compose.onNodeWithTag("cosmetic_emblem").performScrollTo()
        compose.onNodeWithTag("cosmetic_emblem_leaf").performScrollTo().assertIsDisplayed().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals("emblem:leaf", selected) }
    }
}
