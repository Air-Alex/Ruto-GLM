package com.rosan.ruto.data.model

import androidx.room.TypeConverter

class AiTypeConverter {
    @TypeConverter
    fun toAiType(value: String) = enumValueOf<AiType>(value)

    @TypeConverter
    fun fromAiType(value: AiType) = value.name
}