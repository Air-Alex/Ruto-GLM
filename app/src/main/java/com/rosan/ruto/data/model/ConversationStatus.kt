package com.rosan.ruto.data.model

import androidx.room.TypeConverter

enum class ConversationStatus {
    STOPPED,
    RUNNING,
    COMPLETED,
    ERROR
}

class ConversationStatusConverter {
    @TypeConverter
    fun toConversationStatus(value: String) = enumValueOf<ConversationStatus>(value)

    @TypeConverter
    fun fromConversationStatus(value: ConversationStatus) = value.name
}