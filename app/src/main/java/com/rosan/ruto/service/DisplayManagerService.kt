package com.rosan.ruto.service

import android.content.Context
import android.os.Build
import androidx.annotation.Keep
import com.rosan.ruto.service.display.OtherService
import com.rosan.ruto.service.display.UService

class DisplayManagerService(
    private val context: Context,
    val proxy: IDisplayManager.Stub
) : IDisplayManager by proxy {
    @Keep
    constructor(context: Context) : this(
        context,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) UService(context)
        else OtherService(context)
    )
}