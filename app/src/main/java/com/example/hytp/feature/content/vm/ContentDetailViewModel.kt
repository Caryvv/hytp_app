package com.example.hytp.feature.content.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.ContentRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.ContentDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentDetailUiState(
    val loading: Boolean = false,
    val content: ContentDetail? = null,
    val error: String? = null,
    val submitting: Boolean = false,
    val message: String? = null,
)

/**
 * 内容详情：详情 + 点赞/收藏乐观更新 + 报名/取消报名。
 */
@HiltViewModel
class ContentDetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val contentId: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(ContentDetailUiState())
    val uiState: StateFlow<ContentDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = contentRepository.getContentDetail(contentId)) {
                is ApiResult.Success -> _uiState.update { it.copy(loading = false, content = r.data) }
                is ApiResult.Error -> _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    fun toggleLike() {
        val c = _uiState.value.content ?: return
        val target = !c.isLiked
        _uiState.update { it.copy(content = c.copy(isLiked = target, likeCount = (c.likeCount + if (target) 1 else -1).coerceAtLeast(0))) }
        viewModelScope.launch {
            val r = if (target) contentRepository.like(c.id) else contentRepository.unlike(c.id)
            if (r is ApiResult.Success) {
                _uiState.update { s -> s.content?.let { s.copy(content = it.copy(isLiked = r.data.liked, likeCount = r.data.likeCount)) } ?: s }
            } else {
                _uiState.update { it.copy(content = c) }
            }
        }
    }

    fun toggleFavorite() {
        val c = _uiState.value.content ?: return
        val target = !c.isFavorited
        _uiState.update { it.copy(content = c.copy(isFavorited = target, favoriteCount = (c.favoriteCount + if (target) 1 else -1).coerceAtLeast(0))) }
        viewModelScope.launch {
            val r = if (target) contentRepository.favorite(c.id) else contentRepository.unfavorite(c.id)
            if (r is ApiResult.Success) {
                _uiState.update { s -> s.content?.let { s.copy(content = it.copy(isFavorited = r.data.favorited, favoriteCount = r.data.favoriteCount)) } ?: s }
            } else {
                _uiState.update { it.copy(content = c) }
            }
        }
    }

    /** 报名预约（姓名/手机必填，防重复提交）。 */
    fun signup(name: String, phone: String, quantity: Int) {
        val c = _uiState.value.content ?: return
        if (_uiState.value.submitting) return
        if (name.isBlank() || phone.isBlank()) {
            _uiState.update { it.copy(message = "请填写姓名和手机号") }
            return
        }
        _uiState.update { it.copy(submitting = true) }
        viewModelScope.launch {
            when (val r = contentRepository.signup(c.id, name.trim(), phone.trim(), quantity)) {
                is ApiResult.Success -> _uiState.update { s ->
                    s.copy(
                        submitting = false,
                        message = "报名成功",
                        content = s.content?.copy(isSignedUp = r.data.enrolled, signupCount = r.data.signupCount),
                    )
                }
                is ApiResult.Error -> _uiState.update { it.copy(submitting = false, message = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(submitting = false, message = "网络异常，请重试") }
            }
        }
    }

    /** 取消报名。 */
    fun cancelSignup() {
        val c = _uiState.value.content ?: return
        if (_uiState.value.submitting) return
        _uiState.update { it.copy(submitting = true) }
        viewModelScope.launch {
            when (val r = contentRepository.cancelSignup(c.id)) {
                is ApiResult.Success -> _uiState.update { s ->
                    s.copy(
                        submitting = false,
                        message = "已取消报名",
                        content = s.content?.copy(isSignedUp = r.data.enrolled, signupCount = r.data.signupCount),
                    )
                }
                is ApiResult.Error -> _uiState.update { it.copy(submitting = false, message = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(submitting = false, message = "网络异常，请重试") }
            }
        }
    }
}
