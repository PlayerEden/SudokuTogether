package com.playereden.sudokutogether

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import com.playereden.sudokutogether.core.*

internal enum class ProfileSection(val title: String) {
    OVERVIEW("Profile"), STATISTICS("Statistics"), HISTORY("Game history"), ACHIEVEMENTS("Achievements")
}

@Composable internal fun JournalScreen(
    state: GameUiState, actions: GameActions, tab: JournalTab, section: ProfileSection,
    openSection: (ProfileSection) -> Unit, collection: () -> Unit, help: () -> Unit, share: (() -> Unit)?,
    settingsLocked: Boolean, settingsExtra: @Composable () -> Unit
) {
    key(tab, section) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = 520.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val nested = tab == JournalTab.RECORDS && section != ProfileSection.OVERVIEW
                JournalHeader(if (nested) section.title else tab.title,
                    back = if (nested) ({ openSection(ProfileSection.OVERVIEW) }) else null,
                    backLabel = "Back to Profile")
                if (state.localSaveBlocked) state.saveIssue?.let { Notice(it) }
                when (tab) {
                    JournalTab.RECORDS -> when (section) {
                        ProfileSection.OVERVIEW -> ProfileOverview(state, actions, openSection, collection)
                        ProfileSection.STATISTICS -> StatisticsContent(state)
                        ProfileSection.HISTORY -> HistoryContent(state, actions)
                        ProfileSection.ACHIEVEMENTS -> AchievementsContent(state.profile.records)
                    }
                    JournalTab.COLLECTION -> CollectionContent(state, actions)
                    JournalTab.SETTINGS -> SettingsContent(state, actions, help, share, settingsLocked, settingsExtra)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun ProfileOverview(
    state: GameUiState, actions: GameActions, openSection: (ProfileSection) -> Unit, collection: () -> Unit
) {
    var draft by rememberSaveable { mutableStateOf<String?>(null) }
    GamePanel(Modifier.fillMaxWidth(), color = GreenTint, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnimalPortrait(state.appearance, Modifier.size(52.dp).clearAndSetSemantics {})
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(state.name.ifBlank { "Player" }, style = MaterialTheme.typography.titleLarge, color = Forest)
                Text(identityStyleName(state.appearance), style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ draft = state.name }, enabled = !state.busy, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Edit name")
            }
            OutlinedButton(collection, enabled = !state.busy, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Change player icon")
            }
        }
    }

    val records = state.profile.records
    val stats = PersonalPlay.stats(state.profile)
    GamePanel(Modifier.fillMaxWidth(), padding = 14.dp) {
        SectionTitle("Your play")
        if (records.isEmpty()) {
            Text("Welcome! Your first puzzle finish will appear here.", style = MaterialTheme.typography.bodyMedium, color = Muted)
        } else {
            val highlights = listOf("Solo solved" to stats.soloWins,
                "Daily finished" to DailyChallenge.completedDays(records), "Versus wins" to stats.versusWins)
            if (LocalDensity.current.fontScale > 1.3f) {
                highlights.forEach { (label, count) ->
                    Text("$label: $count", style = MaterialTheme.typography.bodyMedium, color = Ink)
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    highlights.forEach { (label, count) ->
                        Column(Modifier.weight(1f)) {
                            Text(count.toString(), style = MaterialTheme.typography.titleLarge, color = Forest)
                            Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                    }
                }
            }
        }
    }

    val milestones = Achievements.progress(records)
    val earned = milestones.filter { it.unlocked }
    GamePanel(Modifier.fillMaxWidth(), color = GoldTint.copy(alpha = .55f), padding = 14.dp) {
        SectionTitle("Achievements", "${earned.size} of ${milestones.size} earned")
        earned.firstOrNull()?.let {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameIcon(GameSymbol.TROPHY, Gold, Modifier.size(20.dp))
                Text(it.title, style = MaterialTheme.typography.bodyMedium, color = Ink)
            }
        }
        GamePrimaryButton("View achievements", { openSection(ProfileSection.ACHIEVEMENTS) })
    }
    ProfileLink("Statistics", "Best times and puzzle totals", GameSymbol.CLOCK) { openSection(ProfileSection.STATISTICS) }
    ProfileLink("Game history", "Daily puzzles and recent finishes", GameSymbol.CALENDAR) { openSection(ProfileSection.HISTORY) }

    draft?.let { name ->
        fun save() { actions.name(name); draft = null }
        AlertDialog(onDismissRequest = { draft = null }, title = { Text("Edit player name") }, text = {
            OutlinedTextField(name, { draft = it }, label = { Text("Player name") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("player_name"), shape = RoundedCornerShape(13.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { save() }))
        }, confirmButton = { TextButton(onClick = { save() }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { draft = null }) { Text("Cancel") } })
    }
}

@Composable private fun ProfileLink(label: String, detail: String, symbol: GameSymbol, open: () -> Unit) {
    OutlinedCard(onClick = open, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Paper), border = BorderStroke(1.dp, Rule)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            GameIcon(symbol, Forest, Modifier.size(21.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = Forest)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            GameIcon(GameSymbol.RIGHT, Forest, Modifier.size(18.dp))
        }
    }
}

