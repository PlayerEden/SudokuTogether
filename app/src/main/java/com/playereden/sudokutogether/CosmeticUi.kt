package com.playereden.sudokutogether

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playereden.sudokutogether.core.*

fun fontFor(id: String): FontFamily = when (id) {
    "serif" -> FontFamily.Serif
    "mono" -> FontFamily.Monospace
    else -> FontFamily.SansSerif
}

fun inkFor(id: String, darkBoard: Boolean): Color = when (id) {
    "cobalt" -> if (darkBoard) Color(0xFF97D6FF) else Color(0xFF205BAC)
    "amber" -> if (darkBoard) Color(0xFFFFD386) else Color(0xFF986018)
    "rose" -> if (darkBoard) Color(0xFFFFA8B9) else Color(0xFFB34B68)
    "violet" -> if (darkBoard) Color(0xFFD4B3FF) else Color(0xFF7644A5)
    else -> if (darkBoard) Color(0xFF8DE8BB) else Forest
}

fun styleLabel(a: Appearance): String = listOf(
    Cosmetics.inks.firstOrNull { it.id == a.ink }?.name ?: "Forest",
    Cosmetics.boards.firstOrNull { it.id == a.board }?.name ?: "Classic",
    Cosmetics.fonts.firstOrNull { it.id == a.font }?.name ?: "Clear"
).joinToString(" · ")

@Composable fun CosmeticSelector(title: String, group: String, options: List<Cosmetic>,
    chosen: String, state: GameUiState, actions: GameActions) {
    Text(title, color = Forest, fontWeight = FontWeight.SemiBold)
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { item ->
            val unlocked = Cosmetics.allowed(item.id, state.profile.records)
            OutlinedButton(onClick = { actions.cosmetic(group, item.id) }, enabled = unlocked,
                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (chosen == item.id) TealColor else Paper)) {
                Text((if (chosen == item.id) "✓ " else "") + item.name, fontSize = 12.sp)
            }
        }
    }
    options.filterNot { Cosmetics.allowed(it.id, state.profile.records) }.forEach {
        Text("Locked: " + it.name + " · " + it.requirement, color = Muted, fontSize = 11.sp)
    }
}

private val TealColor = Color(0xFFDCECE4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun RoomChatSheet(state: GameUiState, actions: GameActions, close: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    val scroll = rememberLazyListState()
    LaunchedEffect(state.chat.size) {
        if (state.chat.isNotEmpty()) scroll.animateScrollToItem(state.chat.lastIndex)
    }
    ModalBottomSheet(onDismissRequest = close, containerColor = Cream) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 16.dp).padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Room chat", fontFamily = FontFamily.Serif, color = Forest, fontSize = 23.sp)
            Text("Only players in this Wi-Fi room can chat. Messages are temporary and aren't saved to your profile.",
                color = Muted, fontSize = 12.sp)
            LazyColumn(Modifier.fillMaxWidth().heightIn(min = 180.dp, max = 310.dp), state = scroll,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.chat, key = { it.id }) { msg ->
                    val mine = msg.player == state.role
                    val author = if (msg.player == HOST) state.snapshot?.hostName ?: "Host"
                        else state.snapshot?.guestName ?: "Partner"
                    val look = if (msg.player == HOST) state.snapshot?.hostAppearance ?: Appearance()
                        else state.snapshot?.guestAppearance ?: Appearance()
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
                        Text(author, fontSize = 10.sp, color = Muted)
                        Text(msg.text, modifier = Modifier.background(if (mine) TealColor else Paper,
                            RoundedCornerShape(12.dp)).padding(12.dp), fontFamily = fontFor(look.font),
                            color = inkFor(look.ink, false), fontSize = 14.sp)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = draft, onValueChange = { draft = it.take(ChatRules.MAX_LENGTH) },
                    placeholder = { Text("Message your partner") }, maxLines = 3, modifier = Modifier.weight(1f),
                    enabled = state.role == HOST || state.connected)
                Button(onClick = { actions.sendChat(draft); draft = "" },
                    enabled = draft.isNotBlank() && (state.role == HOST || state.connected)) { Text("Send") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("Nice move!", "Need a hint?", "Good game!", "Rematch?").forEach { phrase ->
                    TextButton(onClick = { actions.sendChat(phrase) },
                        enabled = state.role == HOST || state.connected) { Text(phrase, fontSize = 11.sp) }
                }
            }
        }
    }
}
