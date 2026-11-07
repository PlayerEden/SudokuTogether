package com.playereden.sudokutogether

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Native visual evidence and everyday flows; repeat with normal/large system text. */
@RunWith(AndroidJUnit4::class)
class GameVisualAuditTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private var originalName: String? = null
    private val prefix get() = if (context.resources.configuration.fontScale > 1.5f) "large" else "normal"

    @Before fun ready() {
        originalName = context.getSharedPreferences("settings", 0).getString("name", "Player")
        compose.waitUntil(30000) { compose.onAllNodes(hasText("New puzzle") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        EmulatorSystemDialogs.dismiss()
        compose.waitUntil(10000) {
            if (!compose.activity.hasWindowFocus()) EmulatorSystemDialogs.dismiss()
            compose.activity.hasWindowFocus()
        }
    }
    @After fun restoreName() {
        originalName?.let { context.getSharedPreferences("settings", 0).edit().putString("name", it).commit() }
    }
    private fun save(name: String) {
        compose.waitForIdle()
        val file = File(context.getExternalFilesDir("screenshots"), "v010d-$prefix-$name.png")
        NativeWindowCapture.save(compose.activity, file)
        println("SUDOKU_SCREENSHOT: ${file.absolutePath}")
    }
    private fun startSolo() {
        compose.onNodeWithTag("difficulty_EASY").performScrollTo().performClick()
        compose.onNodeWithText("New puzzle").performScrollTo().performClick()
        if (compose.onAllNodesWithText("Start new").fetchSemanticsNodes().isNotEmpty()) compose.onNodeWithText("Start new").performClick()
        compose.waitUntil(30000) { compose.onAllNodesWithTag("input_dock").fetchSemanticsNodes().isNotEmpty() }
    }
    @Test fun capturesMenusAndChecksSettingsAndJoinKeyboard() {
        save("home")
        compose.onNodeWithText("Co-op").performScrollTo().performClick(); save("coop-menu")
        compose.onNodeWithText("Versus").performScrollTo().performClick(); save("versus-menu")
        compose.onNodeWithText("Join a game").performScrollTo().performClick(); save("join-top")
        compose.onNodeWithText("Join manually").performScrollTo().performClick()
        compose.onNodeWithText("Host address").performScrollTo().performTextInput("192.0.2.10:41421")
        compose.onNodeWithText("Host address").performImeAction()
        compose.onNodeWithText("Code (if needed)").assertIsFocused().performTextInput("123456")
        compose.runOnUiThread {
            compose.activity.getSystemService(InputMethodManager::class.java)
                .hideSoftInputFromWindow(compose.activity.window.decorView.windowToken, 0)
        }
        compose.onNodeWithTag("join_room").performScrollTo().assertIsEnabled(); save("join-form")
        compose.onNodeWithText("Connection help").performScrollTo().performClick()
        compose.onNodeWithText("Got it").assertIsDisplayed().performClick()
        compose.onNodeWithContentDescription("Back home").performScrollTo().performClick()
        compose.onNodeWithTag("nav_collection").performClick(); save("collection")
        compose.onNodeWithText("Woodland").performScrollTo(); save("woodland")
        compose.onNodeWithTag("portrait_woodland_owl").performScrollTo().performClick(); save("woodland-selected")
        compose.onNodeWithText("Number colors").performScrollTo(); save("collection-colors")
        compose.onNodeWithTag("nav_settings").performClick(); save("settings")
        compose.onNodeWithTag("player_name").performScrollTo().performTextReplacement("Puzzle partner")
        compose.onNodeWithTag("player_name").performImeAction()
        compose.onNodeWithTag("nav_play").performClick(); save("home-portrait")
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("player_name").assertTextContains("Puzzle partner")
        compose.onNodeWithText("Clean pencil marks").performScrollTo(); save("settings-assistance")
        compose.onNodeWithTag("nav_records").performClick(); save("records")
        compose.onNodeWithText("DAILY HISTORY").performScrollTo(); save("daily-history")
    }
    @Test fun capturesPortraitPlayAndNotes() {
        startSolo(); save("play")
        val empty = compose.onAllNodes(hasContentDescription("empty", substring = true)).fetchSemanticsNodes().first()
        val tag = empty.config[SemanticsProperties.TestTag]
        compose.onNodeWithTag(tag).performClick(); save("selected")
        compose.onNodeWithText("Notes").performClick()
        compose.onNodeWithTag("number_3").performClick(); save("notes")
    }
    @Test fun capturesLandscapePlay() {
        startSolo()
        compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        compose.waitUntil(20000) { context.resources.configuration.screenWidthDp > context.resources.configuration.screenHeightDp }
        EmulatorSystemDialogs.dismiss()
        compose.onNodeWithTag("number_9").performScrollTo().assertIsDisplayed()
        save("landscape-play")
        compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(20000) { context.resources.configuration.screenWidthDp < context.resources.configuration.screenHeightDp }
    }
}