@Composable private fun StatisticsContent(state: GameUiState) {
    val records = state.profile.records
    val stats = PersonalPlay.stats(state.profile)
    val soloSummary = PersonalPlay.soloSummary(records)
    GamePanel(Modifier.fillMaxWidth(), padding = 16.dp) {
        SectionTitle("Solo Sudoku")
        Text("Puzzles solved: ${stats.soloWins}", color = Ink)
        Text("Unassisted ${soloSummary.unassisted} · Assisted ${soloSummary.assisted}", color = Muted, fontSize = 12.sp)
        Text("Best unassisted: " + (soloSummary.bestUnassistedMs?.let(::formatElapsed) ?: "Not yet"), color = Forest, fontSize = 13.sp)
        if (soloSummary.assisted > 0) Text("Best assisted: " +
            (soloSummary.bestAssistedMs?.let(::formatElapsed) ?: "Not yet"), color = Muted, fontSize = 12.sp)
        HorizontalDivider(color = Rule)
        for (tier in listOf("EASY", "MEDIUM", "HARD")) {
            val tierSummary = PersonalPlay.soloSummary(records, tier)
            Text(difficultyName(tier) + " · Unassisted ${tierSummary.unassisted}" +
                " · Best " + (tierSummary.bestUnassistedMs?.let(::formatElapsed) ?: "Not yet"), color = Muted, fontSize = 12.sp)
            if (tierSummary.assisted > 0) Text("Assisted ${tierSummary.assisted} · Best " +
                (tierSummary.bestAssistedMs?.let(::formatElapsed) ?: "Not yet"), color = Muted, fontSize = 12.sp)
        }
        Text("Total solving time: ${formatElapsed(stats.soloTotalMs)}", color = Ink)
    }
    GamePanel(Modifier.fillMaxWidth(), padding = 16.dp) {
        SectionTitle("Daily challenges")
        val daily = records.distinctBy { it.id }.filter { it.mode == DAILY && it.result == "SOLVED" }
        val (assisted, unassisted) = daily.partition { it.assisted }
        Text("Daily challenges: ${DailyChallenge.completedDays(records)} completed", color = Ink)
        Text("Unassisted ${unassisted.size} · Assisted ${assisted.size}", color = Muted, fontSize = 12.sp)
        Text("Daily best unassisted: " + (unassisted.minOfOrNull { it.elapsedMs }?.let(::formatElapsed) ?: "Not yet"),
            color = Forest, fontSize = 13.sp)
        assisted.minOfOrNull { it.elapsedMs }?.let {
            Text("Daily best assisted: ${formatElapsed(it)}", color = Muted, fontSize = 12.sp)
        }
    }
    GamePanel(Modifier.fillMaxWidth(), padding = 16.dp) {
        SectionTitle("Co-op")
        Text("Co-op puzzles completed: ${stats.coopCompletions}", color = Ink)
    }
    GamePanel(Modifier.fillMaxWidth(), padding = 16.dp) {
        SectionTitle("Versus")
        Text("Wins ${stats.versusWins} · Losses ${stats.versusLosses} · Draws ${stats.versusDraws}", color = Ink)
        Text("Head-to-head results are saved only on this phone.", fontSize = 12.sp, color = Muted)
    }
}

