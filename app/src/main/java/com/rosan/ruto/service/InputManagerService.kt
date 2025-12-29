package com.rosan.ruto.service

import android.content.Context
import android.graphics.PointF
import android.os.Build
import android.os.IBinder
import android.os.ServiceManager
import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.InputEvent
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.annotation.Keep
import com.rosan.installer.ext.service.ShizukuServiceManager
import com.rosan.installer.ext.util.InputEventUtil
import kotlinx.coroutines.runBlocking
import kotlin.math.pow

class InputManagerService @Keep constructor(
    shizuku: ShizukuServiceManager,
    private val clickDuration: Long = 100,
    private val clickInterval: Long = 100,
    private val longClickDuration: Long = 1000,
    private val swipeDuration: Long = 1000
) : IInputManager.Stub() {
    companion object {
        const val INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2
    }

    private val manager by lazy<android.hardware.input.IInputManager> {
        runBlocking {
            val binder = ServiceManager.getService(Context.INPUT_SERVICE) as IBinder
            val wrapper = shizuku.binderWrapper(binder)
            android.hardware.input.IInputManager.Stub.asInterface(wrapper)
        }
    }

    override fun injectEvent(event: InputEvent?, displayId: Int) {
        event?.let { injectEvent2(event, displayId = displayId) }
    }

    private fun injectEvent2(
        ev: InputEvent,
        mode: Int = INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH,
        uid: Int = -1,
        displayId: Int = 0
    ) {
        if (displayId != 0) {
            try {
                val method =
                    InputEvent::class.java.getMethod("setDisplayId", Int::class.javaPrimitiveType)
                method.invoke(ev, displayId)
            } catch (e: Exception) {
                Log.e("InputManager", "Set displayId failed", e)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) manager.injectInputEventToTarget(
            ev,
            mode,
            uid
        )
        else manager.injectInputEvent(ev, mode)
    }

    private fun sendTouchEvent(
        x: Float,
        y: Float,
        action: Int,
        displayId: Int,
        downTime: Long = SystemClock.uptimeMillis()
    ): Long {
        val eventTime = SystemClock.uptimeMillis()
        val event = MotionEvent.obtain(
            downTime, eventTime, action, x, y, 0
        )
        event.source = InputDevice.SOURCE_TOUCHSCREEN
        try {
            injectEvent2(event, displayId = displayId)
            return downTime
        } finally {
            event.recycle()
        }
    }

    override fun click(p: PointF, displayId: Int) {
        val downTime = sendTouchEvent(p.x, p.y, MotionEvent.ACTION_DOWN, displayId)
        Thread.sleep(clickDuration)
        sendTouchEvent(p.x, p.y, MotionEvent.ACTION_UP, displayId, downTime)
    }

    override fun doubleClick(p: PointF, displayId: Int) {
        click(p, displayId)
        Thread.sleep(clickInterval)
        click(p, displayId)
    }

    override fun longClick(p: PointF, displayId: Int) {
        val downTime = sendTouchEvent(p.x, p.y, MotionEvent.ACTION_DOWN, displayId)
        Thread.sleep(longClickDuration)
        sendTouchEvent(p.x, p.y, MotionEvent.ACTION_UP, displayId, downTime)
    }

    override fun swipe(
        start: PointF, end: PointF, displayId: Int
    ) {
        // 按住
        val downTime = sendTouchEvent(start.x, start.y, MotionEvent.ACTION_DOWN, displayId)

        val t1 = swipeDuration / 11 * 9
        fun interpolator(x: Double, k: Double = 1.toDouble()): Double {
            return 3 * x.pow(2 * k) - 2 * x.pow(3 * k)
        }

        do {
            val x = (SystemClock.uptimeMillis() - downTime).toDouble() / t1
            if (x >= 1) {
                sendTouchEvent(end.x, end.y, MotionEvent.ACTION_MOVE, displayId, downTime)
                continue
            }
            val y = interpolator(x)
            val sx = start.x + (end.x - start.x) * y
            val sy = start.y + (end.y - start.y) * y
            sendTouchEvent(sx.toFloat(), sy.toFloat(), MotionEvent.ACTION_MOVE, displayId, downTime)
        } while (downTime + swipeDuration > SystemClock.uptimeMillis())

        // 抬起
        sendTouchEvent(end.x, end.y, MotionEvent.ACTION_UP, displayId, downTime)
    }

    private fun sendKeyEvent(
        keycode: Int,
        action: Int,
        displayId: Int,
        downTime: Long = SystemClock.uptimeMillis()
    ): Long {
        val eventTime = SystemClock.uptimeMillis()
        val util = InputEventUtil(downTime, eventTime, action, keycode, 0)
        util.event.source = InputDevice.SOURCE_KEYBOARD

        try {
            injectEvent2(util.event, displayId = displayId)
            return downTime
        } finally {
            util.recycle()
        }
    }

    private fun clickKey(keycode: Int, displayId: Int) {
        val downTime = sendKeyEvent(keycode, KeyEvent.ACTION_DOWN, displayId)
        Thread.sleep(clickDuration)
        sendKeyEvent(keycode, KeyEvent.ACTION_UP, displayId, downTime)
    }

    override fun clickBack(displayId: Int) {
        clickKey(KeyEvent.KEYCODE_BACK, displayId)
    }

    override fun clickHome(displayId: Int) {
        clickKey(KeyEvent.KEYCODE_HOME, displayId)
    }
}