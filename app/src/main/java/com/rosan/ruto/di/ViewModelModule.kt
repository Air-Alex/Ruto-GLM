package com.rosan.ruto.di

import com.rosan.ruto.ui.viewmodel.AppListViewModel
import com.rosan.ruto.ui.viewmodel.HomeViewModel
import com.rosan.ruto.ui.viewmodel.MultiTaskPreviewViewModel
import com.rosan.ruto.ui.viewmodel.ScreenListViewModel
import com.rosan.ruto.ui.viewmodel.ScreenPreviewViewModel
import com.rosan.ruto.ui.viewmodel.TaskExecutionViewModel
import com.rosan.ruto.ui.viewmodel.TaskListViewModel
import com.rosan.ruto.viewmodel.ConversationListViewModel
import com.rosan.ruto.viewmodel.ConversationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::AppListViewModel)
    viewModelOf(::TaskExecutionViewModel)
    viewModel { HomeViewModel(get(), get()) }
    viewModelOf(::TaskListViewModel)
    viewModelOf(::ScreenListViewModel)
    viewModel { params -> ScreenPreviewViewModel(params.get(), get()) }
    viewModel { params -> MultiTaskPreviewViewModel(params.get(), get()) }
    viewModelOf(::ConversationListViewModel)
    viewModel { params -> ConversationViewModel(params.get(), get(), get()) }
}
