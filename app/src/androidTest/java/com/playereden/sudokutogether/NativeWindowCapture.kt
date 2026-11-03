package com.playereden.sudokutogether

import android.app.Activity
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.ViewTreeObserver
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/** Capture the next real window draw, without Espresso's next-frame idle loop. */
internal object NativeWindowCapture {
    fun save(activity: Activity, target: File) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val ready = CountDownLatch(1)
        val requested = AtomicBoolean(false)
        val status = AtomicInteger(-1)
        lateinit var bitmap: Bitmap
        instrumentation.runOnMainSync {
            val view = activity.window.decorView
            bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val listener = object : ViewTreeObserver.OnDrawListener {
                override fun onDraw() {
                    if (!requested.compareAndSet(false, true)) return
                    view.post {
                        view.viewTreeObserver.removeOnDrawListener(this)
                        PixelCopy.request(activity.window, bitmap, { code ->
                            status.set(code); ready.countDown()
                        }, Handler(Looper.getMainLooper()))
                    }
                }
            }
            view.viewTreeObserver.addOnDrawListener(listener)
            view.invalidate()
        }
        check(ready.await(15, TimeUnit.SECONDS)) { "Native window draw/copy did not finish in 15 seconds" }
        check(status.get() == PixelCopy.SUCCESS) { "Native PixelCopy failed: ${status.get()}" }
        target.parentFile!!.mkdirs()
        target.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()
    }
}
