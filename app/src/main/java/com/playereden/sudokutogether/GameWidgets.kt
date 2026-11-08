package com.playereden.sudokutogether

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

internal enum class JournalTab(val title: String) {
    // Keep the old enum name for Activity state saved by earlier versions.
    RECORDS("Profile"), COLLECTION("Collection"), SETTINGS("Settings")
}
internal enum class GameSymbol { GRID, PERSON, PEOPLE, TROPHY, CALENDAR, SETTINGS, HELP, RIGHT, BACK, PENCIL, ERASE, UNDO, CHECK, PALETTE, CHAT, CLOCK, LOCK, SHARE, COPY }

/** Original, consistent line icons; no emoji or downloaded asset dependency. */
@Composable internal fun GameIcon(symbol: GameSymbol, color: Color = Forest, modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier.clearAndSetSemantics { }) {
        withTransform({ scale(size.width / 24f, size.height / 24f, Offset.Zero) }) {
            val stroke = Stroke(1.7f, cap = StrokeCap.Round)
            fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(color, Offset(x1, y1), Offset(x2, y2), 1.7f, StrokeCap.Round)
            fun path(vararg points: Pair<Float, Float>) {
                drawPath(Path().apply { points.forEachIndexed { i, p -> if (i == 0) moveTo(p.first, p.second) else lineTo(p.first, p.second) } }, color, style = stroke)
            }
            when (symbol) {
                GameSymbol.GRID -> {
                    drawRoundRect(color, Offset(3f, 3f), Size(18f, 18f), CornerRadius(3f), style = stroke)
                    line(9f, 3f, 9f, 21f); line(15f, 3f, 15f, 21f); line(3f, 9f, 21f, 9f); line(3f, 15f, 21f, 15f)
                }
                GameSymbol.PERSON -> { drawCircle(color, 3.5f, Offset(12f, 7f), style = stroke); drawArc(color, 180f, 180f, false, Offset(4f, 13f), Size(16f, 14f), style = stroke); line(4f, 20f, 20f, 20f) }
                GameSymbol.PEOPLE -> { drawCircle(color, 3f, Offset(8f, 7f), style = stroke); drawCircle(color, 2.7f, Offset(17f, 8f), style = stroke); drawArc(color, 180f, 180f, false, Offset(2f, 13f), Size(12f, 12f), style = stroke); drawArc(color, 215f, 145f, false, Offset(13f, 14f), Size(9f, 10f), style = stroke) }
                GameSymbol.TROPHY -> { path(6f to 3f, 18f to 3f, 17f to 11f, 14f to 14f, 10f to 14f, 7f to 11f, 6f to 3f); path(6f to 5f, 3f to 5f, 3f to 9f, 7f to 11f); path(18f to 5f, 21f to 5f, 21f to 9f, 17f to 11f); line(12f, 14f, 12f, 20f); line(8f, 21f, 16f, 21f) }
                GameSymbol.CALENDAR -> { drawRoundRect(color, Offset(3f, 5f), Size(18f, 16f), CornerRadius(3f), style = stroke); line(3f, 10f, 21f, 10f); line(8f, 3f, 8f, 7f); line(16f, 3f, 16f, 7f); path(8f to 15f, 11f to 18f, 16f to 13f) }
                GameSymbol.SETTINGS -> { line(4f, 6f, 20f, 6f); line(4f, 12f, 20f, 12f); line(4f, 18f, 20f, 18f); drawCircle(Paper, 2.5f, Offset(8f, 6f)); drawCircle(color, 2.5f, Offset(8f, 6f), style = stroke); drawCircle(Paper, 2.5f, Offset(16f, 12f)); drawCircle(color, 2.5f, Offset(16f, 12f), style = stroke); drawCircle(Paper, 2.5f, Offset(10f, 18f)); drawCircle(color, 2.5f, Offset(10f, 18f), style = stroke) }
                GameSymbol.HELP -> { drawCircle(color, 9f, Offset(12f, 12f), style = stroke); drawArc(color, 185f, 260f, false, Offset(9f, 7f), Size(6f, 6f), style = stroke); line(12f, 13f, 12f, 14f); drawCircle(color, 1f, Offset(12f, 17f)) }
                GameSymbol.RIGHT -> { line(4f, 12f, 20f, 12f); path(14f to 6f, 20f to 12f, 14f to 18f) }
                GameSymbol.BACK -> { line(4f, 12f, 20f, 12f); path(10f to 6f, 4f to 12f, 10f to 18f) }
                GameSymbol.PENCIL -> { path(4f to 16f, 16f to 4f, 20f to 8f, 8f to 20f, 3f to 21f, 4f to 16f); line(13f, 7f, 17f, 11f) }
                GameSymbol.ERASE -> { path(3f to 14f, 13f to 4f, 21f to 12f, 13f to 20f, 9f to 20f, 3f to 14f); line(8f, 9f, 16f, 17f); line(12f, 20f, 22f, 20f) }
                GameSymbol.UNDO -> { path(8f to 5f, 3f to 10f, 8f to 15f); line(3f, 10f, 14f, 10f); drawArc(color, -90f, 180f, false, Offset(8f, 10f), Size(12f, 10f), style = stroke) }
                GameSymbol.CHECK -> path(5f to 12f, 10f to 17f, 20f to 7f)
                GameSymbol.PALETTE -> { drawCircle(color, 9f, Offset(12f, 12f), style = stroke); drawCircle(color, 1.2f, Offset(8f, 8f)); drawCircle(color, 1.2f, Offset(15f, 7f)); drawCircle(color, 1.2f, Offset(18f, 13f)); path(5f to 18f, 10f to 18f, 11f to 15f, 14f to 16f, 14f to 21f) }
                GameSymbol.CHAT -> { path(4f to 4f, 20f to 4f, 20f to 17f, 10f to 17f, 4f to 21f, 4f to 4f); line(8f, 9f, 16f, 9f); line(8f, 13f, 13f, 13f) }
                GameSymbol.CLOCK -> { drawCircle(color, 9f, Offset(12f, 12f), style = stroke); path(12f to 6f, 12f to 12f, 16f to 15f) }
                GameSymbol.LOCK -> { drawRoundRect(color, Offset(5f, 10f), Size(14f, 11f), CornerRadius(2f), style = stroke); drawArc(color, 180f, 180f, false, Offset(8f, 2f), Size(8f, 16f), style = stroke); line(12f, 14f, 12f, 17f) }
                GameSymbol.SHARE -> { drawCircle(color, 2.5f, Offset(6f, 12f), style = stroke); drawCircle(color, 2.5f, Offset(18f, 5f), style = stroke); drawCircle(color, 2.5f, Offset(18f, 19f), style = stroke); line(8f, 11f, 16f, 6f); line(8f, 13f, 16f, 18f) }
                GameSymbol.COPY -> { drawRoundRect(color, Offset(8f, 7f), Size(12f, 14f), CornerRadius(2f), style = stroke); path(5f to 17f, 3f to 17f, 3f to 3f, 15f to 3f, 15f to 5f) }
            }
        }
    }
}

