package com.rosan.ruto.data.dao

import android.content.Context
import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.rosan.ruto.data.model.ConversationModel
import com.rosan.ruto.data.model.ConversationStatus
import com.rosan.ruto.data.model.MessageModel
import com.rosan.ruto.data.model.MessageSource
import com.rosan.ruto.data.model.MessageType
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.IOException
import java.io.InputStream

@Dao
interface ConversationDao {
    @Insert
    suspend fun add(conversation: ConversationModel): Long

    @Query("DELETE FROM conversations WHERE id IN (:ids)")
    suspend fun remove(ids: List<Long>)

    suspend fun remove(vararg ids: Long) = remove(ids.toList())

    @Query("UPDATE conversations SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(
        id: Long,
        status: ConversationStatus,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun get(id: Long): ConversationModel?

    @Query("SELECT status FROM conversations WHERE id = :id")
    fun observeStatus(id: Long): Flow<ConversationStatus?>

    @Query("SELECT * FROM conversations")
    fun observeAll(): Flow<List<ConversationModel>>
}

@Dao
abstract class MessageDao : KoinComponent {
    private val context by inject<Context>()

    private val imageCacheDir = File(context.cacheDir, "message_images").also {
        it.mkdirs()
    }

    @Insert
    abstract suspend fun add(message: MessageModel): Long

    @Transaction
    open suspend fun addImage(conversationId: Long, inputStream: InputStream): Long {
        val placeholderMessage = MessageModel(
            conversationId = conversationId,
            source = MessageSource.USER,
            type = MessageType.IMAGE_PATH,
            content = ""
        )
        val messageId = add(placeholderMessage)
        val file = File(imageCacheDir, "$messageId.png")
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        chunkToLast(messageId, file.path)
        return messageId
    }

    @Transaction
    open suspend fun addImage(conversationId: Long, uri: Uri): Long {
        return context.contentResolver.openInputStream(uri)?.buffered()?.use { inputStream ->
            addImage(conversationId, inputStream)
        } ?: throw IOException("openInputStream(Uri) failed: $uri")
    }

    @Query("DELETE FROM messages WHERE id in (:messageIds)")
    abstract suspend fun remove(messageIds: List<Long>)

    open suspend fun remove(vararg messageIds: Long) = remove(messageIds.toList())

    @Query("UPDATE messages SET content = content || :chunk WHERE id = :messageId")
    abstract suspend fun chunkToLast(messageId: Long, chunk: String)

    @Query("UPDATE messages SET type = :type WHERE id = :messageId")
    abstract suspend fun updateType(messageId: Long, type: MessageType)

    @Query("SELECT * FROM messages  ORDER BY created_at DESC LIMIT 1")
    abstract fun observeLast(): Flow<MessageModel?>

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY created_at DESC LIMIT 1")
    abstract suspend fun last(conversationId: Long): MessageModel?

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId")
    abstract fun observeAll(conversationId: Long): Flow<List<MessageModel>>

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId")
    abstract fun all(conversationId: Long): List<MessageModel>
}