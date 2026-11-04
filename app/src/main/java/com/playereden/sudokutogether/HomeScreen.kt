package com.playereden.sudokutogether

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

@Composable internal fun HomeScreen(
    state: GameUiState, actions: GameActions,
    fresh: () -> Unit, freshSolo: () -> Unit, freshRace: () -> Unit,
    help: () -> Unit, journal: (JournalTab) -> Unit,
    markStyle: GridMatesMarkStyle = GridMatesMarkStyle.CONNECTED_TILES
) {
    var mode by rememberSaveable { mutableStateOf("solo") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.widthIn(max = 480.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            HomeHeader(state, help, { journal(JournalTab.RECORDS) }, markStyle)
            (state.message ?: state.saveIssue)?.let { Notice(it) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Triple("solo", "Solo", GameSymbol.PERSON), Triple("together", "Co-op", GameSymbol.PEOPLE),
                    Triple("versus", "Versus", GameSymbol.TROPHY)).forEach { (id, label, symbol) ->
                    val chosen = mode == id
                    val fill by animateColorAsState(if (chosen) Forest else Paper, label = "mode color")
                    Surface(onClick = { mode = id }, modifier = Modifier.weight(1f).heightIn(min = 68.dp)
                        .semantics { selected = chosen }, shape = RoundedCornerShape(17.dp),
                        color = fill, border = BorderStroke(1.dp, if (chosen) Forest else Rule)) {
                        Column(Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            GameIcon(symbol, if (chosen) OnForest else Forest, Modifier.size(20.dp))
                            Text(label, color = if (chosen) OnForest else Forest, fontSize = 13.sp,
                                lineHeight = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            val active = state.profile.activeSolo
            val unfinished = mode == "solo" && state.resumeSolo && active?.completed == false
            Surface(shape = RoundedCornerShape(25.dp), color = NightForest) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(when {
                        unfinished -> "Your puzzle"
                        mode == "together" -> "Shared board"
                        mode == "versus" -> "Race to finish"
                        else -> "Classic 9×9"
                    }, color = OnForest, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold,
                        fontSize = 27.sp, lineHeight = 31.sp)
                    if (unfinished) Text(difficultyName(active?.puzzle?.difficulty) + " · " +
                        (active?.cells?.count { it.value != 0 } ?: 0) + "/81 · " + formatElapsed(active?.elapsedMs ?: 0L),
                        color = OnForest.copy(alpha = .8f), style = MaterialTheme.typography.bodyMedium)
                    else if (mode != "solo") Text(if (mode == "together") "Same Wi-Fi" else "Private boards · Same Wi-Fi",
                        color = OnForest.copy(alpha = .8f), style = MaterialTheme.typography.bodyMedium)
                    if (unfinished) GamePrimaryButton("Continue", { actions.solo(true) },
                        !state.busy && !state.localSaveBlocked, light = true)
                    DifficultyPicker(state, actions)
                    if (mode != "solo") Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .toggleable(state.requireRoomCode, enabled = !state.busy, role = Role.Switch, onValueChange = actions.requireRoomCode)
                        .semantics { contentDescription = "Require a code for a new game" }.testTag("require_code"),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Require a code", color = OnForest, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Switch(state.requireRoomCode, onCheckedChange = null, enabled = !state.busy,
                            colors = SwitchDefaults.colors(checkedThumbColor = Forest, checkedTrackColor = OnForest,
                                uncheckedThumbColor = OnForest, uncheckedTrackColor = NightForest, uncheckedBorderColor = OnForest.copy(alpha = .6f)))
                    }
                    when (mode) {
                        "solo" -> {
                            if (unfinished) OutlinedButton(freshSolo, enabled = !state.busy && !state.localSaveBlocked,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, OnForest.copy(alpha = .4f))) { Text("New puzzle", color = OnForest) }
                            else GamePrimaryButton("New puzzle", freshSolo, !state.busy && !state.localSaveBlocked, light = true)
                            if (active?.completed == true && state.resumeSolo) TextButton(onClick = { actions.solo(true) },
                                enabled = !state.busy && !state.localSaveBlocked, modifier = Modifier.fillMaxWidth()) {
                                Text("Review last puzzle", color = OnForest)
                            }
                        }
                        "together" -> {
                            if (state.resumeHost) GamePrimaryButton("Resume room", { actions.host(true) }, !state.busy, light = true)
                            GamePrimaryButton("Start a game", fresh, !state.busy, light = true)
                            OutlinedButton(onClick = { actions.showJoin(false) }, enabled = !state.busy,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, OnForest.copy(alpha = .4f))) { Text("Join a game", color = OnForest) }
                            if (state.resumeGuest) TextButton(onClick = { actions.showJoin(true) }, enabled = !state.busy,
                                modifier = Modifier.fillMaxWidth()) { Text("Rejoin room", color = OnForest) }
                        }
                        else -> {
                            if (state.resumeRace) GamePrimaryButton("Resume race", { actions.hostRace(true) }, !state.busy, light = true)
                            GamePrimaryButton("Start a race", freshRace, !state.busy, light = true)
                            OutlinedButton(onClick = { actions.showJoin(false) }, enabled = !state.busy,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, OnForest.copy(alpha = .4f))) { Text("Join a game", color = OnForest) }
                        }
                    }
                    if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = OnForest, trackColor = OnForest.copy(alpha = .15f))
                }
            }
            val today = state.profile.dailyProgress.firstOrNull { it.date == state.todayDate }?.solo
            Surface(onClick = actions.daily, enabled = !state.busy && !state.localSaveBlocked,
                modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp).testTag("daily_challenge"),
                shape = RoundedCornerShape(19.dp), color = GoldTint.copy(alpha = .65f), border = BorderStroke(1.dp, Rule)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(38.dp).background(Paper, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        GameIcon(GameSymbol.CALENDAR, Gold)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Daily puzzle", color = Forest, fontSize = 15.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold)
                        Text(when { today?.completed == true -> "Completed"; today != null -> "In progress"; else -> "Ready" },
                            color = Forest, fontSize = 11.sp, lineHeight = 15.sp)
                        Text(state.todayDate + " · Easy", color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
                    }
                    GameIcon(if (today?.completed == true) GameSymbol.CHECK else GameSymbol.RIGHT, modifier = Modifier.size(19.dp))
                }
            }

        }
    }
}