@Composable internal fun GridMark(modifier: Modifier = Modifier.size(42.dp)) {
    Canvas(modifier.clearAndSetSemantics { }) {
        val unit = size.minDimension / 42f
        drawRoundRect(Forest, cornerRadius = CornerRadius(12f * unit))
        for (r in 0..2) for (c in 0..2) {
            drawRoundRect(if (r == c && r != 1) Color(0xFFE4BD6B) else OnForest,
                Offset((10f + c * 8f) * unit, (10f + r * 8f) * unit), Size(6f * unit, 6f * unit), CornerRadius(1.4f * unit))
        }
    }
}

@Composable internal fun GamePanel(modifier: Modifier = Modifier, color: Color = Paper,
    padding: Dp = 18.dp, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(22.dp), color = color,
        border = BorderStroke(1.dp, Rule.copy(alpha = .7f)), shadowElevation = 1.dp) {
        Column(Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable internal fun Eyebrow(text: String, color: Color = Muted) {
    Text(text, color = color, fontSize = 10.sp, lineHeight = 14.sp,
        letterSpacing = 1.3.sp, fontWeight = FontWeight.SemiBold)
}

@Composable internal fun SectionTitle(title: String, detail: String? = null, modifier: Modifier = Modifier) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = Forest, modifier = modifier.semantics { heading() })
    if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall, color = Muted)
}

