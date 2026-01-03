package com.rosan.ruto.data.model.message

import androidx.room.TypeConverter

enum class MessageSource {
    USER, AI, SYSTEM
}

class MessageSourceConverters {
    @TypeConverter
    fun fromMessageSource(value: MessageSource): String {
        return value.name
    }

    @TypeConverter
    fun toMessageSource(value: String): MessageSource {
        return MessageSource.valueOf(value)
    }
}