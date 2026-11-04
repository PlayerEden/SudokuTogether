package com.playereden.sudokutogether

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        Text(hint.explanation,color=Ink)
                        Text("Hints mark this puzzle Assisted.",color=Muted)
                        Text("Check my work compares your entries with the solution.",color=Muted)
                    }
                    else -> {
                        Text("No logical step was found. You can check your entries.",color=Ink)
                        Text("Solution checks mark this puzzle Assisted.",color=Muted)
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
