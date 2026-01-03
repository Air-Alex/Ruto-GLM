package com.rosan.ruto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.ruto.data.dao.AiDao
import com.rosan.ruto.data.model.AiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AiModelListViewModel(private val aiDao: AiDao) : ViewModel() {
    val models = aiDao.observeAll()

    fun add(model: AiModel) {
        viewModelScope.launch(Dispatchers.IO) {
            aiDao.add(model)
        }
    }

    fun remove(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            aiDao.remove(ids)
        }
    }

    fun update(model: AiModel) {
        viewModelScope.launch(Dispatchers.IO) {
            aiDao.update(model)
        }
    }
}
