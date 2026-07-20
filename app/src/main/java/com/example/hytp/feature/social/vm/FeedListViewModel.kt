package com.example.hytp.feature.social.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.SocialRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.Feed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 动态流 Tab。 */
enum class FeedTab(val label: String) {
    RECOMMEND("推荐"),
    FOLLOWING("关注"),
}

data class FeedListUiState(
    val tab: FeedTab = FeedTab.RECOMMEND,
    val feeds: List<Feed> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
)

/**
 * 动态流：推荐/关注双 Tab + 分页 + 点赞/收藏乐观更新。
 */
@HiltViewModel
class FeedListViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
) : ViewModel() {

    private val pageSize = 20

    private val _uiState = MutableStateFlow(FeedListUiState())
    val uiState: StateFlow<FeedListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun selectTab(tab: FeedTab) {
        if (tab == _uiState.value.tab) return
        _uiState.update { it.copy(tab = tab) }
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(loading = true, error = null, page = 1, hasMore = true) }
        viewModelScope.launch {
            when (val r = fetch(1)) {
                is ApiResult.Success -> {
                    val list = r.data.list
                    _uiState.update {
                        it.copy(loading = false, feeds = list, page = 1, hasMore = list.size >= pageSize && r.data.pagination.total > list.size)
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
                    val merged = s.feeds + r.data.list
                    _uiState.update {
                        it.copy(loadingMore = false, feeds = merged, page = next, hasMore = merged.size < r.data.pagination.total && r.data.list.isNotEmpty())
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(loadingMore = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loadingMore = false, error = "网络异常，请重试") }
            }
        }
    }

    /** 点赞乐观更新（失败回滚单项）。 */
    fun toggleLike(feed: Feed) {
        val target = !feed.isLiked
        updateFeed(feed.id) { it.copy(isLiked = target, likeCount = (it.likeCount + if (target) 1 else -1).coerceAtLeast(0)) }
        viewModelScope.launch {
            val r = if (target) socialRepository.like(feed.id) else socialRepository.unlike(feed.id)
            if (r is ApiResult.Success) {
                updateFeed(feed.id) { it.copy(isLiked = r.data.liked, likeCount = r.data.likeCount) }
            } else {
                updateFeed(feed.id) { it.copy(isLiked = feed.isLiked, likeCount = feed.likeCount) }
            }
        }
    }

    /** 收藏乐观更新。 */
    fun toggleFavorite(feed: Feed) {
        val target = !feed.isFavorited
        updateFeed(feed.id) { it.copy(isFavorited = target, favoriteCount = (it.favoriteCount + if (target) 1 else -1).coerceAtLeast(0)) }
        viewModelScope.launch {
            val r = if (target) socialRepository.favorite(feed.id) else socialRepository.unfavorite(feed.id)
            if (r is ApiResult.Success) {
                updateFeed(feed.id) { it.copy(isFavorited = r.data.favorited, favoriteCount = r.data.favoriteCount) }
            } else {
                updateFeed(feed.id) { it.copy(isFavorited = feed.isFavorited, favoriteCount = feed.favoriteCount) }
            }
        }
    }

    private fun updateFeed(id: Long, transform: (Feed) -> Feed) {
        _uiState.update { s -> s.copy(feeds = s.feeds.map { if (it.id == id) transform(it) else it }) }
    }

    private suspend fun fetch(page: Int) = when (_uiState.value.tab) {
        FeedTab.RECOMMEND -> socialRepository.getRecommendFeeds(page, pageSize)
        FeedTab.FOLLOWING -> socialRepository.getFollowingFeeds(page, pageSize)
    }
}