@Composable private fun HomeHeader(state: GameUiState, help: () -> Unit, profile: () -> Unit,
    markStyle: GridMatesMarkStyle) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val reflow = LocalDensity.current.fontScale > 1.3f || maxWidth < 280.dp
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GridMatesMark(Modifier.size(40.dp), markStyle)
                Column(Modifier.weight(1f)) {
                    Text("GridMates", color = Forest, fontSize = 20.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Sudoku", color = Muted, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.sp)
                }
                if (!reflow) HomeHeaderActions(state, help, profile)
            }
            if (reflow) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                HomeHeaderActions(state, help, profile)
            }
        }
    }
}

@Composable private fun HomeHeaderActions(state: GameUiState, help: () -> Unit, profile: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        IconButton(help, modifier = Modifier.size(48.dp).semantics { contentDescription = "How to play Sudoku" }) {
            GameIcon(GameSymbol.HELP, modifier = Modifier.size(22.dp))
        }
        IconButton(profile, enabled = !state.busy,
            modifier = Modifier.size(48.dp).background(GreenTint, RoundedCornerShape(16.dp))
                .semantics { contentDescription = "My profile & records" }) {
            Text(emblemSymbol(state.appearance.emblem), color = inkFor(state.appearance.ink, false), fontSize = 23.sp)
        }
    }
}

@Composable private fun DifficultyPicker(state: GameUiState, actions: GameActions) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf("EASY", "MEDIUM", "HARD").forEach { id ->
            val chosen = state.selectedDifficulty == id
            OutlinedButton(onClick = { actions.difficulty(id) }, enabled = !state.busy,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("difficulty_$id").semantics { selected = chosen; contentDescription = difficultyName(id) + " difficulty for new games" },
                shape = RoundedCornerShape(11.dp), contentPadding = PaddingValues(horizontal = 3.dp, vertical = 10.dp),
                border = BorderStroke(1.dp, if (chosen) OnForest else OnForest.copy(alpha = .3f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (chosen) OnForest.copy(alpha = .16f) else Color.Transparent,
                    contentColor = OnForest, disabledContentColor = OnForest.copy(alpha = .5f))) {
                Text(difficultyName(id), fontSize = 12.sp, fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
