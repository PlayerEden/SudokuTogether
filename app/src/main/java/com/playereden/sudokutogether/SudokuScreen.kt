package com.playereden.sudokutogether

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
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

val Cream = Color(0xFFF7F4EC)
val Paper = Color(0xFFFFFDF8)
val Forest = Color(0xFF234F48)
val Ink = Color(0xFF263D36)
val Muted = Color(0xFF6D7C73)
val Gold = Color(0xFFC39443)
val Rule = Color(0xFFCED4C8)
private val AmberLight = Color(0xFFF3E5BF)
private val TealLight = Color(0xFFDCECE4)
private val Brick = Color(0xFFAB4C42)

data class GameActions(
    val name: (String) -> Unit, val host: (Boolean) -> Unit, val showJoin: (Boolean) -> Unit,
    val home: () -> Unit, val join: (Endpoint, String) -> Unit, val select: (Int) -> Unit,
    val number: (Int) -> Unit, val toggleNotes: () -> Unit, val erase: () -> Unit,
    val undo: () -> Unit, val replacePartner: () -> Unit, val newPuzzle: () -> Unit
)

@Composable fun SudokuApp(state: GameUiState, actions: GameActions, share: () -> Unit) {
    var confirmation by remember { mutableStateOf<String?>(null) }
    BackHandler(state.page != Page.HOME) { if (state.page == Page.PLAY) confirmation = "leave" else actions.home() }
    Surface(Modifier.fillMaxSize(), color = Cream) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            when (state.page) {
                Page.HOME -> HomeScreen(state, actions, share) { if (state.resumeHost || state.saveIssue != null) confirmation = "fresh" else actions.host(false) }
                Page.JOIN -> JoinScreen(state, actions)
                Page.PLAY -> PlayScreen(state, actions, { confirmation = "leave" }, { confirmation = "new" }, { confirmation = "partner" })
            }
        }
    }
    confirmation?.let { kind ->
        val title = when (kind) { "leave" -> "Leave the room?"; "partner" -> "Replace your partner?"; else -> "Start a new puzzle?" }
        val body = when (kind) {
            "leave" -> "Confirmed progress is saved. You can resume or rejoin this puzzle later."
            "partner" -> "Your current partner will be disconnected. A partner can join again using the room code. The puzzle stays as it is."
            else -> "This replaces your saved shared puzzle, including its entries and notes."
        }
        AlertDialog(onDismissRequest = { confirmation = null }, title = { Text(title) }, text = { Text(body) },
            confirmButton = { TextButton(onClick = {
                confirmation = null
                when (kind) { "leave" -> actions.home(); "partner" -> actions.replacePartner(); "fresh" -> actions.host(false); else -> actions.newPuzzle() }
            }) { Text(if (kind == "leave") "Leave room" else if (kind == "partner") "Replace partner" else "Start new") } },
            dismissButton = { TextButton(onClick = { confirmation = null }) { Text("Keep playing") } })
    }
}

