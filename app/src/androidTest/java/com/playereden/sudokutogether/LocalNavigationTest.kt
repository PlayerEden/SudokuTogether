package com.playereden.sudokutogether

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.playereden.sudokutogether.core.*
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import kotlin.coroutines.CoroutineContext

/** Real ViewModel/store with background tasks deliberately executed out of order. */
@RunWith(AndroidJUnit4::class)
class LocalNavigationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var root: File
    private lateinit var model: GameViewModel
    private val queue = QueuedDispatcher()
    private class QueuedDispatcher : CoroutineDispatcher() {
        val tasks = java.util.concurrent.LinkedBlockingDeque<Runnable>()
        override fun dispatch(context: CoroutineContext, block: Runnable) { tasks.add(block) }
        fun first() = tasks.removeFirst().run()
        fun last() = tasks.removeLast().run()
    }
    private class FixtureApplication(context: Context, private val directory: File) : Application() {
        private val prefix = "navigation-${UUID.randomUUID()}-"
        init { attachBaseContext(context) }
        override fun getFilesDir(): File = directory
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = baseContext.getSharedPreferences(prefix + name, mode)
    }
    @Before fun prepare() {
        root = File(instrumentation.targetContext.cacheDir, "navigation-${UUID.randomUUID()}"); check(root.mkdirs())
    }
    private fun launch(corrupt: Boolean = false) {
        if (corrupt) File(root, "local-player.json").writeText("unreadable navigation fixture")
        instrumentation.runOnMainSync { model = GameViewModel(FixtureApplication(instrumentation.targetContext, root), queue) }
        queue.first(); assertFalse(model.state.value.busy)
    }
    @After fun clean() {
        if (::model.isInitialized) instrumentation.runOnMainSync { model.home() }
        root.deleteRecursively()
    }
    private fun saved() = LocalPlayerStore(File(root, "local-player.json")).current()
    @Test fun anOlderQueuedSoloStartCannotReplaceTheNewerVisiblePuzzle() {
        launch(); instrumentation.runOnMainSync { model.solo(false); model.home(); model.solo(false) }
        queue.last(); val newer = model.state.value.snapshot!!.sessionId; queue.first()
        assertEquals(newer, model.state.value.snapshot!!.sessionId); assertEquals(newer, saved().activeSolo!!.id)
    }
    @Test fun canceledDailyOpeningDoesNotCreateSavedProgress() {
        launch(); instrumentation.runOnMainSync { model.daily(); model.home() }; queue.first()
        assertTrue(saved().dailyProgress.isEmpty()); assertEquals(Page.HOME, model.state.value.page); assertNull(model.state.value.snapshot)
    }
    @Test fun aLateSoloStartAfterHomeCannotInstallAnInvisibleSavedPuzzle() {
        launch(); instrumentation.runOnMainSync { model.solo(false); model.home() }; queue.first()
        assertNull(saved().activeSolo); assertEquals(Page.HOME, model.state.value.page)
    }
    @Test fun blockedLocalSavingKeepsItsExplanationAfterHostingAndReturningHome() {
        launch(corrupt = true); val warning = model.state.value.saveIssue
        assertTrue(model.state.value.localSaveBlocked); assertNotNull(warning)
        instrumentation.runOnMainSync { model.host(false) }; queue.first()
        instrumentation.runOnMainSync { model.home() }
        assertTrue(model.state.value.localSaveBlocked); assertEquals(warning, model.state.value.saveIssue)
        instrumentation.runOnMainSync { model.showProfile() }; assertEquals(warning, model.state.value.saveIssue)
    }
}
