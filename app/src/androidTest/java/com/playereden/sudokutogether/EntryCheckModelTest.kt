package com.playereden.sudokutogether

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.playereden.sudokutogether.core.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

/** Real persistence and ViewModel; the dispatcher exposes cancellation/save boundaries. */
@RunWith(AndroidJUnit4::class)
class EntryCheckModelTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var root: File
    private lateinit var model: GameViewModel
    private lateinit var queue: QueuedDispatcher
    private val clues = "530070000600195000098000060800060003400803001700020006060000280000419005000080079".map { it.digitToInt() }
    private val puzzle = Puzzle("check-model", clues, "EASY")
    private class QueuedDispatcher : CoroutineDispatcher() {
        val tasks = java.util.concurrent.LinkedBlockingDeque<Runnable>()
        override fun dispatch(context: CoroutineContext, block: Runnable) { tasks.add(block) }
        fun first() { tasks.pollFirst()?.run() }
    }
    private class FixtureApplication(context: Context, private val directory: File) : Application() {
        private val prefix = "entry-check-${UUID.randomUUID()}-"
        init { attachBaseContext(context) }
        override fun getFilesDir(): File = directory
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = baseContext.getSharedPreferences(prefix + name, mode)
    }
    private fun main(block: () -> Unit) = instrumentation.runOnMainSync(block)
    private fun saved() = LocalPlayerStore(File(root, "local-player.json")).current()
    @Before fun prepare() {
        root = File(instrumentation.targetContext.cacheDir, "entry-check-${UUID.randomUUID()}");check(root.mkdirs())
        queue = QueuedDispatcher()
    }
    private fun progress(digit: Int = 1) = PersonalPlay.edit(PersonalPlay.newSolo(puzzle, 1000L),
        2, Kind.VALUE, digit, elapsedMs = 1000L, nowMs = 2000L)!!
    private fun launch(data: LocalPlayerData = LocalPlayerData(activeSolo = progress()), daily: String? = null) {
        LocalPlayerStore(File(root, "local-player.json")).update { data.copy(playerId = it.playerId) }
        main { model = GameViewModel(FixtureApplication(instrumentation.targetContext, root), queue) }
        queue.first()
        main { if (daily == null) model.solo(true) else model.openDaily(daily) };queue.first()
        assertEquals(Page.PLAY, model.state.value.page)
    }
    @After fun clean() {
        if (::model.isInitialized) main { model.home() }
        root.deleteRecursively()
    }
    @Test fun explicitWrongEntryCheckSavesBeforeShowingAndNeverChangesTheNumber() {
        launch();main { model.checkWork() }
        assertNull(model.state.value.entryCheck);assertEquals(0, saved().activeSolo!!.checksUsed)
        queue.first()
        assertNotNull(model.state.value.entryCheck)
        assertEquals(SudokuAssistance.CheckKind.INCORRECT_ENTRY, model.state.value.entryCheck!!.kind)
        assertEquals(2, model.state.value.selected)
        assertEquals(1, saved().activeSolo!!.checksUsed);assertEquals(0, saved().activeSolo!!.hintsUsed)
        assertEquals(1, model.state.value.snapshot!!.cells[2].value)
        assertEquals(1, saved().activeSolo!!.cells[2].value)
    }
    @Test fun failedSaveCannotRevealAComparisonOrIncrementTheAcceptedCounter() {
        launch();val file = File(root, "local-player.json");val before = file.readBytes()
        val backup = File(file.path + ".recovery");check(backup.delete());check(backup.mkdir());File(backup, "blocker").writeText("keep")
        main { model.checkWork() };queue.first()
        assertNull(model.state.value.entryCheck);assertNotNull(model.state.value.message)
        assertEquals(0, model.state.value.profile.activeSolo!!.checksUsed)
        assertArrayEquals(before, file.readBytes())
        assertEquals(-1,model.state.value.selected)
        assertEquals(1, model.state.value.snapshot!!.cells[2].value)
    }
    @Test fun navigatingHomeCancelsQueuedChecksWithoutAddingAssistance() {
        launch();main { model.checkWork();model.home() };queue.first()
        assertEquals(Page.HOME, model.state.value.page);assertNull(model.state.value.entryCheck)
        assertEquals(0, saved().activeSolo!!.checksUsed)
    }
    @Test fun repeatedTapsWhileAComparisonIsPendingCountOnlyOnce() {
        launch();main { model.checkWork();model.checkWork() }
        while (queue.tasks.isNotEmpty()) queue.first()
        assertEquals(1, saved().activeSolo!!.checksUsed)
        assertNotNull(model.state.value.entryCheck)
        assertEquals(SudokuAssistance.CheckKind.INCORRECT_ENTRY, model.state.value.entryCheck!!.kind)
    }
    @Test fun aPositiveComparisonSurvivesResumeWithoutInventingALogicalHint() {
        launch(LocalPlayerData(activeSolo = progress(4)));main { model.checkWork() };queue.first()
        assertNotNull(model.state.value.entryCheck)
        assertEquals(SudokuAssistance.CheckKind.CORRECT_SO_FAR, model.state.value.entryCheck!!.kind)
        main { model.home();model.solo(true) };queue.first()
        assertEquals(1, model.state.value.profile.activeSolo!!.checksUsed)
        assertTrue(model.state.value.profile.activeSolo!!.assisted)
        assertEquals(0, model.state.value.profile.activeSolo!!.hintsUsed)
        assertEquals(4, model.state.value.snapshot!!.cells[2].value)
        assertNull(model.state.value.entryCheck)
    }
    @Test fun dailyChecksNeverChangeTheIndependentSoloCounter() {
        val day = "2026-11-03";val daily = progress()
        launch(LocalPlayerData(activeSolo = progress(4), dailyProgress = listOf(DailyProgress(day, daily))), day)
        main { model.checkWork() };queue.first()
        assertEquals(1, DailyChallenge.find(saved(), day)!!.checksUsed)
        assertEquals(0, saved().activeSolo!!.checksUsed)
    }
    @Test fun roomRequestsCannotExposeLocalComparisonResults() {
        launch();main { model.home();model.host(false) };queue.first()
        assertEquals(HOST, model.state.value.role);assertNotNull(model.state.value.snapshot)
        main { model.checkWork() }
        while (queue.tasks.isNotEmpty()) queue.first()
        assertNull(model.state.value.entryCheck);assertEquals(0, saved().activeSolo!!.checksUsed)
    }
    @Test fun completedBoardsCannotAcquireChecks() {
        val digits = "123456789456789123789123456234567891567891234891234567345678912678912345912345678".map { it.digitToInt() }
        val one = Puzzle("finished-check", digits.mapIndexed { i,n -> if (i==0) 0 else n }, "EASY")
        val finished = PersonalPlay.edit(PersonalPlay.newSolo(one,1000L),0,Kind.VALUE,1,elapsedMs=1000L,nowMs=2000L)!!
        launch(LocalPlayerData(activeSolo=finished));main { model.checkWork() }
        while (queue.tasks.isNotEmpty()) queue.first()
        assertNull(model.state.value.entryCheck);assertEquals(0,saved().activeSolo!!.checksUsed)
    }
    @Test fun duplicateRuleFeedbackDoesNotCountAsASolutionCheck() {
        launch(LocalPlayerData(activeSolo=progress(5)));main { model.checkWork() };queue.first()
        assertNotNull(model.state.value.entryCheck)
        assertEquals(SudokuAssistance.CheckKind.CONFLICT,model.state.value.entryCheck!!.kind)
        assertEquals(0,saved().activeSolo!!.checksUsed)
        assertFalse(saved().activeSolo!!.assisted)
    }
    @Test fun anEmptyBoardDoesNotAcquireAssistanceFromACheck() {
        launch(LocalPlayerData(activeSolo=PersonalPlay.newSolo(puzzle,1000L)));main { model.checkWork() };queue.first()
        assertNotNull(model.state.value.entryCheck)
        assertEquals(SudokuAssistance.CheckKind.NO_ENTRIES,model.state.value.entryCheck!!.kind)
        assertEquals(0,saved().activeSolo!!.checksUsed)
        assertFalse(saved().activeSolo!!.assisted)
    }

    /** Pause the real StateFlow CAS after the worker has built its result. */
    @OptIn(kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi::class)
    private class PublicationGate(
        private val delegate: MutableStateFlow<GameUiState>,
        private val matches: (GameUiState) -> Boolean
    ) : MutableStateFlow<GameUiState> by delegate {
        val paused = CountDownLatch(1)
        val release = CountDownLatch(1)
        private val armed = AtomicBoolean(true)
        override fun compareAndSet(expect: GameUiState, update: GameUiState): Boolean {
            if (matches(update) && armed.compareAndSet(true, false)) {
                paused.countDown()
                check(release.await(30, TimeUnit.SECONDS)) { "Publication gate was not released" }
            }
            return delegate.compareAndSet(expect, update)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun navigateDuringPublication(
        matches: (GameUiState) -> Boolean,
        beforeNavigate: () -> Unit = {},
        request: () -> Unit
    ) {
        val field = GameViewModel::class.java.getDeclaredField("_state").apply { isAccessible = true }
        val original = field.get(model) as MutableStateFlow<GameUiState>
        val gate = PublicationGate(original, matches)
        field.set(model, gate)
        main(request)
        val failure = AtomicReference<Throwable?>(null)
        val worker = Thread({ try { queue.first() } catch (error: Throwable) { failure.set(error) } }, "coach-publication")
        val navigated = CountDownLatch(1)
        val navigator = Thread({
            try { main { model.home();model.solo(false) } }
            catch (error: Throwable) { failure.set(error) }
            finally { navigated.countDown() }
        }, "coach-navigation")
        worker.start()
        try {
            assertTrue("The old result reached publication", gate.paused.await(25, TimeUnit.SECONDS))
            beforeNavigate()
            navigator.start()
            // Old code finishes navigation while its result is paused. Correct
            // code serializes closeRoom behind publication. Recognize either
            // boundary, without a timing sleep deciding whether the test passes.
            val mainThread = Looper.getMainLooper().thread
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20)
            while (navigated.count != 0L && !(mainThread.state == Thread.State.BLOCKED &&
                    mainThread.stackTrace.any { it.className == GameViewModel::class.java.name && it.methodName == "closeRoom" })) {
                check(System.nanoTime() < deadline) { "Navigation did not reach its publication boundary" }
                Thread.sleep(10)
            }
        } finally {
            gate.release.countDown()
            worker.join(30000)
            if (navigator.state != Thread.State.NEW) navigator.join(30000)
            field.set(model, original)
        }
        failure.get()?.let { throw it }
        assertFalse("Old publication worker completed", worker.isAlive)
        assertEquals("New puzzle request is still pending", 0L, navigated.count)
        val state = model.state.value
        assertEquals(Page.PLAY, state.page)
        assertTrue("Old feedback must not clear the new puzzle's busy state", state.busy)
        assertNull(state.snapshot)
        assertNull(state.hint)
        assertFalse(state.hintUnavailable)
        assertNull(state.entryCheck)
        assertNull(state.message)
        assertEquals(-1, state.selected)
    }

    @Test fun aComputedHintCannotPublishIntoANewerPuzzle() {
        launch(LocalPlayerData(activeSolo = progress(4)))
        navigateDuringPublication(matches = { it.hint != null }) { model.requestHint() }
    }

    @Test fun anUnavailableHintCannotClearANewerPuzzlesBusyState() {
        launch()
        navigateDuringPublication(matches = { it.hintUnavailable }) { model.requestHint() }
    }

    @Test fun aFailedCheckCannotPublishItsErrorIntoANewerPuzzle() {
        launch()
        val backup = File(root, "local-player.json.recovery")
        check(backup.delete());check(backup.mkdir());File(backup, "blocker").writeText("keep")
        navigateDuringPublication(matches = { it.message?.startsWith("Could not save the check") == true },
            beforeNavigate = { check(backup.deleteRecursively()) }) { model.checkWork() }
    }
}
