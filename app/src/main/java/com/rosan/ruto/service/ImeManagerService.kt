package com.rosan.ruto.service

import android.content.ComponentName
import android.content.Context
import android.os.ServiceManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.Keep
import com.rosan.installer.ext.service.ShizukuServiceManager
import com.rosan.installer.ext.util.IInputMethodManagerUtil
import kotlinx.coroutines.runBlocking

class ImeManagerService @Keep constructor(
    private val context: Context,
    shizuku: ShizukuServiceManager
) : IImeManager.Stub() {
    private val manager: IInputMethodManagerUtil by lazy {
        runBlocking {
            val binder = ServiceManager.getService(Context.INPUT_METHOD_SERVICE)
            val binderWrapper = shizuku.binderWrapper(binder)
            IInputMethodManagerUtil(binderWrapper)
        }
    }

    private val myImeId =
        ComponentName(context, MyInputMethodService::class.java).flattenToShortString()

    private var originalImeId: String = myImeId

    private fun getCurrentImeId(): String {
        val str =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return ComponentName.unflattenFromString(str)!!.flattenToShortString()
    }

    override fun readyInput() {
        val curImeId =
            getCurrentImeId()
        if (curImeId == myImeId) return
        originalImeId = curImeId
        Log.e("r0s", "back $originalImeId $this")

        manager.setImeEnabled(myImeId, true)
        manager.switchToTargetIme(myImeId)
    }

    override fun finishInput() {
        val curImeId = getCurrentImeId()
        Log.e("r0s", "back $originalImeId, cur $curImeId $this")
        if (curImeId == originalImeId) return

        manager.setImeEnabled(originalImeId, true)
        manager.switchToTargetIme(originalImeId)
        manager.setImeEnabled(myImeId, false)
    }

    private fun <T> requireIme(action: (MyInputMethodService) -> T): T {
        val curImeId = getCurrentImeId()
//        require(curImeId == myImeId) { "The input method must be ready first" }
//        return action.invoke(MyInputMethodService.INSTANCE!!)
        try {
            if (curImeId != myImeId) {
                readyInput()
                for (i in 0 until 150) {
                    Thread.sleep(500)
                    if (MyInputMethodService.INSTANCE != null) break
                }
            }

            return action.invoke(MyInputMethodService.INSTANCE!!)
        } finally {
            if (curImeId != myImeId) finishInput()
        }
    }

    override fun text(text: String) {
        requireIme {
            it.text(text)
        }
    }

    override fun clear() {
        requireIme { it.clear() }
    }

    override fun print(code: String) {
        requireIme {
            it.print(code)
        }
    }
}