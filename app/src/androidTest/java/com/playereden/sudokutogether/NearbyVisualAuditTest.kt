package com.playereden.sudokutogether

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.playereden.sudokutogether.core.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Deterministic native presentation fixtures; discovery itself is tested separately. */
class NearbyVisualAuditTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun capturesNearbyChoicesAndTheRoomWithChosenPortraits() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val shown = mutableStateOf(GameUiState(page = Page.JOIN, rooms = listOf(
            NearbyRoom("Joseph's puzzle", "192.168.1.20", 41421, requiresCode = false),
            NearbyRoom("Taylor's puzzle", "192.168.1.21", 41422, requiresCode = true))))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var activity: MainActivity
            scenario.onActivity { activity = it; it.setContent { SudokuTogetherTheme { SudokuApp(shown.value, polishActions(), {}) } } }
            EmulatorSystemDialogs.dismiss()
            compose.onNodeWithText("Joseph's puzzle").assertIsDisplayed()
            NativeWindowCapture.save(activity, File(context.getExternalFilesDir("screenshots"), "v011c-nearby.png"))
            val state = polishState(1)
            val snapshot = state.snapshot!!.copy(hostAppearance = Appearance(portrait = "woodland_owl"), guestAppearance = Appearance(portrait = "woodland_fox"))
            compose.runOnIdle { shown.value = state.copy(appearance = snapshot.hostAppearance, snapshot = snapshot,
                chat = listOf(ChatLine("one", HOST, "Ready for another puzzle?", 1), ChatLine("two", GUEST, "Let's play!", 2))) }
            compose.onNodeWithText("Co-op").assertIsDisplayed()
            NativeWindowCapture.save(activity, File(context.getExternalFilesDir("screenshots"), "v011c-room.png"))
            compose.onNodeWithText("Chat (1)").performClick()
            compose.onNode(hasText("You") and hasAnyAncestor(isDialog())).assertIsDisplayed()
            compose.onNode(hasText("Taylor") and hasAnyAncestor(isDialog())).assertIsDisplayed()
            compose.waitForIdle()
            instrumentation.waitForIdleSync()
            instrumentation.uiAutomation.waitForIdle(500, 5000)
            val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
            val output = File(context.getExternalFilesDir("screenshots"), "v011c-chat.png")
            output.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) };bitmap.recycle()
            compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss), useUnmergedTree = true)
                .onFirst().performSemanticsAction(SemanticsActions.Dismiss) { it() }
            compose.waitUntil(10000) { compose.onAllNodesWithText("Room chat").fetchSemanticsNodes().isEmpty() }
            compose.runOnIdle { shown.value = GameUiState(page = Page.PROFILE, appearance = Appearance(portrait = "woodland_owl")) }
            compose.onNodeWithTag("nav_collection").performClick()
            compose.onNodeWithTag("portrait_woodland_bear").performScrollTo().assertIsDisplayed()
            NativeWindowCapture.save(activity, File(context.getExternalFilesDir("screenshots"), "v011c-collection.png"))
        }
    }
}
