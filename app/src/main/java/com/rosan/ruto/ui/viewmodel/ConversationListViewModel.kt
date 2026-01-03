package com.rosan.ruto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.ruto.data.dao.AiDao
import com.rosan.ruto.data.dao.ConversationDao
import com.rosan.ruto.data.model.ConversationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversationListViewModel(
    private val aiDao: AiDao,
    private val conversationDao: ConversationDao
) : ViewModel() {
    val aiModels = aiDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
