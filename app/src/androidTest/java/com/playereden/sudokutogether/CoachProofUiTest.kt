package com.playereden.sudokutogether

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import org.junit.Assert.assertEquals
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
        var selected = -1
        compose.setContent {
            SudokuTogetherTheme {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                    PuzzleCoachDialog(state, polishActions().copy(select = { selected = it }))
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
        compose.onNodeWithText("Go to square").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(26, selected) }
    }

    @Test fun hiddenPairShowsRetainedAndRemovedCandidatesInTheSameSquares() {
        val values = "000008005205090670000000400360000200092106040054070000000030800020500701006807090"
            .map(Char::digitToInt)
        val puzzle = Puzzle("coach-hidden-pair", values, "HARD")
        val hint = requireNotNull(SudokuAssistance.nextCoachHint(puzzle, values))
        val snapshot = Snapshot("coach-hidden-pair", "round", puzzle, values.map { Cell(it) })
        compose.setContent {
            SudokuTogetherTheme {
                PuzzleCoachDialog(GameUiState(page = Page.PLAY, role = SOLO, snapshot = snapshot, hint = hint),
                    polishActions())
            }
        }

        compose.onNodeWithText("Show why").performScrollTo().performClick()
        repeat(3) { compose.onNodeWithText("Next step").performScrollTo().performClick() }
        compose.onNodeWithText("Hidden pair").assertIsDisplayed()
        compose.onNodeWithTag("coach_cell_48")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Hint target and proof square"))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription,
                listOf("Row 6 column 4, empty, notes 2 3 9, cross off 9")))
    }

    @Test fun solutionNumberStaysHiddenUntilAskedAndReturningOnlySelectsTheSquare() {
        val values = "100007090030020008009600500005300900010080002600004000300000010041000007007000300"
            .map(Char::digitToInt)
        val puzzle = Puzzle("coach-reveal", values, "HARD")
        val hint = requireNotNull(SudokuAssistance.nextCoachHint(puzzle, values))
        val step = hint.steps.single()
        val target = step.targetCells.single()
        val snapshot = Snapshot("coach-reveal", "round", puzzle, values.map { Cell(it) })
        var state by mutableStateOf(GameUiState(page = Page.PLAY, role = SOLO, snapshot = snapshot, hint = hint))
        compose.setContent {
            SudokuTogetherTheme {
                PuzzleCoachDialog(state, polishActions().copy(select = { cell ->
                    state = state.copy(selected = cell, hint = null)
                }))
            }
        }

        val placement = "Place ${step.digits.single()} in row ${target / 9 + 1}, column ${target % 9 + 1}."
        compose.onNodeWithText(placement).assertDoesNotExist()
        compose.onNodeWithText("Go to square").assertDoesNotExist()
        compose.onNodeWithText("Reveal number").performScrollTo().performClick()
        compose.onNodeWithText(placement).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Go to square").performScrollTo().performClick()
        compose.onNodeWithText(placement).assertDoesNotExist()
        compose.runOnIdle {
            assertEquals(target, state.selected)
            assertEquals(values, state.snapshot!!.cells.map { it.value })
        }
    }
}
