package com.idormy.sms.forwarder.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.idormy.sms.forwarder.database.entity.TaskLog

@Dao
interface TaskLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(log: TaskLog): Long

    @Query("DELETE FROM TaskLog WHERE id = :id")
    fun delete(id: Long)

    @Query("DELETE FROM TaskLog")
    fun deleteAll()

    @Query("SELECT * FROM TaskLog ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, TaskLog>

}

