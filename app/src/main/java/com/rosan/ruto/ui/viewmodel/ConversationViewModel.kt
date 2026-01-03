package com.rosan.ruto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.ruto.data.dao.ConversationDao
import com.rosan.ruto.data.dao.MessageDao
import com.rosan.ruto.data.model.MessageModel
import com.rosan.ruto.data.model.conversation.ConversationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream

class ConversationViewModel(
    private val conversationId: Long,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) : ViewModel() {
    val status: StateFlow<ConversationStatus?> = conversationDao.observeStatus(conversationId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val messages: Flow<List<MessageModel>> = messageDao.observeAll(conversationId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun add(message: MessageModel) {
        viewModelScope.launch {
            conversationDao.updateStatus(
                conversationId,
                status = ConversationStatus.WAITING
            )
            messageDao.add(message)
        }
    }

    fun addImage(inputStream: InputStream) {
        viewModelScope.launch {
            conversationDao.updateStatus(conversationId, status = ConversationStatus.WAITING)
            messageDao.addImage(conversationId, inputStream)
        }
    }

    fun remove(ids: List<Long>) {
        viewModelScope.launch {
            messageDao.remove(ids)
        }
    }

    fun stop() {
        viewModelScope.launch {
            conversationDao.updateStatus(conversationId, ConversationStatus.STOPPED)
        }
    }
}
