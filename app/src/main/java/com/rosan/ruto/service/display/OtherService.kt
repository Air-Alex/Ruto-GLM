package com.rosan.ruto.service.display

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.IBinder
import android.view.Surface
import android.view.SurfaceControl_Hidden
import com.rosan.installer.ext.util.graphics.pixelCopy
import java.util.concurrent.ConcurrentHashMap

class OtherService(context: Context) : DisplayManagerServiceStub(context) {
    private data class DisplayContext(
        val token: IBinder,
        val width: Int,
        val height: Int,
        val surface: Surface?
    )

    private val displayContextMap = ConcurrentHashMap<Int, DisplayContext>()

    override fun onDestroy() {
        for (display in displayContextMap.values) {
            SurfaceControl_Hidden.destroyDisplay(display.token)
        }
        super.onDestroy()
    }

    override fun mirrorDisplay(displayId: Int, surface: Surface?): Int {
        val display = getDisplayInfo(displayId)
        val name = createNewName()
        val width = display.logicalWidth
        val height = display.logicalHeight
        val layerStack = display.layerStack

        val token = SurfaceControl_Hidden.createDisplay(name, false)
        val key = token.hashCode()
        val rect = Rect(0, 0, width, height)
        SurfaceControl_Hidden.openTransaction()
        try {
            val requireSurface = requireSurface(width, height, surface)

            SurfaceControl_Hidden.setDisplaySurface(token, requireSurface)
            SurfaceControl_Hidden.setDisplayProjection(token, 0, rect, rect)
            SurfaceControl_Hidden.setDisplayLayerStack(token, layerStack)
            displayContextMap[key] = DisplayContext(
                token,
                width,
                height,
                surface
            )
            return key
        } finally {
            SurfaceControl_Hidden.closeTransaction()
        }
    }

    override fun isMyDisplay(displayId: Int): Boolean {
        return displayContextMap[displayId] != null || super.isMyDisplay(displayId)
    }

    override fun captureBitmap(displayId: Int): Bitmap {
        val display = displayContextMap[displayId] ?: return super.captureBitmap(displayId)
        val width = display.width
        val height = display.height
        val surface = display.surface ?: return super.captureBitmap(displayId)
        return surface.pixelCopy(width, height)
    }

    override fun setSurface(displayId: Int, surface: Surface?) {
        val display = displayContextMap[displayId] ?: return super.setSurface(displayId, surface)
        val width = display.width
        val height = display.height
        val requireSurface = requireSurface(width, height, surface)
        SurfaceControl_Hidden.setDisplaySurface(display.token, requireSurface)
    }

    override fun release(displayId: Int) {
        val display = displayContextMap[displayId] ?: return super.release(displayId)
        SurfaceControl_Hidden.destroyDisplay(display.token)
    }
}