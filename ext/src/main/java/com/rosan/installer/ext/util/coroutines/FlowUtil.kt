package com.rosan.installer.ext.util.coroutines

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

fun <T> Flow<T>.takeUntil(stopPredicate: Flow<Boolean>): Flow<T> = flow {
    coroutineScope {
        val job = launch {
            stopPredicate.filter { it }.collect {
                this@coroutineScope.cancel()
            }
        }
        try {
            collect { emit(it) }
        } finally {
            job.cancel()
        }
    }
}