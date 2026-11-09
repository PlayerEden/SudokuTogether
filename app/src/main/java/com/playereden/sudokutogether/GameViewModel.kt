package com.playereden.sudokutogether

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playereden.sudokutogether.core.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicLong
import android.os.SystemClock

internal fun cleanPlayerName(name: String): String =
    name.filterNot { it.isISOControl() }.take(24).dropLastWhile { Character.isHighSurrogate(it) }

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
    val finishedSolo: SoloProgress? = null,
    val soloClockStartedAt: Long? = null,
    val localSaveBlocked: Boolean = false,
    val todayDate: String = DailyChallenge.today(),
    val hintUnavailable: Boolean = false,
    val entryCheck: SudokuAssistance.EntryCheck? = null,
    val requireRoomCode: Boolean = false
) {
    val canEdit: Boolean get() = snapshot != null && !snapshot.completed && !busy && pendingId == null && ((role == SOLO && !localSaveBlocked) || (role != SOLO && !status.paused && (snapshot.mode != VERSUS || status.guestActive) && (role == HOST || connected)))
}

class GameViewModel @JvmOverloads constructor(
    application: Application, private val workDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("settings", 0)
    private fun savedAppearance(): Appearance = Cosmetics.accessible(
        Appearance(
            preferences.getString("cosmeticInk", "forest") ?: "forest",
            preferences.getString("cosmeticBoard", "classic") ?: "classic",
            preferences.getString("cosmeticFont", "clear") ?: "clear",
            preferences.getString("cosmeticEmblem", "tile") ?: "tile",
            preferences.getString("cosmeticPortrait", "") ?: ""
        ), localPlayer.current().records
    )
    private val hostStore = AtomicSessionStore(File(application.filesDir, "host-session.json"))
    private val raceStore = AtomicSessionStore(File(application.filesDir, "host-race-session.json"))
    private val guestStore = AtomicSessionStore(File(application.filesDir, "guest-session.json"))
    private val playerFile = File(application.filesDir, "local-player.json")
    private val backupStore = ProgressBackupStore(playerFile, preferences, File(application.dataDir, "shared_prefs/settings.xml"))
    private lateinit var localPlayer: LocalPlayerStore
    private var soloStartedAt: Long? = null
    private var soloElapsedAnchor: Long = 0L
    private fun GameUiState.withStoredPlayerSettings(): GameUiState = copy(
        name = preferences.getString("name", "Player") ?: "Player",
        uniformNumbers = preferences.getBoolean("uniformNumbers", false),
        touchFeedback = preferences.getBoolean("touchFeedback", true),
        autoCleanNotes = preferences.getBoolean("autoCleanNotes", false),
        requireRoomCode = preferences.getBoolean("requireRoomCode", false),
        selectedDifficulty = preferences.getString("difficulty", "EASY")
            .let { if (it in listOf("EASY", "MEDIUM", "HARD")) it!! else "EASY" })
    private val _state = MutableStateFlow(GameUiState(busy = true).withStoredPlayerSettings())
    val state = _state.asStateFlow()
    private val _progressBackup = MutableStateFlow(ProgressBackupUiState())
    internal val progressBackup = _progressBackup.asStateFlow()
    private val backupLocked: Boolean get() = _progressBackup.value.let { it.working || it.recoveryRequired }
    private val settingsLocked: Boolean get() = _state.value.busy || backupLocked
    private val generation = AtomicLong()
    private val localStartLock = Any()
    @Volatile private var server: HostServer? = null
    @Volatile private var guest: GuestClient? = null
    @Volatile private var guestResume: SavedSession? = null
    @Volatile private var visible = true
    private val discovery = RoomDiscovery(application) { message -> _state.update { it.copy(message = message) } }
    // Fresh puzzles use unpredictable seeds. The previous eight-seed rotation
    // caused regular players to replay identical boards.
    private val puzzleRandom = SecureRandom()

    init { viewModelScope.launch(workDispatcher) {
        localPlayer = LocalPlayerStore(playerFile)
        val recoveredRestore = try {
            backupStore.recoverPendingRestore(localPlayer)
        } catch (error: ProgressBackupException) {
            showBackupFailure(error)
            false
        }
        val profile = localPlayer.current()
        val hosted = hostStore.load(); val raced = raceStore.load(); val joined = guestStore.load()
        guestResume = when (joined) { is LoadResult.Loaded -> joined.data; is LoadResult.Recovered -> joined.data; else -> null }
        val restoreBlocked = _progressBackup.value.recoveryRequired
        _state.update { it.withStoredPlayerSettings().copy(busy = restoreBlocked, profile = profile, resumeSolo = profile.activeSolo != null,
            appearance = savedAppearance(), localSaveBlocked = restoreBlocked || !localPlayer.canSave,
            resumeHost = hosted is LoadResult.Loaded || hosted is LoadResult.Recovered,
            resumeGuest = guestResume != null, resumeRace = raced is LoadResult.Loaded || raced is LoadResult.Recovered,
            message = if (recoveredRestore) "Your progress from before the interrupted restore was recovered." else it.message,
            saveIssue = _progressBackup.value.message ?: localPlayer.recoveryWarning ?: when (hosted) {
                is LoadResult.Invalid -> hosted.message
                is LoadResult.Recovered -> "Your previous backup was recovered. Resume to continue from that saved point."
                else -> null
            }) }
    } }

    internal fun exportProgressBackup(uri: Uri) = runBackupOperation {
        val bytes = backupStore.export(localPlayer, BuildConfig.VERSION_NAME)
        val output = getApplication<Application>().contentResolver.openOutputStream(uri, "wt")
            ?: throw IOException("Progress destination is unavailable.")
        output.use { it.write(bytes) }
        ProgressBackupUiState(message = "Progress backup saved.")
    }

    internal fun previewProgressBackup(uri: Uri) = runBackupOperation {
        val input = getApplication<Application>().contentResolver.openInputStream(uri)
            ?: throw IOException("Progress file is unavailable.")
        ProgressBackupUiState(preview = input.use(ProgressBackup::decode))
    }

    internal fun dismissProgressBackupPreview() {
        if (!backupLocked) _progressBackup.update { it.copy(preview = null) }
    }

    internal fun confirmProgressBackupRestore() {
        val preview = _progressBackup.value.preview ?: return
        runBackupOperation {
            val restored = synchronized(localStartLock) { backupStore.restore(localPlayer, preview) }
            _state.update {
                it.withStoredPlayerSettings().copy(
                    profile = restored,
                    resumeSolo = restored.activeSolo != null,
                    appearance = savedAppearance()
                )
            }
            ProgressBackupUiState(message = "Progress restored.")
        }
    }

    private fun runBackupOperation(operation: () -> ProgressBackupUiState) {
        val before = _state.value
        if (before.page !in listOf(Page.HOME, Page.PROFILE) || before.busy || backupLocked) return
        if (before.localSaveBlocked) {
            showBackupFailure(IOException("Local progress needs recovery."))
            return
        }
        _progressBackup.value = ProgressBackupUiState(working = true)
        _state.update { it.copy(busy = true) }
        viewModelScope.launch(workDispatcher) {
            try {
                val result = operation()
                _progressBackup.value = result
                if (result.message != null) _state.update {
                    if (it.page == Page.HOME) it.copy(message = result.message) else it
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                showBackupFailure(error)
            } finally {
                _state.update { it.copy(busy = _progressBackup.value.recoveryRequired) }
            }
        }
    }

    private fun showBackupFailure(error: Exception) {
        val reason = (error as? ProgressBackupException)?.reason
        val recoveryRequired = reason == ProgressBackupException.Reason.RECOVERY_REQUIRED
        val message = when (reason) {
            ProgressBackupException.Reason.TOO_LARGE -> "This progress backup is too large."
            ProgressBackupException.Reason.INVALID -> "This file could not be read as a GridMates progress backup."
            ProgressBackupException.Reason.UNSUPPORTED_VERSION -> "This backup needs a newer version of GridMates."
            ProgressBackupException.Reason.RECOVERY_REQUIRED -> "Progress recovery needs attention. Check phone storage and reopen Sudoku."
            else -> "Couldn't complete the backup operation. Check phone storage and try again."
        }
        _progressBackup.value = ProgressBackupUiState(message = message, recoveryRequired = recoveryRequired)
        _state.update {
            it.copy(
                message = if (it.page == Page.HOME) message else it.message,
                localSaveBlocked = recoveryRequired || it.localSaveBlocked,
                saveIssue = if (recoveryRequired) message else it.saveIssue
            )
        }
    }

    fun setName(name: String) {
        if (settingsLocked) return
        val value = cleanPlayerName(name)
        _state.update { it.copy(name = value) }; preferences.edit().putString("name", value).apply()
    }
    fun setRequireRoomCode(enabled: Boolean) {
        if (settingsLocked) return
        preferences.edit().putBoolean("requireRoomCode", enabled).apply()
        _state.update { it.copy(requireRoomCode = enabled) }
    }
    fun chooseCosmetic(group: String, id: String) {
        if (settingsLocked) return
        val state = _state.value
        if (!Cosmetics.allowed(id, state.profile.records)) return
        val next = when (group) {
            "ink" -> state.appearance.copy(ink = id)
            "board" -> state.appearance.copy(board = id)
            "font" -> state.appearance.copy(font = id)
            "emblem" -> state.appearance.copy(emblem = id, portrait = "")
            "portrait" -> state.appearance.copy(portrait = id)
            else -> return
        }
        if (!Cosmetics.valid(next)) return
        preferences.edit().putString("cosmeticInk", next.ink)
            .putString("cosmeticBoard", next.board).putString("cosmeticFont", next.font)
            .putString("cosmeticEmblem", next.emblem).putString("cosmeticPortrait", next.portrait).apply()
        _state.update { it.copy(appearance = next) }
        val current = generation.get()
        viewModelScope.launch(workDispatcher) {
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
        if (level !in listOf("EASY", "MEDIUM", "HARD") || settingsLocked) return
        preferences.edit().putString("difficulty", level).apply()
        _state.update { it.copy(selectedDifficulty = level) }
    }
    fun setAutoCleanNotes(enabled: Boolean) {
        if (settingsLocked) return
        preferences.edit().putBoolean("autoCleanNotes", enabled).apply()
        _state.update { it.copy(autoCleanNotes = enabled) }
    }
    fun dismissHint() { _state.update { it.copy(hint = null, hintUnavailable = false, entryCheck = null) } }
    private fun progressFor(data: LocalPlayerData, date: String?): SoloProgress? =
        if (date == null) data.activeSolo else DailyChallenge.find(data, date)

    private fun withProgress(data: LocalPlayerData, date: String?, progress: SoloProgress): LocalPlayerData =
        if (date == null) data.copy(activeSolo = progress) else DailyChallenge.update(data, date, progress)

    private fun isAssistanceTarget(ui: GameUiState, before: GameUiState, token: Long): Boolean =
        generation.get() == token && ui.role == SOLO && ui.page == Page.PLAY &&
            ui.dailyDate == before.dailyDate && ui.snapshot?.sessionId == before.snapshot?.sessionId &&
            ui.snapshot?.revision == before.snapshot?.revision && ui.snapshot?.completed == false

    /** Recheck inside the CAS loop too: another state update can force a retry. */
    private fun publishAssistance(token: Long, before: GameUiState, change: (GameUiState) -> GameUiState) {
        synchronized(localStartLock) {
            _state.update { ui -> if (isAssistanceTarget(ui, before, token)) change(ui) else ui }
        }
    }

    fun requestHint() {
        val before = _state.value
        if (before.role != SOLO || before.page != Page.PLAY || !before.canEdit ||
            before.snapshot == null || before.hint != null || before.hintUnavailable || before.entryCheck != null) return
        val token = generation.get()
        val expectedId = before.snapshot.sessionId
        val expectedMoves = before.snapshot.revision
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch(workDispatcher) {
            try {
                if (generation.get() != token) return@launch
                val progress = progressFor(localPlayer.current(), before.dailyDate)
                val hint = if (progress != null && progress.id == expectedId &&
                    progress.moves.toLong() == expectedMoves && !progress.completed)
                    SudokuAssistance.nextHint(progress.puzzle, progress.cells.map { it.value }) else null
                if (hint == null) {
                    publishAssistance(token, before) { it.copy(busy = false,
                        hintUnavailable = true, message = null) }
                    return@launch
                }
                synchronized(localStartLock) {
                    if (!isAssistanceTarget(_state.value, before, token)) return@launch
                    var accepted = false
                    val updated = localPlayer.update { data ->
                        val current = progressFor(data, before.dailyDate)
                        if (current != null && current.id == expectedId &&
                            current.moves.toLong() == expectedMoves && !current.completed) {
                            accepted = true
                            withProgress(data, before.dailyDate, PersonalPlay.recordHint(current))
                        } else data
                    }
                    val confirmed = progressFor(updated, before.dailyDate)
                    if (accepted && confirmed?.id == expectedId && confirmed.moves.toLong() == expectedMoves) {
                        publishAssistance(token, before) { it.copy(profile = updated, busy = false, hint = hint, selected = hint.cell) }
                    } else publishAssistance(token, before) { it.copy(busy = false) }
                }
            } catch (_: Exception) {
                publishAssistance(token, before) { it.copy(busy = false,
                    message = "Could not save hint usage. Your puzzle is unchanged.") }
            }
        }
    }
    fun checkWork() {
        val before = _state.value
        if (before.role != SOLO || before.page != Page.PLAY || !before.canEdit ||
            before.snapshot == null || before.entryCheck != null) return
        val token = generation.get()
        val expectedId = before.snapshot.sessionId
        val expectedMoves = before.snapshot.revision
        _state.update { it.copy(busy = true, message = null, hint = null, hintUnavailable = false) }
        viewModelScope.launch(workDispatcher) {
            try {
                if (generation.get() != token) return@launch
                val progress = progressFor(localPlayer.current(), before.dailyDate)
                if (progress == null || progress.id != expectedId || progress.moves.toLong() != expectedMoves || progress.completed) {
                    publishAssistance(token, before) { it.copy(busy = false, message = "Your puzzle changed. Try again.") }
                    return@launch
                }
                val check = SudokuAssistance.checkEntries(progress.puzzle, progress.cells.map { it.value })
                if (check == null) {
                    publishAssistance(token, before) { it.copy(busy = false,
                        message = "Couldn't check this puzzle. Your entries are unchanged.") }
                    return@launch
                }
                synchronized(localStartLock) {
                    if (!isAssistanceTarget(_state.value, before, token)) return@launch
                    var accepted: SoloProgress? = null
                    val updated = if (check.usesSolution) localPlayer.update { data ->
                        val current = progressFor(data, before.dailyDate)
                        if (current != null && current.id == expectedId && current.moves.toLong() == expectedMoves && !current.completed) {
                            val counted = PersonalPlay.recordCheck(current)
                            accepted = counted
                            withProgress(data, before.dailyDate, counted)
                        } else data
                    } else localPlayer.current().also { data ->
                        accepted = progressFor(data, before.dailyDate)?.takeIf {
                            it.id == expectedId && it.moves.toLong() == expectedMoves && !it.completed
                        }
                    }
                    if (accepted != null) publishAssistance(token, before) { ui ->
                        ui.copy(profile = updated, busy = false, entryCheck = check,
                            selected = if (check.cell in 0..80) check.cell else ui.selected)
                    } else publishAssistance(token, before) { it.copy(busy = false) }
                }
            } catch (_: Exception) {
                publishAssistance(token, before) { it.copy(busy = false,
                    message = "Could not save the check. Your entries are unchanged.") }
            }
        }
    }
    fun setTouchFeedback(enabled: Boolean) {
        if (settingsLocked) return
        preferences.edit().putBoolean("touchFeedback", enabled).apply()
        _state.update { it.copy(touchFeedback = enabled) }
    }
    fun setUniformNumbers(enabled: Boolean) {
        if (settingsLocked) return
        preferences.edit().putBoolean("uniformNumbers", enabled).apply()
        _state.update { it.copy(uniformNumbers = enabled) }
    }
    fun chatVisible(open: Boolean) { _state.update { it.copy(chatVisible = open, unreadChat = if (open) 0 else it.unreadChat) } }
    fun sendChat(raw: String) {
        val content = ChatRules.clean(raw)
        val s = _state.value
        if (content.isBlank() || s.role == SOLO || s.page != Page.PLAY || s.snapshot == null) return
        val current = generation.get()
        viewModelScope.launch(workDispatcher) {
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
    @Synchronized private fun nextPuzzle(level: String, token: Long): Puzzle {
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
        synchronized(localStartLock) {
            if (generation.get() == token && !backupLocked) {
                check(preferences.edit().putString("recentPuzzleIds", updated).commit()) {
                    "Could not save your puzzle history"
                }
            }
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
                _state.update { it.copy(profile = updated, soloElapsedMs = elapsed, soloClockStartedAt = null) }
            } catch (_: Exception) {
                _state.update { it.copy(saveIssue = "The latest solo time could not be saved. Check phone storage.") }
            }
        }
        soloStartedAt = null
        _state.update { it.copy(soloClockStartedAt = null) }
    }

    private fun startSoloClock(progress: SoloProgress) {
        soloElapsedAnchor = progress.elapsedMs
        soloStartedAt = if (progress.completed || !visible) null else SystemClock.elapsedRealtime()
        _state.update { it.copy(soloElapsedMs = progress.elapsedMs, soloClockStartedAt = soloStartedAt) }
    }

    fun solo(resume: Boolean) {
        if (_state.value.busy) return
        val difficulty = _state.value.selectedDifficulty
        stopSoloClock()
        val current = closeRoom()
        _state.update { it.copy(page = Page.PLAY, role = SOLO, dailyDate = null, finishedSolo = null, snapshot = null, busy = true, selected = -1, pendingId = null, partnerCell = -1, message = null, hint = null, chat = emptyList(), unreadChat = 0, chatVisible = false) }
        viewModelScope.launch(workDispatcher) {
            try {
                if (generation.get() != current) return@launch
                val existing = localPlayer.current().activeSolo
                val prepared = if (resume && existing != null) existing
                    else PersonalPlay.newSolo(nextPuzzle(difficulty, current), System.currentTimeMillis())
                synchronized(localStartLock) {
                    if (generation.get() != current) return@launch
                    val saved = localPlayer.update { data ->
                        if (resume && data.activeSolo != null) data else data.copy(activeSolo = prepared)
                    }
                    val progress = saved.activeSolo!!
                    _state.update { it.copy(page = Page.PLAY, role = SOLO, snapshot = progress.snapshot(),
                        profile = saved, resumeSolo = true, status = RoomStatus(), connected = true,
                        busy = false, soloElapsedMs = progress.elapsedMs, finishedSolo = progress.takeIf { p -> p.completed }) }
                    startSoloClock(progress)
                }
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
            snapshot = null, finishedSolo = null, selected = -1, pendingId = null, notes = false, partnerCell = -1,
            message = null, hint = null, chat = emptyList(), unreadChat = 0, chatVisible = false) }
        viewModelScope.launch(workDispatcher) {
            try {
                if (generation.get() != current) return@launch
                val prepared = if (DailyChallenge.find(localPlayer.current(), date) == null) DailyChallenge.puzzle(date) else null
                synchronized(localStartLock) {
                    if (generation.get() != current) return@launch
                    val saved = localPlayer.update { data ->
                        if (date != today && data.dailyProgress.none { it.date == date })
                            error("That older daily puzzle is no longer saved.")
                        DailyChallenge.open(data, date, System.currentTimeMillis()) {
                            checkNotNull(prepared) { "That daily save changed. Open it again." }
                        }
                    }
                    val progress = DailyChallenge.find(saved, date)!!
                    _state.update { it.copy(page = Page.PLAY, role = SOLO, dailyDate = date,
                        snapshot = progress.snapshot(), profile = saved, status = RoomStatus(),
                        connected = true, busy = false, soloElapsedMs = progress.elapsedMs, finishedSolo = progress.takeIf { p -> p.completed }) }
                    startSoloClock(progress)
                }
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
        if (!::localPlayer.isInitialized || _state.value.busy) return
        stopSoloClock()
        closeRoom()
        val saved = localPlayer.current()
        _state.update { it.copy(page = Page.PROFILE, profile = saved, resumeSolo = saved.activeSolo != null,
            snapshot = null, pendingId = null, busy = false, role = SOLO, message = null,
            saveIssue = localPlayer.recoveryWarning ?: it.saveIssue) }
    }

    private fun closeRoom(): Long {
        val current = synchronized(localStartLock) {
            generation.incrementAndGet().also {
                _state.update { ui -> ui.copy(hint = null, hintUnavailable = false, entryCheck = null) }
            }
        }
        guest?.close(); guest = null; server?.close(); server = null; discovery.stop()
        return current
    }
    fun home() {
        if (!::localPlayer.isInitialized || backupLocked) return
        stopSoloClock()
        closeRoom()
        val saved = localPlayer.current()
        _state.update { GameUiState(name = it.name, resumeHost = it.resumeHost,
            resumeGuest = guestResume != null, saveIssue = localPlayer.recoveryWarning ?: it.saveIssue, profile = saved, localSaveBlocked = !localPlayer.canSave,
            resumeSolo = saved.activeSolo != null, resumeRace = it.resumeRace, appearance = it.appearance,
            uniformNumbers = it.uniformNumbers, touchFeedback = it.touchFeedback,
            autoCleanNotes = it.autoCleanNotes, selectedDifficulty = it.selectedDifficulty, requireRoomCode = it.requireRoomCode,
            todayDate = DailyChallenge.today()) }
    }
    fun host(resume: Boolean) = hostInternal(resume, versus = false)

    fun hostRace(resume: Boolean) = hostInternal(resume, versus = true)

    private fun recordMultiplayer(snapshot: Snapshot, role: String) {
        if (!snapshot.completed || backupLocked) return
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
        val requireCode = _state.value.requireRoomCode
        _state.update { it.copy(page = Page.PLAY, role = HOST, snapshot = null, busy = true, pendingId = null, selected = -1, partnerCell = -1, message = null) }
        viewModelScope.launch(workDispatcher) {
            try {
                val session = if (resume) {
                    val saved = when (val loaded = store.load()) { is LoadResult.Loaded -> loaded.data; is LoadResult.Recovered -> loaded.data; else -> error("The saved puzzle is unavailable. It has not been replaced.") }
                    GameSession(saved, store::save).also { it.setAppearance(HOST, selectedStyle) }
                } else {
                    val puzzle = nextPuzzle(_state.value.selectedDifficulty, current)
                    synchronized(localStartLock) {
                        if (generation.get() != current) return@launch
                        GameSession.create(puzzle, name, store::save, if (versus) VERSUS else COOP, selectedStyle, requireCode)
                    }
                }
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
                _state.update { it.copy(busy = false, roomCode = if (session.requiresCode) hosted.code else "", address = address, resumeHost = if (versus) it.resumeHost else true, resumeRace = if (versus) true else it.resumeRace, saveIssue = null) }
                discovery.startHosting(session.snapshot().hostName, port, session.snapshot().sessionId, session.requiresCode)
            } catch (e: Exception) { if (generation.get() == current) _state.update { it.copy(page = Page.HOME, busy = false, message = e.message ?: "Couldn't open the saved puzzle.") } }
        }
    }
    fun showJoin(resume: Boolean = false) {
        if (_state.value.busy) return
        stopSoloClock()
        val current = closeRoom()
        _state.update { it.copy(page = Page.JOIN, role = GUEST, snapshot = null, busy = false, rooms = emptyList(), pendingId = null,
            resumingGuest = resume && guestResume != null, address = if (resume) preferences.getString("lastAddress", "") ?: "" else "", message = null) }
        val expectedSession = if (resume) guestResume?.snapshot?.sessionId else null
        discovery.discover { found ->
            val rooms = if (expectedSession == null) found else found.filter { it.sessionId == expectedSession }
            _state.update { if (generation.get() == current && it.page == Page.JOIN) it.copy(rooms = rooms) else it }
        }
    }
    fun join(endpoint: Endpoint, code: String) {
        val resume = _state.value.resumingGuest
        val identity = if (resume) guestResume?.guest else null
        val expectedSession = if (resume) guestResume?.snapshot?.sessionId else null
        val current = closeRoom(); val name = cleanName(_state.value.name, "Partner")
        _state.update { it.copy(busy = true, role = GUEST, connected = false, pendingId = null, message = null, selected = -1, partnerCell = -1, chat = emptyList(), unreadChat = 0, chatVisible = false) }
        viewModelScope.launch(workDispatcher) {
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
        _state.update { it.copy(selected = cell, hint = null, hintUnavailable = false, entryCheck = null) }
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
        val current = generation.get(); _state.update { it.copy(pendingId = operation.id, message = null,
            hint = null, hintUnavailable = false, entryCheck = null) }
        if (value.role == SOLO) {
            val elapsed = elapsedSolo()
            viewModelScope.launch(workDispatcher) {
                try {
                    var acceptedProgress: SoloProgress? = null
                    val saved = localPlayer.update { data ->
                        val changed = PersonalPlay.applyEdit(data, value.dailyDate, operation, elapsed,
                            System.currentTimeMillis(), value.autoCleanNotes)
                        acceptedProgress = changed.progress
                        changed.data
                    }
                    if (generation.get() == current) {
                        val progress = acceptedProgress ?: error("The saved puzzle changed before this move.")
                        if (progress.completed) {
                            soloStartedAt = null
                        } else {
                            soloElapsedAnchor = progress.elapsedMs
                            soloStartedAt = if (visible) SystemClock.elapsedRealtime() else null
                        }
                        _state.update { it.copy(snapshot = progress.snapshot(), profile = saved,
                            soloElapsedMs = progress.elapsedMs, soloClockStartedAt = soloStartedAt, pendingId = null, finishedSolo = progress.takeIf { p -> p.completed }) }
                    }
                } catch (_: Exception) {
                    if (generation.get() == current) _state.update { it.copy(pendingId = null,
                        message = "That move could not be saved. Check available phone storage.") }
                }
            }
            return
        }
        viewModelScope.launch(workDispatcher) {
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
        viewModelScope.launch(workDispatcher) { server?.setForeground(active); guest?.setForeground(active) }
    }
    fun replacePartner() { viewModelScope.launch(workDispatcher) {
        try { server?.replaceGuest() } catch (_: Exception) { _state.update { it.copy(message = "Couldn't save the partner change. Try again.") } }
    } }
    fun newPuzzle() {
        val before = _state.value
        if (before.busy) return
        if (before.role == SOLO) {
            if (before.dailyDate != null) home() else solo(false)
            return
        }
        val token = generation.get()
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch(workDispatcher) {
            try {
                val host = server ?: return@launch
                val difficulty = host.session.snapshot().puzzle.difficulty
                    ?.takeIf { it in listOf("EASY", "MEDIUM", "HARD") }
                    ?: before.selectedDifficulty
                val puzzle = nextPuzzle(difficulty, token)
                if (generation.get() == token) host.newPuzzle(puzzle)
            } catch (_: Exception) {
                if (generation.get() == token) {
                    _state.update { it.copy(message = "Couldn't save a new puzzle. Your current puzzle is still here.") }
                }
            } finally {
                if (generation.get() == token) _state.update { it.copy(busy = false) }
            }
        }
    }
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
