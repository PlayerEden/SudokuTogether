package com.playereden.sudokutogether

import android.os.Bundle
import android.content.ClipData
import android.content.Intent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
        val background = Color(0xFFF7F4EC).toArgb()
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(background, Forest.toArgb()), navigationBarStyle = SystemBarStyle.light(background, Forest.toArgb()))
        setContent {
            val state by model.state.collectAsStateWithLifecycle()
            val actions = remember { GameActions(model::setName, model::host, model::showJoin, model::home, model::join, model::select, model::number, model::toggleNotes, model::erase, model::undo, model::replacePartner, model::newPuzzle, model::solo, model::showProfile, model::hostRace, model::sendChat, model::chatVisible, model::chooseCosmetic) }
            DisposableEffect(state.page) {
                if (state.page == Page.PLAY) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
            }
            MaterialTheme(colorScheme = lightColorScheme(primary = Forest, onPrimary = Paper, background = Cream,
                surface = Paper, onSurface = Ink, onBackground = Ink, secondary = Gold, outline = Rule)) {
                SudokuApp(state, actions, ::shareApk)
            }
        }
    }
    override fun onStart() { super.onStart(); model.foreground(true) }
    override fun onStop() { if (!isChangingConfigurations) model.foreground(false); super.onStop() }
    private fun shareApk() { lifecycleScope.launch {
        try {
            val file = withContext(Dispatchers.IO) {
                File(cacheDir, "shared/SudokuTogether-${BuildConfig.VERSION_NAME}.apk").also { target ->
                    target.parentFile!!.mkdirs(); File(applicationInfo.sourceDir).copyTo(target, overwrite = true)
                }
            }
            val uri = FileProvider.getUriForFile(this@MainActivity, "$packageName.files", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"; putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("Sudoku Together APK", uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share Sudoku Together"))
        } catch (_: Exception) { Toast.makeText(this@MainActivity, "Couldn't prepare the APK to share. Try sharing your downloaded APK file.", Toast.LENGTH_LONG).show() }
    } }
}
