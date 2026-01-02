package com.rosan.ruto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "status")
    val status: ConversationStatus = ConversationStatus.COMPLETED,
    @ColumnInfo(name = "ai_type")
    val aiType: AiType,
    @ColumnInfo(name = "host_url")
    val hostUrl: String,
    @ColumnInfo(name = "model_id")
    val modelId: String,
    @ColumnInfo(name = "api_key")
    val apiKey: String,
    @ColumnInfo(name = "screen_name")
    val name: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
