package com.playereden.sudokutogether

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModelProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class ProgressBackupSettingsUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun settingsOffersBothPortableProgressActions() {
        openSettings()
        compose.onNodeWithText("Back up progress").performScrollTo().assertIsDisplayed().assertIsEnabled()
        compose.onNodeWithText("Restore backup").performScrollTo().assertIsDisplayed().assertIsEnabled()
    }

    @Test fun previewCanBeCancelledAndConfirmationKeepsTheDestinationIdentity() {
        openSettings()
        val model = compose.runOnUiThread { ViewModelProvider(compose.activity)[GameViewModel::class.java] }
        val before = model.state.value
        val saved = File.createTempFile("progress-original-", ".json", compose.activity.cacheDir)
        val incoming = File.createTempFile("progress-incoming-", ".json", compose.activity.cacheDir)
        try {
            compose.runOnUiThread { model.exportProgressBackup(Uri.fromFile(saved)) }
            awaitMessage(model, "Progress backup saved.")
            incoming.writeBytes(
                ProgressBackup.encode(
                    before.profile.copy(playerId = "another-installation"),
                    BackupSettings(name = "Transferred player", difficulty = "HARD"),
                    BuildConfig.VERSION_NAME
                )
            )
            compose.runOnUiThread { model.previewProgressBackup(Uri.fromFile(incoming)) }
            awaitPreview(model)
            compose.onNodeWithText("Restore progress?").assertIsDisplayed()
            compose.onNodeWithText("Cancel").performClick()
            assertEquals(before.profile, model.state.value.profile)
            assertEquals(before.name, model.state.value.name)

            compose.runOnUiThread { model.previewProgressBackup(Uri.fromFile(incoming)) }
            awaitPreview(model)
            compose.onNodeWithText("Restore").assertIsDisplayed().performClick()
            awaitMessage(model, "Progress restored.")
            assertEquals(before.profile, model.state.value.profile)
            assertEquals("Transferred player", model.state.value.name)
            assertEquals("HARD", model.state.value.selectedDifficulty)
        } finally {
            if (saved.length() > 0L && !model.state.value.busy) {
                compose.runOnUiThread { model.previewProgressBackup(Uri.fromFile(saved)) }
                awaitPreview(model)
                compose.runOnUiThread { model.confirmProgressBackupRestore() }
                awaitMessage(model, "Progress restored.")
            }
            saved.delete()
            incoming.delete()
        }
    }

    private fun openSettings() {
        compose.waitUntil(30000) {
            compose.onAllNodes(hasTestTag("nav_settings") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
        }
        EmulatorSystemDialogs.dismiss()
        compose.onNodeWithTag("nav_settings").assertIsEnabled().performClick()
    }

    private fun awaitMessage(model: GameViewModel, message: String) {
        compose.waitUntil(30000) { model.progressBackup.value.message == message && !model.state.value.busy }
    }

    private fun awaitPreview(model: GameViewModel) {
        compose.waitUntil(30000) { model.progressBackup.value.preview != null && !model.state.value.busy }
    }
}
