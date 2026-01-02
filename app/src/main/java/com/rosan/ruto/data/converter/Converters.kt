package com.rosan.ruto.data.converter

import androidx.room.TypeConverter
import com.rosan.ruto.data.model.MessageSource

class Converters {
    @TypeConverter
    fun fromMessageSource(value: MessageSource): String {
        return value.name
    }

    @TypeConverter
    fun toMessageSource(value: String): MessageSource {
        return MessageSource.valueOf(value)
    }
}
