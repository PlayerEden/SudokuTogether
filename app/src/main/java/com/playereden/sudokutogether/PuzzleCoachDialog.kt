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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.playereden.sudokutogether.core.SOLO
import com.playereden.sudokutogether.core.SudokuAssistance

/** Only explicit Solo/Daily assistance reaches this dialog. */
@Composable internal fun PuzzleCoachDialog(state: GameUiState, actions: GameActions) {
    if (state.role != SOLO || state.page != Page.PLAY) return
    val hint = state.hint
    val check = state.entryCheck
    if (hint == null && check == null && !state.hintUnavailable) return
    val scroll = rememberScrollState()
    val title = when (check?.kind) {
        SudokuAssistance.CheckKind.INCORRECT_ENTRY -> "Entry to review"
        SudokuAssistance.CheckKind.CONFLICT -> "Repeated number"
        SudokuAssistance.CheckKind.CORRECT_SO_FAR -> "No errors found"
        SudokuAssistance.CheckKind.NO_ENTRIES -> "No entries to check"
        null -> if (hint != null) "Your next deduction" else "No logical step"
    }
    AlertDialog(
        onDismissRequest = actions.dismissHint,
        title = { Text(title, color = Forest, fontFamily = FontFamily.Serif) },
        text = {
            Column(Modifier.verticalScroll(scroll), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when {
                    check != null -> {
                        if (check.cell in 0..80) Text("Row ${check.cell / 9 + 1}, column ${check.cell % 9 + 1}",color=Forest)
                        Text(when (check.kind) {
                            SudokuAssistance.CheckKind.INCORRECT_ENTRY -> "This entry doesn't match the puzzle's solution."
                            SudokuAssistance.CheckKind.CONFLICT -> "This number repeats in its row, column or box. Review the highlighted square."
                            SudokuAssistance.CheckKind.CORRECT_SO_FAR -> "Your entries match the puzzle's solution so far."
                            SudokuAssistance.CheckKind.NO_ENTRIES -> "Place a number first."
                        },color=Ink)
                        if (check.usesSolution) Text("This puzzle is marked Assisted.",color=Muted)
                    }
                    hint != null -> {
                        CoachProof(state, hint, scroll)
                        Text("Hints mark this puzzle Assisted.", color = Muted)
                    }
                    else -> {
                        Text("Check my work can find an earlier entry that’s blocking the puzzle.", color = Ink)
                        Text("Solution checks mark this puzzle Assisted.", color = Muted)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick=actions.dismissHint,modifier=Modifier.heightIn(min=48.dp)) { Text("Back to puzzle") }
        },
        dismissButton = {
            when {
                check?.kind == SudokuAssistance.CheckKind.INCORRECT_ENTRY ->
                    TextButton(onClick={ actions.dismissHint();actions.erase() },enabled=state.canEdit,
                        modifier=Modifier.heightIn(min=48.dp)) { Text("Erase entry") }
                check == null -> TextButton(onClick=actions.checkWork,enabled=state.canEdit,
                    modifier=Modifier.heightIn(min=48.dp)) { Text("Check my work") }
            }
        }
    )
}

@Composable private fun CoachProof(state: GameUiState, hint: SudokuAssistance.CoachHint, scroll: ScrollState) {
    val snapshot = state.snapshot ?: return
    var index by rememberSaveable(hint) { mutableIntStateOf(0) }
    var explained by rememberSaveable(hint) { mutableStateOf(false) }
    LaunchedEffect(index) { scroll.scrollTo(0) }
    val step = hint.steps[index.coerceIn(hint.steps.indices)]
    val targets = step.targetCells.toSet()
    val sources = if (explained) step.sourceCells.toSet() else emptySet()
    if (explained) {
        if (hint.steps.size > 1) {
            Text("Step ${index + 1} of ${hint.steps.size}", color = Forest)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { index-- }, enabled = index > 0,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Previous step") }
                TextButton(onClick = { index++ }, enabled = index < hint.steps.lastIndex,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Next step") }
            }
        }
        Text(step.explanation, color = Ink)
    } else {
        val cell = step.targetCells.singleOrNull()
        Text(if (cell == null) "Look at the outlined squares."
            else "Look at row ${cell / 9 + 1}, column ${cell % 9 + 1}.", color = Ink)
        OutlinedButton(onClick = { explained = true }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Show why") }
    }

    val preview = remember(snapshot, step, explained) {
        snapshot.copy(cells = snapshot.cells.mapIndexed { cellIndex, cell ->
            val candidates = if (!explained) emptyList() else step.eliminations[cellIndex]
                ?: if (step.eliminations.isNotEmpty() && cellIndex in sources) step.digits else emptyList()
            cell.copy(notes = candidates.fold(0) { notes, digit -> notes or (1 shl (digit - 1)) })
        })
    }
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        SudokuBoard(preview, selected = -1, partner = -1, size = maxWidth.coerceAtMost(280.dp), select = null,
            boardTheme = state.appearance.board, localStyle = state.appearance, solo = true,
            uniformNumbers = state.uniformNumbers, proofTargets = targets, proofSources = sources)
    }
    if (explained) {
        Text("Outlined squares are the focus. Shaded squares support the deduction.", color = Muted,
            style = MaterialTheme.typography.bodySmall)
        for ((cell, digits) in step.eliminations) {
            Text("Cross off ${digits.joinToString(" and ")} at row ${cell / 9 + 1}, column ${cell % 9 + 1}.",
                color = Ink, style = MaterialTheme.typography.bodySmall)
        }
    }
}
