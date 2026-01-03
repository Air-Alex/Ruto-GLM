package com.rosan.ruto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rosan.ruto.data.model.message.MessageSource
import com.rosan.ruto.data.model.message.MessageType

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationModel::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversation_id"])]
)
data class MessageModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "conversation_id")
    val conversationId: Long,
    @ColumnInfo(name = "source")
    val source: MessageSource,
    @ColumnInfo(name = "type")
    val type: MessageType = MessageType.TEXT,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
