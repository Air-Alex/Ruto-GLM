package com.rosan.ruto.ruto.impl

import android.content.Context
import com.rosan.ruto.data.AppDatabase
import com.rosan.ruto.device.repo.DeviceRepo
import com.rosan.ruto.ruto.observer.RutoAiTasker
import com.rosan.ruto.ruto.observer.RutoResponder
import com.rosan.ruto.ruto.repo.RutoObserver
import kotlinx.coroutines.CoroutineScope

class RutoCoordinator(
    context: Context,
    database: AppDatabase,
    device: DeviceRepo
) : RutoObserver {
    private val tasks = listOf(
        RutoResponder(database),
        RutoAiTasker(context, database, device)
    )

    override fun onInitialize(scope: CoroutineScope) {
        for (observer in tasks) {
            observer.onInitialize(scope)
        }
    }

    override fun onDestroy() {
        for (observer in tasks) {
            observer.onDestroy()
        }
    }
}