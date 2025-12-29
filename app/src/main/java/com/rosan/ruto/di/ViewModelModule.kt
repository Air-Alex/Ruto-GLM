package com.rosan.ruto.di

import com.rosan.ruto.ui.viewmodel.AppListViewModel
import com.rosan.ruto.ui.viewmodel.HomeViewModel
import com.rosan.ruto.ui.viewmodel.TaskExecutionViewModel
import com.rosan.ruto.ui.viewmodel.TaskListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::AppListViewModel)
    viewModelOf(::TaskExecutionViewModel)
    viewModel { HomeViewModel(get(), get()) }
    viewModelOf(::TaskListViewModel)
}