package com.playereden.sudokutogether

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

@Composable internal fun HomeScreen(
    state: GameUiState, actions: GameActions, share: () -> Unit,
    fresh: () -> Unit, freshSolo: () -> Unit, freshRace: () -> Unit,
    help: () -> Unit, journal: (JournalTab) -> Unit
) {
    var mode by rememberSaveable { mutableStateOf("solo") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.widthIn(max = 480.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GridMark(Modifier.size(40.dp))
                Column(Modifier.weight(1f)) {
                    Text("Sudoku", color = Forest, fontSize = 20.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Together", color = Muted, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.sp)
                }
                TextButton(help, modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Help", fontSize = 12.sp)
                }
                IconButton(onClick = { journal(JournalTab.RECORDS) }, enabled = !state.busy,
                    modifier = Modifier.size(48.dp).background(GreenTint, RoundedCornerShape(16.dp))
                        .semantics { contentDescription = "My profile & records" }) {
                    Text(emblemSymbol(state.appearance.emblem), color = inkFor(state.appearance.ink, false), fontSize = 23.sp)
                }
            }
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
            Surface(shape = RoundedCornerShape(25.dp), color = NightForest) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Eyebrow(when (mode) { "together" -> "SOLVE TOGETHER"; "versus" -> "HEAD TO HEAD"; else -> "YOUR NEXT GOOD MOVE" }, OnForest.copy(alpha = .72f))
                        Spacer(Modifier.weight(1f)); GameIcon(GameSymbol.GRID, OnForest.copy(alpha = .45f), Modifier.size(23.dp))
                    }
                    val active = state.profile.activeSolo
                    val unfinished = mode == "solo" && state.resumeSolo && active?.completed == false
                    Text(when {
                        unfinished -> "Pick up where you left off."
                        mode == "together" -> "Two minds. One grid."
                        mode == "versus" -> "A friendly rivalry."
                        else -> "Find your flow."
                    }, color = OnForest, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold,
                        fontSize = 29.sp, lineHeight = 33.sp)
                    Text(when (mode) {
                        "together" -> "Share every move on two phones over the same Wi-Fi."
                        "versus" -> "The same puzzle. Private boards. First valid finish wins."
                        else -> if (unfinished) difficultyName(active?.puzzle?.difficulty) + " · " +
                            (active?.cells?.count { it.value != 0 } ?: 0) + "/81 filled · " + formatElapsed(active?.elapsedMs ?: 0L)
                        else "A fresh classic puzzle, at your own pace."
                    }, color = OnForest.copy(alpha = .8f), style = MaterialTheme.typography.bodyMedium)
                    if (unfinished) GamePrimaryButton("Resume solo puzzle", { actions.solo(true) },
                        !state.busy && !state.localSaveBlocked, light = true)
                    DifficultyPicker(state, actions)
                    when (mode) {
                        "solo" -> {
                            if (unfinished) OutlinedButton(freshSolo, enabled = !state.busy && !state.localSaveBlocked,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, OnForest.copy(alpha = .4f))) { Text("New solo puzzle", color = OnForest) }
                            else GamePrimaryButton("New solo puzzle", freshSolo, !state.busy && !state.localSaveBlocked, light = true)
                            if (active?.completed == true && state.resumeSolo) TextButton(onClick = { actions.solo(true) },
                                enabled = !state.busy && !state.localSaveBlocked, modifier = Modifier.fillMaxWidth()) {
                                Text("Review last solo puzzle", color = OnForest)
                            }
                        }
                        "together" -> {
                            if (state.resumeHost) GamePrimaryButton("Resume your puzzle", { actions.host(true) }, !state.busy, light = true)
                            GamePrimaryButton("Host a puzzle", fresh, !state.busy, light = true)
                            OutlinedButton(onClick = { actions.showJoin(false) }, enabled = !state.busy,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, OnForest.copy(alpha = .4f))) { Text("Join a puzzle", color = OnForest) }
                            if (state.resumeGuest) TextButton(onClick = { actions.showJoin(true) }, enabled = !state.busy,
                                modifier = Modifier.fillMaxWidth()) { Text("Rejoin last puzzle", color = OnForest) }
                        }
                        else -> {
                            if (state.resumeRace) GamePrimaryButton("Resume head-to-head room", { actions.hostRace(true) }, !state.busy, light = true)
                            GamePrimaryButton("Host a versus race", freshRace, !state.busy, light = true)
                            OutlinedButton(onClick = { actions.showJoin(false) }, enabled = !state.busy,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, OnForest.copy(alpha = .4f))) { Text("Join a puzzle", color = OnForest) }
                        }
                    }
                    Text("Difficulty applies to new games. Saved puzzles keep their own.",
                        color = OnForest.copy(alpha = .68f), fontSize = 10.sp, lineHeight = 15.sp)
                    if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = OnForest, trackColor = OnForest.copy(alpha = .15f))
                }
            }
            val today = state.profile.dailyProgress.firstOrNull { it.date == state.todayDate }?.solo
            GamePanel(color = GoldTint.copy(alpha = .6f)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(42.dp).background(Paper, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                        GameIcon(GameSymbol.CALENDAR, Gold)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Daily Challenge", style = MaterialTheme.typography.titleMedium, color = Forest)
                        Text(state.todayDate + " · Easy · Offline", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                TextButton(actions.daily, enabled = !state.busy && !state.localSaveBlocked,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("daily_challenge"),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)) {
                    Text(when { today?.completed == true -> "Review today's challenge"; today != null -> "Continue today's challenge"; else -> "Play today's challenge" },
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Start, fontWeight = FontWeight.SemiBold)
                    GameIcon(if (today?.completed == true) GameSymbol.CHECK else GameSymbol.RIGHT)
                }
            }
            TextButton(share, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                GameIcon(GameSymbol.SHARE, Muted, Modifier.size(17.dp)); Spacer(Modifier.width(9.dp))
                Text("Share the game with a friend", color = Muted, fontSize = 12.sp)
            }
            Text("SUDOKU TOGETHER  ·  " + BuildConfig.VERSION_NAME, modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center, color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable private fun DifficultyPicker(state: GameUiState, actions: GameActions) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf("EASY", "MEDIUM", "HARD").forEach { id ->
            val chosen = state.selectedDifficulty == id
            OutlinedButton(onClick = { actions.difficulty(id) }, enabled = !state.busy,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("difficulty_$id").semantics { selected = chosen },
                shape = RoundedCornerShape(11.dp), contentPadding = PaddingValues(horizontal = 3.dp, vertical = 10.dp),
                border = BorderStroke(1.dp, if (chosen) OnForest else OnForest.copy(alpha = .3f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (chosen) OnForest.copy(alpha = .16f) else Color.Transparent,
                    contentColor = OnForest, disabledContentColor = OnForest.copy(alpha = .5f))) {
                Text(difficultyName(id), fontSize = 12.sp, fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
