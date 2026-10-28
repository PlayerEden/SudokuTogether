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
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicLong
import android.os.SystemClock
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

enum class Page { HOME, JOIN, PLAY, PROFILE }
data class GameUiState(
    val page: Page = Page.HOME, val name: String = "Player", val role: String = HOST,
    val snapshot: Snapshot? = null, val status: RoomStatus = RoomStatus(), val selected: Int = -1,
    val partnerCell: Int = -1, val notes: Boolean = false, val pendingId: String? = null,
    val busy: Boolean = false, val connected: Boolean = false, val roomCode: String = "",
    val address: String = "", val rooms: List<NearbyRoom> = emptyList(), val resumeHost: Boolean = false,
    val resumeGuest: Boolean = false, val resumingGuest: Boolean = false, val saveIssue: String? = null, val message: String? = null,
    val profile: LocalPlayerData = LocalPlayerData(), val resumeSolo: Boolean = false, val soloElapsedMs: Long = 0L,
    val resumeRace: Boolean = false, val appearance: Appearance = Appearance(),
    val chat: List<ChatLine> = emptyList(), val unreadChat: Int = 0,
    val chatVisible: Boolean = false,
    val uniformNumbers: Boolean = false,
    val touchFeedback: Boolean = true,
    val selectedDifficulty: String = "EASY",
    val autoCleanNotes: Boolean = false,
    val hint: SudokuAssistance.Hint? = null,
    val dailyDate: String? = null,
    val todayDate: String = DailyChallenge.today()
) {
    val canEdit: Boolean get() = snapshot != null && !snapshot.completed && !busy && pendingId == null && (role == SOLO || (!status.paused && (snapshot.mode != VERSUS || status.guestActive) && (role == HOST || connected)))
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("settings", 0)
    private fun savedAppearance(): Appearance = Cosmetics.accessible(
        Appearance(
            preferences.getString("cosmeticInk", "forest") ?: "forest",
            preferences.getString("cosmeticBoard", "classic") ?: "classic",
            preferences.getString("cosmeticFont", "clear") ?: "clear",
            preferences.getString("cosmeticEmblem", "tile") ?: "tile"
        ), localPlayer.current().records
    )
    private val hostStore = AtomicSessionStore(File(application.filesDir, "host-session.json"))
    private val raceStore = AtomicSessionStore(File(application.filesDir, "host-race-session.json"))
    private val guestStore = AtomicSessionStore(File(application.filesDir, "guest-session.json"))
    private val localPlayer = LocalPlayerStore(File(application.filesDir, "local-player.json"))
    private var soloStartedAt: Long? = null
    private var soloElapsedAnchor: Long = 0L
    private var soloTick: Job? = null
    private val _state = MutableStateFlow(GameUiState(name = preferences.getString("name", "Player") ?: "Player", busy = true, profile = localPlayer.current(), resumeSolo = localPlayer.current().activeSolo != null, appearance = savedAppearance(),
        uniformNumbers = preferences.getBoolean("uniformNumbers", false),
        touchFeedback = preferences.getBoolean("touchFeedback", true),
        autoCleanNotes = preferences.getBoolean("autoCleanNotes", false),
        selectedDifficulty = preferences.getString("difficulty", "EASY")
            .let { if (it in listOf("EASY", "MEDIUM", "HARD")) it!! else "EASY" }))
    val state = _state.asStateFlow()
    private val generation = AtomicLong()
    @Volatile private var server: HostServer? = null
    @Volatile private var guest: GuestClient? = null
    @Volatile private var guestResume: SavedSession? = null
    @Volatile private var visible = true
    private val discovery = RoomDiscovery(application) { message -> _state.update { it.copy(message = message) } }
    // Fresh puzzles use unpredictable seeds. The previous eight-seed rotation
    // caused regular players to replay identical boards.
    private val puzzleRandom = SecureRandom()

    init { viewModelScope.launch(Dispatchers.IO) {
        val hosted = hostStore.load(); val raced = raceStore.load(); val joined = guestStore.load()
        guestResume = when (joined) { is LoadResult.Loaded -> joined.data; is LoadResult.Recovered -> joined.data; else -> null }
        _state.update { it.copy(busy = false, resumeHost = hosted is LoadResult.Loaded || hosted is LoadResult.Recovered,
            resumeGuest = guestResume != null, resumeRace = raced is LoadResult.Loaded || raced is LoadResult.Recovered,
            saveIssue = localPlayer.recoveryWarning ?: when (hosted) {
                is LoadResult.Invalid -> hosted.message
                is LoadResult.Recovered -> "Your previous backup was recovered. Resume to continue from that saved point."
                else -> null
            }) }
    } }

    fun setName(name: String) {
        val value = name.filterNot { it.isISOControl() }.take(24)
        _state.update { it.copy(name = value) }; preferences.edit().putString("name", value).apply()
    }
    fun chooseCosmetic(group: String, id: String) {
        val state = _state.value
        if (!Cosmetics.allowed(id, state.profile.records)) return
        val next = when (group) {
            "ink" -> state.appearance.copy(ink = id)
            "board" -> state.appearance.copy(board = id)
            "font" -> state.appearance.copy(font = id)
            "emblem" -> state.appearance.copy(emblem = id)
            else -> return
        }
        if (!Cosmetics.valid(next)) return
        preferences.edit().putString("cosmeticInk", next.ink)
            .putString("cosmeticBoard", next.board).putString("cosmeticFont", next.font)
            .putString("cosmeticEmblem", next.emblem).apply()
        _state.update { it.copy(appearance = next) }
        val current = generation.get()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (generation.get() == current) {
                    if (state.role == HOST) server?.setHostAppearance(next)
                    else if (state.role == GUEST) guest?.changeAppearance(next)
                }
            } catch (_: Exception) {
                _state.update { it.copy(message = "Your style is saved for your next room.") }
            }
        }
    }
    fun setDifficulty(level: String) {
        if (level !in listOf("EASY", "MEDIUM", "HARD") || _state.value.busy) return
        preferences.edit().putString("difficulty", level).apply()
        _state.update { it.copy(selectedDifficulty = level) }
    }
    fun setAutoCleanNotes(enabled: Boolean) {
        preferences.edit().putBoolean("autoCleanNotes", enabled).apply()
        _state.update { it.copy(autoCleanNotes = enabled) }
    }
    fun dismissHint() { _state.update { it.copy(hint = null) } }
    private fun progressFor(data: LocalPlayerData, date: String?): SoloProgress? =
        if (date == null) data.activeSolo else DailyChallenge.find(data, date)

    private fun withProgress(data: LocalPlayerData, date: String?, progress: SoloProgress): LocalPlayerData =
        if (date == null) data.copy(activeSolo = progress) else DailyChallenge.update(data, date, progress)

    fun requestHint() {
        val before = _state.value
        if (before.role != SOLO || before.page != Page.PLAY || !before.canEdit ||
            before.snapshot == null || before.hint != null) return
        val token = generation.get()
        val expectedId = before.snapshot.sessionId
        val expectedMoves = before.snapshot.revision
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val progress = progressFor(localPlayer.current(), before.dailyDate)
                val hint = if (progress != null && progress.id == expectedId &&
                    progress.moves.toLong() == expectedMoves && !progress.completed)
                    SudokuAssistance.nextHint(progress.puzzle, progress.cells.map { it.value }) else null
                if (hint == null) {
                    if (generation.get() == token) _state.update { it.copy(busy = false,
                        message = "No safe logical hint is available. Check for incorrect numbers or use Undo.") }
                    return@launch
                }
                val updated = localPlayer.update { data ->
                    val current = progressFor(data, before.dailyDate)
                    if (current != null && current.id == expectedId &&
                        current.moves.toLong() == expectedMoves && !current.completed)
                        withProgress(data, before.dailyDate, PersonalPlay.recordHint(current)) else data
                }
                val confirmed = progressFor(updated, before.dailyDate)
                if (generation.get() == token && confirmed?.id == expectedId &&
                    confirmed.moves.toLong() == expectedMoves) {
                    _state.update { it.copy(profile = updated, busy = false, hint = hint, selected = hint.cell) }
                } else if (generation.get() == token) _state.update { it.copy(busy = false) }
            } catch (_: Exception) {
                if (generation.get() == token) _state.update { it.copy(busy = false,
                    message = "Could not save hint usage. Your puzzle is unchanged.") }
            }
        }
    }
    fun setTouchFeedback(enabled: Boolean) {
        preferences.edit().putBoolean("touchFeedback", enabled).apply()
        _state.update { it.copy(touchFeedback = enabled) }
    }
    fun setUniformNumbers(enabled: Boolean) {
        preferences.edit().putBoolean("uniformNumbers", enabled).apply()
        _state.update { it.copy(uniformNumbers = enabled) }
    }
    fun chatVisible(open: Boolean) { _state.update { it.copy(chatVisible = open, unreadChat = if (open) 0 else it.unreadChat) } }
    fun sendChat(raw: String) {
        val content = ChatRules.clean(raw)
        val s = _state.value
        if (content.isBlank() || s.role == SOLO || s.page != Page.PLAY || s.snapshot == null) return
        val current = generation.get()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (generation.get() == current) {
                    if (s.role == HOST) {
                        if (server?.chat(content) != true) _state.update { it.copy(message = "Chat is busy. Try again shortly.") }
                    } else guest?.sendChat(content)
                }
            } catch (_: Exception) {
                _state.update { it.copy(message = "Could not send chat. Check your connection.") }
            }
        }
    }
    @Synchronized private fun nextPuzzle(level: String): Puzzle {
        // History is deliberately independent of saved solo/host/guest sessions:
        // installing this update doesn't modify or invalidate unfinished games.
        val recent = preferences.getString("recentPuzzleIds", "")
            .orEmpty().split('|').filter { it.isNotBlank() }
        val grade = when (level) {
            "EASY" -> HumanSudoku.Grade.EASY
            "MEDIUM" -> HumanSudoku.Grade.MEDIUM
            "HARD" -> HumanSudoku.Grade.HARD
            else -> error("Choose Easy, Medium or Hard before starting a new puzzle.")
        }
        val puzzle = PuzzleSupply.nextRated(grade, recent, puzzleRandom::nextLong).puzzle
        val updated = PuzzleSupply.remember(recent, puzzle.id).joinToString("|")
        check(preferences.edit().putString("recentPuzzleIds", updated).commit()) {
            "Could not save your puzzle history"
        }
        return puzzle
    }
    private fun elapsedSolo(): Long =
        soloElapsedAnchor + (soloStartedAt?.let { (SystemClock.elapsedRealtime() - it).coerceAtLeast(0L) } ?: 0L)

    private fun stopSoloClock() {
        val previous = _state.value
        if (previous.role == SOLO && previous.snapshot != null && !previous.snapshot.completed) {
            val elapsed = elapsedSolo()
            try {
                val updated = localPlayer.update { data ->
                    val solo = progressFor(data, previous.dailyDate)
                    if (solo != null && solo.id == previous.snapshot.sessionId)
                        withProgress(data, previous.dailyDate, PersonalPlay.withElapsed(solo, elapsed))
                    else data
                }
                _state.update { it.copy(profile = updated, soloElapsedMs = elapsed) }
            } catch (_: Exception) {
                _state.update { it.copy(saveIssue = "The latest solo time could not be saved. Check phone storage.") }
            }
        }
        soloStartedAt = null
        soloTick?.cancel()
        soloTick = null
    }

    private fun startSoloClock(progress: SoloProgress) {
        soloTick?.cancel()
        soloElapsedAnchor = progress.elapsedMs
        soloStartedAt = if (progress.completed || !visible) null else SystemClock.elapsedRealtime()
        soloTick = viewModelScope.launch {
            while (isActive && _state.value.role == SOLO && !_state.value.snapshot.let { it == null || it.completed }) {
                _state.update { it.copy(soloElapsedMs = elapsedSolo()) }
                delay(500)
            }
        }
    }

    fun solo(resume: Boolean) {
        if (_state.value.busy) return
        stopSoloClock()
        val current = closeRoom()
        _state.update { it.copy(page = Page.PLAY, role = SOLO, dailyDate = null, busy = true, selected = -1, pendingId = null, partnerCell = -1, message = null, hint = null, chat = emptyList(), unreadChat = 0, chatVisible = false) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val saved = localPlayer.update { data ->
                    if (resume && data.activeSolo != null) data
                    else data.copy(activeSolo = PersonalPlay.newSolo(nextPuzzle(_state.value.selectedDifficulty), System.currentTimeMillis()))
                }
                if (generation.get() != current) return@launch
                val progress = saved.activeSolo!!
                _state.update { it.copy(page = Page.PLAY, role = SOLO, snapshot = progress.snapshot(),
                    profile = saved, resumeSolo = true, status = RoomStatus(), connected = true,
                    busy = false, soloElapsedMs = progress.elapsedMs) }
                startSoloClock(progress)
            } catch (e: Exception) {
                if (generation.get() == current) _state.update { it.copy(page = Page.HOME, busy = false, message = "Could not save the solo puzzle: " + (e.message ?: "storage unavailable")) }
            }
        }
    }

    fun daily() = openDaily(DailyChallenge.today())

    /** Reopening a past daily is allowed only when it was already saved. */
    fun openDaily(date: String) {
        if (_state.value.busy) return
        val today = DailyChallenge.today()
        if (date != today && localPlayer.current().dailyProgress.none { it.date == date }) {
            _state.update { it.copy(message = "That daily puzzle is not saved on this phone.") }
            return
        }
        stopSoloClock()
        val current = closeRoom()
        _state.update { it.copy(page = Page.PLAY, role = SOLO, dailyDate = date, busy = true,
            snapshot = null, selected = -1, pendingId = null, notes = false, partnerCell = -1,
            message = null, hint = null, chat = emptyList(), unreadChat = 0, chatVisible = false) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val saved = localPlayer.update { data ->
                    if (date != today && data.dailyProgress.none { it.date == date })
                        error("That older daily puzzle is no longer saved.")
                    DailyChallenge.open(data, date, System.currentTimeMillis())
                }
                if (generation.get() != current) return@launch
                val progress = DailyChallenge.find(saved, date)!!
                _state.update { it.copy(page = Page.PLAY, role = SOLO, dailyDate = date,
                    snapshot = progress.snapshot(), profile = saved, status = RoomStatus(),
                    connected = true, busy = false, soloElapsedMs = progress.elapsedMs) }
                startSoloClock(progress)
            } catch (e: Exception) {
                if (generation.get() == current) _state.update { it.copy(page = Page.HOME,
                    busy = false, dailyDate = null, todayDate = DailyChallenge.today(),
                    message = if (e.message?.contains("Too many unfinished") == true)
                        "Your daily progress is safe. Finish an older Daily Challenge before starting a new one."
                    else "Couldn't open that daily puzzle. Your Solo progress is still saved.") }
            }
        }
    }

    fun showProfile() {
        stopSoloClock()
        closeRoom()
        val saved = localPlayer.current()
        _state.update { it.copy(page = Page.PROFILE, profile = saved, resumeSolo = saved.activeSolo != null,
            snapshot = null, pendingId = null, busy = false, role = SOLO, message = null) }
    }

    private fun closeRoom(): Long {
        val current = generation.incrementAndGet()
        guest?.close(); guest = null; server?.close(); server = null; discovery.stop()
        return current
    }
    fun home() {
        stopSoloClock()
        closeRoom()
        val saved = localPlayer.current()
        _state.update { GameUiState(name = it.name, resumeHost = it.resumeHost,
            resumeGuest = guestResume != null, saveIssue = it.saveIssue, profile = saved,
            resumeSolo = saved.activeSolo != null, resumeRace = it.resumeRace, appearance = it.appearance,
            uniformNumbers = it.uniformNumbers, touchFeedback = it.touchFeedback,
            autoCleanNotes = it.autoCleanNotes, selectedDifficulty = it.selectedDifficulty,
            todayDate = DailyChallenge.today()) }
    }
    fun host(resume: Boolean) = hostInternal(resume, versus = false)

    fun hostRace(resume: Boolean) = hostInternal(resume, versus = true)

    private fun recordMultiplayer(snapshot: Snapshot, role: String) {
        if (!snapshot.completed) return
        if (snapshot.mode == VERSUS && snapshot.winner == null) return
        val id = snapshot.sessionId + ":" + snapshot.epoch + ":" + role
        // Presence changes and chat also emit room events. A finished match
        // should not rewrite the same durable record for every such event.
        if (localPlayer.current().records.any { it.id == id }) return
        try {
            val saved = localPlayer.update { data ->
                if (data.records.any { it.id == id }) data
                else {
                    val now = System.currentTimeMillis()
                    val elapsed = if (snapshot.mode == VERSUS)
                        (now - (snapshot.raceStartedAtMs ?: now)).coerceAtLeast(0L) else 0L
                    val record = GameRecord(id, snapshot.mode,
                        if (snapshot.mode == COOP) "SOLVED" else if (snapshot.winner == role) "WIN" else "LOSS",
                        snapshot.puzzle.id, elapsed, 0, now, snapshot.puzzle.difficulty)
                    data.copy(records = (data.records + record).takeLast(MAX_RECORDS))
                }
            }
            _state.update { it.copy(profile = saved) }
        } catch (_: Exception) {
            _state.update { it.copy(message = "Match finished, but this phone could not save your record.") }
        }
    }

    private fun hostInternal(resume: Boolean, versus: Boolean) {
        if (_state.value.busy) return
        stopSoloClock()
        val current = closeRoom(); val name = cleanName(_state.value.name, "Player")
        val store = if (versus) raceStore else hostStore
        val selectedStyle = _state.value.appearance
        _state.update { it.copy(page = Page.PLAY, role = HOST, snapshot = null, busy = true, pendingId = null, selected = -1, partnerCell = -1, message = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val session = if (resume) {
                    val saved = when (val loaded = store.load()) { is LoadResult.Loaded -> loaded.data; is LoadResult.Recovered -> loaded.data; else -> error("The saved puzzle is unavailable. It has not been replaced.") }
                    GameSession(saved, store::save).also { it.setAppearance(HOST, selectedStyle) }
                } else GameSession.create(nextPuzzle(_state.value.selectedDifficulty), name, store::save, if (versus) VERSUS else COOP, selectedStyle)
                if (generation.get() != current) return@launch
                val hosted = HostServer(session) { event -> if (generation.get() == current) {
                    recordMultiplayer(event.snapshot, HOST)
                    _state.update { previous ->
                        val changed = previous.snapshot?.epoch != event.snapshot.epoch
                        previous.copy(snapshot = event.snapshot, status = event.status, connected = event.status.guestActive,
                            partnerCell = event.partnerCell, selected = if (changed) -1 else previous.selected,
                            pendingId = if (changed || event.outcome?.operationId == previous.pendingId) null else previous.pendingId,
                            message = if (event.outcome != null) event.outcome?.let { if (it.accepted) null else explain(it.code) } else if (changed) null else previous.message,
                            resumeRace = if (versus) true else previous.resumeRace,
                            chat = if (event.chat != null) (previous.chat + listOfNotNull(event.chat)).takeLast(ChatRules.HISTORY_LIMIT) else previous.chat,
                            unreadChat = if (previous.chatVisible) 0 else previous.unreadChat + if (event.chat?.player == GUEST) 1 else 0)
                    }
                } }
                server = hosted; val port = hosted.start(); hosted.setForeground(visible)
                if (generation.get() != current) { hosted.close(); return@launch }
                val address = discovery.localAddresses().firstOrNull()?.let { "$it:$port" } ?: "Connect this phone to home Wi-Fi"
                _state.update { it.copy(busy = false, roomCode = hosted.code, address = address, resumeHost = if (versus) it.resumeHost else true, resumeRace = if (versus) true else it.resumeRace, saveIssue = null) }
                discovery.startHosting(session.snapshot().hostName, port, session.snapshot().sessionId)
            } catch (e: Exception) { if (generation.get() == current) _state.update { it.copy(page = Page.HOME, busy = false, message = e.message ?: "Couldn't open the saved puzzle.") } }
        }
    }
    fun showJoin(resume: Boolean = false) {
        if (_state.value.busy) return
        stopSoloClock()
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
        _state.update { it.copy(busy = true, role = GUEST, connected = false, pendingId = null, message = null, selected = -1, partnerCell = -1, chat = emptyList(), unreadChat = 0, chatVisible = false) }
        viewModelScope.launch(Dispatchers.IO) {
            var savedToken = identity?.token
            val client = GuestClient(socketFactory = discovery::newWifiSocket) { event ->
                if (generation.get() == current) {
                    if (event.token != null) savedToken = event.token
                    val snapshot = event.snapshot
                    if (snapshot != null) recordMultiplayer(snapshot, GUEST)
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
                            message = when { saveMessage != null -> saveMessage; event.message != null -> event.message; event.outcome != null -> event.outcome?.let { if (it.accepted) null else explain(it.code) }; changed -> null; else -> previous.message },
                            chat = event.history ?: if (event.chat != null) (previous.chat + listOfNotNull(event.chat)).takeLast(ChatRules.HISTORY_LIMIT) else previous.chat,
                            unreadChat = if (event.history != null || previous.chatVisible) 0 else previous.unreadChat + if (event.chat?.player == HOST) 1 else 0)
                    }
                }
            }
            try {
                guest = client; client.connect(endpoint.host, endpoint.port, code, name, identity?.token, _state.value.appearance)
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
        _state.update { it.copy(selected = cell, hint = null) }
        if (_state.value.role == HOST) server?.select(cell) else if (_state.value.role == GUEST) guest?.select(cell)
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
        val current = generation.get(); _state.update { it.copy(pendingId = operation.id, message = null, hint = null) }
        if (value.role == SOLO) {
            val elapsed = elapsedSolo()
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val saved = localPlayer.update { data ->
                        val progress = progressFor(data, value.dailyDate)
                        if (progress == null || progress.id != snapshot.sessionId) data
                        else {
                            val next = PersonalPlay.edit(progress, cell, kind, number, enabled, elapsed,
                                autoCleanNotes = value.autoCleanNotes)
                            if (next == null) data else if (next.completed) {
                                if (value.dailyDate == null) PersonalPlay.addCompletedSolo(data, next)
                                else DailyChallenge.complete(data, value.dailyDate, next)
                            } else withProgress(data, value.dailyDate, next)
                        }
                    }
                    if (generation.get() == current) {
                        val progress = progressFor(saved, value.dailyDate)!!
                        if (progress.completed) {
                            soloStartedAt = null
                            soloTick?.cancel()
                        } else {
                            soloElapsedAnchor = progress.elapsedMs
                            soloStartedAt = if (visible) SystemClock.elapsedRealtime() else null
                        }
                        _state.update { it.copy(snapshot = progress.snapshot(), profile = saved,
                            soloElapsedMs = progress.elapsedMs, pendingId = null) }
                    }
                } catch (_: Exception) {
                    if (generation.get() == current) _state.update { it.copy(pendingId = null,
                        message = "That move could not be saved. Check available phone storage.") }
                }
            }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try { if (generation.get() == current) { if (value.role == HOST) server?.submitLocal(operation) else guest?.submit(operation) } }
            catch (_: Exception) { if (generation.get() == current) _state.update { it.copy(pendingId = null, message = "The connection paused. Reconnect before entering another move.") } }
        }
    }
    fun foreground(active: Boolean) {
        visible = active
        if (_state.value.role == SOLO && _state.value.snapshot != null) {
            if (!active) stopSoloClock()
            else if (soloStartedAt == null && !_state.value.snapshot!!.completed) {
                val progress = progressFor(localPlayer.current(), _state.value.dailyDate)
                if (progress != null) startSoloClock(progress)
            }
        }
        if (active && _state.value.page == Page.HOME) {
            _state.update { it.copy(todayDate = DailyChallenge.today()) }
        }
        viewModelScope.launch(Dispatchers.IO) { server?.setForeground(active); guest?.setForeground(active) }
    }
    fun replacePartner() { viewModelScope.launch(Dispatchers.IO) {
        try { server?.replaceGuest() } catch (_: Exception) { _state.update { it.copy(message = "Couldn't save the partner change. Try again.") } }
    } }
    fun newPuzzle() { if (_state.value.role == SOLO) {
        if (_state.value.dailyDate != null) home() else solo(false)
        return
    }; _state.update { it.copy(busy = true, message = null) }; viewModelScope.launch(Dispatchers.IO) {
        try { server?.let { host ->
            val difficulty = host.session.snapshot().puzzle.difficulty
                ?.takeIf { it in listOf("EASY", "MEDIUM", "HARD") }
                ?: _state.value.selectedDifficulty
            host.newPuzzle(nextPuzzle(difficulty))
        } } catch (_: Exception) { _state.update { it.copy(message = "Couldn't save a new puzzle. Your current puzzle is still here.") } }
        finally { _state.update { it.copy(busy = false) } }
    } }
    override fun onCleared() { stopSoloClock(); closeRoom(); super.onCleared() }
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
