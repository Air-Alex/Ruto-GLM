package com.rosan.ruto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.ruto.device.repo.DeviceRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DisplayItem(
    val displayId: Int,
    val name: String?,
    val uniqueId: String?,
    val logicalWidth: Int,
    val logicalHeight: Int,
    val logicalDensityDpi: Int,
    val isMyDisplay: Boolean
)

data class ScreenListUiState(
    val displays: List<DisplayItem> = emptyList(),
    val isRefreshing: Boolean = false
)

class ScreenListViewModel(private val device: DeviceRepo) : ViewModel() {
    private val _uiState = MutableStateFlow(ScreenListUiState())
    val uiState: StateFlow<ScreenListUiState> = _uiState.asStateFlow()

    fun loadDisplays() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(1000)
            val displayItems = device.displayManager.displays.map { displayInfo ->
                DisplayItem(
                    displayId = displayInfo.displayId,
                    name = displayInfo.name,
                    uniqueId = displayInfo.uniqueId,
                    logicalWidth = displayInfo.logicalWidth,
                    logicalHeight = displayInfo.logicalHeight,
                    logicalDensityDpi = displayInfo.logicalDensityDpi,
                    isMyDisplay = device.displayManager.isMyDisplay(displayInfo.displayId)
                )
            }
            _uiState.update { it.copy(displays = displayItems, isRefreshing = false) }
        }
    }

    fun createDisplay(name: String, width: Int, height: Int, density: Int) {
        viewModelScope.launch {
            device.displayManager.createDisplay(name, width, height, density, null)
            loadDisplays()
        }
    }

    fun release(displayId: Int) {
        viewModelScope.launch {
            device.displayManager.release(displayId)
            loadDisplays()
        }
    }
}
