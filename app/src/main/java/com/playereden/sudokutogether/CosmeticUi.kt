package com.playereden.sudokutogether

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playereden.sudokutogether.core.*

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

private enum class CollectionCategory(val label: String) { ICONS("Player icons"), BOARDS("Boards"), NUMBERS("Numbers") }
private enum class IconFamily(val label: String) { ALL("All"), BASIC("Basic"), WOODLAND("Woodland") }

private enum class CosmeticGroup(val key: String, val label: String, val useLabel: String, val family: IconFamily? = null) {
    EMBLEM("emblem", "Basic player icon", "Use icon", IconFamily.BASIC),
    PORTRAIT("portrait", "Woodland player icon", "Use icon", IconFamily.WOODLAND),
    BOARD("board", "Board style", "Use board"),
    INK("ink", "Number color", "Use color"),
    FONT("font", "Number font", "Use font");

    fun withItem(look: Appearance, id: String): Appearance = when (this) {
        EMBLEM -> look.copy(emblem = id, portrait = "")
        PORTRAIT -> look.copy(portrait = id)
        BOARD -> look.copy(board = id)
        INK -> look.copy(ink = id)
        FONT -> look.copy(font = id)
    }
}

private data class CollectionItem(val cosmetic: Cosmetic, val group: CosmeticGroup) {
    val id: String get() = cosmetic.id
    val tag: String get() = if (group == CosmeticGroup.PORTRAIT) "portrait_$id" else "cosmetic_${group.key}_$id"
    fun isEquipped(look: Appearance): Boolean = group.withItem(look, id) == look
}

private val iconItems = Cosmetics.emblems.map { CollectionItem(it, CosmeticGroup.EMBLEM) } +
    Cosmetics.portraits.map { CollectionItem(it, CosmeticGroup.PORTRAIT) }
private val boardItems = Cosmetics.boards.map { CollectionItem(it, CosmeticGroup.BOARD) }
private val inkItems = Cosmetics.inks.map { CollectionItem(it, CosmeticGroup.INK) }
private val fontItems = Cosmetics.fonts.map { CollectionItem(it, CosmeticGroup.FONT) }
private val allCollectionItems = iconItems + boardItems + inkItems + fontItems

