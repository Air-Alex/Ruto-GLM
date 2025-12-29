package com.rosan.ruto.di

import com.rosan.ruto.device.impl.ShizukuDeviceImpl
import com.rosan.ruto.device.repo.DeviceRepo
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val deviceModule = module {
    singleOf(::ShizukuDeviceImpl) {
        bind<DeviceRepo>()
        onClose { it?.close() }
    }
}
