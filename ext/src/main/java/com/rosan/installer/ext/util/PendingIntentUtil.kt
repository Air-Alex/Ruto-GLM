package com.rosan.installer.ext.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle

private val defaultFlags =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    else PendingIntent.FLAG_UPDATE_CURRENT

fun Intent.pendingBroadcast(
    context: Context,
    requestCode: Int,
    flags: Int = defaultFlags
): PendingIntent = PendingIntent.getBroadcast(context, requestCode, this, flags)

fun Intent.pendingActivity(
    context: Context,
    requestCode: Int,
    flags: Int = defaultFlags,
    options: Bundle? = null
): PendingIntent = PendingIntent.getActivity(context, requestCode, this, flags, options)

fun Intent.pendingService(
    context: Context,
    requestCode: Int,
    flags: Int = defaultFlags
): PendingIntent = PendingIntent.getService(context, requestCode, this, flags)