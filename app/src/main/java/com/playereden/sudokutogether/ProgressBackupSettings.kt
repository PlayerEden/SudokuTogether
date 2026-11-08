package com.playereden.sudokutogether

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.util.Date

internal data class ProgressBackupUiState(
    val working: Boolean = false,
    val preview: BackupPreview? = null,
    val message: String? = null,
    val recoveryRequired: Boolean = false
)

@Composable internal fun ProgressBackupSettings(
    backup: ProgressBackupUiState,
    enabled: Boolean,
    export: () -> Unit,
    open: () -> Unit
) {
    GamePanel(Modifier.fillMaxWidth()) {
        Text("Progress", fontFamily = FontFamily.Serif, fontSize = 21.sp, color = Forest)
        Text("Save your Solo and Daily puzzles, game records, and settings to a file.", color = Muted)
        OutlinedButton(export, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text("Back up progress")
        }
        OutlinedButton(open, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text("Restore backup")
        }
        if (backup.working) Text("Working with your progress file…", color = Muted)
        backup.message?.let { Notice(it) }
    }

}

@Composable internal fun ProgressBackupRestoreDialog(
    preview: BackupPreview?,
    enabled: Boolean,
    dismiss: () -> Unit,
    restore: () -> Unit
) {
    if (preview == null) return
    val savedAt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(Date(preview.exportedAtMs))
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("Restore progress?") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Saved $savedAt")
                if (preview.hasSolo) Text("Includes a saved Solo puzzle")
                Text("Daily puzzles · ${preview.dailyCount}")
                Text("Game records · ${preview.recordCount}")
                Text("Restoring replaces the progress and settings on this device.")
            }
        },
        confirmButton = { TextButton(restore, enabled = enabled) { Text("Restore") } },
        dismissButton = { TextButton(dismiss) { Text("Cancel") } }
    )
}
