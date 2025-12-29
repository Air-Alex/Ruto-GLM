//package com.rosan.ruto.display.impl
//
//import android.annotation.SuppressLint
//import android.hardware.display.DisplayManager_Hidden
//import android.hardware.display.VirtualDisplay
//import android.hardware.display.VirtualDisplayConfig
//import android.os.Build
//import android.os.IBinder
//import android.view.Surface
//import android.view.SurfaceControl
//import android.view.SurfaceControl_UHidden
//import androidx.annotation.RequiresApi
//import com.android.server.display.DisplayControl
//import com.rosan.ruto.display.repo.DisplayManagerRepo
//import java.util.concurrent.ConcurrentHashMap
//
//
//@SuppressLint("BlockedPrivateApi")
//@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
//class DisplayUManagerRepo : DisplayManagerRepo() {
//    private val displayMap = ConcurrentHashMap<Int, VirtualDisplay>()
//
//    override fun monitor(displayId: Int, surface: Surface): Int {
//        val displayInfo = getDisplayInfo(displayId)
//        val name = "ruto-mirror:${surface.hashCode()}"
//        val width = displayInfo.logicalWidth
//        val height = displayInfo.logicalHeight
//        val virtualDisplay = DisplayManager_Hidden.createVirtualDisplay(
//            name,
//            width,
//            height,
//            displayId,
//            surface
//        )
//        val displayId = virtualDisplay!!.display.displayId
//        displayMap[displayId] = virtualDisplay
//        return displayId
//    }
//
//    override fun release(monitorId: Int) {
//        displayMap.computeIfPresent(monitorId) { _, display ->
//            display.release()
//            return@computeIfPresent null
//        }
//    }
//
//    override fun releaseAll() {
//        displayMap.keys.forEach { release(it) }
//    }
//
//    override fun createDisplay(name: String, secure: Boolean): IBinder {
//        return DisplayControl.createVirtualDisplay(name, secure)
//    }
//
//    override fun destroyDisplay(displayToken: IBinder) {
//        DisplayControl.destroyDisplay(displayToken)
//    }
//
//    override fun setDisplaySurface(displayToken: IBinder, surface: Surface) {
//        VirtualDisplayConfig.Builder("",1080,2560,222)
//            .setSurface(surface)
//            .build()
//        val control = SurfaceControl.Builder()
//            .setName("ruto-set:${surface.hashCode()}")
//            .build() as SurfaceControl_UHidden
//        control.setDisplaySurface(displayToken, surface)
//    }
//}