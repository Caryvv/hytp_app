package com.example.hytp.feature.social.vm

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.SocialRepository
import com.example.hytp.core.data.UploadRepository
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
    /** 正在上传的图片 URI */
    val uploadingImages: List<Uri> = emptyList(),
    /** 已上传成功的图片 URL 列表 */
    val uploadedUrls: List<String> = emptyList(),
)

/**
 * 发布动态：选择图片(上传到本地) + 文案 + 标签 + 城市。
 */
@HiltViewModel
class FeedPublishViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val uploadRepository: UploadRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedPublishUiState())
    val uiState: StateFlow<FeedPublishUiState> = _uiState.asStateFlow()

    /** 用户选择图片后，逐个上传。 */
    fun uploadImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _uiState.update { it.copy(uploadingImages = uris) }
        viewModelScope.launch {
            val urls = mutableListOf<String>()
            for (uri in uris) {
                when (val r = uploadRepository.uploadImage(uri)) {
                    is ApiResult.Success -> urls.add(r.data.url)
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(uploadingImages = it.uploadingImages.filter { img -> img != uri }, error = r.message) }
                    }
                    is ApiResult.Failure -> {
                        _uiState.update { it.copy(uploadingImages = it.uploadingImages.filter { img -> img != uri }, error = "图片上传失败") }
                    }
                }
            }
            _uiState.update { it.copy(uploadingImages = emptyList(), uploadedUrls = it.uploadedUrls + urls) }
        }
    }

    /** 移除已上传的图片。 */
    fun removeImage(url: String) {
        _uiState.update { it.copy(uploadedUrls = it.uploadedUrls - url) }
    }

    fun publish(content: String, tags: List<String>, city: String) {
        if (content.isBlank()) {
            _uiState.update { it.copy(error = "请输入动态内容") }
            return
        }
        _uiState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            val req = PublishFeedRequest(
                content = content.trim(),
                media = _uiState.value.uploadedUrls,
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
