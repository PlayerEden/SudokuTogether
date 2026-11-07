package com.playereden.sudokutogether

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import com.playereden.sudokutogether.core.*

@Composable internal fun ProfileScreen(state: GameUiState, actions: GameActions, tab: JournalTab, help: () -> Unit, share: () -> Unit) {
    key(tab) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = 520.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                JournalHeader(when (tab) { JournalTab.RECORDS -> "Records"; JournalTab.COLLECTION -> "Collection"; JournalTab.SETTINGS -> "Settings" },
                    null, actions.home)
                if (state.localSaveBlocked) state.saveIssue?.let { Notice(it) }
                when (tab) {
                    JournalTab.RECORDS -> {
                        PlayerIdentity(state.appearance, state.name, compact = false)
                        RecordOverview(state)
                        RecordsContent(state, actions)
                    }
                    JournalTab.COLLECTION -> CollectionContent(state, actions)
                    JournalTab.SETTINGS -> SettingsContent(state, actions, help, share)
                }
            }
        }
    }
}

@Composable private fun ColumnScope.RecordsContent(state: GameUiState, actions: GameActions) {
    val stats = PersonalPlay.stats(state.profile)
    val soloSummary = PersonalPlay.soloSummary(state.profile.records)
    OutlinedCard(colors = CardDefaults.outlinedCardColors(containerColor = Paper), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("SOLO SUDOKU", fontSize = 11.sp, letterSpacing = 1.5.sp, color = Forest)
            Text("Puzzles solved: " + stats.soloWins, color = Ink)
            val dailyCompleted = state.profile.records.filter { it.mode == DAILY && it.result == "SOLVED" }
            Text("Daily challenges: " + DailyChallenge.completedDays(state.profile.records) + " completed",
                color = Forest, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            val dailyClassic = dailyCompleted.filterNot { it.assisted }
            Text("Daily best unassisted: " +
                (dailyClassic.minOfOrNull { it.elapsedMs }?.let(::formatElapsed) ?: "Not yet"),
                color = Muted, fontSize = 12.sp)
            Text("Unassisted " + soloSummary.unassisted + " · Assisted " + soloSummary.assisted,
                color = Muted, fontSize = 12.sp)
            Text("Best unassisted: " + (soloSummary.bestUnassistedMs?.let(::formatElapsed) ?: "Not yet"),
                color = Forest, fontSize = 13.sp)
            if (soloSummary.assisted > 0) Text("Best assisted: " +
                (soloSummary.bestAssistedMs?.let(::formatElapsed) ?: "Not yet"), color = Muted, fontSize = 12.sp)
            HorizontalDivider(color = Rule)
            for (tier in listOf("EASY", "MEDIUM", "HARD")) {
                val tierSummary = PersonalPlay.soloSummary(state.profile.records, tier)
                Text(difficultyName(tier) + " · Unassisted " + tierSummary.unassisted +
                    " · Best " + (tierSummary.bestUnassistedMs?.let(::formatElapsed) ?: "Not yet"),
                    color = Muted, fontSize = 12.sp)
                if (tierSummary.assisted > 0) Text("  Assisted " + tierSummary.assisted +
                    " · Best " + (tierSummary.bestAssistedMs?.let(::formatElapsed) ?: "Not yet"),
                    color = Muted, fontSize = 12.sp)
            }
            Text("Total solving time: " + formatElapsed(stats.soloTotalMs), color = Ink)
        }
    }
    DailyHistoryCard(state.profile.dailyProgress, !state.busy && !state.localSaveBlocked, actions.openDaily)
    OutlinedCard(colors = CardDefaults.outlinedCardColors(containerColor = Paper), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("HEAD-TO-HEAD", fontSize = 11.sp, letterSpacing = 1.5.sp, color = Forest)
            Text("Wins " + stats.versusWins + "   Losses " + stats.versusLosses +
                "   Draws " + stats.versusDraws, color = Ink)
            Text("Head-to-head results are saved only on this phone.", fontSize = 12.sp, color = Muted)
            Text("Co-op puzzles completed: " + stats.coopCompletions, color = Ink)
        }
    }
    Text("RECENT FINISHES", fontSize = 11.sp, letterSpacing = 1.5.sp, color = Muted)
    val recent = state.profile.records.takeLast(15).reversed()
    if (recent.isEmpty()) Text("No games yet.", fontSize = 14.sp, color = Muted)
    recent.forEach { result ->
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(result.mode.uppercase() + " · " + result.result + " · " + difficultyName(result.difficulty) +
                (if (result.mode == SOLO || result.mode == DAILY) (if (result.assisted) " · Assisted" else " · Unassisted") else ""),
                fontSize = 12.sp, color = Ink, modifier = Modifier.weight(1f))
            Text(if (result.mode == COOP) "Finished" else formatElapsed(result.elapsedMs), fontSize = 12.sp, color = Forest, modifier = Modifier.padding(start = 8.dp))
        }
        if (result.checksUsed > 0) Text("${if (result.checksUsed == 9999) "9999+" else result.checksUsed} entry check" +
            (if (result.checksUsed == 1) "" else "s"),fontSize=11.sp,color=Muted)
        HorizontalDivider(color = Rule)
    }
}

