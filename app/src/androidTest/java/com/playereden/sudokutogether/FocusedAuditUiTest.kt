package com.playereden.sudokutogether

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.playereden.sudokutogether.core.LocalPlayerData
import com.playereden.sudokutogether.core.PersonalPlay
import com.playereden.sudokutogether.core.Puzzle
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FocusedAuditUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun savedProgressCountsEntriesAndLabelsTheNextPuzzleDifficulty() {
        val clues = "530070000600195000098000060800060003400803001700020006060000280000419005000080079"
            .map(Char::digitToInt)
        val active = PersonalPlay.newSolo(Puzzle("audit-home", clues, "EASY"), 1000L)
        val withEntryAndNote = active.copy(cells = active.cells.mapIndexed { index, cell ->
            when (index) {
                2 -> cell.copy(value = 4)
                3 -> cell.copy(notes = 1 shl 5)
                else -> cell
            }
        }, elapsedMs = 4000L)
        var continued = false
        var requestedNew = false
        val state = GameUiState(page = Page.HOME, resumeSolo = true, selectedDifficulty = "MEDIUM",
            profile = LocalPlayerData(activeSolo = withEntryAndNote))
        compose.setContent {
            SudokuTogetherTheme {
                HomeScreen(state, polishActions().copy(solo = { continued = it }),
                    fresh = {}, freshSolo = { requestedNew = true }, freshRace = {}, help = {}, profile = {})
            }
        }

        compose.onNodeWithText("Easy · 1 of 51 filled · 00:00:04").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("New puzzle difficulty").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("difficulty_MEDIUM").assertIsSelected()
        compose.onNodeWithText("Continue").performScrollTo().performClick()
        compose.onNodeWithText("New puzzle").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(continued); assertTrue(requestedNew) }
    }

    @Test fun installedLauncherForegroundShowsTheSudokuGridAndClue() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val icon = context.packageManager.getApplicationIcon(context.applicationInfo) as AdaptiveIconDrawable
        val foreground = render(icon.foreground, 108)
        assertEquals(Color.TRANSPARENT, foreground.getPixel(21, 54))
        assertEquals(Color.parseColor("#F8F7ED"), foreground.getPixel(54, 34))
        assertEquals(Color.parseColor("#E5C982"), foreground.getPixel(60, 47))
        assertEquals(Color.parseColor("#214F3F"), foreground.getPixel(45, 34))
        assertEquals(Color.parseColor("#214F3F"), foreground.getPixel(36, 41))

        val directory = requireNotNull(context.getExternalFilesDir("screenshots"))
        File(directory, "gridmates-sudoku-icon.png").outputStream().use {
            assertTrue(render(icon, 96).compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }

    private fun render(drawable: Drawable, size: Int): Bitmap =
        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also {
            drawable.setBounds(0, 0, size, size)
            drawable.draw(Canvas(it))
        }
}
