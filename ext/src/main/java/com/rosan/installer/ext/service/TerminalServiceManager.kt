package com.rosan.installer.ext.service

import android.content.ComponentName
import android.content.Context
import android.os.IBinder
import com.rosan.app_process.AppProcess
import com.rosan.installer.ext.exception.RootNotWorkException
import com.rosan.installer.ext.exception.TerminalNotWorkException
import com.rosan.installer.ext.util.closeQuietly
import com.rosan.installer.ext.util.coroutines.SuspendLazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import java.util.StringTokenizer

class TerminalServiceManager(private val context: Context, val shell: String) : ServiceManager,
    KoinComponent {
    private val terminalLoader = SuspendLazy<AppProcess> {
        val command = mutableListOf<String>()
        val st = StringTokenizer(shell)
        while (st.hasMoreTokens()) command.add(st.nextToken())

        val obj = object : AppProcess.Terminal() {
            override fun newTerminal(): List<String?> {
                return command
            }
        }
        withContext(Dispatchers.IO) {
            if (obj.init(context)) return@withContext obj
            if (command.firstOrNull() == "su") throw RootNotWorkException()
            throw TerminalNotWorkException("Terminal start failed. Check permissions or commands: $shell")
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