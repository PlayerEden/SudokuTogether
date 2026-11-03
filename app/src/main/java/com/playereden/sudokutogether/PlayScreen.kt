package com.playereden.sudokutogether

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.playereden.sudokutogether.core.*
import kotlinx.coroutines.delay

@Composable internal fun PlayScreen(state: GameUiState, actions: GameActions, leave: () -> Unit,
    new: () -> Unit, replace: () -> Unit, help: () -> Unit) {
    var details by rememberSaveable { mutableStateOf(false) }
    var chatOpen by rememberSaveable { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    fun tick() { if (state.touchFeedback) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
    fun copyCode() { clipboard.setText(AnnotatedString(state.roomCode)); copied = true; tick() }
    LaunchedEffect(copied) { if (copied) { delay(1800); copied = false } }
    Column(Modifier.fillMaxSize()) {
        PlayTopBar(state, leave, help, ::copyCode, copied) { chatOpen = true; actions.chatVisible(true) }
        val snapshot = state.snapshot
        if (snapshot == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(); Text("Opening your puzzle…", color = Muted)
                }
            }
        } else BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val wide = maxWidth >= 600.dp && maxWidth > maxHeight
            if (wide) {
                val railWidth = (maxWidth * .46f).coerceIn(288.dp, 352.dp)
                Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PlayBoardPane(state, actions, new, replace, details, { details = !details },
                        Modifier.weight(1f).fillMaxHeight(), roomStatus = false)
                    Column(Modifier.width(railWidth).fillMaxHeight().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (state.role != SOLO) RoomStatusCard(state, actions)
                        if (!snapshot.completed) SudokuInputDock(snapshot, state, actions, ::tick)
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    PlayBoardPane(state, actions, new, replace, details, { details = !details },
                        Modifier.weight(1f).fillMaxWidth(), roomStatus = true)
                    if (!snapshot.completed) SudokuInputDock(snapshot, state, actions, ::tick)
                }
            }
        }
    }
    HintAndChat(state, actions, chatOpen) { chatOpen = false; actions.chatVisible(false) }
}

