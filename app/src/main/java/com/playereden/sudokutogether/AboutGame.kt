package com.playereden.sudokutogether

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable internal fun AboutGame(onDismiss: () -> Unit, share: (() -> Unit)?) {
    AlertDialog(
        modifier = Modifier.testTag("about_gridmates"),
        onDismissRequest = onDismiss,
        title = { Text("About GridMates") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GridMatesMark(Modifier.size(48.dp))
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                }
                Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()
                Text("Progress is stored on this phone. Uninstalling or clearing app data can erase it.")
                if (share != null) {
                    OutlinedButton(share, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        GameIcon(GameSymbol.SHARE, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Share APK")
                    }
                }
            }
        },
        confirmButton = { TextButton(onDismiss) { Text("Done") } }
    )
}
