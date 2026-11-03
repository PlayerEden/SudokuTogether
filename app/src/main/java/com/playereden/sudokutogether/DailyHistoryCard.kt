package com.playereden.sudokutogether

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.playereden.sudokutogether.core.DailyProgress

@Composable internal fun DailyHistoryCard(days: List<DailyProgress>, enabled: Boolean, open: (String) -> Unit) {
    val saved = remember(days) { days.sortedByDescending { it.date } }
    var showAll by rememberSaveable { mutableStateOf(false) }
    val largeText = LocalDensity.current.fontScale > 1.3f
    OutlinedCard(colors = CardDefaults.outlinedCardColors(containerColor = Paper), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (largeText) {
                Text("DAILY HISTORY", fontSize = 11.sp, letterSpacing = 1.5.sp, color = Forest)
                if (saved.size > 6) TextButton(onClick = { showAll = !showAll }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(if (showAll) "Show recent" else "Show all ${saved.size}")
                }
            } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("DAILY HISTORY", fontSize = 11.sp, letterSpacing = 1.5.sp, color = Forest, modifier = Modifier.weight(1f))
                if (saved.size > 6) TextButton(onClick = { showAll = !showAll }) {
                    Text(if (showAll) "Show recent" else "Show all ${saved.size}", fontSize = 12.sp)
                }
            }
            Text("Continue a saved day or review a finished one.", fontSize = 12.sp, color = Muted)
            if (saved.isEmpty()) Text("No daily puzzles started yet.", fontSize = 12.sp, color = Muted)
            for (day in saved.take(if (showAll) saved.size else 6)) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val stacked = largeText || maxWidth < 240.dp
                    if (stacked) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DailyDetails(day)
                        DailyAction(day, enabled, open, Modifier.fillMaxWidth())
                    } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.weight(1f)) { DailyDetails(day) }
                        DailyAction(day, enabled, open, Modifier)
                    }
                }
                HorizontalDivider(color = Rule)
            }
        }
    }
}

@Composable private fun DailyDetails(day: DailyProgress) {
    Text(day.date, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
    Text((if (day.solo.completed) "Completed" else "In progress") + " · " + formatElapsed(day.solo.elapsedMs) +
        (if (day.solo.completed && (day.solo.hintsUsed > 0 || day.solo.usedNoteAssist)) " · Assisted" else ""),
        color = Muted, fontSize = 11.sp)
}

@Composable private fun DailyAction(day: DailyProgress, enabled: Boolean, open: (String) -> Unit, modifier: Modifier) {
    OutlinedButton(onClick = { open(day.date) }, enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp).testTag("daily_history_${day.date}")) {
        Text(if (day.solo.completed) "Review" else "Continue")
    }
}
