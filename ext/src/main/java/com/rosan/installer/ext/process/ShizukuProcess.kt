package com.rosan.installer.ext.process

import android.annotation.SuppressLint
import android.content.ComponentName
import android.os.Build
import android.system.Os
import androidx.annotation.Keep
import com.android.server.display.DisplayControl
import com.rosan.app_process.NewProcess
import com.rosan.app_process.ParcelableBinder
import com.rosan.app_process.ProcessManager
import com.rosan.installer.ext.util.process.ProcessUtil
import dalvik.system.PathClassLoader
import java.io.File

internal class ShizukuProcess @Keep constructor() :
    IShizukuProcess.Stub() {
    init {
        val classLoader = this::class.java.classLoader!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val systemServerClasspath = Os.getenv("SYSTEMSERVERCLASSPATH")

            @SuppressLint("DiscouragedPrivateApi")
            val parentField = ClassLoader::class.java.getDeclaredField("parent")
            parentField.isAccessible = true

            val systemServerClassLoader =
                PathClassLoader(systemServerClasspath, null, classLoader.parent)
            parentField.set(classLoader, systemServerClassLoader)

            @SuppressLint("BlockedPrivateApi")
            val loadMethod = Runtime::class.java.getDeclaredMethod(
                "loadLibrary0",
                Class::class.java,
                String::class.java
            )
            loadMethod.isAccessible = true
            loadMethod.invoke(Runtime.getRuntime(), DisplayControl::class.java, "android_servers")

            parentField.set(classLoader, systemServerClassLoader)
        }
    }

    private val context = NewProcess.getUIDContext().also {
        ProcessUtil.koin(it)
    }

    private val manager = ProcessManager()

    override fun destroy() {
        manager.exit(0)
    }

    override fun isAlive(): Boolean = true

    override fun serviceBinder(className: String): ParcelableBinder {
        return manager.serviceBinder(
            context,
            this::class.java.classLoader,
            ComponentName("package", className)
        )
    }
}