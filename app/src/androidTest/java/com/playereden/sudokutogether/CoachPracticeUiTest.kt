package com.playereden.sudokutogether

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.playereden.sudokutogether.core.Appearance
import com.playereden.sudokutogether.core.CoachPractice
import java.io.File
import java.io.FileInputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CoachPracticeUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun xWingCanBePracticedWithLargeText() {
        val lesson = CoachPractice.lessons.single { it.id == "x-wing" }
        compose.setContent { SudokuTogetherTheme { CoachPracticeDialog(Appearance(), false) {} } }

        compose.onNodeWithTag("practice_lesson_x-wing").performScrollTo().assertIsDisplayed().performClick()
        val given = lesson.puzzle.clues.indexOfFirst { it != 0 }
        compose.onNodeWithTag("coach_cell_$given").assert(SemanticsMatcher.expectValue(
            SemanticsProperties.ContentDescription,
            listOf("Row ${given / 9 + 1} column ${given % 9 + 1}, clue ${lesson.puzzle.clues[given]}")
        ))
        val source = lesson.step.sourceCells.first()
        compose.onNodeWithTag("coach_cell_$source").assert(SemanticsMatcher.expectValue(
            SemanticsProperties.ContentDescription,
            listOf("Row ${source / 9 + 1} column ${source % 9 + 1}, empty, notes ${lesson.candidatesAt(source).joinToString(" ")}")
        ))
        val explanation = compose.onNodeWithText(lesson.step.explanation)
        explanation.assertDoesNotExist()
        compose.onNodeWithText("Show why").performScrollTo().performClick()
        explanation.performScrollTo().assertIsDisplayed()
        if (InstrumentationRegistry.getArguments().getString("requireLargePracticeText") == "true") {
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText(lesson.step.explanation, useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertEquals(2f, layouts.single().layoutInput.density.fontScale, 0.01f)
        }
        compose.onNodeWithTag("coach_cell_${lesson.step.sourceCells.first()}")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Proof square"))
        val digit = lesson.correctDigits.single()
        compose.onNodeWithTag("practice_choice_$digit").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Correct").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Practice again").performScrollTo().performClick()
        explanation.assertDoesNotExist()
        compose.onNodeWithText("Show why").performScrollTo().assertIsDisplayed()
    }

    @Test fun hiddenPairCanShowTheStepsThatPreparedItsCandidates() {
        val lesson = CoachPractice.lessons.single { it.id == "hidden-pair" }
        compose.setContent { SudokuTogetherTheme { CoachPracticeDialog(Appearance(), false) {} } }
        compose.onNodeWithTag("practice_lesson_hidden-pair").performScrollTo().performClick()
        val setup = compose.onNodeWithText(lesson.setup.first().explanation)
        setup.assertDoesNotExist()
        compose.onNodeWithText("Show setup steps").performScrollTo().performClick()
        setup.performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Hide setup steps").performScrollTo().performClick()
        setup.assertDoesNotExist()
        val digit = lesson.correctDigits.last()
        compose.onNodeWithTag("practice_choice_$digit").performScrollTo().performClick()
        compose.onNodeWithText("Correct").performScrollTo().assertIsDisplayed()
    }
}

class CoachPracticeIsolationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun homePracticeAndReplayLeaveTheSavedPuzzleAndRecordsIntact() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val model = compose.runOnUiThread { ViewModelProvider(compose.activity)[GameViewModel::class.java] }
        EmulatorSystemDialogs.dismiss()
        compose.waitUntil(30000) { !model.state.value.busy }
        val profile = model.state.value.profile
        val active = profile.activeSolo?.takeUnless { it.completed }
        if (InstrumentationRegistry.getArguments().getString("requirePracticeProgress") == "true") {
            assertNotNull("The upgrade rehearsal should leave an active Solo puzzle", active)
            assertTrue(profile.dailyProgress.isNotEmpty())
            assertTrue(profile.records.isNotEmpty())
        }
        val save = File(compose.activity.filesDir, "local-player.json")
        val before = save.takeIf { it.isFile }?.readBytes()

        val help = compose.onNodeWithContentDescription("How to play Sudoku")
        if (!help.isDisplayed()) help.performScrollTo()
        help.performClick()
        compose.onNodeWithText("Coach practice").performClick()
        compose.onNodeWithTag("practice_lesson_only-digit").performScrollTo().performClick()
        compose.onNodeWithTag("practice_choice_1").performScrollTo().performClick()
        compose.onNodeWithText("Try another number.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("practice_choice_5").performScrollTo().performClick()
        compose.onNodeWithText("Correct").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Practice again").performScrollTo().performClick()
        compose.onNodeWithText("Try another number.").assertDoesNotExist()
        instrumentation.uiAutomation.executeShellCommand("input keyevent 4").use { output ->
            FileInputStream(output.fileDescriptor).use { it.readBytes() }
        }
        compose.waitUntil(10000) { compose.onAllNodesWithText("Coach practice").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Coach practice").assertIsDisplayed()
        compose.onNodeWithText("Home").performClick()
        assertArrayEquals(before, save.takeIf { it.isFile }?.readBytes())
        assertEquals(profile, model.state.value.profile)

        if (active != null) {
            compose.onNodeWithText("Continue", substring = false).performScrollTo().performClick()
            compose.waitUntil(20000) { model.state.value.page == Page.PLAY && model.state.value.canEdit }
            val resumed = model.state.value.snapshot!!
            assertEquals(active.id, resumed.sessionId)
            assertEquals(active.cells.map { it.value to it.notes }, resumed.cells.map { it.value to it.notes })
        }
    }
}
