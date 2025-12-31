package com.rosan.ruto.service.display

import android.content.Context
import android.hardware.display.DisplayManager_Hidden
import android.os.Build
import android.view.Surface
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class UService(context: Context) : DisplayManagerServiceStub(context) {
    override fun mirrorDisplay(displayId: Int, surface: Surface?): Int {
        val display = getDisplayInfo(displayId)
        val name = createNewName()
        val width = display.logicalWidth
        val height = display.logicalHeight
        val density = display.logicalDensityDpi
        val requireSurface = requireSurface(width, height, surface)

        @Suppress("CAST_NEVER_SUCCEEDS")
        val mirrorDisplay =
            DisplayManager_Hidden.createVirtualDisplay(
                name,
                width,
                height,
                displayId,
                requireSurface
            )
        val mirrorDisplayId = mirrorDisplay!!.display.displayId
        displayMap[mirrorDisplayId] = mirrorDisplay
        return mirrorDisplayId
    }
}