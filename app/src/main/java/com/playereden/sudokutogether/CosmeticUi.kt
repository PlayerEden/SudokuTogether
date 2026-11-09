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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playereden.sudokutogether.core.*
import kotlinx.coroutines.launch

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
private enum class IconFamily(val label: String) { ALL("All"), BASIC("Basic"), WOODLAND("Woodland"), OCEAN("Ocean") }
private enum class NumberCategory(val label: String) { COLORS("Number colors"), FONTS("Number fonts") }

private enum class CosmeticGroup(val key: String, val label: String, val useLabel: String) {
    EMBLEM("emblem", "Player icon", "Use icon"),
    PORTRAIT("portrait", "Player icon", "Use icon"),
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

private data class CollectionItem(val cosmetic: Cosmetic, val group: CosmeticGroup, val family: IconFamily? = null) {
    val id: String get() = cosmetic.id
    val tag: String get() = if (group == CosmeticGroup.PORTRAIT) "portrait_$id" else "cosmetic_${group.key}_$id"
    val previewLabel: String get() = family?.let { "${it.label} player icon" } ?: group.label
    fun isEquipped(look: Appearance): Boolean = group.withItem(look, id) == look
}

private val iconItems = Cosmetics.emblems.map { CollectionItem(it, CosmeticGroup.EMBLEM, IconFamily.BASIC) } +
    Cosmetics.woodlandPortraits.map { CollectionItem(it, CosmeticGroup.PORTRAIT, IconFamily.WOODLAND) } +
    Cosmetics.oceanPortraits.map { CollectionItem(it, CosmeticGroup.PORTRAIT, IconFamily.OCEAN) }
private val boardItems = Cosmetics.boards.map { CollectionItem(it, CosmeticGroup.BOARD) }
private val inkItems = Cosmetics.inks.map { CollectionItem(it, CosmeticGroup.INK) }
private val fontItems = Cosmetics.fonts.map { CollectionItem(it, CosmeticGroup.FONT) }
private val allCollectionItems = iconItems + boardItems + inkItems + fontItems

@Composable internal fun CollectionContent(state: GameUiState, actions: GameActions, scroll: ScrollState) {
    var category by rememberSaveable { mutableStateOf(CollectionCategory.ICONS) }
    var family by rememberSaveable { mutableStateOf(IconFamily.ALL) }
    var numbers by rememberSaveable { mutableStateOf(NumberCategory.COLORS) }
    var stylePreviewOpen by rememberSaveable { mutableStateOf(false) }
    var previewId by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val records = state.profile.records
    val progress = remember(records) {
        allCollectionItems.associate { it.id to requireNotNull(Cosmetics.progress(it.id, records)) }
    }
    val look = state.appearance
    val open: (CollectionItem) -> Unit = { previewId = it.id }

    CollectionChoices(CollectionCategory.entries, category, { it.label }, Role.Tab) { category = it }
    Surface(onClick = { stylePreviewOpen = true }, modifier = Modifier.fillMaxWidth(),
        color = GreenTint, shape = RoundedCornerShape(15.dp), border = BorderStroke(1.dp, Rule)) {
        Row(Modifier.heightIn(min = 60.dp).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnimalPortrait(look, Modifier.size(38.dp).clearAndSetSemantics {})
            Column(Modifier.weight(1f)) {
                Text(identityStyleName(look), style = MaterialTheme.typography.titleSmall, color = Forest)
                Text("Style preview", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            GameIcon(GameSymbol.RIGHT, Forest, Modifier.size(18.dp))
        }
    }
    val items = when (category) {
        CollectionCategory.ICONS -> {
            CollectionChoices(IconFamily.entries, family, { it.label }, Role.RadioButton) { family = it }
            iconItems.filter { family == IconFamily.ALL || it.family == family }
        }
        CollectionCategory.BOARDS -> boardItems
        CollectionCategory.NUMBERS -> {
            CollectionChoices(NumberCategory.entries, numbers, { it.label }, Role.Tab) { numbers = it }
            if (state.uniformNumbers) {
                Text("Classic number display is on. You can still preview colors and fonts here.",
                    style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            if (numbers == NumberCategory.COLORS) inkItems else fontItems
        }
    }
    val (available, locked) = items.partition { it.isEquipped(look) || progress.getValue(it.id).unlocked }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("${available.size} of ${items.size} available")
        CosmeticGrid(available.sortedByDescending { it.isEquipped(look) }, progress, look, !state.busy, open)
    }
    if (locked.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Locked")
            CosmeticGrid(locked, progress, look, !state.busy, open)
        }
    }
    if (stylePreviewOpen) {
        AlertDialog(
            onDismissRequest = { stylePreviewOpen = false },
            title = { Text("Your style") },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) { StylePreview(look, state.uniformNumbers) } },
            confirmButton = {
                TextButton(onClick = { stylePreviewOpen = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Close") }
            }
        )
    }
    allCollectionItems.firstOrNull { it.id == previewId }?.let { item ->
        CosmeticPreview(item, progress.getValue(item.id), state, close = { previewId = null }, use = {
            actions.cosmetic(item.group.key, item.id)
            previewId = null
            scope.launch { scroll.animateScrollTo(0) }
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
    val status = when { equipped -> "Equipped"; progress.unlocked -> "Available"; else -> "Locked" }
    val family = item.family?.label
    Surface(onClick = open, enabled = enabled, modifier = modifier.testTag(item.tag).semantics {
        role = Role.Button
        selected = equipped
        stateDescription = if (family == null) status else "$family. $status"
    }, shape = RoundedCornerShape(15.dp), color = if (equipped) GreenTint else Paper,
        border = BorderStroke(if (equipped) 2.dp else 1.dp, if (equipped) Forest else Rule)) {
        val sample = item.group.withItem(look, item.id)
        val detail = if (equipped) status else family
        val label: @Composable () -> Unit = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = if (singleColumn) Alignment.Start else Alignment.CenterHorizontally) {
                Text(item.cosmetic.name, color = Forest, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold,
                    textAlign = if (singleColumn) TextAlign.Start else TextAlign.Center)
                if (detail != null) {
                    Text(detail, color = Muted, fontSize = 11.sp, lineHeight = 14.sp,
                        modifier = Modifier.clearAndSetSemantics {})
                }
            }
        }
        val statusIcon: @Composable (Modifier) -> Unit = { iconModifier ->
            if (equipped || !progress.unlocked) {
                GameIcon(if (equipped) GameSymbol.CHECK else GameSymbol.LOCK, if (equipped) Forest else Muted,
                    iconModifier.size(16.dp))
            }
        }
        if (singleColumn) {
            Row(Modifier.heightIn(min = 78.dp).padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CosmeticSwatch(item, sample, Modifier.size(50.dp))
                Box(Modifier.weight(1f)) { label() }
                statusIcon(Modifier)
            }
        } else {
            Box(Modifier.fillMaxWidth().heightIn(min = 110.dp).padding(10.dp)) {
                Column(Modifier.fillMaxWidth().align(Alignment.Center), verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    CosmeticSwatch(item, sample, Modifier.size(50.dp))
                    label()
                }
                statusIcon(Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

@Composable private fun CosmeticSwatch(item: CollectionItem, look: Appearance, modifier: Modifier) {
    when (item.group) {
        CosmeticGroup.EMBLEM, CosmeticGroup.PORTRAIT -> AnimalPortrait(look, modifier.clearAndSetSemantics {})
        CosmeticGroup.BOARD -> StyleSampleBoard(look, Appearance(), modifier.clearAndSetSemantics {}, compact = true)
        CosmeticGroup.INK, CosmeticGroup.FONT -> {
            val scale = LocalDensity.current.fontScale.coerceAtLeast(1f)
            Box(modifier.clip(RoundedCornerShape(10.dp)).background(Cream).clearAndSetSemantics {},
                contentAlignment = Alignment.Center) {
                Text("59", color = inkFor(look.ink, false), fontFamily = fontFor(look.font), fontSize = (26f / scale).sp)
            }
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
            if (item.family != null) AnimalPortrait(sample, Modifier.size(78.dp).clearAndSetSemantics {})
            Text(item.previewLabel, color = Muted, style = MaterialTheme.typography.bodySmall)
            Text(when { equipped -> "Equipped"; progress.unlocked -> "Available to use"; else -> "Locked" },
                color = Forest, style = MaterialTheme.typography.titleSmall)
            if (!equipped && !progress.unlocked) {
                Text(item.cosmetic.requirement, color = Ink)
                Text("${progress.current} of ${progress.target}" + if (item.group == CosmeticGroup.PORTRAIT) " completed" else "",
                    color = Muted, style = MaterialTheme.typography.bodySmall)
                MilestoneTrack(progress.current, progress.target)
            }
            if (item.family == null) {
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
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("STYLE PREVIEW", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
        StyleSampleBoard(look, numberLook, Modifier.fillMaxWidth().height(144.dp))
        Text(styleLabel(numberLook), color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        if (uniformNumbers) {
            Text("Classic number display is on. Turn it off in Settings to use your equipped font and ink.",
                color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable private fun StyleSampleBoard(look: Appearance, numberLook: Appearance, modifier: Modifier, compact: Boolean = false) {
    val entryFont = fontFor(numberLook.font)
    val clueFont = fontFor(Appearance().font)
    val dark = look.board in listOf("midnight", "circuit")
    val scale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    val digitSize = ((if (compact) 11f else 25f) / scale).sp
    val edge = if (dark) Color(0xFF789690) else Rule
    val shape = RoundedCornerShape(if (compact) 7.dp else 12.dp)
    Column(modifier.clip(shape).background(boardFillFor(look.board)).border(1.dp, edge, shape).drawWithContent {
        drawContent()
        for (i in 1..2) {
            drawLine(edge, Offset(size.width * i / 3, 0f), Offset(size.width * i / 3, size.height), 1.dp.toPx())
            drawLine(edge, Offset(0f, size.height * i / 3), Offset(size.width, size.height * i / 3), 1.dp.toPx())
        }
    }) {
        for (r in 0..2) Row(Modifier.weight(1f).fillMaxWidth()) {
            for (c in 0..2) Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                if (!compact || r == c) {
                    val entered = (r + c) % 2 == 0
                    Text((r * 3 + c + 1).toString(), fontFamily = if (entered) entryFont else clueFont,
                        fontSize = digitSize, lineHeight = digitSize, fontWeight = if (entered) FontWeight.Normal else FontWeight.Bold,
                        color = if (entered) inkFor(numberLook.ink, dark) else if (dark) OnForest else Ink)
                }
            }
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
