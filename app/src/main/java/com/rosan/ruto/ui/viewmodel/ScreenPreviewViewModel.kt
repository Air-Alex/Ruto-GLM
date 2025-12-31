package com.rosan.ruto.ui.viewmodel

import android.view.InputEvent
import android.view.Surface
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.ruto.device.repo.DeviceRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreenPreviewViewModel(
    private val displayId: Int,
    private val device: DeviceRepo
) : ViewModel() {
    private var mirroredDisplayId: Int? = null

    val displaySize by lazy {
        val displayInfo = device.displayManager.getDisplayInfo(displayId)
        Size(displayInfo.logicalWidth.toFloat(), displayInfo.logicalHeight.toFloat())
    }

    fun setSurface(surface: Surface?) {
        viewModelScope.launch(Dispatchers.IO) {
            val dm = device.displayManager
            if (surface == null) {
                mirroredDisplayId?.let {
                    dm.release(it)
                    mirroredDisplayId = null
                }
                return@launch
            }

            if (dm.isMyDisplay(displayId)) {
                dm.setSurface(displayId, surface)
            } else {
                mirroredDisplayId = dm.mirrorDisplay(displayId, surface)
            }
        }
    }

    fun clickBack() {
        viewModelScope.launch(Dispatchers.IO) {
            device.inputManager.clickBack(mirroredDisplayId ?: displayId)
        }
    }

    fun injectEvent(event: InputEvent) {
        device.inputManager.injectEvent(event, mirroredDisplayId ?: displayId)
    }

    fun launch(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            device.activityManager.startApp(packageName, mirroredDisplayId ?: displayId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO) {
            mirroredDisplayId?.let {
                device.displayManager.release(it)
                mirroredDisplayId = null
            }
        }
    }
}
