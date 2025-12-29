package com.rosan.ruto.ui.viewmodel

import android.view.DisplayInfo
import android.view.Surface
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.ruto.device.repo.DeviceRepo
import com.rosan.ruto.ruto.DefaultRutoRuntime
import com.rosan.ruto.ruto.RutoGLM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

sealed interface TaskExecutionUiState {
    object Idle : TaskExecutionUiState
    data class Running(val log: String, val displayInfo: DisplayInfo? = null) : TaskExecutionUiState
    data class Success(val finalLog: String) : TaskExecutionUiState
    data class Error(val message: String) : TaskExecutionUiState
}

class TaskExecutionViewModel(
    savedStateHandle: SavedStateHandle,
    private val deviceRepo: DeviceRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow<TaskExecutionUiState>(TaskExecutionUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private var executionJob: Job? = null
    private var displayId: Int? = null

    private val packageName: String = URLDecoder.decode(
        savedStateHandle.get<String>("packageName").orEmpty(),
        StandardCharsets.UTF_8.toString()
    )
    private val apiKey: String = URLDecoder.decode(
        savedStateHandle.get<String>("apiKey").orEmpty(),
        StandardCharsets.UTF_8.toString()
    )
    private val hostUrl: String = URLDecoder.decode(
        savedStateHandle.get<String>("hostUrl").orEmpty(),
        StandardCharsets.UTF_8.toString()
    )
    private val modelId: String = URLDecoder.decode(
        savedStateHandle.get<String>("modelId").orEmpty(),
        StandardCharsets.UTF_8.toString()
    )
    private val task: String = URLDecoder.decode(
        savedStateHandle.get<String>("task").orEmpty(),
        StandardCharsets.UTF_8.toString()
    )

    private val prompt = """
你是一个智能体分析专家，可以根据操作历史和当前状态图执行一系列操作来完成任务。
你必须严格按照要求输出以下格式：
<think>选择这个操作的简短推理说明</think>
<answer>指令</answer>

操作指令集及其作用如下：
- do(action="Launch", app="xxx")
- do(action="Tap", element=[x,y])
- do(action="Type", text="xxx")
- do(action="Swipe", start=[x1,y1], end=[x2,y2])
- finish(message="xxx")
""".trimIndent()

    fun startTask(surface: Surface) {
        if (executionJob?.isActive == true) return

        executionJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = TaskExecutionUiState.Running("Creating virtual display...\n")
            try {
                val newDisplayId = deviceRepo.displayManager.createDisplay(surface)
                if (newDisplayId == -1) {
                    throw IllegalStateException("Failed to create display")
                }
                displayId = newDisplayId

                val info = deviceRepo.displayManager.getDisplayInfo(newDisplayId)
                _uiState.update {
                    (it as TaskExecutionUiState.Running).copy(
                        log = it.log + "Virtual display created (id=$newDisplayId).\n",
                        displayInfo = info
                    )
                }

                deviceRepo.activityManager.startApp(packageName, newDisplayId)
                val runtime = DefaultRutoRuntime(deviceRepo, newDisplayId)
                val glm = RutoGLM(hostUrl, modelId, apiKey, prompt, runtime, deviceRepo)

                glm.ruto(
                    text = task,
                    onStreaming = { newLog ->
                        _uiState.update {
                            if (it is TaskExecutionUiState.Running) it.copy(log = it.log + newLog) else it
                        }
                    },
                    onComplete = { finalLog ->
                        _uiState.value = TaskExecutionUiState.Success(finalLog)
                    },
                    onCapture = { null }
                )
            } catch (e: Exception) {
                _uiState.value = TaskExecutionUiState.Error(e.stackTraceToString())
            } finally {
                stopTask()
            }
        }
    }

    fun stopTask() {
        executionJob?.cancel()
        executionJob = null
        viewModelScope.launch(Dispatchers.IO) {
            displayId?.let { deviceRepo.displayManager.release(it) }
            displayId = null
        }
        if (_uiState.value is TaskExecutionUiState.Running)
            _uiState.value = TaskExecutionUiState.Idle
    }
}