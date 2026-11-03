package com.playereden.sudokutogether

import android.os.SystemClock
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

internal fun formatElapsed(milliseconds: Long): String {
    val seconds = (milliseconds.coerceAtLeast(0) / 1000)
    return "%02d:%02d:%02d".format(seconds / 3600, (seconds / 60) % 60, seconds % 60)
}

/** Only this small label recomposes each second; the board/profile state stays still. */
@Composable internal fun SoloTimer(elapsedMs: Long, startedAt: Long?) {
    var now by remember(startedAt) { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(startedAt) {
        if (startedAt != null) while (isActive) {
            now = SystemClock.elapsedRealtime()
            delay(1000)
        }
    }
    val displayed = elapsedMs + if (startedAt == null) 0 else (now - startedAt).coerceAtLeast(0)
    Text("Time  " + formatElapsed(displayed), color = Forest, fontSize = 10.sp)
}
