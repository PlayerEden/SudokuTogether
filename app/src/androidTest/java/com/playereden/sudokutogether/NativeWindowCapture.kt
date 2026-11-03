package com.playereden.sudokutogether

import android.app.Activity
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.view.Window
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/** Copy a completed frame; an onDraw callback can still expose the previous buffer. */
internal object NativeWindowCapture {
    fun save(activity: Activity, target: File) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val ready = CountDownLatch(1)
        val requested = AtomicBoolean(false)
        val status = AtomicInteger(-1)
        val worker = HandlerThread("NativeFrameCapture").apply { start() }
        val handler = Handler(worker.looper)
        lateinit var bitmap: Bitmap
        lateinit var listener: Window.OnFrameMetricsAvailableListener
        instrumentation.waitForIdleSync()
        instrumentation.runOnMainSync {
            val view = activity.window.decorView
            bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            listener = Window.OnFrameMetricsAvailableListener { _, _, _ ->
                if (requested.compareAndSet(false, true)) {
                    PixelCopy.request(activity.window, bitmap, { code ->
                        status.set(code); ready.countDown()
                    }, handler)
                }
            }
            activity.window.addOnFrameMetricsAvailableListener(listener, handler)
            view.invalidate()
        }
        try {
            check(ready.await(15, TimeUnit.SECONDS)) { "Native frame completion/copy did not finish in 15 seconds" }
            check(status.get() == PixelCopy.SUCCESS) { "Native PixelCopy failed: ${status.get()}" }
            target.parentFile!!.mkdirs()
            target.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        } finally {
            instrumentation.runOnMainSync { activity.window.removeOnFrameMetricsAvailableListener(listener) }
            worker.quitSafely()
            bitmap.recycle()
        }
    }
}
