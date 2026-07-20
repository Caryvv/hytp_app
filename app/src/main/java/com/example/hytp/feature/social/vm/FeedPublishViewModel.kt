package com.example.hytp.feature.social.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.SocialRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.PublishFeedRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedPublishUiState(
    val submitting: Boolean = false,
    val error: String? = null,
    val publishedId: Long? = null,
)

/**
 * 发布动态：文案 + 图片 URL(直接填,不上传) + 标签 + 城市。
 */
@HiltViewModel
class FeedPublishViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedPublishUiState())
    val uiState: StateFlow<FeedPublishUiState> = _uiState.asStateFlow()

    fun publish(content: String, imageUrls: List<String>, tags: List<String>, city: String) {
        if (content.isBlank()) {
            _uiState.update { it.copy(error = "请输入动态内容") }
            return
        }
        _uiState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            val req = PublishFeedRequest(
                content = content.trim(),
                media = imageUrls,
                tags = tags,
                city = city.trim(),
                mediaType = 1,
            )
            when (val r = socialRepository.publish(req)) {
                is ApiResult.Success -> _uiState.update { it.copy(submitting = false, publishedId = r.data.id) }
                is ApiResult.Error -> _uiState.update { it.copy(submitting = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(submitting = false, error = "网络异常，请重试") }
            }
        }
    }
}
