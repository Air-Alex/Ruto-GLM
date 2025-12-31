package com.rosan.installer.ext.util.graphics

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.view.Surface
import java.util.concurrent.CountDownLatch

fun Surface.pixelCopy(width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    var exception: Exception? = null

    if (!this.isValid) return bitmap

    val latch = CountDownLatch(1)
    val thread = HandlerThread("Surface PixelCopy ${bitmap.hashCode()}").also {
        it.start()
    }

    val rect = Rect(0, 0, width, height)
    PixelCopy.request(this@pixelCopy, rect, bitmap, {
        if (it != PixelCopy.SUCCESS)
//            exception = Exception("Surface(${this@pixelCopy.hashCode()}) copy failed #$it")
        latch.countDown()
    }, Handler(thread.looper))

    latch.await()
    thread.quitSafely()
    if (exception != null) throw exception

    return bitmap
}