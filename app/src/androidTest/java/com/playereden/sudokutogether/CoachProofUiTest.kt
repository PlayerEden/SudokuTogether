package com.playereden.sudokutogether

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.playereden.sudokutogether.core.Cell
import com.playereden.sudokutogether.core.Puzzle
import com.playereden.sudokutogether.core.SOLO
import com.playereden.sudokutogether.core.Snapshot
import com.playereden.sudokutogether.core.SudokuAssistance
import org.junit.Rule
import org.junit.Test

class CoachProofUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun largeTextCoachShowsACandidateRemovalBeforeThePlacement() {
        val values = "600945002002673000054128060001567400576894231849312006003056920000031600465289003"
            .map(Char::digitToInt)
        val puzzle = Puzzle("coach-proof", values, "MEDIUM")
        val hint = requireNotNull(SudokuAssistance.nextCoachHint(puzzle, values))
        val snapshot = Snapshot("coach-preview", "round", puzzle, values.map { Cell(it) })
        val state = GameUiState(page = Page.PLAY, role = SOLO, snapshot = snapshot, hint = hint)
        compose.setContent {
            SudokuTogetherTheme {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                    PuzzleCoachDialog(state, polishActions())
                }
            }
        }

        compose.onNodeWithText("Look at the outlined squares.").assertExists()
        compose.onNodeWithText("Show why").performScrollTo().performClick()
        compose.onNodeWithText("Step 1 of 2").assertExists()
        compose.onNodeWithTag("coach_cell_78")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Proof square"))
        compose.onNodeWithTag("coach_cell_62")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Hint target"))
        compose.onNodeWithText("Cross off 7 at row 7, column 9.").assertExists()
        compose.onNodeWithText("Next step").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Step 2 of 2").assertIsDisplayed()
        compose.onNodeWithText(hint.steps[1].explanation).assertIsDisplayed()
        compose.onNodeWithTag("coach_cell_26")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Hint target"))
        compose.onNodeWithText("Previous step").performScrollTo().assertIsDisplayed()
    }
}
