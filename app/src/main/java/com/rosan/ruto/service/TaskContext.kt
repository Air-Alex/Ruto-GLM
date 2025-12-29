package com.rosan.ruto.service

enum class TaskStatus {
    RUNNING,
    THINKING,
    ACTION,
    COMPLETED,
    STOPPED,
    FAILED
}

data class TaskContext(
    val apiKey: String,
    val hostUrl: String,
    val modelId: String,
    val task: String,
    val displayId: Int,
    val status: TaskStatus = TaskStatus.RUNNING,
    val statusMessage: String? = null
)