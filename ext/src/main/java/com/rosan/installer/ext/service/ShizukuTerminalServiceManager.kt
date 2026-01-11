package com.rosan.installer.ext.service

import android.content.ComponentName
import android.content.Context
import android.os.IBinder
import com.rosan.app_process.AppProcess
import com.rosan.installer.ext.exception.ShizukuNotWorkException
import com.rosan.installer.ext.util.closeQuietly
import com.rosan.installer.ext.util.coroutines.SuspendLazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.lang.reflect.Method

class ShizukuTerminalServiceManager(private val context: Context) :
    ServiceManager,
    KoinComponent {
    /**
     * 强行调用 Shizuku.newProcess (Private Static)
     */
    fun invokeShizukuNewProcess(
        cmd: Array<String>,
        env: Array<String>?,
        dir: String?
    ): ShizukuRemoteProcess? {
        return try {
            val shizukuClass = Shizuku::class.java

            val method: Method = shizukuClass.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java, // cmd
                Array<String>::class.java, // env
                String::class.java          // dir
            )

            method.isAccessible = true

            method.invoke(null, cmd, env, dir) as ShizukuRemoteProcess?
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private val terminalLoader = SuspendLazy<AppProcess> {
        val obj = object : AppProcess.Terminal() {
            override fun newTerminal(): List<String?> = mutableListOf("sh")

            override fun innerProcess(params: ProcessParams): Process {
                val cmd = params.cmdList.toTypedArray()
                val env = params.env?.mapNotNull { (key, value) ->
                    if (value != null) "$key=$value" else null
                }?.toTypedArray()
                val dir = params.directory
                return invokeShizukuNewProcess(cmd, env, dir)!!
            }
        }
        withContext(Dispatchers.IO) {
            if (obj.init(context)) return@withContext obj
            throw ShizukuNotWorkException("Check Shizuku permissions")
        }
    }

    override suspend fun ensureConnected() {
        terminalLoader.get().init(context)
    }

    override suspend fun binderWrapper(binder: IBinder): IBinder =
        terminalLoader.get().binderWrapper(binder)

    override suspend fun serviceBinder(className: String): IBinder =
        terminalLoader.get().serviceBinder(ComponentName(context, className))

    override fun close() {
        runBlocking {
            terminalLoader.get().closeQuietly()
            terminalLoader.clear()
        }
    }
}