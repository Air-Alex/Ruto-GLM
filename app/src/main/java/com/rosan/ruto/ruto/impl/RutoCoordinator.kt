package com.rosan.ruto.ruto.impl

import com.rosan.ruto.data.AppDatabase
import com.rosan.ruto.ruto.observer.RutoResponder
import com.rosan.ruto.ruto.repo.RutoObserver
import kotlinx.coroutines.CoroutineScope

class RutoCoordinator(private val database: AppDatabase) : RutoObserver {
    private val tasks = listOf<RutoObserver>(
        RutoResponder(database)
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