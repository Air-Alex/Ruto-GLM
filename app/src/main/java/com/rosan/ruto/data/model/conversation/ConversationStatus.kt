package com.rosan.ruto.data.model.conversation

import androidx.room.TypeConverter

enum class ConversationStatus {
    WAITING,
    RUNNING,
    COMPLETED,
    STOPPED,
    ERROR
}

class ConversationStatusConverter {
    @TypeConverter
    fun toConversationStatus(value: String) = enumValueOf<ConversationStatus>(value)

    @TypeConverter
    fun fromConversationStatus(value: ConversationStatus) = value.name
}