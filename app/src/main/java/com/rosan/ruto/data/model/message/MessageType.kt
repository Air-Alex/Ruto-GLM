package com.rosan.ruto.data.model.message

import androidx.room.TypeConverter

enum class MessageType {
    ERROR,
    TEXT,
    IMAGE_URL,
    IMAGE_PATH
}

class MessageTypeConverter {
    @TypeConverter
    fun toMessageType(value: String) = enumValueOf<MessageType>(value)

    @TypeConverter
    fun fromMessageType(value: MessageType) = value.name
}