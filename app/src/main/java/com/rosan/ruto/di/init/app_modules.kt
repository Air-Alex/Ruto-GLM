package com.rosan.ruto.di.init

import com.rosan.ruto.di.deviceModule
import com.rosan.ruto.di.reflectModule
import com.rosan.ruto.di.viewModelModule

val appModules = listOf(
    reflectModule,
    deviceModule,
    viewModelModule
)
