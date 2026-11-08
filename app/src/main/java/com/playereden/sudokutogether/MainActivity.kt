package com.playereden.sudokutogether

import android.os.Bundle
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    private val model: GameViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val background = Cream.toArgb()
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(background, Forest.toArgb()), navigationBarStyle = SystemBarStyle.light(background, Forest.toArgb()))
        val shareStandaloneApk = if (applicationInfo.splitSourceDirs.isNullOrEmpty()) ::shareApk else null
        setContent {
            val state by model.state.collectAsStateWithLifecycle()
            val progressBackup by model.progressBackup.collectAsStateWithLifecycle()
            var pendingExport by rememberSaveable { mutableStateOf<Uri?>(null) }
            var pendingRestore by rememberSaveable { mutableStateOf<Uri?>(null) }
            val saveProgress = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
                pendingExport = uri
            }
            val openProgress = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                pendingRestore = uri
            }
            LaunchedEffect(pendingExport, pendingRestore, state.busy) {
                if (state.busy) return@LaunchedEffect
                val export = pendingExport
                val restore = pendingRestore
                if (export != null) {
                    pendingExport = null
                    model.exportProgressBackup(export)
                } else if (restore != null) {
                    pendingRestore = null
                    model.previewProgressBackup(restore)
                }
            }
            val actions = remember {
                GameActions(
                    model::setName, model::host, model::showJoin, model::home, model::join,
                    model::select, model::number, model::toggleNotes, model::erase, model::undo,
                    model::replacePartner, model::newPuzzle, model::solo, model::showProfile, model::hostRace,
                    model::sendChat, model::chatVisible, model::chooseCosmetic, model::setUniformNumbers,
                    model::setTouchFeedback, model::setDifficulty, model::requestHint, model::dismissHint,
                    model::setAutoCleanNotes, model::daily, model::openDaily, model::checkWork,
                    model::setRequireRoomCode, rematch = model::rematch
                )
            }
            DisposableEffect(state.page) {
                if (state.page == Page.PLAY) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
            }
            SudokuTogetherTheme {
                SudokuApp(
                    state,
                    actions,
                    shareStandaloneApk,
                    settingsLocked = progressBackup.working || progressBackup.recoveryRequired,
                    settingsExtra = {
                        ProgressBackupSettings(
                            progressBackup,
                            enabled = !state.busy && !state.localSaveBlocked,
                            export = { saveProgress.launch("GridMates-Sudoku-progress.json") },
                            open = { openProgress.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }
                        )
                    }
                )
                ProgressBackupRestoreDialog(
                    progressBackup.preview,
                    enabled = !state.busy,
                    dismiss = model::dismissProgressBackupPreview,
                    restore = model::confirmProgressBackupRestore
                )
            }
        }
    }
    override fun onStart() { super.onStart(); model.foreground(true) }
    override fun onStop() { if (!isChangingConfigurations) model.foreground(false); super.onStop() }
    private fun shareApk() { lifecycleScope.launch {
        try {
            val file = withContext(Dispatchers.IO) {
                File(cacheDir, "shared/GridMates-Sudoku-${BuildConfig.VERSION_NAME}.apk").also { target ->
                    target.parentFile!!.mkdirs(); File(applicationInfo.sourceDir).copyTo(target, overwrite = true)
                }
            }
            val uri = FileProvider.getUriForFile(this@MainActivity, "$packageName.files", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"; putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("GridMates: Sudoku APK", uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share GridMates: Sudoku"))
        } catch (_: Exception) { Toast.makeText(this@MainActivity, "Couldn't prepare the APK to share. Try sharing your downloaded APK file.", Toast.LENGTH_LONG).show() }
    } }
}