@Composable private fun HistoryContent(state: GameUiState, actions: GameActions) {
    DailyHistoryCard(state.profile.dailyProgress, !state.busy && !state.localSaveBlocked, actions.openDaily)
    GamePanel(Modifier.fillMaxWidth(), padding = 16.dp) {
        SectionTitle("Recent finishes")
        val recent = state.profile.records.takeLast(15).reversed()
        if (recent.isEmpty()) Text("No games yet.", fontSize = 14.sp, color = Muted)
        recent.forEach { result ->
            val mode = when (result.mode) { COOP -> "CO-OP"; else -> result.mode.uppercase() }
            Text("$mode · ${result.result} · ${difficultyName(result.difficulty)}" +
                (if (result.mode == SOLO || result.mode == DAILY) (if (result.assisted) " · Assisted" else " · Unassisted") else ""),
                fontSize = 13.sp, color = Ink)
            Text(when {
                result.mode == COOP -> "Finished"
                result.mode == VERSUS && result.elapsedMs == 0L -> "Time unavailable"
                else -> formatElapsed(result.elapsedMs)
            }, fontSize = 12.sp, color = Forest)
            if (result.checksUsed > 0) Text("${if (result.checksUsed == 9999) "9999+" else result.checksUsed} entry check" +
                (if (result.checksUsed == 1) "" else "s"), fontSize = 11.sp, color = Muted)
            HorizontalDivider(color = Rule)
        }
    }
}

@Composable private fun AchievementsContent(records: List<GameRecord>) {
    val milestones = Achievements.progress(records)
    Text("${milestones.count { it.unlocked }} of ${milestones.size} earned", style = MaterialTheme.typography.bodyMedium, color = Muted)
    milestones.forEach { progress ->
        GamePanel(Modifier.fillMaxWidth(), color = if (progress.unlocked) GoldTint.copy(alpha = .45f) else Paper, padding = 14.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameIcon(GameSymbol.TROPHY, if (progress.unlocked) Gold else Muted, Modifier.size(22.dp))
                Column(Modifier.weight(1f)) { SectionTitle(progress.title, progress.description) }
            }
            Text(if (progress.unlocked) "Earned" else "${progress.current} of ${progress.target}",
                color = if (progress.unlocked) Forest else Muted, style = MaterialTheme.typography.bodySmall)
            MilestoneTrack(progress.current, progress.target)
        }
    }
}

@Composable private fun SettingsContent(state: GameUiState, actions: GameActions, help: () -> Unit, share: (() -> Unit)?,
    locked: Boolean, extra: @Composable () -> Unit) {
    var aboutOpen by rememberSaveable { mutableStateOf(false) }
    val enabled = !state.busy && !locked
    GamePanel(Modifier.fillMaxWidth()) {
        SectionTitle("Comfort & display")
        SettingToggle("Classic number display", "Use the Clear font for entries and notes, and the default entry color.",
            state.uniformNumbers, actions.uniformNumbers, enabled)
        HorizontalDivider(color = Rule)
        SettingToggle("Touch feedback", "Board and keypad vibration.", state.touchFeedback, actions.touchFeedback, enabled)
    }
    GamePanel(Modifier.fillMaxWidth()) {
        SectionTitle("Solo assistance")
        SettingToggle("Clean pencil marks", "Remove matching notes from nearby cells.", state.autoCleanNotes, actions.autoCleanNotes, enabled)
        Text("Marks puzzles Assisted. Unassisted best times stay separate.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
    }
    extra()
    GamePanel(Modifier.fillMaxWidth()) {
        SectionTitle("Help")
        OutlinedButton(help, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(13.dp)) {
            GameIcon(GameSymbol.HELP, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("How to play Sudoku")
        }
        OutlinedButton(onClick = { aboutOpen = true }, enabled = enabled,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(13.dp)) {
            Text("About GridMates")
        }
    }
    if (aboutOpen) AboutGame({ aboutOpen = false }, share)
}

@Composable private fun SettingToggle(title: String, detail: String, checked: Boolean, change: (Boolean) -> Unit, enabled: Boolean) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).toggleable(checked, enabled = enabled, onValueChange = change, role = Role.Switch)
        .padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Forest, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp)
            Text(detail, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
        }
        Switch(checked, onCheckedChange = null, enabled = enabled)
    }
}