@Composable private fun HomeScreen(state: GameUiState, actions: GameActions, share: () -> Unit, fresh: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(14.dp))
        Text("A LITTLE THINKING, TOGETHER", fontSize = 10.sp, letterSpacing = 2.sp, color = Muted)
        Spacer(Modifier.height(22.dp))
        Canvas(Modifier.size(126.dp).clip(RoundedCornerShape(10.dp)).background(Paper).clearAndSetSemantics {}) {
            val unit = size.width / 9
            drawRect(AmberLight, Offset(unit * 2, unit * 2), Size(unit * 2, unit))
            drawRect(TealLight, Offset(unit * 5, unit * 5), Size(unit, unit * 2))
            for (i in 0..9) {
                val stroke = if (i % 3 == 0) 2.dp.toPx() else 0.5.dp.toPx()
                drawLine(if (i % 3 == 0) Forest else Rule, Offset(i * unit, 0f), Offset(i * unit, size.height), stroke)
                drawLine(if (i % 3 == 0) Forest else Rule, Offset(0f, i * unit), Offset(size.width, i * unit), stroke)
            }
            drawCircle(Gold, unit * .23f, Offset(unit * 2.5f, unit * 2.5f))
            drawCircle(Forest, unit * .23f, Offset(unit * 5.5f, unit * 5.5f))
        }
        Spacer(Modifier.height(20.dp))
        Text("Sudoku Together", fontFamily = FontFamily.Serif, fontSize = 34.sp, color = Forest, textAlign = TextAlign.Center)
        Text("One grid. Two minds.", color = Muted, fontSize = 16.sp, modifier = Modifier.padding(top = 6.dp, bottom = 24.dp))
        Column(Modifier.widthIn(max = 440.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = state.name, onValueChange = actions.name, label = { Text("Your name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
            Spacer(Modifier.height(2.dp))
            if (state.resumeHost) Button(onClick = { actions.host(true) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(14.dp)) { Text("Resume your puzzle") }
            Button(onClick = fresh, enabled = !state.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(14.dp)) { Text("Host a puzzle") }
            OutlinedButton(onClick = { actions.showJoin(false) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(14.dp)) { Text("Join a puzzle") }
            if (state.resumeGuest) TextButton(onClick = { actions.showJoin(true) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Rejoin last puzzle") }
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            (state.message ?: state.saveIssue)?.let { Notice(it) }
        }
        Spacer(Modifier.height(22.dp))
        Text("Your own phones. The same home Wi-Fi.\nNo account or internet needed to play.", textAlign = TextAlign.Center, color = Muted, fontSize = 13.sp, lineHeight = 20.sp)
        TextButton(onClick = share) { Text("Share APK") }
        Text("LOCAL CO-OP  ·  ${BuildConfig.VERSION_NAME}", fontSize = 10.sp, letterSpacing = 1.5.sp, color = Muted)
    }
}

@Composable private fun JoinScreen(state: GameUiState, actions: GameActions) {
    var address by remember(state.address) { mutableStateOf(state.address) }
    var code by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = actions.home, contentPadding = PaddingValues(0.dp)) { Text("Back home") }
        Text(if (state.resumingGuest) "Back to your puzzle" else "Find your partner", fontFamily = FontFamily.Serif, fontSize = 30.sp, color = Forest)
        Text("Open the host's game and connect both phones to the same Wi-Fi.", color = Muted, fontSize = 14.sp)
        Text("NEARBY ROOMS", fontSize = 10.sp, letterSpacing = 1.5.sp, color = Muted, modifier = Modifier.padding(top = 12.dp))
        if (state.rooms.isEmpty()) Text("Looking for a puzzle nearby…\nYou can also enter its address below.", color = Muted, fontSize = 14.sp, lineHeight = 22.sp)
        for (room in state.rooms) {
            val formatted = if (room.host.contains(':')) "[${room.host}]:${room.port}" else "${room.host}:${room.port}"
            OutlinedCard(onClick = { address = formatted }, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors(containerColor = if (address == formatted) TealLight else Paper)) {
                Column(Modifier.padding(14.dp)) { Text(room.name, fontWeight = FontWeight.SemiBold, color = Forest); Text("Tap to select", color = Muted, fontSize = 12.sp) }
            }
        }
        OutlinedTextField(address, { address = it }, label = { Text("Host address, including port") }, placeholder = { Text("192.168.1.24:41421") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri), shape = RoundedCornerShape(14.dp))
        OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) }, label = { Text(if (state.resumingGuest) "Room code (optional when rejoining)" else "Six-digit room code") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), shape = RoundedCornerShape(14.dp))
        Button(onClick = { parseAddress(address)?.let { actions.join(it, code) } }, enabled = !state.busy && parseAddress(address) != null && (state.resumingGuest || code.length == 6), modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(14.dp)) { Text(if (state.busy) "Connecting…" else "Join room") }
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.message?.let { Notice(it) }
        if (state.resumingGuest) TextButton(onClick = { actions.showJoin(false) }, enabled = !state.busy) { Text("Join with a new code instead") }
        Text("A room code works on your local Wi-Fi. If a room is unreachable, check that the network allows devices to connect to each other.", fontSize = 12.sp, lineHeight = 19.sp, color = Muted)
    }
}

