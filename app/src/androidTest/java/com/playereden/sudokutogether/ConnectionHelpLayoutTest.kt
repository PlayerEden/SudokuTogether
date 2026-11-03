package com.playereden.sudokutogether

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream

@RunWith(AndroidJUnit4::class)
class ConnectionHelpLayoutTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private var scenario: ActivityScenario<MainActivity>? = null
    private var oldFont = 1f
    private var oldSize: String? = null
    private fun shell(s: String) = instrumentation.uiAutomation.executeShellCommand(s).use { p ->
        FileInputStream(p.fileDescriptor).use { it.readBytes().toString(Charsets.UTF_8) }
    }
    @Before fun configure() {
        oldFont = Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE, 1f)
        oldSize = Regex("Override size: (\\d+x\\d+)").find(shell("wm size"))?.groupValues?.get(1)
        shell("settings put system font_scale 2.0"); shell("wm size 640x1136")
    }
    @After fun restore() {
        scenario?.close(); shell("settings put system font_scale $oldFont"); shell("wm size ${oldSize ?: "reset"}")
    }
    private fun launch() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitUntil(30000) { compose.onAllNodes(hasTestTag("nav_records") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        EmulatorSystemDialogs.dismiss()
        compose.waitUntil(10000) {
            var focused = false
            scenario!!.onActivity { focused = it.hasWindowFocus() }
            if (!focused) EmulatorSystemDialogs.dismiss()
            focused
        }
    }
    @Test fun compactLargeTextBrandNameStaysWhole() {
        launch()
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText("GridMates").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val image = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val file = File(context.getExternalFilesDir("screenshots"), "gridmates-header-large.png")
        file.parentFile!!.mkdirs(); file.outputStream().use { check(image.compress(Bitmap.CompressFormat.PNG, 100, it)) }; image.recycle()
        assertEquals("GridMates wordmark must stay on one line", 1, layouts.single().lineCount)
    }
    @Test fun compactLargeTextConnectionHelpKeepsAllGuidanceReachable() {
        launch()
        compose.onNodeWithText("Co-op").performScrollTo().performClick()
        compose.onNodeWithText("Join room").performScrollTo().performClick()
        for (landscape in listOf(false, true)) {
            if (landscape) {
                scenario!!.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
                compose.waitUntil(15000) { context.resources.configuration.screenWidthDp > context.resources.configuration.screenHeightDp }
                EmulatorSystemDialogs.dismiss()
            }
            compose.onNodeWithText("Connection help").performScrollTo().performClick()
            val last = compose.onNodeWithText("Some guest networks block connections between phones.", substring = true)
            // A fully fitting body needs no scroll; overflowing text must be reachable.
            try { last.performScrollTo() } catch (_: AssertionError) { }
            last.assertIsDisplayed()
            val layouts = mutableListOf<TextLayoutResult>()
            last.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
            val file = File(context.getExternalFilesDir("screenshots"), "gridmates-connection-help-${if (landscape) "landscape" else "portrait"}.png")
            file.parentFile!!.mkdirs(); file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }; bitmap.recycle()
            assertFalse("Connection help must show all instructions without clipping", layouts.single().hasVisualOverflow)
            compose.onNodeWithText("Got it").assertIsDisplayed().performClick()
        }
    }
}
