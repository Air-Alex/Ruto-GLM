package com.rosan.ruto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ai_tasks")
data class AITask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "ai_type")
    val aiType: String,
    @ColumnInfo(name = "host_url")
    val hostUrl: String,
    @ColumnInfo(name = "model_id")
    val modelId: String,
    @ColumnInfo(name = "api_key")
    val apiKey: String,
    @ColumnInfo(name = "screen_name")
    val screenName: String,
    @ColumnInfo(name = "task_name")
    val taskName: String
)

@Entity(
    tableName = "task_data",
    foreignKeys = [
        ForeignKey(
            entity = AITask::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["task_id"])]
)
data class TaskData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "task_id")
    val taskId: Long,
    @ColumnInfo(name = "ai_reply")
    val aiReply: String?,
    @ColumnInfo(name = "user_image_uri")
    val userImageUri: String?,
    @ColumnInfo(name = "user_text")
    val userText: String?
)
