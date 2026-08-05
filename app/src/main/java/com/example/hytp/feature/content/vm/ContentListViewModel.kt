package com.example.hytp.feature.content.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.ContentRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.ContentListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentListUiState(
    val type: Int = 1, // 1文旅 2文化传承（进入时由导航参数固定）
    val items: List<ContentListItem> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
)

/**
 * 文旅/文化内容列表：按 type 拉分页列表 + 收藏乐观更新。
 * type 由导航参数传入（文旅=1 / 文化=2 共用本 VM，只是筛不同 type）。
 */
@HiltViewModel
class ContentListViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val pageSize = 20
    private val type: Int = savedStateHandle.get<String>("type")?.toIntOrNull() ?: 1

    private val _uiState = MutableStateFlow(ContentListUiState(type = type))
    val uiState: StateFlow<ContentListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(loading = true, error = null, page = 1, hasMore = true) }
        viewModelScope.launch {
            when (val r = fetch(1)) {
                is ApiResult.Success -> {
                    val list = r.data.list
                    _uiState.update {
                        it.copy(loading = false, items = list, page = 1, hasMore = list.size >= pageSize && r.data.pagination.total > list.size)
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.loading || s.loadingMore || !s.hasMore) return
        _uiState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            val next = s.page + 1
            when (val r = fetch(next)) {
                is ApiResult.Success -> {
                    val merged = s.items + r.data.list
                    _uiState.update {
                        it.copy(loadingMore = false, items = merged, page = next, hasMore = merged.size < r.data.pagination.total && r.data.list.isNotEmpty())
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(loadingMore = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loadingMore = false, error = "网络异常，请重试") }
            }
        }
    }

    /** 收藏乐观更新（失败回滚单项）。 */
    fun toggleFavorite(item: ContentListItem) {
        val target = !item.isFavorited
        updateItem(item.id) { it.copy(isFavorited = target, favoriteCount = (it.favoriteCount + if (target) 1 else -1).coerceAtLeast(0)) }
        viewModelScope.launch {
            val r = if (target) contentRepository.favorite(item.id) else contentRepository.unfavorite(item.id)
            if (r is ApiResult.Success) {
                updateItem(item.id) { it.copy(isFavorited = r.data.favorited, favoriteCount = r.data.favoriteCount) }
            } else {
                updateItem(item.id) { it.copy(isFavorited = item.isFavorited, favoriteCount = item.favoriteCount) }
            }
        }
    }

    private fun updateItem(id: Long, transform: (ContentListItem) -> ContentListItem) {
        _uiState.update { s -> s.copy(items = s.items.map { if (it.id == id) transform(it) else it }) }
    }

    private suspend fun fetch(page: Int) = contentRepository.getContents(type = type, page = page, pageSize = pageSize)
}
