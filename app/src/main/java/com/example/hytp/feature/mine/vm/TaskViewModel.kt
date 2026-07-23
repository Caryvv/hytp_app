package com.example.hytp.feature.mine.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.TaskRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.TaskItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val tasks: List<TaskItem> = emptyList(),
    val claimingKey: String? = null,   // 正在领取的任务 key
    val toast: String? = null,         // 领取成功提示
    val newBalanceCoin: Int? = null,   // 领取后最新余额，回传刷新「我的」
)

/**
 * 任务中心 ViewModel。加载任务列表；签到主动领取后刷新列表并回传余额。
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = taskRepository.getTasks()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(loading = false, tasks = r.data.list) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    fun claim(taskKey: String) {
        if (_uiState.value.claimingKey != null) return
        _uiState.update { it.copy(claimingKey = taskKey, error = null) }
        viewModelScope.launch {
            when (val r = taskRepository.claim(taskKey)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            claimingKey = null,
                            toast = "领取成功 +${r.data.reward} 同袍币",
                            newBalanceCoin = r.data.balanceCoin,
                        )
                    }
                    load() // 刷新完成状态
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(claimingKey = null, toast = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(claimingKey = null, toast = "网络异常，请重试") }
            }
        }
    }

    fun consumeToast() = _uiState.update { it.copy(toast = null) }
}
