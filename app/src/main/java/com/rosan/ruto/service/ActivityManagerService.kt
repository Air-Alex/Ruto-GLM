package com.rosan.ruto.service

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import androidx.annotation.Keep
import androidx.core.content.getSystemService
import com.rosan.installer.ext.util.pendingActivity

class ActivityManagerService @Keep constructor(private val context: Context) :
    IActivityManager.Stub() {
    // package manager func
    private val packageManager by lazy {
        context.packageManager
    }

    override fun startLabel(label: String, displayId: Int) {
        val apps = packageManager.getInstalledApplications(0)
        val app = apps.find {
            it.loadLabel(packageManager).contains(label, ignoreCase = true)
        } ?: return
        startApp(app.packageName, displayId)
    }

    override fun startApp(packageName: String, displayId: Int) {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        startActivity(intent, displayId)
    }

    override fun startActivity(intent: Intent, displayId: Int) {


        val options = ActivityOptions.makeBasic()
        options.launchDisplayId = displayId
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)

        intent.pendingActivity(
            context.createDisplayContext(context.getSystemService<DisplayManager>()!!.getDisplay(displayId)),
            requestCode = intent.hashCode(),
            options = options.toBundle()
        ).send()
    }
}