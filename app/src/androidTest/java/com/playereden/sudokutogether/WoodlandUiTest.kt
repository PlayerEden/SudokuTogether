package com.playereden.sudokutogether

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.playereden.sudokutogether.core.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class WoodlandUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun largeTextKeepsLockedProgressAndTheBasicIconActionReachable() {
        var selected: String? = null
        val data = LocalPlayerData(records = listOf(GameRecord("one", SOLO, "SOLVED", "p", 1000, 1, 1000)))
        compose.setContent { SudokuTogetherTheme { CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
            Box(Modifier.width(320.dp).fillMaxHeight()) {
                SudokuApp(GameUiState(page = Page.PROFILE, profile = data, appearance = Appearance(portrait = "woodland_owl")),
                    polishActions(cosmetic = { group, id -> selected = "$group:$id" }), {})
            }
        } } }
        compose.onNodeWithTag("nav_collection").performClick()
        compose.onNodeWithTag("portrait_woodland_cat").performScrollTo().performClick()
        compose.onNodeWithText("Complete 15 puzzles").assertIsDisplayed()
        compose.onNodeWithText("1 of 15 completed").assertIsDisplayed()
        compose.onNodeWithText("Use icon").assertDoesNotExist()
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithText("Basic").performScrollTo().performClick()
        compose.onNodeWithTag("portrait_woodland_cat").assertDoesNotExist()
        compose.onNodeWithTag("cosmetic_emblem_tile").performScrollTo().performClick()
        compose.onNodeWithText("Use icon").performClick()
        compose.runOnIdle { assertEquals("emblem:tile", selected) }
    }
    @Test fun previewingAnAvailableIconLeavesItUnequippedUntilUse() {
        var selected: String? = null
        compose.setContent { SudokuTogetherTheme { SudokuApp(GameUiState(page = Page.PROFILE), polishActions(cosmetic = { group, id -> selected = "$group:$id" }), {}) } }
        compose.onNodeWithTag("nav_collection").performClick()
        compose.onNodeWithTag("portrait_woodland_owl").performScrollTo().performClick()
        compose.runOnIdle { assertNull(selected) }
        compose.onNodeWithText("Use icon").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("portrait:woodland_owl", selected) }
    }
    @Test fun anAssistedCompletedPuzzleUnlocksTheRabbitAndOldEmblemsRemain() {
        val data = LocalPlayerData(records = listOf(GameRecord("one",SOLO,"SOLVED","p",1000,1,1000,assisted = true,hintsUsed = 1)))
        var selected: String? = null
        compose.setContent { SudokuTogetherTheme { SudokuApp(GameUiState(page = Page.PROFILE,profile = data),polishActions(cosmetic = { group,id -> selected = "$group:$id" }),{}) } }
        compose.onNodeWithTag("nav_collection").performClick()
        compose.onNodeWithTag("portrait_woodland_rabbit").performScrollTo().performClick()
        compose.onNodeWithText("Use icon").performClick()
        compose.runOnIdle { assertEquals("portrait:woodland_rabbit", selected) }
        compose.onNodeWithTag("cosmetic_emblem_leaf").performScrollTo().assertIsDisplayed().assertIsEnabled().performClick()
        compose.onNodeWithText("Use icon").performClick()
        compose.runOnIdle { assertEquals("emblem:leaf", selected) }
    }
}
