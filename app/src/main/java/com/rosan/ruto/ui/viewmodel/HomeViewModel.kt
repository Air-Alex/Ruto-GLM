package com.rosan.ruto.ui.viewmodel

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.installer.ext.util.process.ProcessUtil
import com.rosan.ruto.device.repo.DeviceRepo
import com.rosan.ruto.service.ITaskListener
import com.rosan.ruto.service.KeepAliveService
import com.rosan.ruto.service.KeepAliveService.LocalBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import java.util.UUID

data class HomeUiState(
    val isShizukuReady: Boolean = false,
    val isKeepAliveServiceRunning: Boolean = false,
    val shizukuVersion: String = "",
    val physicalDisplayIds: List<Int> = emptyList()
)

class HomeViewModel(private val context: Context, private val deviceRepo: DeviceRepo) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private var keepAliveService: KeepAliveService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocalBinder
            keepAliveService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            keepAliveService = null
        }
    }

    init {
        checkStatus()
        Intent(context, KeepAliveService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCleared() {
        context.unbindService(connection)
        super.onCleared()
    }

    fun executeTask(
        packageName: String,
        apiKey: String,
        hostUrl: String,
        modelId: String,
        task: String,
        displayId: Int,
        listener: ITaskListener? = null
    ) {
        keepAliveService?.executeTask(
            UUID.randomUUID().toString(),
            packageName,
            apiKey,
            hostUrl,
            modelId,
            task,
            displayId,
            listener
        )
    }

    fun checkStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val shizukuReady = ProcessUtil.isShizukuPermissionsGranted()
            val notificationPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
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