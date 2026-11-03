package com.playereden.sudokutogether

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Real Activity-window captures avoid Espresso's next-frame-idle screenshot loop. */
@RunWith(AndroidJUnit4::class)
class NativeScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun save(name: String) {
        val app = InstrumentationRegistry.getInstrumentation().targetContext
        val output = File(requireNotNull(app.getExternalFilesDir("screenshots")), name)
        NativeWindowCapture.save(compose.activity, output)
        println("SUDOKU_SCREENSHOT: " + output.absolutePath)
    }

    @Test fun capturesHomeModesAndSelectedPuzzleForVisualReview() {
        EmulatorSystemDialogs.dismiss()
        compose.waitUntil(20000) {
            compose.onAllNodes(hasText("New puzzle") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
        }
        compose.waitUntil(10000) {
            if (!compose.activity.hasWindowFocus()) EmulatorSystemDialogs.dismiss()
            compose.activity.hasWindowFocus()
        }
        save("01-home.png")
        compose.onNodeWithText("Co-op").performScrollTo().assertIsDisplayed().performClick()
        save("02-coop-menu.png")
        compose.onNodeWithText("Solo").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("difficulty_EASY").performScrollTo().performClick()
        compose.onNodeWithText("New puzzle").performScrollTo().assertIsDisplayed().performClick()
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Start new").performClick()
        }
        compose.waitUntil(20000) {
            compose.onAllNodesWithTag("cell_40").fetchSemanticsNodes().isNotEmpty()
        }
        save("03-puzzle.png")
        compose.onNodeWithTag("cell_40").performClick()
        save("04-selected.png")
    }
}
