package com.rosan.ruto.ui.model

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap

/**
 * A data class to hold all pre-loaded information for an application item.
 */
data class AppItem(
    val appInfo: ApplicationInfo,
    val label: String,
    val icon: Bitmap
)
