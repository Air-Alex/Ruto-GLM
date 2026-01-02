package com.rosan.ruto.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rosan.ruto.data.converter.Converters
import com.rosan.ruto.data.dao.ConversationDao
import com.rosan.ruto.data.dao.MessageDao
import com.rosan.ruto.data.model.AiTypeConverter
import com.rosan.ruto.data.model.ConversationModel
import com.rosan.ruto.data.model.ConversationStatusConverter
import com.rosan.ruto.data.model.MessageModel
import com.rosan.ruto.data.model.MessageTypeConverter

@Database(entities = [ConversationModel::class, MessageModel::class], version = 6)
@TypeConverters(
    Converters::class, 
    AiTypeConverter::class, 
    MessageTypeConverter::class, 
    ConversationStatusConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversations(): ConversationDao
    abstract fun messages(): MessageDao
}
