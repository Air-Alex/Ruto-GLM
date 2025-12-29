package com.rosan.ruto.ruto

import android.graphics.PointF
import android.view.DisplayInfo
import com.rosan.ruto.device.repo.DeviceRepo
import com.rosan.ruto.ruto.script.RutoRuntime

class DefaultRutoRuntime(
    private val device: DeviceRepo,
    private val displayId: Int
) : RutoRuntime() {
    init {
        registerFunction("launch") {
            launch(arg(0))
        }
        registerFunction("click") {
            click(arg(0), arg(1))
        }
        registerFunction("double_click") {
            doubleClick(arg(0), arg(1))
        }
        registerFunction("long_click") {
            longClick(arg(0), arg(1))
        }
        registerFunction("swipe") {
            swipe(arg(0), arg(1), arg(2), arg(3))
        }
        registerFunction("back") {
            device.inputManager.clickBack(displayId)
        }
        registerFunction("home") {
            device.inputManager.clickHome(displayId)
        }

        registerFunction("text") {
            device.imeManager.text(args.joinToString(""))
        }
        registerFunction("type") {
            device.imeManager.print(args.joinToString(""))
        }
        registerFunction("clear") {
            device.imeManager.clear()
        }
        registerFunction("wait") {
            Thread.sleep(arg<Long>(0) * 1000)
        }
    }

    val displayManager by lazy {
        device.displayManager
    }

    val displayInfo: DisplayInfo
        get() = displayManager.getDisplayInfo(displayId)

    private fun launch(name: String) {
        device.activityManager.startLabel(name, displayId)
    }

    private fun click(xt: Float, yt: Float) {
        val x = xt * displayInfo.logicalWidth / 1000
        val y = yt * displayInfo.logicalHeight / 1000
        device.inputManager.click(PointF(x, y), displayId)
    }

    private fun doubleClick(xt: Float, yt: Float) {
        val x = xt * displayInfo.logicalWidth / 1000
        val y = yt * displayInfo.logicalHeight / 1000
        device.inputManager.doubleClick(PointF(x, y), displayId)
    }

    private fun longClick(xt: Float, yt: Float) {
        val x = xt * displayInfo.logicalWidth / 1000
        val y = yt * displayInfo.logicalHeight / 1000
        device.inputManager.longClick(PointF(x, y), displayId)
    }

    private fun swipe(x1t: Float, y1t: Float, x2t: Float, y2t: Float) {
        val x1 = x1t * displayInfo.logicalWidth / 1000
        val y1 = y1t * displayInfo.logicalHeight / 1000
        val x2 = x2t * displayInfo.logicalWidth / 1000
        val y2 = y2t * displayInfo.logicalHeight / 1000
        device.inputManager.swipe(PointF(x1, y1), PointF(x2, y2), displayId)
    }
}