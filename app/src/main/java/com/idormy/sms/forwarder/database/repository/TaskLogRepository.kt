package com.idormy.sms.forwarder.database.repository

import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.idormy.sms.forwarder.database.dao.TaskLogDao
import com.idormy.sms.forwarder.database.entity.TaskLog
import kotlinx.coroutines.flow.Flow

class TaskLogRepository(private val dao: TaskLogDao) {

    @WorkerThread
    suspend fun insert(log: TaskLog): Long = dao.insert(log)

    fun delete(id: Long) = dao.delete(id)

    fun deleteAll() = dao.deleteAll()

    val allLogs: Flow<PagingData<TaskLog>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false,
            initialLoadSize = 10
        )
    ) { dao.pagingSource() }.flow
}