@Composable private fun ColumnScope.CollectionContent(state: GameUiState, actions: GameActions) {
    GamePanel(color = GreenTint) {
        PlayerIdentity(state.appearance, state.name, compact = false)
        StylePreview(state.appearance)
    }
    Text("Earned milestones " + Achievements.earnedCount(state.profile.records) + " of " + Achievements.progress(state.profile.records).size,
        color = Muted, fontSize = 13.sp)
    WoodlandCollection(state, actions)
    CosmeticSelector("Number colors", "ink", Cosmetics.inks, state.appearance.ink, state, actions)
    CosmeticSelector("Board styles", "board", Cosmetics.boards, state.appearance.board, state, actions)
    CosmeticSelector("Number fonts", "font", Cosmetics.fonts, state.appearance.font, state, actions)
    CosmeticSelector("Player emblems", "emblem", Cosmetics.emblems, state.appearance.emblem, state, actions)
    Text("ACHIEVEMENTS", fontSize = 11.sp, letterSpacing = 1.5.sp, color = Muted)
    Achievements.progress(state.profile.records).forEach { progress ->
        OutlinedCard(colors = CardDefaults.outlinedCardColors(containerColor = Paper), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (progress.unlocked) "★ " else "◇ ", color = if (progress.unlocked) Gold else Muted)
                    Column(Modifier.weight(1f)) {
                        Text(progress.title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(progress.description, color = Muted, fontSize = 11.sp)
                    }
                    Text(if (progress.unlocked) "Earned" else
                        "${progress.current.coerceAtMost(progress.target)}/${progress.target}",
                        color = if (progress.unlocked) Forest else Muted, fontSize = 11.sp)
                }
                LinearProgressIndicator(progress = { (progress.current.toFloat() / progress.target).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(), color = Forest, trackColor = Rule)
            }
        }
    }
}

@Composable private fun RecordOverview(state: GameUiState) {
    val solo = PersonalPlay.stats(state.profile).soloWins
    val daily = DailyChallenge.completedDays(state.profile.records)
    if (androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            RecordTile("SOLO SOLVED", solo.toString(), GameSymbol.GRID, Modifier.fillMaxWidth())
            RecordTile("DAILY FINISHES", daily.toString(), GameSymbol.CALENDAR, Modifier.fillMaxWidth())
        }
    } else Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        RecordTile("SOLO SOLVED", solo.toString(), GameSymbol.GRID, Modifier.weight(1f))
        RecordTile("DAILY FINISHES", daily.toString(), GameSymbol.CALENDAR, Modifier.weight(1f))
    }
}

@Composable private fun RecordTile(label: String, value: String, symbol: GameSymbol, modifier: Modifier) {
    GamePanel(modifier, color = GreenTint.copy(alpha = .7f), padding = 14.dp) {
        GameIcon(symbol, modifier = Modifier.size(21.dp))
        Text(value, color = Forest, fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold)
        Eyebrow(label)
    }
}

@Composable private fun ColumnScope.SettingsContent(state: GameUiState, actions: GameActions, help: () -> Unit, share: () -> Unit) {
    var aboutOpen by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    GamePanel {
        SectionTitle("Player")
        OutlinedTextField(state.name, actions.name, label = { Text("Player name") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("player_name"), shape = RoundedCornerShape(13.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }))
        PlayerIdentity(state.appearance, state.name, compact = true)
    }
    GamePanel {
        SectionTitle("Comfort & display")
        SettingToggle("Classic number display", "One style for all entries.", state.uniformNumbers, actions.uniformNumbers)
        HorizontalDivider(color = Rule)
        SettingToggle("Touch feedback", "Board and keypad vibration.", state.touchFeedback, actions.touchFeedback)
    }
    GamePanel {
        SectionTitle("Solo assistance")
        SettingToggle("Clean pencil marks", "Remove matching notes from nearby cells.", state.autoCleanNotes, actions.autoCleanNotes)
        Text("Marks puzzles Assisted. Unassisted best times stay separate.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
    }
    GamePanel {
        SectionTitle("Help")
        OutlinedButton(help, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(13.dp)) {
            GameIcon(GameSymbol.HELP, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("How to play Sudoku")
        }
    }
    OutlinedButton(onClick = { focus.clearFocus(); aboutOpen = true },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(13.dp)) {
        Text("About GridMates")
    }
    if (aboutOpen) AboutGame({ aboutOpen = false }, share)

}

@Composable internal fun SettingToggle(title: String, detail: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).toggleable(checked, onValueChange = change, role = Role.Switch)
        .padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Forest, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp)
            Text(detail, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
        }
        Switch(checked, onCheckedChange = null)
    }
}
