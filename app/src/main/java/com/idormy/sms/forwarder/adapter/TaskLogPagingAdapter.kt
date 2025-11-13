package com.idormy.sms.forwarder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.idormy.sms.forwarder.database.entity.TaskLog
import com.idormy.sms.forwarder.databinding.ItemTaskLogBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskLogPagingAdapter : PagingDataAdapter<TaskLog, TaskLogPagingAdapter.MyViewHolder>(diffCallback) {

    class MyViewHolder(val binding: ItemTaskLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemTaskLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position) ?: return
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        holder.binding.tvTaskId.text = item.taskId.toString()
        holder.binding.tvPlannedTime.text = fmt.format(item.plannedTime)
        holder.binding.tvActualTime.text = fmt.format(item.actualTime)
        holder.binding.tvSimSlot.text = item.simSlot.toString()
        holder.binding.tvPhone.text = item.phoneNumber
        holder.binding.tvResult.text = if (item.result == 1) "SUCCESS" else "FAIL"
        holder.binding.tvReason.text = item.reason
    }

    companion object {
        val diffCallback: DiffUtil.ItemCallback<TaskLog> = object : DiffUtil.ItemCallback<TaskLog>() {
            override fun areItemsTheSame(oldItem: TaskLog, newItem: TaskLog): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: TaskLog, newItem: TaskLog): Boolean = oldItem == newItem
        }
    }
}

