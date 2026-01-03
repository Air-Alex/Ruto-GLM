package com.rosan.ruto.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rosan.ruto.data.model.ai_model.AiCapability
import com.rosan.ruto.data.model.ai_model.AiType

@Entity(tableName = "ai_models")
data class AiModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val baseUrl: String,
    val modelId: String,
    val apiKey: String,
    val type: AiType,
    val capabilities: List<AiCapability>
)
