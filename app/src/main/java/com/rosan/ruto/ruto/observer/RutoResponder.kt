package com.rosan.ruto.ruto.observer

import android.graphics.BitmapFactory
import android.util.Base64
import com.rosan.installer.ext.util.coroutines.takeUntil
import com.rosan.ruto.data.AppDatabase
import com.rosan.ruto.data.model.AiType
import com.rosan.ruto.data.model.ConversationModel
import com.rosan.ruto.data.model.ConversationStatus
import com.rosan.ruto.data.model.MessageModel
import com.rosan.ruto.data.model.MessageSource
import com.rosan.ruto.data.model.MessageType
import com.rosan.ruto.retrofit.RetrofitHttpClientBuilderFactory
import com.rosan.ruto.ruto.repo.RutoObserver
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.Content
import dev.langchain4j.data.message.ImageContent
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.TextContent
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.kotlin.model.chat.StreamingChatModelReply
import dev.langchain4j.kotlin.model.chat.chatFlow
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.io.File

class RutoResponder(database: AppDatabase) : RutoObserver {
    private val conversationDao = database.conversations()

    private val messageDao = database.messages()

    private var job: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onInitialize(scope: CoroutineScope) {
        job = scope.launch {
            messageDao.observeLast()
                .filterNotNull()
                .distinctUntilChanged()
                .filter { it.source == MessageSource.USER }
                .mapNotNull { message ->
                    val conversation =
                        conversationDao.get(message.conversationId) ?: return@mapNotNull null
                    val messages = messageDao.all(message.conversationId)
                    conversation to messages
                }.flatMapLatest { (conversation, messages) ->
                    processAiResponse(conversation, messages)
                }.collect()
        }
    }

    override fun onDestroy() {
        job?.cancel()
        job = null
    }

    private suspend fun processAiResponse(
        conversation: ConversationModel,
        messages: List<MessageModel>
    ): Flow<String> {
        val aiMessage = MessageModel(
            conversationId = conversation.id,
            source = MessageSource.AI,
            type = MessageType.TEXT,
            content = ""
        )
        val aiMessageId = messageDao.add(aiMessage)
        val requestModel = buildStreamingChatModel(conversation)
        val requestMessages = buildMessages(messages)

        val statusFlow = conversationDao.observeStatus(conversation.id)

        return requestModel.chatFlow {
            messages(requestMessages)
        }.mapNotNull {
            if (it is StreamingChatModelReply.PartialResponse) it.partialResponse else null
        }.filter {
            it.isNotEmpty()
        }.onStart {
            conversationDao.updateStatus(conversation.id, ConversationStatus.RUNNING)
        }.takeUntil(statusFlow.map { it != ConversationStatus.RUNNING }
        ).onEach { chunk ->
            messageDao.chunkToLast(aiMessageId, chunk)
        }.catch { cause ->
            cause.printStackTrace()
            val status = when (cause) {
                is CancellationException -> ConversationStatus.STOPPED
                else -> {
                    messageDao.chunkToLast(aiMessageId, "\n${cause.stackTraceToString()}")
                    messageDao.updateType(aiMessageId, MessageType.ERROR)
                    ConversationStatus.ERROR
                }
            }
            conversationDao.updateStatus(conversation.id, status)
        }.onCompletion { cause ->
            if (cause != null) return@onCompletion
            conversationDao.updateStatus(conversation.id, ConversationStatus.COMPLETED)
        }
    }

    private fun buildStreamingChatModel(conversation: ConversationModel): StreamingChatModel {
        return when (conversation.aiType) {
            AiType.OpenAI -> buildOpenAIStreamingChatModel(conversation)
        }
    }

    private fun buildOpenAIStreamingChatModel(conversation: ConversationModel): OpenAiStreamingChatModel {
        return OpenAiStreamingChatModel.builder().baseUrl(conversation.hostUrl)
            .modelName(conversation.modelId).apiKey(conversation.apiKey)
            .httpClientBuilder(RetrofitHttpClientBuilderFactory().create()).build()
    }

    private fun buildMessages(messages: List<MessageModel>): List<ChatMessage> {
        return messages.fold(mutableListOf()) { acc, message ->
            if (message.content.isEmpty()) return@fold acc
            when (message.source to message.type) {
                MessageSource.SYSTEM to MessageType.TEXT -> SystemMessage.from(message.content)
                MessageSource.AI to MessageType.TEXT -> AiMessage.from(message.content)
                MessageSource.USER to MessageType.TEXT -> {
                    val content = TextContent.from(message.content)
                    val last = acc.lastOrNull() as? UserMessage
                    if (last != null) {
                        mutableListOf<Content>().also {
                            it.addAll(last.contents())
                            it.add(content)
                        }
                        null
                    } else UserMessage.from(content)
                }

                MessageSource.USER to MessageType.IMAGE_URL -> {
                    val content = ImageContent.from(message.content)
                    val last = acc.lastOrNull() as? UserMessage
                    if (last != null) {
                        mutableListOf<Content>().also {
                            it.addAll(last.contents())
                            it.add(content)
                        }
                        null
                    } else UserMessage.from(content)
                }

                MessageSource.USER to MessageType.IMAGE_PATH -> {
                    val path = message.content
                    val file = File(path)
                    val bytes = file.inputStream().buffered().use { it.readBytes() }
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

                    val mimeType = options.outMimeType
                    val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val content = ImageContent(base64String, mimeType)
                    val last = acc.lastOrNull() as? UserMessage
                    if (last != null) {
                        mutableListOf<Content>().also {
                            it.addAll(last.contents())
                            it.add(content)
                        }
                        null
                    } else UserMessage.from(content)
                }

                else -> null
            }?.let {
                acc.add(it)
            }
            acc
        }
    }
}