package com.idormy.sms.forwarder.database.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(
    tableName = "TaskLog",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["task_id"]),
        Index(value = ["actual_time"])
    ]
)
data class TaskLog(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") var id: Long = 0,
    @ColumnInfo(name = "task_id") var taskId: Long = 0,
    @ColumnInfo(name = "planned_time") var plannedTime: Date = Date(),
    @ColumnInfo(name = "actual_time") var actualTime: Date = Date(),
    @ColumnInfo(name = "sim_slot") var simSlot: Int = 0, // 1=SIM1, 2=SIM2，0=未知
    @ColumnInfo(name = "phone_number", defaultValue = "") var phoneNumber: String = "",
    @ColumnInfo(name = "result", defaultValue = "0") var result: Int = 0, // 1=成功, 0=失败
    @ColumnInfo(name = "reason", defaultValue = "") var reason: String = "",
): Parcelable

