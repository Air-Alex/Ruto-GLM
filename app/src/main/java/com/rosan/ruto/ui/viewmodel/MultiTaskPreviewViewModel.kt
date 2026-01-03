package com.rosan.ruto.ui.viewmodel

import android.view.InputEvent
import android.view.Surface
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.ruto.device.repo.DeviceRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MultiTaskPreviewViewModel(
    private val displayIds: List<Int>,
    private val device: DeviceRepo
) : ViewModel() {
    private val mirroredDisplayIds = mutableMapOf<Int, Int>()

    private val _displaySizes = MutableStateFlow<Map<Int, Size>>(emptyMap())
    val displaySizes = _displaySizes.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _displaySizes.value = displayIds.associateWith {
                val displayInfo = device.displayManager.getDisplayInfo(it)
                Size(displayInfo.logicalWidth.toFloat(), displayInfo.logicalHeight.toFloat())
            }
        }
    }

    fun setSurface(displayId: Int, surface: Surface?) {
        viewModelScope.launch(Dispatchers.IO) {
            val dm = device.displayManager
            val id = mirroredDisplayIds[displayId] ?: displayId
            if (dm.isMyDisplay(id)) {
                dm.setSurface(id, surface)
            } else {
                mirroredDisplayIds[id] = dm.mirrorDisplay(id, surface)
            }
        }
    }

    private fun getTargetDisplayId(originalDisplayId: Int) =
        mirroredDisplayIds[originalDisplayId] ?: originalDisplayId

    fun clickBack(displayId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            device.inputManager.clickBack(getTargetDisplayId(displayId))
        }
    }

    fun injectEvent(displayId: Int, event: InputEvent) {
        device.inputManager.injectEvent(event, getTargetDisplayId(displayId))
    }

    fun launch(displayId: Int, packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            device.activityManager.startApp(packageName, getTargetDisplayId(displayId))
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO) {
            mirroredDisplayIds.values.forEach {
                device.displayManager.release(it)
            }
            mirroredDisplayIds.clear()
        }
    }
}