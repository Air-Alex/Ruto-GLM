package com.rosan.ruto.ui.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.ruto.device.repo.DeviceRepo
import com.rosan.ruto.ui.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AppListUiState {
    object Loading : AppListUiState
    data class Success(val apps: List<AppItem>, val sortBy: SortBy = SortBy.NAME) : AppListUiState
    data class Error(val message: String) : AppListUiState
}

enum class SortBy {
    NAME, PACKAGE_NAME
}

class AppListViewModel(private val deviceRepo: DeviceRepo, private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<AppListUiState>(AppListUiState.Loading)
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = AppListUiState.Loading
            try {
                val pm = context.packageManager
                val apps = deviceRepo.packageManager.getInstalledApplications(
                    PackageManager.GET_META_DATA,
                    ApplicationInfo.FLAG_SYSTEM
                )

                val items = apps
                    .map { appInfo ->
                        // Launch a new coroutine for each item to process them in parallel
                        async {
                            try {
                                AppItem(
                                    appInfo = appInfo,
                                    label = appInfo.loadLabel(pm).toString(),
                                    icon = appInfo.loadIcon(pm).toBitmap()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }
                    .awaitAll() // Wait for all async tasks to complete
                    .filterNotNull() // Filter out any that failed

                _uiState.value = AppListUiState.Success(items.sortedBy { it.label })
            } catch (e: Exception) {
                _uiState.value = AppListUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun sortApps(sortBy: SortBy) {
        _uiState.update { currentState ->
            if (currentState is AppListUiState.Success) {
                val sortedApps = when (sortBy) {
                    SortBy.NAME -> currentState.apps.sortedBy { it.label }
                    SortBy.PACKAGE_NAME -> currentState.apps.sortedBy { it.appInfo.packageName }
                }
                currentState.copy(apps = sortedApps, sortBy = sortBy)
            } else {
                currentState
            }
        }
    }
}