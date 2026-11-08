package com.playereden.sudokutogether

import android.app.Instrumentation
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Moving app information off Home must preserve working APK sharing and return navigation. */
@RunWith(AndroidJUnit4::class)
class GridMatesConvenienceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun ready() {
        compose.waitUntil(30000) { compose.onAllNodes(hasTestTag("nav_settings") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        EmulatorSystemDialogs.dismiss()
        compose.waitUntil(10000) {
            if (!compose.activity.hasWindowFocus()) EmulatorSystemDialogs.dismiss()
            compose.activity.hasWindowFocus()
        }
    }
    @Test fun aboutSharesTheInstalledApkAndReturnsToSettings() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentation.addMonitor(IntentFilter(Intent.ACTION_CHOOSER), Instrumentation.ActivityResult(0, null), true)
        try {
            compose.onNodeWithTag("nav_settings").performClick()
            compose.onNodeWithText("About GridMates").performScrollTo().performClick()
            compose.onNodeWithTag("about_gridmates").assertIsDisplayed()
            val context = instrumentation.targetContext
            val scale = if (context.resources.configuration.fontScale > 1.5f) "large" else "normal"
            val image = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
            val file = File(context.getExternalFilesDir("screenshots"), "gridmates-about-$scale.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { check(image.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            image.recycle()
            compose.onNodeWithText("Share APK").performScrollTo().performClick()
            compose.waitUntil(15000) { monitor.hits == 1 }
            compose.onNodeWithText("Done").assertIsDisplayed().performClick()
            compose.onNodeWithTag("nav_settings").assertIsSelected()
            compose.onNodeWithTag("nav_play").performClick()
            compose.onNodeWithTag("nav_play").assertIsSelected()
        } finally { instrumentation.removeMonitor(monitor) }
    }
}
