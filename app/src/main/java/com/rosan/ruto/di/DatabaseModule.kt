package com.rosan.ruto.di

import androidx.room.Room
import com.rosan.ruto.data.AppDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            "ruto-database"
        ).build()
    }

    single { get<AppDatabase>().aiTaskDao() }
    single { get<AppDatabase>().taskDataDao() }
}
