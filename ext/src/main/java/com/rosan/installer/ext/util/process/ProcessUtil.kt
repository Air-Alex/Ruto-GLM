package com.rosan.installer.ext.util.process

import android.content.Context
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.IInterface
import com.rosan.installer.ext.di.reflectModule
import com.rosan.installer.ext.exception.ShizukuNotWorkException
import com.rosan.installer.ext.exception.ShizukuPermissionsException
import com.rosan.installer.ext.util.coroutines.closeWith
import com.rosan.installer.ext.util.coroutines.closeWithException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import rikka.shizuku.Shizuku

object ProcessUtil {
    fun koin(context: Context) {
        startKoin {
            androidContext(context)
            modules(reflectModule)
        }
    }

    // 建立一个IBinder对象，方便跨进程调用
    fun binder(clazz: Class<*>, context: Context): IBinder {
        val contextConstructor =
            runCatching { clazz.getDeclaredConstructor(Context::class.java) }.getOrNull()
        val instance = if (contextConstructor != null) contextConstructor.newInstance(context)
        else clazz.getDeclaredConstructor().newInstance()
        return (instance as IInterface).asBinder()
    }

    fun binder(className: String, context: Context): IBinder =
        binder(Class.forName(className), context)

    fun binder(classLoader: ClassLoader, className: String, context: Context): IBinder =
        binder(classLoader.loadClass(className), context)

    private const val SHIZUKU_NOT_WORK_EXCEPTION_MESSAGE =
        "Shizuku/Sui is not installed or activated"

    private const val SHIZUKU_PERMISSIONS_EXCEPTION_MESSAGE = "Shizuku/Sui permission was denied"

    suspend fun isShizukuInstalled(): Boolean = Shizuku.pingBinder()


    suspend fun isShizukuPermissionsGranted(): Boolean {
        // 检测Shizuku是否已经安装、激活
        if (!isShizukuInstalled()) return false
        // 检测是否授权
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) return false
        return true
    }

    // 在确保拥有Shizuku权限后，执行一些的操作
    suspend fun <T> requireShizuku(action: suspend () -> T): T {
        if (!isShizukuInstalled()) throw ShizukuNotWorkException(SHIZUKU_NOT_WORK_EXCEPTION_MESSAGE)
        if (!isShizukuPermissionsGranted()) {
            // 请求权限
            requestShizukuPermissions()
            // 重新走一遍检测授权的流程
            return requireShizuku(action)
        }
        return action.invoke()
    }

    suspend fun requestShizukuPermissions() = callbackFlow {
        val requestCode = this.hashCode()
        val listener = Shizuku.OnRequestPermissionResultListener { code, result ->
            if (requestCode != code) return@OnRequestPermissionResultListener
            if (result == PackageManager.PERMISSION_GRANTED) closeWith(Unit)
            else closeWithException(
                ShizukuPermissionsException(
                    SHIZUKU_PERMISSIONS_EXCEPTION_MESSAGE
                )
            )
        }
        runCatching {
            Shizuku.addRequestPermissionResultListener(listener)
            Shizuku.requestPermission(requestCode)
        }.onFailure {
            closeWithException(ShizukuNotWorkException(SHIZUKU_NOT_WORK_EXCEPTION_MESSAGE, it))
        }
        awaitClose {
            Shizuku.removeRequestPermissionResultListener(listener)
        }
    }.first()
}

suspend fun <T> requireShizuku(action: suspend () -> T): T = ProcessUtil.requireShizuku(action)
