//package com.rosan.ruto.display.repo
//
//import android.annotation.SuppressLint
//import android.graphics.BitmapFactory
//import android.hardware.display.DisplayManagerGlobal
//import android.os.Build
//import android.os.IBinder
//import android.view.DisplayAddress
//import android.view.DisplayInfo
//import android.view.Surface
//import com.rosan.ruto.display.BitmapWrapper
//import com.rosan.ruto.service.IDisplayManager
//
//abstract class DisplayManagerRepo : IDisplayManager.Stub() {
//    val displayManagerGlobal: DisplayManagerGlobal
//        get() = DisplayManagerGlobal.getInstance()
//
//    override fun getDisplayIds(): IntArray = displayManagerGlobal.displayIds
//
//    override fun getDisplayInfo(displayId: Int): DisplayInfo =
//        displayManagerGlobal.getDisplayInfo(displayId)
//
//    private fun getDisplayPhysicalId(displayId: Int): String {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return displayId.toString()
//
//        val address = getDisplayInfo(displayId).address as DisplayAddress.Physical
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
//            val clazz = DisplayAddress.Physical::class.java
//
//            @SuppressLint("BlockedPrivateApi")
//            val method = clazz.getDeclaredField("mPhysicalDisplayId")
//            method.isAccessible = true
//            return method.get(address)?.toString().toString()
//        }
//
//        return address.physicalDisplayId.toString()
//    }
//
//    override fun capture(displayId: Int): BitmapWrapper? {
//        val process =
//            ProcessBuilder("screencap", "-d", getDisplayPhysicalId(displayId), "-p").start()
//
//        val bitmap = BitmapFactory.decodeStream(process.inputStream.buffered())
//
//        process.waitFor()
//
//        return BitmapWrapper(bitmap)
//    }
//
//    abstract override fun release(monitorId: Int)
//}