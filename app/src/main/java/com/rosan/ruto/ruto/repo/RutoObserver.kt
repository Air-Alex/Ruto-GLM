package com.rosan.ruto.ruto.repo

import kotlinx.coroutines.CoroutineScope

interface RutoObserver {
    fun onInitialize(scope: CoroutineScope)

    fun onDestroy()
}