//package com.rosan.ruto.display.impl
//
//import android.graphics.Rect
//import android.os.Build
//import android.os.IBinder
//import android.view.Surface
//import android.view.SurfaceControl
//import android.view.SurfaceControl_Hidden
//import com.rosan.ruto.display.repo.DisplayManagerRepo
//import java.util.concurrent.ConcurrentHashMap
//
//class DisplayLowerUManagerRepo : DisplayManagerRepo() {
//    private val tokenMap = ConcurrentHashMap<Int, IBinder>()
//
//    override fun monitor(displayId: Int, surface: Surface): Int {
//        val displayInfo = getDisplayInfo(displayId)
//        val name = "ruto-mirror:${surface.hashCode()}"
//
//        // https://github.com/Genymobile/scrcpy/blob/master/server/src/main/java/com/genymobile/scrcpy/video/ScreenCapture.java#L201
//        val secure =
//            Build.VERSION.SDK_INT < Build.VERSION_CODES.R || (Build.VERSION.SDK_INT == Build.VERSION_CODES.R && Build.VERSION.CODENAME.uppercase() == "S")
//
//        val token = SurfaceControl_Hidden.createDisplay(name, secure)
//        val key = token.hashCode()
//
//        val rect = Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight)
//        val layerStack = displayInfo.layerStack
//
//        SurfaceControl_Hidden.openTransaction()
//        try {
//            SurfaceControl_Hidden.setDisplaySurface(token, surface)
//            SurfaceControl_Hidden.setDisplayProjection(token, 0, rect, rect)
//            SurfaceControl_Hidden.setDisplayLayerStack(token, layerStack)
//
//            tokenMap[key] = token
//            return key
//        } finally {
//            SurfaceControl_Hidden.closeTransaction()
//        }
//    }
//
//    override fun release(monitorId: Int) {
//        tokenMap.computeIfPresent(monitorId) { _, token ->
//            SurfaceControl_Hidden.openTransaction()
//            try {
//                SurfaceControl_Hidden.destroyDisplay(token)
//                return@computeIfPresent null
//            } finally {
//                SurfaceControl_Hidden.closeTransaction()
//            }
//        }
//    }
//
//    override fun releaseAll() {
//        tokenMap.keys.forEach { release(it) }
//    }
//
//    override fun createDisplay(name: String, secure: Boolean): IBinder {
//        SurfaceControl_Hidden.openTransaction()
//        try {
//            return SurfaceControl_Hidden.createDisplay(name, secure)
//        } finally {
//            SurfaceControl_Hidden.closeTransaction()
//        }
//    }
//
//    override fun destroyDisplay(displayToken: IBinder) {
//
//        SurfaceControl_Hidden.openTransaction()
//        try {
//            SurfaceControl_Hidden.destroyDisplay(displayToken)
//        } finally {
//            SurfaceControl_Hidden.closeTransaction()
//        }
//    }
//
//    override fun setDisplaySurface(displayToken: IBinder, surface: Surface) {
//        SurfaceControl_Hidden.openTransaction()
//        try {
//            SurfaceControl_Hidden.setDisplaySurface(displayToken, surface)
//        } finally {
//            SurfaceControl_Hidden.closeTransaction()
//        }
//    }
//}