package com.rosan.installer.ext.process

import android.os.Build
import android.system.Os
import androidx.annotation.Keep
import com.android.server.display.DisplayControl
import com.rosan.app_process.NewProcess
import com.rosan.app_process.ParcelableBinder
import com.rosan.installer.ext.util.parcelable
import com.rosan.installer.ext.util.process.ProcessUtil
import dalvik.system.PathClassLoader
import kotlin.system.exitProcess

internal class ShizukuProcess @Keep constructor() :
    IShizukuProcess.Stub() {
    private val context by lazy { NewProcess.getUIDContext() }

    init {
        val classLoader = this::class.java.classLoader!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val systemServerClasspath = Os.getenv("SYSTEMSERVERCLASSPATH")
            val parentField = ClassLoader::class.java.getDeclaredField("parent")
            parentField.isAccessible = true

            val systemServerClassLoader =
                PathClassLoader(systemServerClasspath, null, classLoader.parent)
            parentField.set(classLoader, systemServerClassLoader)

            val loadMethod = Runtime::class.java.getDeclaredMethod(
                "loadLibrary0",
                Class::class.java,
                String::class.java
            )
            loadMethod.isAccessible = true
            loadMethod.invoke(Runtime.getRuntime(), DisplayControl::class.java, "android_servers")

            parentField.set(classLoader, systemServerClassLoader)
        }
        ProcessUtil.koin(context)
    }

    override fun destroy() = exitProcess(0)

    override fun isAlive(): Boolean = true

    override fun serviceBinder(className: String): ParcelableBinder {
        return ProcessUtil.binder(className, context).parcelable()
    }
}