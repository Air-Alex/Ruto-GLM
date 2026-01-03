package com.rosan.ruto.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.installer.ext.util.process.ProcessUtil
import com.rosan.ruto.device.repo.DeviceRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

data class HomeUiState(
    val isShizukuReady: Boolean = false,
    val isKeepAliveServiceRunning: Boolean = false,
    val shizukuVersion: String = "",
    val physicalDisplayIds: List<Int> = emptyList()
)

class HomeViewModel(private val context: Context, private val deviceRepo: DeviceRepo) :
    ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkStatus()
    }

    fun checkStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val shizukuReady = ProcessUtil.isShizukuPermissionsGranted()
            val notificationPermissionGranted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true // On older versions, permission is not needed, so we assume it's running.
                }

            val version = if (shizukuReady) {
                "Version ${Shizuku.getVersion()}"
            } else {
                "Not running"
            }

            val displayIds = if (shizukuReady) {
                deviceRepo.displayManager.displayIds.toList()
            } else {
                emptyList()
            }

            _uiState.value = HomeUiState(
                isShizukuReady = shizukuReady,
                isKeepAliveServiceRunning = notificationPermissionGranted,
                shizukuVersion = version,
                physicalDisplayIds = displayIds
            )
        }
    }
}