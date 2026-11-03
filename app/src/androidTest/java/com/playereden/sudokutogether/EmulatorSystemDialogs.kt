package com.playereden.sudokutogether

import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry

/** Close only a known emulator system-process ANR; a game ANR must remain a failure. */
internal object EmulatorSystemDialogs {
    fun dismiss() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        repeat(20) {
            val root = automation.rootInActiveWindow ?: return
            if (root.packageName?.toString() != "android") return
            val systemAnr = listOf("System UI isn't responding", "Launcher3 isn't responding")
                .any { root.findAccessibilityNodeInfosByText(it).isNotEmpty() }
            if (!systemAnr) return
            var close = root.findAccessibilityNodeInfosByText("Close app").firstOrNull()
            while (close != null && !close.isClickable) close = close.parent
            check(close?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) { "Emulator system dialog blocks input" }
            SystemClock.sleep(150)
        }
        error("Emulator system UI did not recover")
    }
}
