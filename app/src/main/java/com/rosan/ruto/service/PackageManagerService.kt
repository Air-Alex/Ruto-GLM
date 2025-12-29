package com.rosan.ruto.service

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.annotation.Keep

@Keep
class PackageManagerService(private val context: Context) : IPackageManager.Stub() {

    private val packageManager by lazy {
        context.packageManager
    }

    override fun getInstalledApplications(flags: Int, appFlags: Int): List<ApplicationInfo> {
        return packageManager.getInstalledApplications(flags).filter { (it.flags and appFlags) == 0 }
    }
}