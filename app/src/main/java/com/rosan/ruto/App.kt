package com.rosan.ruto

import android.app.Application
import com.rosan.ruto.di.init.appModules
import com.rosan.ruto.service.KeepAliveService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import rikka.sui.Sui

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            // Koin Android Logger
            androidLogger()
            // Koin Android Context
            androidContext(this@App)
            // use modules
            modules(appModules)
        }
        Sui.init(packageName)

        // Start KeepAliveService
        KeepAliveService.start(this)
    }
}