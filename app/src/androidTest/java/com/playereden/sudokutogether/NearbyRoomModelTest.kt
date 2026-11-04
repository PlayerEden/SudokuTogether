package com.playereden.sudokutogether

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import com.playereden.sudokutogether.core.*
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.UUID
import kotlin.coroutines.CoroutineContext

class NearbyRoomModelTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var root: File
    private lateinit var app: FixtureApplication
    private val models = mutableListOf<GameViewModel>()
    private class Queue : CoroutineDispatcher() {
        val tasks = java.util.concurrent.LinkedBlockingDeque<Runnable>()
        override fun dispatch(context: CoroutineContext, block: Runnable) { tasks.add(block) }
        fun drain() { repeat(100) { (tasks.poll() ?: return).run() };error("Unfinished model work") }
    }
    private class FixtureApplication(context: Context, private val root: File) : Application() {
        private val prefix = "nearby-${UUID.randomUUID()}-"
        init { attachBaseContext(context) }
        override fun getFilesDir() = root
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = baseContext.getSharedPreferences(prefix + name, mode)
    }
    private fun main(block: () -> Unit) = instrumentation.runOnMainSync(block)
    private fun model(): Pair<GameViewModel, Queue> {
        val queue = Queue();lateinit var value: GameViewModel
        main { value = GameViewModel(app, queue) };queue.drain();models.add(value);return value to queue
    }
    @Before fun prepare() {
        root = File(instrumentation.targetContext.cacheDir, "nearby-${UUID.randomUUID()}");check(root.mkdirs())
        app = FixtureApplication(instrumentation.targetContext, root)
    }
    @After fun clean() { main { models.forEach { it.home() } };root.deleteRecursively() }
    @Test fun newRoomsDefaultOpenAndAnExplicitChoiceIsRemembered() {
        val (first, _) = model();assertFalse(first.state.value.requireRoomCode)
        main { first.setRequireRoomCode(true) };assertTrue(first.state.value.requireRoomCode)
        val (restored, _) = model();assertTrue(restored.state.value.requireRoomCode)
    }
    @Test fun theChosenCodePolicySurvivesHomeAndTheNextFreshRoom() {
        val (value, queue) = model()
        main { value.setRequireRoomCode(true);value.home() }
        assertTrue("Going Home must keep the chosen code policy", value.state.value.requireRoomCode)
        main { value.host(false) };queue.drain()
        assertTrue(value.state.value.roomCode.isNotBlank())
        assertTrue((AtomicSessionStore(File(root,"host-session.json")).load() as LoadResult.Loaded).data.requireCode)
        main { value.home();value.host(false) };queue.drain()
        assertTrue("The next new room must remain protected", value.state.value.roomCode.isNotBlank())
        assertTrue((AtomicSessionStore(File(root,"host-session.json")).load() as LoadResult.Loaded).data.requireCode)
    }
    @Test fun anUnknownRestoredPortraitDoesNotBlockHosting() {
        app.getSharedPreferences("settings",0).edit().putString("cosmeticPortrait","forest").commit()
        val (value, queue) = model()
        assertEquals("", value.state.value.appearance.portrait)
        main { value.host(false) };queue.drain()
        assertEquals(Page.PLAY, value.state.value.page)
        assertNotNull(value.state.value.snapshot)
    }
    @Test fun resumingRestoresActualAccessInsteadOfTheNewGamePreference() {
        val digits = "123456789456789123789123456234567891567891234891234567345678912678912345912345678".map { it.digitToInt() }
        val puzzle = Puzzle("saved-access", digits.mapIndexed { i, n -> if (i == 0) 0 else n }, "EASY")
        val store = AtomicSessionStore(File(root, "host-session.json"))
        GameSession.create(puzzle, persist = store::save, requireCode = true)
        val (first, queue) = model();main { first.host(true) };queue.drain()
        assertTrue(first.state.value.roomCode.isNotBlank());main { first.home() }
        GameSession.create(puzzle, persist = store::save, requireCode = false)
        app.getSharedPreferences("settings",0).edit().putBoolean("requireRoomCode",true).commit()
        val (second, other) = model();main { second.host(true) };other.drain()
        assertEquals("",second.state.value.roomCode)
        assertFalse((store.load() as LoadResult.Loaded).data.requireCode)
    }
    @Test fun portraitsPersistAlongsideExistingEarnedEmblems() {
        LocalPlayerStore(File(root,"local-player.json")).update { it.copy(records = listOf(GameRecord("one",SOLO,"SOLVED","p",1000,1,1000))) }
        val (first, queue) = model()
        main { first.chooseCosmetic("emblem","leaf");first.chooseCosmetic("portrait","woodland_owl") };queue.drain()
        assertEquals("woodland_owl",first.state.value.appearance.portrait)
        val (restored, _) = model();assertEquals("leaf",restored.state.value.appearance.emblem)
        assertEquals("woodland_owl",restored.state.value.appearance.portrait)
        main { restored.chooseCosmetic("emblem","tile") }
        assertEquals("",restored.state.value.appearance.portrait)
        main { restored.chooseCosmetic("portrait","woodland_cat") }
        assertEquals("",restored.state.value.appearance.portrait)
    }
}
