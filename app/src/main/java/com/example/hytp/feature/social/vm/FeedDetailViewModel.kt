package com.example.hytp.feature.social.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.SocialRepository
import com.example.hytp.core.data.UserSessionManager
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.Feed
import com.example.hytp.core.network.dto.FeedComment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedDetailUiState(
    val loading: Boolean = false,
    val feed: Feed? = null,
    val comments: List<FeedComment> = emptyList(),
    val error: String? = null,
    val submitting: Boolean = false,
    val deleted: Boolean = false,
    val message: String? = null,
)

/**
 * 动态详情：详情 + 评论 + 点赞/收藏乐观更新 + 评论 + 删除(仅作者)。
 */
@HiltViewModel
class FeedDetailViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val sessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val feedId: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(FeedDetailUiState())
    val uiState: StateFlow<FeedDetailUiState> = _uiState.asStateFlow()

    /** 当前用户是否为作者(可删除)。 */
    val isOwner: Boolean
        get() = _uiState.value.feed?.userId == sessionManager.currentUserId()

    init {
        load()
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = socialRepository.getFeedDetail(feedId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(loading = false, feed = r.data) }
                    loadComments()
                }
                is ApiResult.Error -> _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    private fun loadComments() {
        viewModelScope.launch {
            when (val r = socialRepository.getComments(feedId, page = 1, pageSize = 50)) {
                is ApiResult.Success -> _uiState.update { it.copy(comments = r.data.list) }
                else -> Unit
            }
        }
    }

    fun toggleLike() {
        val feed = _uiState.value.feed ?: return
        val target = !feed.isLiked
        _uiState.update { it.copy(feed = feed.copy(isLiked = target, likeCount = (feed.likeCount + if (target) 1 else -1).coerceAtLeast(0))) }
        viewModelScope.launch {
            val r = if (target) socialRepository.like(feed.id) else socialRepository.unlike(feed.id)
            if (r is ApiResult.Success) {
                _uiState.update { s -> s.feed?.let { s.copy(feed = it.copy(isLiked = r.data.liked, likeCount = r.data.likeCount)) } ?: s }
            } else {
                _uiState.update { it.copy(feed = feed) }
            }
        }
    }

    fun toggleFavorite() {
        val feed = _uiState.value.feed ?: return
        val target = !feed.isFavorited
        _uiState.update { it.copy(feed = feed.copy(isFavorited = target, favoriteCount = (feed.favoriteCount + if (target) 1 else -1).coerceAtLeast(0))) }
        viewModelScope.launch {
            val r = if (target) socialRepository.favorite(feed.id) else socialRepository.unfavorite(feed.id)
            if (r is ApiResult.Success) {
                _uiState.update { s -> s.feed?.let { s.copy(feed = it.copy(isFavorited = r.data.favorited, favoriteCount = r.data.favoriteCount)) } ?: s }
            } else {
                _uiState.update { it.copy(feed = feed) }
            }
        }
    }

    fun addComment(content: String) {
        if (content.isBlank()) return
        _uiState.update { it.copy(submitting = true) }
        viewModelScope.launch {
            when (val r = socialRepository.addComment(feedId, content.trim())) {
                is ApiResult.Success -> _uiState.update { s ->
                    s.copy(
                        submitting = false,
                        comments = listOf(r.data) + s.comments,
                        feed = s.feed?.copy(commentCount = s.feed.commentCount + 1),
                    )
                }
                is ApiResult.Error -> _uiState.update { it.copy(submitting = false, message = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(submitting = false, message = "网络异常，请重试") }
            }
        }
    }

    fun deleteFeed() {
        viewModelScope.launch {
            when (socialRepository.deleteFeed(feedId)) {
                is ApiResult.Success -> _uiState.update { it.copy(deleted = true) }
                else -> _uiState.update { it.copy(message = "删除失败") }
            }
        }
    }
}
