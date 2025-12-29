package com.rosan.ruto.ui.viewmodel

import android.content.Context
import android.util.Log
import android.view.InputEvent
import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.ruto.device.impl.ShizukuDeviceImpl
import com.rosan.ruto.service.ITaskListener
import com.rosan.ruto.service.KeepAliveService
import com.rosan.ruto.service.TaskContext
import com.rosan.ruto.service.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskListUiState(
    val tasks: Map<String, TaskContext> = emptyMap()
)

class TaskListViewModel(private val context: Context) : ViewModel() {
    val device by lazy {
        ShizukuDeviceImpl(context)
    }

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState = _uiState.asStateFlow()

    private val keepAliveService: KeepAliveService?
        get() = KeepAliveService.instance

    private val taskListeners = mutableMapOf<String, ITaskListener>()

    fun refreshTasks() {
        viewModelScope.launch {
            keepAliveService?.let { service ->
                val tasks = service.getTasks()
                _uiState.value = TaskListUiState(tasks = tasks)
                tasks.keys.forEach { taskId ->
                    bindListenerToTask(taskId)
                }
            }
        }
    }

    fun stopTask(taskId: String) {
        keepAliveService?.stopTask(taskId)
        refreshTasks()
    }

    fun setSurface(key: String, surface: Surface?) {
        Log.e("r0s", "viewModel: setSurface $surface $key")
        keepAliveService?.setSurface(key, surface)
    }

    fun onTouch(key: String, event: InputEvent) {
        keepAliveService?.onTouch(key, event)
    }

    private fun bindListenerToTask(taskId: String) {
        if (taskListeners.containsKey(taskId)) return

        val listener = object : ITaskListener.Stub() {
            override fun onThink(text: String) {
                _uiState.update { currentState ->
                    val updatedTasks = currentState.tasks.toMutableMap()
                    updatedTasks[taskId]?.let {
                        updatedTasks[taskId] =
                            it.copy(status = TaskStatus.THINKING, statusMessage = text)
                    }
                    currentState.copy(tasks = updatedTasks)
                }
            }

            override fun onError(msg: String) {
                _uiState.update { currentState ->
                    val updatedTasks = currentState.tasks.toMutableMap()
                    updatedTasks[taskId]?.let {
                        updatedTasks[taskId] =
                            it.copy(status = TaskStatus.FAILED, statusMessage = msg)
                    }
                    currentState.copy(tasks = updatedTasks)
                }
            }

            override fun onAction(action: String) {
                _uiState.update { currentState ->
                    val updatedTasks = currentState.tasks.toMutableMap()
                    updatedTasks[taskId]?.let {
                        updatedTasks[taskId] =
                            it.copy(status = TaskStatus.RUNNING, statusMessage = action)
                    }
                    currentState.copy(tasks = updatedTasks)
                }
            }

            override fun onFinish() {
                _uiState.update { currentState ->
                    val updatedTasks = currentState.tasks.toMutableMap()
                    updatedTasks.remove(taskId)
                    currentState.copy(tasks = updatedTasks)
                }
            }
        }
        keepAliveService?.bindListener(taskId, listener)
        taskListeners[taskId] = listener
    }

    override fun onCleared() {
        keepAliveService?.let { service ->
            taskListeners.forEach { (taskId, listener) ->
                service.unbindListener(taskId, listener)
            }
        }
        super.onCleared()
    }
}