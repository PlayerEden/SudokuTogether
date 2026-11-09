package com.playereden.sudokutogether

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.playereden.sudokutogether.core.Appearance
import com.playereden.sudokutogether.core.Cell
import com.playereden.sudokutogether.core.CoachPractice
import com.playereden.sudokutogether.core.SOLO
import com.playereden.sudokutogether.core.Snapshot

/** Practice reads the equipped style and keeps each attempt inside this dialog. */
@Composable internal fun CoachPracticeDialog(
    appearance: Appearance,
    uniformNumbers: Boolean,
    close: () -> Unit
) {
    var lessonId by rememberSaveable { mutableStateOf<String?>(null) }
    val lesson = CoachPractice.lessons.firstOrNull { it.id == lessonId }
    val back: () -> Unit = { if (lesson == null) close() else lessonId = null }

    Dialog(onDismissRequest = back, properties = DialogProperties(
        dismissOnClickOutside = false, usePlatformDefaultWidth = false
    )) {
        Surface(Modifier.fillMaxSize(), color = Cream) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp)) {
                TextButton(onClick = back, modifier = Modifier.heightIn(min = 48.dp)) {
                    GameIcon(GameSymbol.BACK, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (lesson == null) "Home" else "Lessons")
                }
                key(lessonId) {
                    val scroll = rememberScrollState()
                    BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                        val boardLimit = maxHeight.coerceAtMost(360.dp)
                        Column(
                            Modifier.fillMaxSize().verticalScroll(scroll).padding(top = 4.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (lesson == null) {
                                Text("Coach practice", style = MaterialTheme.typography.headlineMedium,
                                    color = Forest, fontFamily = FontFamily.Serif)
                                Text("Pick a technique and try one move on a practice board.", color = Ink)
                                for (entry in CoachPractice.lessons) {
                                    OutlinedCard(
                                        onClick = { lessonId = entry.id },
                                        modifier = Modifier.fillMaxWidth().testTag("practice_lesson_${entry.id}")
                                    ) {
                                        Row(Modifier.fillMaxWidth().padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Column(Modifier.weight(1f)) {
                                                Text(entry.technique.label, color = Forest,
                                                    style = MaterialTheme.typography.titleMedium)
                                                Text(entry.description, color = Ink,
                                                    style = MaterialTheme.typography.bodyMedium)
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            GameIcon(GameSymbol.RIGHT, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            } else PracticeMove(lesson, appearance, uniformNumbers, scroll, boardLimit)
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun PracticeMove(
    lesson: CoachPractice.Lesson,
    appearance: Appearance,
    uniformNumbers: Boolean,
    scroll: ScrollState,
    boardLimit: Dp
) {
    var choice by rememberSaveable { mutableIntStateOf(0) }
    var explained by rememberSaveable { mutableStateOf(false) }
    var setupOpen by rememberSaveable { mutableStateOf(false) }
    val correct = choice in lesson.correctDigits
    val step = lesson.step
    val removesCandidate = step.eliminations.isNotEmpty()
    val showProof = explained || correct
    val sources = if (showProof) step.sourceCells.toSet() else emptySet()
    val removals = if (showProof) step.eliminations else emptyMap()
    LaunchedEffect(correct) { scroll.scrollTo(0) }

    Text(lesson.technique.label, style = MaterialTheme.typography.headlineMedium,
        color = Forest, fontFamily = FontFamily.Serif)
    Text(lesson.description, color = Ink)
    if (correct) {
        Text("Correct", color = Forest, style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
    } else {
        Text(if (removesCandidate) "Which pencil mark can you cross off in the outlined square?"
            else "Which number belongs in the outlined square?", color = Ink)
        if (!explained) OutlinedButton(onClick = { explained = true }, modifier = Modifier.heightIn(min = 48.dp)) {
            Text("Show why")
        }
    }
    if (showProof) Text(step.explanation, color = Ink)
    if (lesson.setup.isNotEmpty()) {
        Text("This position starts with pencil marks from earlier deductions.", color = Muted,
            style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = { setupOpen = !setupOpen }, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(if (setupOpen) "Hide setup steps" else "Show setup steps")
        }
        if (setupOpen) for (setup in lesson.setup) {
            Text(setup.explanation, color = Ink, style = MaterialTheme.typography.bodySmall)
            for ((cell, digits) in setup.eliminations) {
                Text("Cross off ${digits.joinToString(" and ")} at row ${cell / 9 + 1}, column ${cell % 9 + 1}.",
                    color = Ink, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    if (correct) Button(onClick = { choice = 0; explained = false; setupOpen = false },
        modifier = Modifier.heightIn(min = 48.dp)) { Text("Practice again") }

    val placedDigit = if (correct && !removesCandidate) choice else 0
    val preview = remember(lesson, placedDigit) {
        Snapshot(
            sessionId = lesson.id, epoch = "practice", puzzle = lesson.puzzle, mode = SOLO,
            cells = lesson.puzzle.clues.mapIndexed { index, clue ->
                val value = if (placedDigit != 0 && index == lesson.target) placedDigit else clue
                val notes = if (removesCandidate) lesson.candidatesAt(index) else emptyList()
                Cell(value, notes.fold(0) { mask, digit -> mask or (1 shl (digit - 1)) })
            }
        )
    }
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        SudokuBoard(preview, selected = -1, partner = -1, size = maxWidth.coerceAtMost(boardLimit), select = null,
            boardTheme = appearance.board, localStyle = appearance, solo = true, uniformNumbers = uniformNumbers,
            proofTargets = setOf(lesson.target), proofSources = sources, proofEliminations = removals,
            announceAllCells = true)
    }
    if (!correct) {
        if (choice != 0) Text("Try another number.", color = Brick,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        for (row in lesson.options.chunked(5)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (digit in row) {
                    OutlinedButton(onClick = { choice = digit }, contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("practice_choice_$digit")
                            .semantics { contentDescription = if (removesCandidate) "Cross off $digit" else "Place $digit" }
                    ) { Text(digit.toString(), style = MaterialTheme.typography.titleMedium) }
                }
                repeat(5 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