@Composable private fun PlayScreen(state: GameUiState, actions: GameActions, leave: () -> Unit, new: () -> Unit, replace: () -> Unit) {
    var details by remember { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val boardSize = minOf((maxWidth - 32.dp).coerceAtLeast(180.dp), (maxHeight - 300.dp).coerceAtLeast(220.dp), 440.dp)
        val keypadHeight = maxOf(48.dp, (40 * LocalDensity.current.fontScale).dp)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Sudoku Together", fontFamily = FontFamily.Serif, fontSize = 23.sp, color = Forest)
                    SelectionContainer { Text(if (state.role == HOST) "Room code  ${state.roomCode}" else "STANDARD  ·  ONE SHARED GRID", fontSize = if (state.role == HOST) 12.sp else 9.sp, letterSpacing = 1.sp, color = if (state.role == HOST) Forest else Muted) }
                }
                TextButton(onClick = leave) { Text("Home") }
            }
            val snapshot = state.snapshot
            if (snapshot == null) {
                CircularProgressIndicator(Modifier.padding(48.dp)); Text("Opening your puzzle…", color = Muted)
            } else {
                val partnerName = if (state.role == HOST) snapshot.guestName else snapshot.hostName
                val statusText = when {
                    snapshot.completed -> "Solved together. Nice work!"
                    state.role == GUEST && !state.connected -> "Connection paused. Rejoin to continue."
                    state.status.paused -> "Paused while your partner is away"
                    partnerName == null -> "Waiting for your partner to join"
                    else -> "Solving with $partnerName"
                }
                Row(Modifier.fillMaxWidth().background(if (state.status.paused) AmberLight else TealLight, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).background(if (state.status.paused) Gold else Forest, RoundedCornerShape(4.dp)))
                    Spacer(Modifier.width(8.dp))
                    Text(statusText, Modifier.weight(1f), fontSize = 12.sp, color = Forest, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (state.role == GUEST && !state.connected) TextButton(onClick = { actions.showJoin(true) }, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("Reconnect", fontSize = 11.sp) }
                }
                Row(Modifier.width(boardSize), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("● You", color = Gold, fontSize = 10.sp)
                    Text(if (state.partnerCell >= 0) "◇ Partner: R${state.partnerCell / 9 + 1} C${state.partnerCell % 9 + 1}" else "◇ Partner", color = Forest, fontSize = 10.sp)
                }
                SudokuBoard(snapshot, state.selected, state.partnerCell, boardSize, actions.select)
                val editableCell = state.selected in 0..80 && snapshot.puzzle.clues[state.selected] == 0
                Row(Modifier.widthIn(max = 440.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = actions.toggleNotes, enabled = state.canEdit, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (state.notes) AmberLight else Paper), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(6.dp)) { Text("Notes", fontSize = 13.sp) }
                    OutlinedButton(onClick = actions.erase, enabled = state.canEdit && editableCell, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(6.dp)) { Text("Erase", fontSize = 13.sp) }
                    OutlinedButton(onClick = actions.undo, enabled = state.canEdit && snapshot.undoTargets.containsKey(state.role), modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(6.dp)) { Text("Undo", fontSize = 13.sp) }
                }
                for (row in listOf((1..5).toList(), (6..9).toList())) {
                    Row(Modifier.widthIn(max = 440.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (number in row) OutlinedButton(onClick = { actions.number(number) }, enabled = state.canEdit && editableCell, modifier = Modifier.weight(1f).height(keypadHeight).testTag("number_$number"), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.outlinedButtonColors(containerColor = Paper)) { Text(number.toString(), fontSize = 23.sp, fontWeight = FontWeight.Medium) }
                        if (row.size == 4) Box(Modifier.weight(1f).height(keypadHeight), contentAlignment = Alignment.Center) { Text("${snapshot.cells.count { it.value != 0 }}/81", fontSize = 11.sp, color = Muted) }
                    }
                }
                state.message?.let { Notice(it) }
                if (state.role == HOST) {
                    Row(Modifier.widthIn(max = 440.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Saved on this phone", Modifier.weight(1f), color = Muted, fontSize = 11.sp)
                        TextButton(onClick = { details = !details }) { Text(if (details) "Less" else "Room details", fontSize = 11.sp) }
                    }
                    if (details) {
                        SelectionContainer { Text("Join address\n${state.address}", color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center) }
                        Text("Keep both apps open while you play.", color = Muted, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = new, enabled = !state.busy) { Text("New puzzle") }
                            if (snapshot.guestName != null) TextButton(onClick = replace, enabled = !state.busy) { Text("Replace partner") }
                        }
                    }
                } else Text("Every number and pencil mark is shared.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable private fun SudokuBoard(snapshot: Snapshot, selected: Int, partner: Int, size: Dp, select: (Int) -> Unit) {
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
    Column(Modifier.size(size).clip(RoundedCornerShape(3.dp)).background(Paper).drawWithContent {
        drawContent()
        val step = this.size.width / 9
        for (i in 0..9) {
            val stroke = if (i % 3 == 0) 1.8.dp.toPx() else .5.dp.toPx()
            val color = if (i % 3 == 0) Forest else Rule
            drawLine(color, Offset(i * step, 0f), Offset(i * step, this.size.height), stroke)
            drawLine(color, Offset(0f, i * step), Offset(this.size.width, i * step), stroke)
        }
    }) {
        for (row in 0..8) Row(Modifier.weight(1f).fillMaxWidth()) {
            for (col in 0..8) {
                val i = row * 9 + col; val cell = snapshot.cells[i]; val given = snapshot.puzzle.clues[i] != 0
                val noteNumbers = (1..9).filter { cell.notes and (1 shl (it - 1)) != 0 }
                val content = "Row ${row + 1} column ${col + 1}, " + when { given -> "clue ${cell.value}"; cell.value != 0 -> "value ${cell.value}"; noteNumbers.isNotEmpty() -> "empty, notes ${noteNumbers.joinToString(" ")}"; else -> "empty" }
                val background = when { i == selected -> AmberLight; i == partner -> TealLight; i in conflicts -> Color(0xFFF8E7DF); selectedValue != 0 && selectedValue == cell.value -> Color(0xFFF5EEDB); else -> Paper }
                Box(Modifier.weight(1f).fillMaxHeight().background(background).then(if (i == partner) Modifier.border(2.dp, Forest) else Modifier)
                    .clickable(role = Role.Button) { select(i) }.testTag("cell_$i").semantics(mergeDescendants = true) { contentDescription = content }, contentAlignment = Alignment.Center) {
                    if (cell.value != 0) Text(cell.value.toString(), style = digitStyle, maxLines = 1, softWrap = false, fontWeight = if (given) FontWeight.SemiBold else FontWeight.Normal, color = if (i in conflicts) Brick else if (given) Ink else Forest)
                    else if (cell.notes != 0) Column(Modifier.fillMaxSize().padding(1.dp)) {
                        for (r in 0..2) Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0..2) {
                                val n = r * 3 + c + 1
                                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    if (n in noteNumbers) Text(n.toString(), color = Muted, style = noteStyle, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun Notice(message: String) { Text(message, fontSize = 12.sp, lineHeight = 18.sp, color = Brick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) }
