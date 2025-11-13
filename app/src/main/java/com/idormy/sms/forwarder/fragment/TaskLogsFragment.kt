package com.idormy.sms.forwarder.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.idormy.sms.forwarder.adapter.TaskLogPagingAdapter
import com.idormy.sms.forwarder.core.BaseFragment
import com.idormy.sms.forwarder.database.viewmodel.BaseViewModelFactory
import com.idormy.sms.forwarder.database.viewmodel.TaskLogViewModel
import com.idormy.sms.forwarder.databinding.FragmentTaskLogsBinding
import com.idormy.sms.forwarder.utils.Log
import com.xuexiang.xpage.annotation.Page
import com.xuexiang.xui.widget.actionbar.TitleBar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Page(name = "任务日志")
class TaskLogsFragment : BaseFragment<FragmentTaskLogsBinding?>() {

    private val adapter = TaskLogPagingAdapter()
    private val viewModel by viewModels<TaskLogViewModel> { BaseViewModelFactory(context) }

    override fun viewBindingInflate(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): FragmentTaskLogsBinding {
        return FragmentTaskLogsBinding.inflate(inflater, container, false)
    }

    override fun initTitle(): TitleBar? {
        return super.initTitle()!!.setImmersive(false).setTitle("任务日志")
    }

    override fun initViews() {
        val virtualLayoutManager = VirtualLayoutManager(requireContext())
        binding!!.recyclerView.layoutManager = virtualLayoutManager
        binding!!.recyclerView.adapter = adapter
    }

    override fun initListeners() {
        binding!!.refreshLayout.setOnRefreshListener { refreshLayout ->
            refreshLayout.layout.postDelayed({
                lifecycleScope.launch {
                    viewModel.allLogs.collectLatest { adapter.submitData(it) }
                }
                refreshLayout.finishRefresh()
            }, 200)
        }
        binding!!.refreshLayout.autoRefresh()
    }
}

