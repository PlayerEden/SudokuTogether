package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SessionStoreTest {
    @get:Rule val folder = TemporaryFolder()
    private fun file() = File(folder.root, "session.json")
    @Test fun roundTripPreservesSharedWorkHistoryAndGuestIdentity() {
        val store = AtomicSessionStore(file())
        val game = GameSession.create(fixture(), persist = store::save)
        game.admitGuest("Partner"); game.setPresence()
        game.submit(HOST, game.snapshot().move(0, Kind.NOTE, 3))
        game.submit(GUEST, game.snapshot().move(1, number = 2))
        val restored = (store.load() as LoadResult.Loaded).data
        assertEquals(game.saved(), restored)
        val resumed = GameSession(restored, store::save).also { it.setPresence() }
        assertTrue(resumed.submit(GUEST, resumed.snapshot().move(1, Kind.UNDO)).accepted)
        assertEquals(0, resumed.snapshot().cells[1].value)
    }
    @Test fun truncatedPrimaryRecoversThePreviousValidSnapshot() {
        val target = file(); val store = AtomicSessionStore(target)
        val game = GameSession.create(fixture(), persist = store::save)
        val original = game.saved()
        game.submit(HOST, game.snapshot().move(0))
        target.writeText("{truncated")
        assertEquals(original, (store.load() as LoadResult.Recovered).data)
    }
    @Test fun missingAndUnreadableDataAreDifferentResults() {
        val target = file(); val store = AtomicSessionStore(target)
        assertEquals(LoadResult.Missing, store.load())
        target.writeText("broken")
        assertTrue(store.load() is LoadResult.Invalid)
    }
    @Test fun unknownSchemaAndTamperedCluesAreNotSilentlyReset() {
        val target = file(); val store = AtomicSessionStore(target)
        val saved = GameSession.create(fixture()).saved()
        target.writeText(Json.encodeToString(saved.copy(schema = 999)))
        assertTrue(store.load() is LoadResult.Invalid)
        val cells = saved.snapshot.cells.toMutableList().also { it[2] = Cell(9) }
        target.writeText(Json.encodeToString(saved.copy(snapshot = saved.snapshot.copy(cells = cells))))
        assertTrue(store.load() is LoadResult.Invalid)
    }
    @Test fun impossibleHistoryDoesNotCrashOnLoad() {
        val target = file(); val saved = GameSession.create(fixture()).saved()
        target.writeText(Json.encodeToString(saved.copy(history = listOf(EditRecord("bad", HOST, 500, Cell(), 1)))))
        assertTrue(AtomicSessionStore(target).load() is LoadResult.Invalid)
    }
    @Test fun failedWriteDoesNotAcknowledgeAnUnsavedEdit() {
        val blocker = folder.newFile("not-a-directory")
        val store = AtomicSessionStore(File(blocker, "save.json"))
        assertThrows(Exception::class.java) { store.save(GameSession.create(fixture()).saved()) }
    }
}
