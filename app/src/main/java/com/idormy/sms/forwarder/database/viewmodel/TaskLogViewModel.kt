package com.idormy.sms.forwarder.database.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.idormy.sms.forwarder.database.entity.TaskLog
import com.idormy.sms.forwarder.database.repository.TaskLogRepository
import kotlinx.coroutines.flow.Flow

class TaskLogViewModel(private val repo: TaskLogRepository) : ViewModel() {
    val allLogs: Flow<PagingData<TaskLog>> = repo.allLogs.cachedIn(viewModelScope)
}