@Composable internal fun CollectionContent(state: GameUiState, actions: GameActions) {
    var category by rememberSaveable { mutableStateOf(CollectionCategory.ICONS) }
    var family by rememberSaveable { mutableStateOf(IconFamily.ALL) }
    var styleExpanded by rememberSaveable { mutableStateOf(false) }
    var previewId by rememberSaveable { mutableStateOf<String?>(null) }
    val records = state.profile.records
    val progress = remember(records) {
        allCollectionItems.associate { it.id to requireNotNull(Cosmetics.progress(it.id, records)) }
    }
    val look = state.appearance
    val open: (CollectionItem) -> Unit = { previewId = it.id }

    GamePanel(Modifier.fillMaxWidth(), color = GreenTint, padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            AnimalPortrait(look, Modifier.size(40.dp).clearAndSetSemantics {})
            Column(Modifier.weight(1f)) {
                Text(identityStyleName(look), style = MaterialTheme.typography.titleSmall, color = Forest)
                Text("Equipped player icon", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
        TextButton(onClick = { styleExpanded = !styleExpanded }, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(if (styleExpanded) "Hide style preview" else "Show style preview")
        }
        if (styleExpanded) StylePreview(look, state.uniformNumbers)
    }
    CollectionChoices(CollectionCategory.entries, category, { it.label }, Role.Tab) { category = it }
    when (category) {
        CollectionCategory.ICONS -> {
            CollectionChoices(IconFamily.entries, family, { it.label }, Role.RadioButton) { family = it }
            val icons = iconItems.filter { family == IconFamily.ALL || it.group.family == family }
            Text("${icons.count { progress.getValue(it.id).unlocked }} of ${icons.size} available",
                style = MaterialTheme.typography.bodySmall, color = Muted)
            CosmeticGrid(icons, progress, look, !state.busy, open)
        }
        CollectionCategory.BOARDS -> {
            SectionTitle("Board styles")
            CosmeticGrid(boardItems, progress, look, !state.busy, open)
        }
        CollectionCategory.NUMBERS -> {
            if (state.uniformNumbers && !styleExpanded) {
                Text("Classic number display is on. Your equipped font and color apply when it is off.",
                    style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            SectionTitle("Number colors")
            CosmeticGrid(inkItems, progress, look, !state.busy, open)
            SectionTitle("Number fonts")
            CosmeticGrid(fontItems, progress, look, !state.busy, open)
        }
    }
    allCollectionItems.firstOrNull { it.id == previewId }?.let { item ->
        CosmeticPreview(item, progress.getValue(item.id), state, close = { previewId = null }, use = {
            actions.cosmetic(item.group.key, item.id)
            previewId = null
        })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun <T> CollectionChoices(values: List<T>, choice: T, label: (T) -> String, role: Role, select: (T) -> Unit) {
    FlowRow(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        values.forEach { value ->
            val selected = value == choice
            val shape = RoundedCornerShape(13.dp)
            Box(Modifier.clip(shape).background(if (selected) Forest else Paper).border(1.dp, if (selected) Forest else Rule, shape)
                .selectable(selected, role = role, onClick = { select(value) })
                .heightIn(min = 48.dp).padding(horizontal = 11.dp, vertical = 7.dp), contentAlignment = Alignment.Center) {
                Text(label(value), style = MaterialTheme.typography.labelLarge, color = if (selected) OnForest else Forest)
            }
        }
    }
}

@Composable private fun CosmeticGrid(items: List<CollectionItem>, progress: Map<String, CosmeticProgress>,
    look: Appearance, enabled: Boolean, open: (CollectionItem) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val scale = LocalDensity.current.fontScale
        val columns = when {
            scale > 1.3f || maxWidth < 260.dp -> 1
            maxWidth >= 430.dp -> 3
            else -> 2
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { item ->
                        CosmeticCard(item, progress.getValue(item.id), look, enabled, columns == 1,
                            Modifier.weight(1f), open = { open(item) })
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable private fun CosmeticCard(item: CollectionItem, progress: CosmeticProgress, look: Appearance, enabled: Boolean,
    singleColumn: Boolean, modifier: Modifier, open: () -> Unit) {
    val equipped = item.isEquipped(look)
    val status = when { equipped -> "Selected"; progress.unlocked -> "Available"; else -> "Locked" }
    val family = item.group.family?.label
    Surface(onClick = open, enabled = enabled, modifier = modifier.testTag(item.tag).semantics {
        role = Role.Button
        selected = equipped
        stateDescription = if (family == null) status else "$family. $status"
    }, shape = RoundedCornerShape(15.dp), color = if (equipped) GreenTint else Paper,
        border = BorderStroke(if (equipped) 2.dp else 1.dp, if (equipped) Forest else Rule)) {
        val sample = item.group.withItem(look, item.id)
        val minHeight = if (LocalDensity.current.fontScale > 1.15f) 145.dp else 131.dp
        val label: @Composable () -> Unit = {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.cosmetic.name, color = Forest, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clearAndSetSemantics {}) {
                    if (equipped || !progress.unlocked) GameIcon(if (equipped) GameSymbol.CHECK else GameSymbol.LOCK,
                        if (equipped) Forest else Muted, Modifier.size(13.dp))
                    Text(listOfNotNull(family, status).joinToString(" · "), color = Muted, fontSize = 11.sp, lineHeight = 14.sp)
                }
            }
        }
        if (singleColumn) {
            Row(Modifier.heightIn(min = 92.dp).padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CosmeticSwatch(item.group, sample, Modifier.size(58.dp))
                Box(Modifier.weight(1f)) { label() }
            }
        } else {
            Column(Modifier.heightIn(min = minHeight).padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CosmeticSwatch(item.group, sample, Modifier.align(Alignment.CenterHorizontally).size(52.dp))
                label()
            }
        }
    }
}

@Composable private fun CosmeticSwatch(group: CosmeticGroup, look: Appearance, modifier: Modifier) {
    if (group.family != null) {
        AnimalPortrait(look, modifier.clearAndSetSemantics {})
    } else {
        val dark = group == CosmeticGroup.BOARD && look.board in listOf("midnight", "circuit")
        val color = if (group == CosmeticGroup.BOARD) boardFillFor(look.board) else Cream
        val scale = LocalDensity.current.fontScale.coerceAtLeast(1f)
        Box(modifier.clip(RoundedCornerShape(10.dp)).background(color).clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
            Text(if (group == CosmeticGroup.FONT) "12" else "9", color = inkFor(look.ink, dark),
                fontFamily = fontFor(look.font), fontSize = (26f / scale).sp)
        }
    }
}

@Composable private fun CosmeticPreview(item: CollectionItem, progress: CosmeticProgress, state: GameUiState,
    close: () -> Unit, use: () -> Unit) {
    val sample = item.group.withItem(state.appearance, item.id)
    val equipped = item.isEquipped(state.appearance)
    val canUse = progress.unlocked && !equipped
    AlertDialog(onDismissRequest = close, title = { Text(item.cosmetic.name) }, text = {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (item.group.family != null) AnimalPortrait(sample, Modifier.size(78.dp).clearAndSetSemantics {})
            Text(item.group.label, color = Muted, style = MaterialTheme.typography.bodySmall)
            Text(when { equipped -> "Selected"; progress.unlocked -> "Available to use"; else -> "Locked" },
                color = Forest, style = MaterialTheme.typography.titleSmall)
            if (!progress.unlocked) {
                Text(item.cosmetic.requirement, color = Ink)
                Text("${progress.current} of ${progress.target}" + if (item.group == CosmeticGroup.PORTRAIT) " completed" else "",
                    color = Muted, style = MaterialTheme.typography.bodySmall)
                MilestoneTrack(progress.current, progress.target)
            }
            if (item.group.family == null) {
                StylePreview(sample, state.uniformNumbers && item.group == CosmeticGroup.BOARD)
                if (state.uniformNumbers && item.group != CosmeticGroup.BOARD) {
                    Text("Turn off Classic number display in Settings to see this style in a puzzle.",
                        style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
        }
    }, confirmButton = {
        if (canUse) Button(onClick = use, enabled = !state.busy, modifier = Modifier.heightIn(min = 48.dp)) { Text(item.group.useLabel) }
        else TextButton(onClick = close, modifier = Modifier.heightIn(min = 48.dp)) { Text("Close") }
    }, dismissButton = {
        if (canUse) TextButton(onClick = close, modifier = Modifier.heightIn(min = 48.dp)) { Text("Close") }
    })
}

@Composable internal fun MilestoneTrack(current: Int, target: Int) {
    val fill = if (target <= 0) 1f else (current.toFloat() / target).coerceIn(0f, 1f)
    Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(Rule)) {
        if (fill > 0f) Box(Modifier.fillMaxWidth(fill).fillMaxHeight().background(Forest))
    }
}

/** A labeled visual sample, never a generated or scored game board. */
@Composable internal fun StylePreview(look: Appearance, uniformNumbers: Boolean) {
    val numberLook = look.withNumberDisplay(uniformNumbers)
    val entryFont = fontFor(numberLook.font)
    val clueFont = fontFor(Appearance().font)
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
                    Text((r * 3 + c + 1).toString(), fontFamily = if (entered) entryFont else clueFont,
                        fontSize = (25f / scale).sp, fontWeight = if (entered) FontWeight.Normal else FontWeight.Bold,
                        color = if (entered) inkFor(numberLook.ink, dark) else if (dark) OnForest else Ink)
                }
            }
        }
        Text(styleLabel(numberLook), color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        if (uniformNumbers) {
            Text("Classic number display is on. Turn it off in Settings to use your equipped font and ink.",
                color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        }
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
