package com.rosan.installer.ext.service

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import com.rosan.installer.ext.process.IShizukuProcess
import com.rosan.installer.ext.process.ShizukuProcess
import com.rosan.installer.ext.util.coroutines.closeWith
import com.rosan.installer.ext.util.coroutines.closeWithException
import com.rosan.installer.ext.util.process.requireShizuku
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper

class ShizukuServiceManager : ServiceManager, KoinComponent {
    private val context by inject<Context>()

    private val lock = Mutex()
    private var _process: IShizukuProcess? = null
    private var args: Shizuku.UserServiceArgs? = null

    private val recipient = IBinder.DeathRecipient {
        synchronized(this) { _process = null }
    }

    private suspend fun getProcess(): IShizukuProcess = lock.withLock {
        _process?.takeIf { it.asBinder().isBinderAlive }
            ?: requireShizuku { createNewProcess() }.also {
                _process = it
                it.asBinder().linkToDeath(recipient, 0)
            }
    }

    private suspend fun createNewProcess(): IShizukuProcess = callbackFlow {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                closeWith(IShizukuProcess.Stub.asInterface(service))
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }
        val args = Shizuku.UserServiceArgs(ComponentName(context, ShizukuProcess::class.java))
            .processNameSuffix(connection.hashCode().toString(0xF))
            .daemon(false)
        runCatching {
            Shizuku.bindUserService(args, connection)
        }.getOrElse { closeWithException(it) }
        awaitClose { }
    }.first()

    override suspend fun ping(): Boolean =
// //        binderWrapper 不依赖 process，所以只检测权限是否可用
//        runCatching { process.isAlive }.getOrDefault(false)
        runCatching { requireShizuku {} }.isSuccess

    override suspend fun binderWrapper(binder: IBinder): IBinder = requireShizuku {
        ShizukuBinderWrapper(binder)
    }

    override suspend fun serviceBinder(className: String): IBinder =
        getProcess().serviceBinder(className).binder

    override fun close() {
        synchronized(this) {
            runCatching {
                _process?.asBinder()?.unlinkToDeath(recipient, 0)
                _process?.destroy()
                args?.let { Shizuku.unbindUserService(it, null, true) }
                _process = null
            }
        }
    }
}