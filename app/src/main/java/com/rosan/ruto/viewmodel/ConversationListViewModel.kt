package com.rosan.ruto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.ruto.data.dao.ConversationDao
import com.rosan.ruto.data.model.ConversationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversationListViewModel(
    private val conversationDao: ConversationDao
) : ViewModel() {
    val conversations = conversationDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun add(conversation: ConversationModel) {
        viewModelScope.launch {
            conversationDao.add(conversation)
        }
    }

    fun remove(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            conversationDao.remove(ids)
        }
    }
}
