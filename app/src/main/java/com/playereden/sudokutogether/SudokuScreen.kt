package com.playereden.sudokutogether

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playereden.sudokutogether.core.SOLO
import com.playereden.sudokutogether.core.VERSUS

data class GameActions(
    val name: (String) -> Unit, val host: (Boolean) -> Unit, val showJoin: (Boolean) -> Unit,
    val home: () -> Unit, val join: (Endpoint, String) -> Unit, val select: (Int) -> Unit,
    val number: (Int) -> Unit, val toggleNotes: () -> Unit, val erase: () -> Unit,
    val undo: () -> Unit, val replacePartner: () -> Unit, val newPuzzle: () -> Unit,
    val solo: (Boolean) -> Unit, val profile: () -> Unit, val hostRace: (Boolean) -> Unit,
    val sendChat: (String) -> Unit, val chatVisible: (Boolean) -> Unit, val cosmetic: (String, String) -> Unit,
    val uniformNumbers: (Boolean) -> Unit,
    val touchFeedback: (Boolean) -> Unit,
    val difficulty: (String) -> Unit,
    val hint: () -> Unit,
    val dismissHint: () -> Unit,
    val autoCleanNotes: (Boolean) -> Unit,
    val daily: () -> Unit,
    val openDaily: (String) -> Unit,
    val checkWork: () -> Unit = {},
    val requireRoomCode: (Boolean) -> Unit = {},
    val rematch: (String) -> Unit = {}
)

@Composable fun SudokuApp(
    state: GameUiState, actions: GameActions, share: (() -> Unit)?,
    settingsLocked: Boolean = false, settingsExtra: @Composable () -> Unit = {}
) {
    var confirmation by remember { mutableStateOf<String?>(null) }
    var helpOpen by rememberSaveable { mutableStateOf(false) }
    var journalTab by rememberSaveable { mutableStateOf(JournalTab.RECORDS) }
    var profileSection by rememberSaveable { mutableStateOf(ProfileSection.OVERVIEW) }
    fun journal(tab: JournalTab, section: ProfileSection = ProfileSection.OVERVIEW) {
        journalTab = tab
        profileSection = section
        if (state.page != Page.PROFILE) actions.profile()
    }
    val routedActions = actions.copy(profile = {
        journal(JournalTab.RECORDS, if (state.dailyDate != null) ProfileSection.HISTORY else ProfileSection.STATISTICS)
    })
    BackHandler(state.page != Page.HOME) {
        when {
            state.page == Page.PROFILE && journalTab == JournalTab.SETTINGS && settingsLocked -> Unit
            state.page == Page.PROFILE && journalTab == JournalTab.RECORDS && profileSection != ProfileSection.OVERVIEW -> {
                profileSection = ProfileSection.OVERVIEW
            }
            state.page == Page.PLAY && state.role != SOLO -> confirmation = "leave"
            else -> actions.home()
        }
    }
    Surface(Modifier.fillMaxSize(), color = Cream) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (state.page) {
                    Page.HOME -> HomeScreen(state, actions, { if (state.resumeHost || state.saveIssue != null) confirmation = "fresh" else actions.host(false) },
                        { if (state.resumeSolo && state.profile.activeSolo?.completed == false) confirmation = "freshSolo" else actions.solo(false) },
                        { if (state.resumeRace) confirmation = "freshRace" else actions.hostRace(false) }, { helpOpen = true }, { journal(JournalTab.RECORDS) })
                    Page.JOIN -> JoinScreen(state, actions)
                    Page.PLAY -> PlayScreen(state, routedActions, { if (state.role == SOLO) actions.home() else confirmation = "leave" }, { confirmation = "new" }, { confirmation = "partner" }, { helpOpen = true })
                    Page.PROFILE -> JournalScreen(state, actions, journalTab, profileSection,
                        openSection = { profileSection = it }, collection = { journal(JournalTab.COLLECTION) },
                        help = { helpOpen = true }, share = share, settingsLocked = settingsLocked, settingsExtra = settingsExtra)
                }
            }
            if (state.page == Page.HOME || state.page == Page.PROFILE)
                GameNavigation(state.page, journalTab, actions.home, { journal(it) }, enabled = !state.busy && !settingsLocked)
        }
    }
    if (helpOpen) AlertDialog(
        onDismissRequest = { helpOpen = false },
        title = { Text("How to play Sudoku", color = Forest, fontFamily = FontFamily.Serif) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Fill each row, column and 3×3 box with the numbers 1 through 9. Each number appears only once in each.", color = Ink)
                Text("Tap a square, then a number. The small counter under each number shows how many of that digit are still missing. Notes lets you pencil in candidates.", color = Ink)
                Text("Erase clears a square. Undo reverses your most recent safe move.", color = Ink)
                Text("Solo Hint explains a logical next step. Check my work can identify an earlier mistake without filling in an answer. Erase entry is optional; Undo restores it.", color = Ink)
                Text("Optional Clean pencil marks is in Settings. It removes matching notes near a placed number; Undo restores them. It's off by default.", color = Ink)
                Text("Hints, solution checks or automatic note cleanup mark a finish Assisted. Unassisted best times stay separate.", color = Ink)
                Text("Daily Challenge: one offline Easy puzzle per local calendar date. It saves separately from regular Solo and you can return throughout the day.", color = Ink)
                Text("Co-op: share one board over the same Wi-Fi. Versus: race on private boards. Assistance is Solo-only.", color = Ink)
            }
        },
        confirmButton = { TextButton(onClick = { helpOpen = false }) { Text("Got it") } }
    )
    confirmation?.let { kind ->
        val title = when (kind) { "leave" -> "Leave the room?"; "partner" -> "Replace your partner?"; else -> "Start a new puzzle?" }
        val body = when (kind) {
            "leave" -> "Confirmed progress is saved. You can resume or rejoin this puzzle later."
            "partner" -> "Your current partner will be disconnected. A partner can join again using the room code. The puzzle stays as it is."
            "freshSolo" -> "Starting again replaces your unfinished solo puzzle. Your previous completed records are kept."
            "freshRace" -> "Starting a fresh race replaces the saved head-to-head room and its unfinished match."
            else -> if (state.role == SOLO) "This replaces your current solo puzzle. Completed records are kept." else if (state.snapshot?.mode == VERSUS) "This starts another race on both phones and resets their entries." else "This replaces your saved shared puzzle, including its entries and notes."
        }
        AlertDialog(onDismissRequest = { confirmation = null }, title = { Text(title) }, text = { Text(body) },
            confirmButton = { TextButton(onClick = {
                confirmation = null
                when (kind) { "leave" -> actions.home(); "partner" -> actions.replacePartner(); "fresh" -> actions.host(false); "freshSolo" -> actions.solo(false); "freshRace" -> actions.hostRace(false); else -> actions.newPuzzle() }
            }) { Text(if (kind == "leave") "Leave room" else if (kind == "partner") "Replace partner" else "Start new") } },
            dismissButton = { TextButton(onClick = { confirmation = null }) { Text("Keep playing") } })
    }
}

internal fun difficultyName(level: String?): String = when (level) {
    "EASY" -> "Easy"
    "MEDIUM" -> "Medium"
    "HARD" -> "Hard"
    else -> "Legacy"
}

@Composable internal fun Notice(message: String) {
    Surface(color = Color(0xFFF8E8E0), shape = RoundedCornerShape(13.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            GameIcon(GameSymbol.HELP, Brick, Modifier.size(19.dp))
            Text(message, fontSize = 12.sp, lineHeight = 18.sp, color = Brick, modifier = Modifier.weight(1f))
        }
    }
}
