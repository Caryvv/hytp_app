package com.example.hytp.feature.tryon.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.TryonRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.TryonTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyTryonUiState(
    val tasks: List<TryonTask> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
)

/** 我的试衣历史（倒序分页）。 */
@HiltViewModel
class MyTryonViewModel @Inject constructor(
    private val tryonRepository: TryonRepository,
) : ViewModel() {

    private val pageSize = 20

    private val _uiState = MutableStateFlow(MyTryonUiState())
    val uiState: StateFlow<MyTryonUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(loading = true, error = null, page = 1, hasMore = true) }
        viewModelScope.launch {
            when (val r = tryonRepository.myTasks(1, pageSize)) {
                is ApiResult.Success -> {
                    val list = r.data.list
                    _uiState.update {
                        it.copy(loading = false, tasks = list, page = 1, hasMore = list.size >= pageSize && r.data.pagination.total > list.size)
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    /** 软删除：先从列表乐观移除，失败回滚。 */
    fun deleteTask(task: TryonTask) {
        val before = _uiState.value.tasks
        _uiState.update { it.copy(tasks = it.tasks.filter { t -> t.id != task.id }) }
        viewModelScope.launch {
            when (tryonRepository.deleteTask(task.id)) {
                is ApiResult.Success -> Unit // 已乐观移除
                else -> _uiState.update { it.copy(tasks = before, error = "删除失败，请重试") }
            }
        }
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.loading || s.loadingMore || !s.hasMore) return
        _uiState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            val next = s.page + 1
            when (val r = tryonRepository.myTasks(next, pageSize)) {
                is ApiResult.Success -> {
                    val merged = s.tasks + r.data.list
                    _uiState.update {
                        it.copy(loadingMore = false, tasks = merged, page = next, hasMore = merged.size < r.data.pagination.total && r.data.list.isNotEmpty())
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(loadingMore = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loadingMore = false, error = "网络异常，请重试") }
            }
        }
    }
}