@Composable internal fun GamePrimaryButton(text: String, onClick: () -> Unit,
    enabled: Boolean = true, modifier: Modifier = Modifier, light: Boolean = false) {
    Button(onClick, enabled = enabled, modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(15.dp), contentPadding = PaddingValues(horizontal = 17.dp, vertical = 14.dp),
        colors = if (light) ButtonDefaults.buttonColors(containerColor = OnForest, contentColor = Forest,
            disabledContainerColor = OnForest.copy(alpha = .35f), disabledContentColor = OnForest.copy(alpha = .7f))
        else ButtonDefaults.buttonColors()) {
        Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.width(10.dp)); GameIcon(GameSymbol.RIGHT, if (light) Forest else OnForest, Modifier.size(19.dp))
    }
}

@Composable internal fun GameNavigation(page: Page, tab: JournalTab, play: () -> Unit, journal: (JournalTab) -> Unit, enabled: Boolean = true) {
    val largeText = LocalDensity.current.fontScale > 1.3f
    Surface(color = Paper, shadowElevation = 8.dp) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 6.dp).selectableGroup()) {
                NavigationItem("Play", "play", GameSymbol.GRID, page == Page.HOME, enabled, play,
                    Modifier.weight(if (largeText) .67f else 1f))
                // Retain the existing automation tag for the destination formerly called Records.
                NavigationItem(JournalTab.RECORDS.title, "records", GameSymbol.PERSON,
                    page == Page.PROFILE && tab == JournalTab.RECORDS, enabled, { journal(JournalTab.RECORDS) },
                    Modifier.weight(if (largeText) .93f else 1f))
                NavigationItem(JournalTab.COLLECTION.title, "collection", GameSymbol.PALETTE,
                    page == Page.PROFILE && tab == JournalTab.COLLECTION, enabled, { journal(JournalTab.COLLECTION) },
                    Modifier.weight(if (largeText) 1.31f else 1f))
                NavigationItem(JournalTab.SETTINGS.title, "settings", GameSymbol.SETTINGS,
                    page == Page.PROFILE && tab == JournalTab.SETTINGS, enabled, { journal(JournalTab.SETTINGS) },
                    Modifier.weight(if (largeText) 1.09f else 1f))
            }
        }
    }
}

@Composable private fun NavigationItem(label: String, id: String, symbol: GameSymbol, selected: Boolean,
    enabled: Boolean, open: () -> Unit, modifier: Modifier) {
    Column(modifier.testTag("nav_$id")
        .selectable(selected, enabled = enabled, onClick = open, role = Role.Tab)
        .semantics { contentDescription = label }
        .heightIn(min = 68.dp).padding(horizontal = 2.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(42.dp, 27.dp).background(if (selected) GreenTint else Color.Transparent, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center) {
            GameIcon(symbol, if (selected) Forest else Muted, Modifier.size(20.dp))
        }
        Text(label, color = if (selected) Forest else Muted, fontSize = 11.sp, lineHeight = 13.sp,
            textAlign = TextAlign.Center, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.clearAndSetSemantics {})
    }
}

@Composable internal fun JournalHeader(title: String, subtitle: String? = null, back: (() -> Unit)? = null,
    backLabel: String = "Back home") {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (back != null) IconButton(back, modifier = Modifier.size(48.dp).background(Paper, RoundedCornerShape(14.dp))
            .semantics { contentDescription = backLabel }) { GameIcon(GameSymbol.BACK) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = Forest, modifier = Modifier.semantics { heading() })
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
    }
}
