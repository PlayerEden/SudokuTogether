package com.playereden.sudokutogether

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.playereden.sudokutogether.core.*

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
    val openDaily: (String) -> Unit
)

@Composable fun SudokuApp(state: GameUiState, actions: GameActions, share: () -> Unit) {
    var confirmation by remember { mutableStateOf<String?>(null) }
    var helpOpen by rememberSaveable { mutableStateOf(false) }
    var journalTab by rememberSaveable { mutableStateOf(JournalTab.RECORDS) }
    fun journal(tab: JournalTab) { journalTab = tab; actions.profile() }
    val routedActions = actions.copy(profile = { journal(JournalTab.RECORDS) })
    BackHandler(state.page != Page.HOME) { if (state.page == Page.PLAY && state.role != SOLO) confirmation = "leave" else actions.home() }
    Surface(Modifier.fillMaxSize(), color = Cream) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
            when (state.page) {
                Page.HOME -> HomeScreen(state, actions, share, { if (state.resumeHost || state.saveIssue != null) confirmation = "fresh" else actions.host(false) },
                    { if (state.resumeSolo && state.profile.activeSolo?.completed == false) confirmation = "freshSolo" else actions.solo(false) },
                    { if (state.resumeRace) confirmation = "freshRace" else actions.hostRace(false) }, { helpOpen = true }, ::journal)
                Page.JOIN -> JoinScreen(state, actions)
                Page.PLAY -> PlayScreen(state, routedActions, { if (state.role == SOLO) actions.home() else confirmation = "leave" }, { confirmation = "new" }, { confirmation = "partner" }, { helpOpen = true })
                Page.PROFILE -> ProfileScreen(state, actions, journalTab, { helpOpen = true })
            }
            }
            if (state.page == Page.HOME || state.page == Page.PROFILE)
                GameNavigation(state.page, journalTab, actions.home, ::journal, enabled = !state.busy)
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
                Text("Solo Hint shows a logical next step without entering a number. An incorrect entry can prevent hints.", color = Ink)
                Text("Optional Clean pencil marks is in Settings. It removes matching notes near a placed number; Undo restores them. It's off by default.", color = Ink)
                Text("Solo records distinguish assisted and unassisted puzzles. Hints or automatic note cleanup mark a finish Assisted.", color = Ink)
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

@Composable internal fun Notice(message: String) { Text(message, fontSize = 12.sp, lineHeight = 18.sp, color = Brick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) }
