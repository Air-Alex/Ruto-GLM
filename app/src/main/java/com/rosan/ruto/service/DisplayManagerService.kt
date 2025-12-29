package com.rosan.ruto.service

import android.annotation.SuppressLint
import android.companion.virtual.VirtualDeviceManager
import android.content.Context
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManagerGlobal
import android.hardware.display.VirtualDisplay
import android.os.Build
import android.view.Display
import android.view.DisplayAddress
import android.view.DisplayInfo
import android.view.Surface
import androidx.annotation.Keep
import androidx.core.content.getSystemService
import com.rosan.ruto.display.BitmapWrapper
import java.util.concurrent.ConcurrentHashMap

class DisplayManagerService @Keep constructor(private val context: Context) :
    IDisplayManager.Stub() {
    val manager = context.getSystemService<DisplayManager>()!!

    val global: DisplayManagerGlobal
        get() = DisplayManagerGlobal.getInstance()

    override fun getDisplayIds(): IntArray = global.displayIds

    override fun getDisplayInfo(displayId: Int): DisplayInfo =
        global.getDisplayInfo(displayId)

    private fun getDisplayPhysicalId(displayId: Int): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return displayId.toString()

        val address = getDisplayInfo(displayId).address as DisplayAddress.Physical

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val clazz = DisplayAddress.Physical::class.java

            @SuppressLint("BlockedPrivateApi")
            val method = clazz.getDeclaredField("mPhysicalDisplayId")
            method.isAccessible = true
            return method.get(address)?.toString().toString()
        }

        return address.physicalDisplayId.toString()
    }

    override fun capture(displayId: Int): BitmapWrapper? {
        val process =
            ProcessBuilder("screencap", "-d", getDisplayPhysicalId(displayId), "-p").start()

        val bitmap = BitmapFactory.decodeStream(process.inputStream.buffered())

        process.waitFor()

        return BitmapWrapper(bitmap)
    }

    private val displayMap = ConcurrentHashMap<Int, VirtualDisplay>()

    override fun createDisplay(surface: Surface?): Int {
        val displayInfo = getDisplayInfo(Display.DEFAULT_DISPLAY)
        val name = "ruto-display:${surface?.hashCode() ?: System.currentTimeMillis()}"
        val width = displayInfo.logicalWidth
        val height = displayInfo.logicalHeight
        val density = displayInfo.logicalDensityDpi

        val flags = 0/*DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or
                *//*DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION or*//*
                1 shl  10 or
                1 shl 11*/
        val display = manager.createVirtualDisplay(name, width, height, density, surface, flags)
        val displayId = display.display.displayId
        displayMap[displayId] = display
        return displayId
    }

    override fun release(displayId: Int) {
        displayMap.computeIfPresent(displayId) { _, display ->
            display.release()
            return@computeIfPresent null
        }
    }

    override fun setSurface(displayId: Int, surface: Surface?) {
        displayMap.computeIfPresent(displayId) { _, display ->
            display.surface = surface
            return@computeIfPresent display
        }
    }
}