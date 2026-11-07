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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

fun boardFillFor(id: String): Color = when (id) {
    "linen" -> Color(0xFFFFF6DF)
    "midnight" -> Color(0xFF152A36)
    "circuit" -> Color(0xFF102A2B)
    else -> Paper
}

fun emblemSymbol(id: String): String = when (id) {
    "leaf" -> "✿"
    "duo" -> "◈"
    "flash" -> "ϟ"
    "star" -> "★"
    "laurel" -> "♛"
    else -> "◆"
}

fun emblemName(id: String): String = Cosmetics.emblems.firstOrNull { it.id == id }?.name ?: "Gridkeeper"

fun styleLabel(a: Appearance): String = listOf(
    Cosmetics.inks.firstOrNull { it.id == a.ink }?.name ?: "Forest",
    Cosmetics.boards.firstOrNull { it.id == a.board }?.name ?: "Classic",
    Cosmetics.fonts.firstOrNull { it.id == a.font }?.name ?: "Clear"
).joinToString(" · ")

@Composable fun PlayerIdentity(appearance: Appearance, name: String, compact: Boolean = true) {
    val color = inkFor(appearance.ink, false)
    Row(Modifier.background(Paper, RoundedCornerShape(12.dp))
        .border(1.dp, Rule, RoundedCornerShape(12.dp))
        .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(if (compact) 30.dp else 44.dp)
            .background(color.copy(alpha = .12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center) {
            AnimalPortrait(appearance, Modifier.size(if (compact) 30.dp else 44.dp))
        }
        Column(Modifier.weight(1f, fill = false)) {
            Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = Ink, fontWeight = FontWeight.SemiBold, fontSize = if (compact) 12.sp else 16.sp)
            Text(identityStyleName(appearance), maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable fun CosmeticSelector(title: String, group: String, options: List<Cosmetic>,
    chosen: String, state: GameUiState, actions: GameActions) {
    SectionTitle(title)
    val scale = LocalDensity.current.fontScale
    val tileWidth = if (scale > 1.3f) 172.dp else 136.dp
    Row(Modifier.fillMaxWidth().testTag("cosmetic_$group").horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { item ->
            val unlocked = Cosmetics.allowed(item.id, state.profile.records)
            val equipped = chosen == item.id && (group != "emblem" || state.appearance.portrait.isEmpty())
            Surface(onClick = { actions.cosmetic(group, item.id) }, enabled = unlocked,
                modifier = Modifier.width(tileWidth).testTag("cosmetic_${group}_${item.id}").semantics {
                    selected = equipped
                    stateDescription = if (equipped) "Equipped" else if (unlocked) "Available" else "Locked. ${item.requirement}"
                }, shape = RoundedCornerShape(17.dp), color = if (equipped) GreenTint else Paper,
                border = BorderStroke(if (equipped) 2.dp else 1.dp, if (equipped) Forest else Rule)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    val look = when (group) {
                        "board" -> state.appearance.copy(board = item.id)
                        "ink" -> state.appearance.copy(ink = item.id)
                        "font" -> state.appearance.copy(font = item.id)
                        else -> state.appearance
                    }
                    val dark = look.board in listOf("midnight", "circuit")
                    Box(Modifier.fillMaxWidth().height(66.dp).clip(RoundedCornerShape(10.dp))
                        .background(if (group == "board") boardFillFor(item.id) else Cream), contentAlignment = Alignment.Center) {
                        when (group) {
                            "emblem" -> Text(emblemSymbol(item.id), color = inkFor(look.ink, false), fontSize = (37f / scale.coerceAtLeast(1f)).sp)
                            "font" -> Text("1 2 3", color = Forest, fontFamily = fontFor(item.id), fontSize = (27f / scale.coerceAtLeast(1f)).sp)
                            else -> Text("9", color = inkFor(look.ink, if (group == "board") dark else false),
                                fontFamily = fontFor(look.font), fontSize = (38f / scale.coerceAtLeast(1f)).sp)
                        }
                    }
                    Text(item.name, color = if (unlocked) Forest else Muted, fontSize = 13.sp,
                        lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        GameIcon(if (equipped) GameSymbol.CHECK else if (unlocked) GameSymbol.RIGHT else GameSymbol.LOCK,
                            if (unlocked) Forest else Muted, Modifier.size(14.dp))
                        Text(if (equipped) "Equipped" else if (unlocked) "Tap to equip" else item.requirement,
                            color = Muted, fontSize = 10.sp, lineHeight = 15.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** A labeled visual sample, never a generated or scored game board. */
@Composable internal fun StylePreview(look: Appearance) {
    val dark = look.board in listOf("midnight", "circuit")
    val scale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    val edge = if (dark) Color(0xFF789690) else Rule
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("STYLE PREVIEW", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
        Column(Modifier.fillMaxWidth().height(144.dp).clip(RoundedCornerShape(12.dp))
            .background(boardFillFor(look.board)).border(1.dp, edge, RoundedCornerShape(12.dp)).drawWithContent {
                drawContent()
                for (i in 1..2) {
                    drawLine(edge, Offset(size.width * i / 3, 0f), Offset(size.width * i / 3, size.height), 1.dp.toPx())
                    drawLine(edge, Offset(0f, size.height * i / 3), Offset(size.width, size.height * i / 3), 1.dp.toPx())
                }
            }) {
            for (r in 0..2) Row(Modifier.weight(1f).fillMaxWidth()) {
                for (c in 0..2) Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    val entered = (r + c) % 2 == 0
                    Text((r * 3 + c + 1).toString(), fontFamily = if (entered) fontFor(look.font) else FontFamily.SansSerif,
                        fontSize = (25f / scale).sp, fontWeight = if (entered) FontWeight.Normal else FontWeight.SemiBold,
                        color = if (entered) inkFor(look.ink, dark) else if (dark) OnForest else Ink)
                }
            }
        }
        Text(styleLabel(look), color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
    }
}

private val TealColor = Color(0xFFDCECE4)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun RoomChatSheet(state: GameUiState, actions: GameActions, close: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    val scroll = rememberLazyListState()
    LaunchedEffect(state.chat.size) { if (state.chat.isNotEmpty()) scroll.animateScrollToItem(state.chat.lastIndex) }
    ModalBottomSheet(onDismissRequest = close, containerColor = Cream,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        BoxWithConstraints(Modifier.fillMaxWidth().heightIn(max = 560.dp).imePadding()) {
            val showReplies = WindowInsets.ime.getBottom(LocalDensity.current) == 0 && maxHeight >= 360.dp
            Column(Modifier.fillMaxWidth().heightIn(max = maxHeight).padding(horizontal = 16.dp).padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Room chat", fontFamily = FontFamily.Serif, color = Forest, fontSize = 23.sp)
                Text("Messages aren't saved.", color = Muted, fontSize = 12.sp)
                LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(max = 240.dp), state = scroll,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.chat, key = { it.id }) { msg ->
                        val mine = msg.player == state.role
                        val author = if (msg.player == HOST) state.snapshot?.hostName ?: "Host" else state.snapshot?.guestName ?: "Partner"
                        val look = if (msg.player == HOST) state.snapshot?.hostAppearance ?: Appearance() else state.snapshot?.guestAppearance ?: Appearance()
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                AnimalPortrait(look, Modifier.size(24.dp))
                                Text(if (mine) "You" else author, fontSize = 12.sp, color = Muted)
                            }
                            Text(msg.text, modifier = Modifier.background(if (mine) TealColor else Paper,
                                RoundedCornerShape(12.dp)).padding(12.dp), fontFamily = fontFor(look.font),
                                color = inkFor(look.ink, false), fontSize = 14.sp)
                        }
                    }
                }
                if (showReplies) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf("Nice move!", "Need a hint?", "Good game!", "Rematch?").forEach { phrase ->
                        TextButton(onClick = { actions.sendChat(phrase) }, modifier = Modifier.heightIn(min = 48.dp),
                            enabled = state.role == HOST || state.connected) { Text(phrase, fontSize = 13.sp) }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = draft, onValueChange = { draft = it.take(ChatRules.MAX_LENGTH) },
                        placeholder = { Text("Message your partner") }, maxLines = 3,
                        modifier = Modifier.weight(1f).testTag("chat_input"), enabled = state.role == HOST || state.connected)
                    Button(onClick = { actions.sendChat(draft);draft = "" }, modifier = Modifier.heightIn(min = 48.dp),
                        enabled = draft.isNotBlank() && (state.role == HOST || state.connected)) { Text("Send") }
                }
            }
        }
    }
}