@Composable private fun PlayTopBar(state: GameUiState, home: () -> Unit, help: () -> Unit, copy: () -> Unit, copied: Boolean, chat: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TextButton(home, modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 6.dp)) {
            GameIcon(GameSymbol.BACK, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Home", fontSize = 12.sp)
        }
        Column(Modifier.weight(1f)) {
            Text(when { state.dailyDate != null -> "Daily"; state.role == SOLO -> "Solo"; state.snapshot?.mode == VERSUS -> "Versus"; else -> "Co-op" },
                color = Forest, fontSize = 22.sp, lineHeight = 25.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
            Text(difficultyName(state.snapshot?.puzzle?.difficulty) + when {
                state.dailyDate != null -> " · ${state.dailyDate}"
                state.role == HOST && state.roomCode.isNotBlank() -> " · Room ${state.roomCode}"
                else -> " · Classic 9×9"
            }, color = Muted, fontSize = 10.sp, lineHeight = 14.sp,
                modifier = if (state.role == HOST) Modifier.semantics { contentDescription = "Room code ${state.roomCode}" } else Modifier)
        }
        if (state.role == HOST && state.roomCode.isNotBlank()) TextButton(copy, contentPadding = PaddingValues(horizontal = 4.dp)) {
            Text(if (copied) "Copied" else "Copy code", fontSize = 10.sp)
        }
        if (state.role != SOLO) TextButton(chat, contentPadding = PaddingValues(horizontal = 5.dp)) {
            GameIcon(GameSymbol.CHAT, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp))
            Text(if (state.unreadChat > 0) "Chat (${state.unreadChat})" else "Chat", fontSize = 12.sp)
        }
        IconButton(help, modifier = Modifier.size(48.dp).semantics { contentDescription = "How to play Sudoku" }) {
            GameIcon(GameSymbol.HELP, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable private fun PlayBoardPane(state: GameUiState, actions: GameActions, new: () -> Unit,
    replace: () -> Unit, details: Boolean, toggleDetails: () -> Unit,
    modifier: Modifier, roomStatus: Boolean) {
    val snapshot = state.snapshot ?: return
    val density = LocalDensity.current
    var metaPixels by remember { mutableIntStateOf(0) }
    var captionPixels by remember { mutableIntStateOf(0) }
    BoxWithConstraints(modifier) {
        // Measure the actual surrounding text, including system font scale.
        // Short-height fallbacks retain a scrollable board instead of clipping it behind controls.
        val overhead = with(density) { (metaPixels + captionPixels).toDp() } + 36.dp
        val boardSize = minOf(maxWidth - 40.dp, maxHeight - overhead, 440.dp).coerceAtLeast(144.dp)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (snapshot.completed) PuzzleFinishCard(state, actions, new)
            Column(Modifier.fillMaxWidth().onSizeChanged { metaPixels = it.height },
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (roomStatus && state.role != SOLO) Text(roomStatusText(state), color = Forest, fontSize = 11.sp, lineHeight = 16.sp)
                PuzzleMeta(state)
            }
            Surface(shape = RoundedCornerShape(16.dp), color = boardFillFor(state.appearance.board),
                shadowElevation = 3.dp, border = BorderStroke(1.dp, Rule), modifier = Modifier.testTag("board_frame")) {
                Box(Modifier.padding(8.dp)) {
                    val haptics = LocalHapticFeedback.current
                    SudokuBoard(snapshot, state.selected, if (state.role == SOLO || snapshot.mode == VERSUS) -1 else state.partnerCell,
                        boardSize, { if (state.touchFeedback) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); actions.select(it) },
                        state.appearance, state.role == SOLO, state.uniformNumbers)
                }
            }
            Text(when {
                snapshot.completed -> "Your finished grid"
                state.selected !in 0..80 -> "Tap a square, then choose a number."
                snapshot.puzzle.clues[state.selected] != 0 -> "R${state.selected / 9 + 1} · C${state.selected % 9 + 1} · Starting clue"
                state.notes -> "R${state.selected / 9 + 1} · C${state.selected % 9 + 1} · Pencil marks"
                else -> "R${state.selected / 9 + 1} · C${state.selected % 9 + 1} · Enter a number"
            }, modifier = Modifier.onSizeChanged { captionPixels = it.height }, color = Muted, fontSize = 11.sp,
                lineHeight = 16.sp, textAlign = TextAlign.Center)
            if (state.role != SOLO) PartnerStrip(state)
            if (snapshot.mode == COOP && snapshot.completed && snapshot.guestName != null) {
                GamePanel(Modifier.widthIn(max = 460.dp).fillMaxWidth()) {
                    SectionTitle("A board solved together")
                    Text("Final entries: ${snapshot.hostName} ${snapshot.cells.count { it.owner == HOST && it.value in 1..9 }} · ${snapshot.guestName} ${snapshot.cells.count { it.owner == GUEST && it.value in 1..9 }}", color = Ink, fontSize = 12.sp)
                    Text("A shared contribution, not a skill ranking.", color = Muted, fontSize = 11.sp)
                }
            }
            state.message?.let { Notice(it) }
            if (roomStatus && state.role == GUEST && !state.connected)
                GamePrimaryButton("Reconnect", { actions.showJoin(true) }, !state.busy)
            if (state.role == SOLO) {
                Row(Modifier.widthIn(max = 460.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Saved on this device", Modifier.weight(1f), color = Muted, fontSize = 10.sp)
                    if (!snapshot.completed && state.dailyDate == null) TextButton(new) { Text("New solo puzzle", fontSize = 11.sp) }
                }
            } else if (state.role == HOST) {
                TextButton(toggleDetails) { Text(if (details) "Less" else "Room details", fontSize = 12.sp) }
                if (details) GamePanel(Modifier.widthIn(max = 460.dp).fillMaxWidth()) {
                    SelectionContainer { Text("Join address\n${state.address}", color = Forest, fontSize = 13.sp) }
                    Text("Keep both apps open while you play.", color = Muted, fontSize = 12.sp)
                    TextButton(new, enabled = !state.busy) { Text(if (snapshot.mode == VERSUS) "Next race" else "New puzzle") }
                    if (snapshot.guestName != null) TextButton(replace, enabled = !state.busy) { Text("Replace partner") }
                }
            } else Text(if (snapshot.mode == VERSUS) "Your answers are private. Your opponent sees progress only."
                else "Every number and pencil mark is shared.", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable private fun PuzzleMeta(state: GameUiState) {
    val snapshot = state.snapshot ?: return
    val filled = snapshot.cells.count { it.value != 0 }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        if (state.role == SOLO) {
            if (LocalDensity.current.fontScale > 1.3f) Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("$filled/81 squares filled", color = Muted, fontSize = 11.sp)
                SoloTimer(state.soloElapsedMs, state.soloClockStartedAt)
            } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$filled/81 squares filled", color = Muted, fontSize = 11.sp)
                SoloTimer(state.soloElapsedMs, state.soloClockStartedAt)
            }
        } else if (snapshot.mode == VERSUS) {
            val given = snapshot.puzzle.clues.count { it != 0 }
            val own = if (state.role == HOST) snapshot.hostProgress else snapshot.guestProgress
            val rival = if (state.role == HOST) snapshot.guestProgress else snapshot.hostProgress
            Text("You ${(own - given).coerceAtLeast(0)} · Rival ${(rival - given).coerceAtLeast(0)} entries", color = Forest, fontSize = 12.sp)
        } else Text("You · shared grid · " + if (state.partnerCell >= 0) "Partner: R${state.partnerCell / 9 + 1} C${state.partnerCell % 9 + 1}" else "Partner", color = Forest, fontSize = 11.sp)
        LinearProgressIndicator(progress = { filled / 81f }, modifier = Modifier.fillMaxWidth().height(3.dp), color = Forest, trackColor = Rule.copy(alpha = .6f))
    }
}

private fun roomStatusText(state: GameUiState): String {
    val snapshot = state.snapshot ?: return "Opening room…"
    val partner = if (state.role == HOST) snapshot.guestName else snapshot.hostName
    return when {
        snapshot.mode == VERSUS && snapshot.completed && snapshot.winner == state.role -> "You won! The result is saved in your records."
        snapshot.mode == VERSUS && snapshot.completed -> "Your rival finished first. Match complete."
        snapshot.mode == VERSUS && !state.status.guestActive -> "Waiting for your rival to join or reconnect"
        snapshot.mode == VERSUS -> "Race in progress! First valid solution wins."
        snapshot.completed -> "Solved together. Nice work!"
        state.role == GUEST && !state.connected -> "Connection paused. Rejoin to continue."
        state.status.paused -> "Paused while your partner is away"
        partner == null -> "Waiting for your partner to join"
        else -> "Solving with $partner"
    }
}

@Composable private fun RoomStatusCard(state: GameUiState, actions: GameActions) {
    Surface(color = if (state.status.paused) GoldTint else GreenTint, shape = RoundedCornerShape(13.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(roomStatusText(state), color = Forest, fontSize = 12.sp, lineHeight = 17.sp)
            if (state.role == GUEST && !state.connected) TextButton(onClick = { actions.showJoin(true) }) { Text("Reconnect") }
        }
    }
}

@Composable private fun PartnerStrip(state: GameUiState) {
    val snapshot = state.snapshot ?: return
    val other = if (state.role == HOST) snapshot.guestAppearance else snapshot.hostAppearance
    val partner = if (state.role == HOST) snapshot.guestName else snapshot.hostName
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(emblemSymbol(state.appearance.emblem) + " " + state.name, Modifier.weight(1f),
            color = inkFor(state.appearance.ink, false), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(emblemSymbol(other.emblem) + " " + (partner ?: "Waiting for partner"), Modifier.weight(1f),
            color = inkFor(other.ink, false), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
    }
}

@Composable private fun DockTool(label: String, symbol: GameSymbol, enabled: Boolean, chosen: Boolean = false,
    modifier: Modifier, description: String? = null, click: () -> Unit) {
    OutlinedButton(click, enabled = enabled, modifier = modifier.heightIn(min = 52.dp)
        .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
        .semantics { selected = chosen }, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (chosen) 2.dp else 1.dp, if (chosen) Forest else Rule),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (chosen) GreenTint else Paper),
        contentPadding = PaddingValues(horizontal = 3.dp, vertical = 7.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            GameIcon(symbol, if (enabled) Forest else Muted.copy(alpha = .45f), Modifier.size(17.dp))
            Text(label, fontSize = 11.sp, lineHeight = 14.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable private fun PuzzleFinishCard(state: GameUiState, actions: GameActions, new: () -> Unit) {
    val snapshot = state.snapshot ?: return
    if (snapshot.completed) {
        OutlinedCard(Modifier.widthIn(max = 440.dp).fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = GoldTint)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(52.dp).background(Forest, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    GameIcon(GameSymbol.TROPHY, OnForest, Modifier.size(29.dp))
                }
                Text(when {
                    state.dailyDate != null -> "Daily challenge complete"
                    state.role == SOLO -> "Puzzle completed"
                    snapshot.mode == VERSUS && snapshot.winner == state.role -> "Victory!"
                    snapshot.mode == VERSUS -> "Race finished"
                    else -> "Solved together!"
                }, color = Forest, fontSize = 20.sp, fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold)
                if (state.role == SOLO) {
                    val finished = state.finishedSolo?.takeIf { it.id == snapshot.sessionId }
                        ?: if (state.dailyDate == null) state.profile.activeSolo
                        else state.profile.dailyProgress.firstOrNull { it.date == state.dailyDate }?.solo
                    if (finished?.id == snapshot.sessionId && finished.completed) {
                        val assisted = finished.hintsUsed > 0 || finished.usedNoteAssist
                        Text("Time  " + formatElapsed(finished.elapsedMs) + "   ·   " +
                            difficultyName(finished.puzzle.difficulty),
                            color = Forest, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${finished.moves} " + (if (finished.moves == 1) "move" else "moves") + "   ·   " +
                            (if (assisted) "Assisted" else "Unassisted"),
                            color = Ink, fontSize = 12.sp)
                        if (finished.hintsUsed > 0)
                            Text("${finished.hintsUsed} logical hint" +
                                (if (finished.hintsUsed == 1) "" else "s") + " used",
                                color = Muted, fontSize = 11.sp)
                        Text("Saved privately on this phone", color = Muted, fontSize = 11.sp)
                    }
                }
                if (state.dailyDate != null) {
                    Text(if (state.dailyDate == state.todayDate) "Come back tomorrow for a new challenge."
                        else "Find today's challenge on Home.", color = Muted, fontSize = 12.sp)
                    Button(onClick = actions.home,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
                        Text("Back home")
                    }
                } else if (state.role == HOST || state.role == SOLO) {
                    Button(onClick = new, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
                        Text(if (state.role == SOLO) "Play another solo puzzle"
                            else if (snapshot.mode == VERSUS) "Start another race"
                            else "Start another co-op puzzle")
                    }
                } else Text("Your host can start the next puzzle.", color = Muted, fontSize = 12.sp)
                if (state.role == SOLO) TextButton(onClick = actions.profile) {
                    Text("View personal records")
                }
            }
        }
    }
}

@Composable private fun HintAndChat(state: GameUiState, actions: GameActions, chatOpen: Boolean, close: () -> Unit) {
    state.hint?.let { hint ->
        AlertDialog(
            onDismissRequest = actions.dismissHint,
            title = { Text("Your next deduction", color = Forest, fontFamily = FontFamily.Serif) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Row ${hint.cell / 9 + 1}, column ${hint.cell % 9 + 1}",
                        fontWeight = FontWeight.SemiBold, color = Forest)
                    Text(hint.explanation, color = Ink)
                    Text("Suggested number: ${hint.digit}. Nothing was entered for you.",
                        color = Muted, fontSize = 12.sp)
                    Text("Hints are marked as assisted in your personal records.",
                        color = Muted, fontSize = 11.sp)
                }
            },
            confirmButton = { TextButton(onClick = actions.dismissHint) { Text("Back to puzzle") } }
        )
    }
    if (chatOpen && state.role != SOLO) RoomChatSheet(state, actions, close)
}

@Composable private fun SudokuInputDock(snapshot: Snapshot, state: GameUiState, actions: GameActions, tick: () -> Unit) {
    val keypadHeight = maxOf(48.dp, (40 * LocalDensity.current.fontScale).dp)
    val editable = state.selected in 0..80 && snapshot.puzzle.clues[state.selected] == 0
    val chosenValue = snapshot.cells.getOrNull(state.selected)?.value
    Column(Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        .border(1.dp, Rule, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        .testTag("input_dock")
        .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.widthIn(max = 440.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DockTool(if (state.notes) "Notes on" else "Notes", GameSymbol.PENCIL, state.canEdit, state.notes, Modifier.weight(1f)) { tick(); actions.toggleNotes() }
            DockTool("Erase", GameSymbol.ERASE, state.canEdit && editable, modifier = Modifier.weight(1f)) { tick(); actions.erase() }
            DockTool("Undo", GameSymbol.UNDO, state.canEdit && snapshot.undoTargets.containsKey(state.role), modifier = Modifier.weight(1f)) { tick(); actions.undo() }
            if (state.role == SOLO) DockTool("Hint", GameSymbol.HELP, state.canEdit, modifier = Modifier.weight(1f),
                description = "Request a solo Sudoku hint") { actions.hint() }
        }
        for (numbers in listOf((1..5).toList(), (6..9).toList())) {
            Row(Modifier.widthIn(max = 440.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (number in numbers) {
                    val remaining = (9 - snapshot.cells.count { it.value == number }).coerceAtLeast(0)
                    val highlighted = number == chosenValue
                    OutlinedButton(onClick = { tick(); actions.number(number) },
                        enabled = state.canEdit && editable,
                        modifier = Modifier.weight(1f).height(keypadHeight).testTag("number_$number")
                            .semantics { contentDescription = "Number $number, $remaining remaining" },
                        shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(if (highlighted) 2.dp else 1.dp, if (highlighted) Forest else Rule),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (highlighted) TealLight else Paper)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            Text(number.toString(), fontSize = 21.sp, lineHeight = 22.sp,
                                fontWeight = FontWeight.SemiBold)
                            Text(remaining.toString(), fontSize = 10.sp, lineHeight = 11.sp, color = Muted)
                        }
                    }
                }
                if (numbers.size == 4) Box(Modifier.weight(1f).height(keypadHeight),
                    contentAlignment = Alignment.Center) {
                    Text("${snapshot.cells.count { it.value != 0 }}/81", fontSize = 11.sp, color = Muted)
                }
            }
        }
    }
}

@Composable private fun SudokuBoard(snapshot: Snapshot, selected: Int, partner: Int, size: Dp, select: (Int) -> Unit, localStyle: Appearance, solo: Boolean, uniformNumbers: Boolean) {
    val systemScale = LocalDensity.current.fontScale.coerceAtLeast(.5f)
    // A nine-cell row has fixed space. Let surrounding controls scale freely,
    // while fitting grid glyphs and their line heights inside each square.
    val correction = systemScale.coerceAtMost(1.15f) / systemScale
    val digitSize = (size.value / 9 * .64f * correction).sp
    val noteSize = ((size.value / 9 - 2f) / 3 * .68f * correction).sp
    val digitStyle = TextStyle(fontSize = digitSize, lineHeight = digitSize, platformStyle = PlatformTextStyle(includeFontPadding = false))
    val noteStyle = TextStyle(fontSize = noteSize, lineHeight = noteSize, platformStyle = PlatformTextStyle(includeFontPadding = false))
    val conflicts = remember(snapshot.cells) { Sudoku.conflicts(snapshot.cells.map { it.value }) }
    val selectedValue = snapshot.cells.getOrNull(selected)?.value ?: 0
    val darkBoard = localStyle.board == "midnight" || localStyle.board == "circuit"
    val boardPaper = when (localStyle.board) {
        "linen" -> Color(0xFFFFF6DF)
        "midnight" -> Color(0xFF152A36)
        "circuit" -> Color(0xFF102A2B)
        else -> Paper
    }
    val boardRule = if (darkBoard) Color(0xFF49616B) else Rule
    val boardEdge = if (darkBoard) Color(0xFFA9C8C3) else Forest
    val neutralInk = if (darkBoard) Color(0xFFF2F2E9) else Ink
    Column(Modifier.size(size).clip(RoundedCornerShape(3.dp)).background(boardPaper).drawWithContent {
        drawContent()
        val step = this.size.width / 9
        for (i in 0..9) {
            val stroke = if (i % 3 == 0) 1.8.dp.toPx() else .5.dp.toPx()
            val color = if (i % 3 == 0) boardEdge else boardRule
            drawLine(color, Offset(i * step, 0f), Offset(i * step, this.size.height), stroke)
            drawLine(color, Offset(0f, i * step), Offset(this.size.width, i * step), stroke)
        }
    }) {
        for (row in 0..8) Row(Modifier.weight(1f).fillMaxWidth()) {
            for (col in 0..8) {
                val i = row * 9 + col; val cell = snapshot.cells[i]; val given = snapshot.puzzle.clues[i] != 0
                val noteNumbers = (1..9).filter { cell.notes and (1 shl (it - 1)) != 0 }
                val content = "Row ${row + 1} column ${col + 1}, " + when { given -> "clue ${cell.value}"; cell.value != 0 -> "value ${cell.value}"; noteNumbers.isNotEmpty() -> "empty, notes ${noteNumbers.joinToString(" ")}"; else -> "empty" }
                val authored = if (uniformNumbers && !given) Appearance() else if (solo && !given) localStyle else when (cell.owner) {
                    HOST -> snapshot.hostAppearance
                    GUEST -> snapshot.guestAppearance
                    else -> Appearance()
                }
                val authoredFont = fontFor(authored.font)
                val authoredInk = inkFor(authored.ink, darkBoard)
                val sameHouse = selected in 0..80 && (
                    row == selected / 9 || col == selected % 9 ||
                    (row / 3 == selected / 27 && col / 3 == (selected % 9) / 3)
                )
                val background = when {
                    i == selected -> if (darkBoard) Color(0xFF555336) else AmberLight
                    i == partner -> if (darkBoard) Color(0xFF305C55) else TealLight
                    i in conflicts -> if (darkBoard) Color(0xFF63362C) else Color(0xFFF8E7DF)
                    selectedValue != 0 && selectedValue == cell.value -> if (darkBoard) Color(0xFF37473B) else Color(0xFFF5EEDB)
                    sameHouse -> if (darkBoard) Color(0xFF213B3E) else Color(0xFFF0F3EB)
                    else -> boardPaper
                }
                Box(Modifier.weight(1f).fillMaxHeight().background(background).then(if (i == partner || i == selected) Modifier.border(2.dp, boardEdge) else Modifier)
                    .clickable(role = Role.Button) { select(i) }.testTag("cell_$i").semantics(mergeDescendants = true) {
                         contentDescription = content
                         this.selected = i == selected
                         stateDescription = when {
                             i == selected -> "Selected square"
                             i == partner -> "Partner's selected square"
                             i in conflicts -> "Conflicting value"
                             else -> "Puzzle square"
                         }
                     }, contentAlignment = Alignment.Center) {
                    if (cell.value != 0) Text(cell.value.toString(), style = digitStyle.copy(fontFamily = if (given) FontFamily.SansSerif else authoredFont),
                        maxLines = 1, softWrap = false, fontWeight = if (given) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (i in conflicts) (if (darkBoard) Color(0xFFFFB7A6) else Brick) else if (given) neutralInk else authoredInk)
                    else if (cell.notes != 0) Column(Modifier.fillMaxSize().padding(1.dp)) {
                        for (r in 0..2) Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0..2) {
                                val n = r * 3 + c + 1
                                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    if (n in noteNumbers) Text(n.toString(), color = if (darkBoard) authoredInk else Muted, style = noteStyle.copy(fontFamily = authoredFont), maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


