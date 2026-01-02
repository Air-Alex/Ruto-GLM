package com.rosan.ruto.di.init

import com.rosan.ruto.di.databaseModule
import com.rosan.ruto.di.deviceModule
import com.rosan.ruto.di.reflectModule
import com.rosan.ruto.di.rutoModule
import com.rosan.ruto.di.viewModelModule

val appModules = listOf(
    rutoModule,
    databaseModule,
    reflectModule,
    deviceModule,
    viewModelModule
)
