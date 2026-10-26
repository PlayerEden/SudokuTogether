package com.playereden.sudokutogether

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playereden.sudokutogether.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.concurrent.atomic.AtomicLong

enum class Page { HOME, JOIN, PLAY }
data class GameUiState(
    val page: Page = Page.HOME, val name: String = "Player", val role: String = HOST,
    val snapshot: Snapshot? = null, val status: RoomStatus = RoomStatus(), val selected: Int = -1,
    val partnerCell: Int = -1, val notes: Boolean = false, val pendingId: String? = null,
    val busy: Boolean = false, val connected: Boolean = false, val roomCode: String = "",
    val address: String = "", val rooms: List<NearbyRoom> = emptyList(), val resumeHost: Boolean = false,
    val resumeGuest: Boolean = false, val resumingGuest: Boolean = false, val saveIssue: String? = null, val message: String? = null
) {
    val canEdit: Boolean get() = snapshot != null && !snapshot.completed && !busy && pendingId == null && !status.paused && (role == HOST || connected)
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("settings", 0)
    private val hostStore = AtomicSessionStore(File(application.filesDir, "host-session.json"))
    private val guestStore = AtomicSessionStore(File(application.filesDir, "guest-session.json"))
    private val _state = MutableStateFlow(GameUiState(name = preferences.getString("name", "Player") ?: "Player", busy = true))
    val state = _state.asStateFlow()
    private val generation = AtomicLong()
    @Volatile private var server: HostServer? = null
    @Volatile private var guest: GuestClient? = null
    @Volatile private var guestResume: SavedSession? = null
    @Volatile private var visible = true
    private val discovery = RoomDiscovery(application) { message -> _state.update { it.copy(message = message) } }
    private val seeds = longArrayOf(20261025, 2048, 729, 81, 314159, 271828, 1009, 303)

    init { viewModelScope.launch(Dispatchers.IO) {
        val hosted = hostStore.load(); val joined = guestStore.load()
        guestResume = when (joined) { is LoadResult.Loaded -> joined.data; is LoadResult.Recovered -> joined.data; else -> null }
        _state.update { it.copy(busy = false, resumeHost = hosted is LoadResult.Loaded || hosted is LoadResult.Recovered,
            resumeGuest = guestResume != null, saveIssue = when (hosted) {
                is LoadResult.Invalid -> hosted.message
                is LoadResult.Recovered -> "Your previous backup was recovered. Resume to continue from that saved point."
                else -> null
            }) }
    } }

    fun setName(name: String) {
        val value = name.filterNot { it.isISOControl() }.take(24)
        _state.update { it.copy(name = value) }; preferences.edit().putString("name", value).apply()
    }
    private fun nextSeed(): Long {
        val index = preferences.getInt("puzzleIndex", 0).coerceAtLeast(0)
        preferences.edit().putInt("puzzleIndex", (index + 1) % seeds.size).apply()
        return seeds[index % seeds.size]
    }
    private fun closeRoom(): Long {
        val current = generation.incrementAndGet()
        guest?.close(); guest = null; server?.close(); server = null; discovery.stop()
        return current
    }
    fun home() {
        closeRoom()
        _state.update { GameUiState(name = it.name, resumeHost = it.resumeHost, resumeGuest = guestResume != null, saveIssue = it.saveIssue) }
    }
    fun host(resume: Boolean) {
        if (_state.value.busy) return
        val current = closeRoom(); val name = cleanName(_state.value.name, "Player")
        _state.update { it.copy(page = Page.PLAY, role = HOST, snapshot = null, busy = true, pendingId = null, selected = -1, partnerCell = -1, message = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val session = if (resume) {
                    val saved = when (val loaded = hostStore.load()) { is LoadResult.Loaded -> loaded.data; is LoadResult.Recovered -> loaded.data; else -> error("The saved puzzle is unavailable. It has not been replaced.") }
                    GameSession(saved, hostStore::save)
                } else GameSession.create(Sudoku.generate(nextSeed()), name, hostStore::save)
                if (generation.get() != current) return@launch
                val hosted = HostServer(session) { event -> if (generation.get() == current) {
                    _state.update { previous ->
                        val changed = previous.snapshot?.epoch != event.snapshot.epoch
                        previous.copy(snapshot = event.snapshot, status = event.status, connected = event.status.guestActive,
                            partnerCell = event.partnerCell, selected = if (changed) -1 else previous.selected,
                            pendingId = if (changed || event.outcome?.operationId == previous.pendingId) null else previous.pendingId,
                            message = if (event.outcome != null) event.outcome?.let { if (it.accepted) null else explain(it.code) } else if (changed) null else previous.message)
                    }
                } }
                server = hosted; val port = hosted.start(); hosted.setForeground(visible)
                if (generation.get() != current) { hosted.close(); return@launch }
                val address = discovery.localAddresses().firstOrNull()?.let { "$it:$port" } ?: "Connect this phone to home Wi-Fi"
                _state.update { it.copy(busy = false, roomCode = hosted.code, address = address, resumeHost = true, saveIssue = null) }
                discovery.startHosting(session.snapshot().hostName, port, session.snapshot().sessionId)
            } catch (e: Exception) { if (generation.get() == current) _state.update { it.copy(page = Page.HOME, busy = false, message = e.message ?: "Couldn't open the saved puzzle.") } }
        }
    }
    fun showJoin(resume: Boolean = false) {
        if (_state.value.busy) return
        val current = closeRoom()
        _state.update { it.copy(page = Page.JOIN, role = GUEST, snapshot = null, busy = false, rooms = emptyList(), pendingId = null,
            resumingGuest = resume && guestResume != null, address = if (resume) preferences.getString("lastAddress", "") ?: "" else "", message = null) }
        discovery.discover { found -> if (generation.get() == current) _state.update { it.copy(rooms = found) } }
    }
    fun join(endpoint: Endpoint, code: String) {
        val resume = _state.value.resumingGuest
        val identity = if (resume) guestResume?.guest else null
        val expectedSession = if (resume) guestResume?.snapshot?.sessionId else null
        val current = closeRoom(); val name = cleanName(_state.value.name, "Partner")
        _state.update { it.copy(busy = true, role = GUEST, connected = false, pendingId = null, message = null, selected = -1, partnerCell = -1) }
        viewModelScope.launch(Dispatchers.IO) {
            var savedToken = identity?.token
            val client = GuestClient(socketFactory = discovery::newWifiSocket) { event ->
                if (generation.get() == current) {
                    if (event.token != null) savedToken = event.token
                    val snapshot = event.snapshot
                    var saveMessage: String? = null
                    if (snapshot != null && savedToken != null) {
                        if (expectedSession != null && snapshot.sessionId != expectedSession) {
                            guest?.close()
                            _state.update { it.copy(busy = false, connected = false, message = "This is a different puzzle room. Join it again with its room code.") }
                            return@GuestClient
                        }
                        val mirror = SavedSession(snapshot = snapshot, guest = GuestIdentity(snapshot.guestName ?: name, savedToken!!))
                        try { guestStore.save(mirror); guestResume = mirror } catch (_: Exception) { saveMessage = "This phone couldn't save its rejoin information. Your host still has the shared puzzle." }
                    }
                    _state.update { previous ->
                        val changed = snapshot != null && previous.snapshot?.epoch != snapshot.epoch
                        previous.copy(page = if (snapshot != null || previous.snapshot != null) Page.PLAY else previous.page,
                            snapshot = snapshot ?: previous.snapshot, status = if (event.disconnected) previous.status.copy(guestActive = false, reserved = true) else event.status ?: previous.status,
                            connected = if (event.disconnected) false else if (snapshot != null) true else previous.connected,
                            busy = false, resumeGuest = guestResume != null,
                            selected = if (changed) -1 else previous.selected,
                            partnerCell = if (changed || event.disconnected) -1 else event.selected ?: previous.partnerCell,
                            pendingId = if (changed || event.disconnected || event.outcome?.operationId == previous.pendingId) null else previous.pendingId,
                            message = when { saveMessage != null -> saveMessage; event.message != null -> event.message; event.outcome != null -> event.outcome?.let { if (it.accepted) null else explain(it.code) }; changed -> null; else -> previous.message })
                    }
                }
            }
            try {
                guest = client; client.connect(endpoint.host, endpoint.port, code, name, identity?.token)
                if (generation.get() != current) { client.close(); return@launch }
                client.setForeground(visible)
                val formatted = if (endpoint.host.contains(':')) "[${endpoint.host}]:${endpoint.port}" else "${endpoint.host}:${endpoint.port}"
                preferences.edit().putString("lastAddress", formatted).apply()
            } catch (e: Exception) {
                client.close()
                if (generation.get() == current) _state.update { it.copy(busy = false, connected = false, message = if (!discovery.hasWifi()) "Wi-Fi disconnected. Connect both phones to the same home Wi-Fi and join again." else e.message ?: "Couldn't reach the host. Check that both phones are on the same Wi-Fi.") }
            }
        }
    }
    fun select(cell: Int) {
        if (cell !in 0..80) return
        _state.update { it.copy(selected = cell) }
        if (_state.value.role == HOST) server?.select(cell) else guest?.select(cell)
    }
    fun toggleNotes() { _state.update { it.copy(notes = !it.notes) } }
    fun number(number: Int) {
        val value = _state.value; val snapshot = value.snapshot ?: return
        if (value.selected !in 0..80 || !value.canEdit) return
        val enabled = snapshot.cells[value.selected].notes and (1 shl (number - 1)) == 0
        submit(value.selected, if (value.notes) Kind.NOTE else Kind.VALUE, number, enabled)
    }
    fun erase() { submit(_state.value.selected, Kind.CLEAR) }
    fun undo() { val value = _state.value; submit(value.snapshot?.undoTargets?.get(value.role) ?: return, Kind.UNDO) }
    private fun submit(cell: Int, kind: Kind, number: Int = 0, enabled: Boolean = true) {
        val value = _state.value; val snapshot = value.snapshot ?: return
        if (!value.canEdit || cell !in 0..80) return
        val operation = Operation(snapshot.sessionId, snapshot.epoch, cell, snapshot.cells[cell].revision, kind, number, enabled)
        val current = generation.get(); _state.update { it.copy(pendingId = operation.id, message = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try { if (generation.get() == current) { if (value.role == HOST) server?.submitLocal(operation) else guest?.submit(operation) } }
            catch (_: Exception) { if (generation.get() == current) _state.update { it.copy(pendingId = null, message = "The connection paused. Reconnect before entering another move.") } }
        }
    }
    fun foreground(active: Boolean) {
        visible = active
        viewModelScope.launch(Dispatchers.IO) { server?.setForeground(active); guest?.setForeground(active) }
    }
    fun replacePartner() { viewModelScope.launch(Dispatchers.IO) {
        try { server?.replaceGuest() } catch (_: Exception) { _state.update { it.copy(message = "Couldn't save the partner change. Try again.") } }
    } }
    fun newPuzzle() { _state.update { it.copy(busy = true, message = null) }; viewModelScope.launch(Dispatchers.IO) {
        try { server?.newPuzzle(nextSeed()) } catch (_: Exception) { _state.update { it.copy(message = "Couldn't save a new puzzle. Your current puzzle is still here.") } }
        finally { _state.update { it.copy(busy = false) } }
    } }
    override fun onCleared() { closeRoom(); super.onCleared() }
    private fun explain(code: ResultCode): String = when (code) {
        ResultCode.STALE -> "Your partner changed that square. Check the updated square and try again."
        ResultCode.GIVEN -> "Starting clues stay fixed. Choose an empty square."
        ResultCode.PAUSED -> "Both players need to return to the game to continue."
        ResultCode.COMPLETE -> "Puzzle complete! The host can start another."
        ResultCode.NO_UNDO -> "That move can no longer be undone without changing your partner's work."
        ResultCode.WRONG_GAME -> "The host started a different puzzle. Your board has been refreshed."
        ResultCode.SAVE_FAILED -> "Your move wasn't saved. Free some space on the host phone and try again."
        else -> "Choose an empty square for notes, or switch Notes off to enter a number."
    }
}
