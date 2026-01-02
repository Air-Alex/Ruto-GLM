package com.rosan.ruto.di

import com.rosan.ruto.ruto.impl.RutoCoordinator
import com.rosan.ruto.ruto.repo.RutoObserver
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val rutoModule = module {
    singleOf(::RutoCoordinator) bind RutoObserver::class
}