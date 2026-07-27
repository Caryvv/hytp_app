package com.example.hytp.feature.order.vm

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.OrderRepository
import com.example.hytp.core.data.UploadRepository
import com.example.hytp.core.network.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewUiState(
    val submitting: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
    /** 正在上传的晒图 URI */
    val uploadingImages: List<Uri> = emptyList(),
    /** 已上传成功的晒图 URL */
    val uploadedUrls: List<String> = emptyList(),
)

/**
 * 评价提交页：评分 + 内容 + 晒图。orderNo + productId 经导航参数注入。
 * 提交后端触发情感分析（占位）。
 */
@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val uploadRepository: UploadRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val orderNo: String = savedStateHandle.get<String>("orderNo") ?: ""
    val productId: Long = savedStateHandle.get<String>("productId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    /** 用户选择晒图后，逐个上传。 */
    fun uploadImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _uiState.update { it.copy(uploadingImages = uris) }
        viewModelScope.launch {
            val urls = mutableListOf<String>()
            for (uri in uris) {
                when (val r = uploadRepository.uploadImage(uri)) {
                    is ApiResult.Success -> urls.add(r.data.url)
                    is ApiResult.Error -> _uiState.update { it.copy(error = r.message) }
                    is ApiResult.Failure -> _uiState.update { it.copy(error = "图片上传失败") }
                }
            }
            _uiState.update { it.copy(uploadingImages = emptyList(), uploadedUrls = it.uploadedUrls + urls) }
        }
    }

    fun removeImage(url: String) {
        _uiState.update { it.copy(uploadedUrls = it.uploadedUrls - url) }
    }

    fun submit(rating: Int, content: String) {
        _uiState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            val images = _uiState.value.uploadedUrls.takeIf { it.isNotEmpty() }
            when (val r = orderRepository.review(orderNo, productId, rating, content, images)) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(submitting = false, done = true) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(submitting = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(submitting = false, error = "网络异常，请重试") }
            }
        }
    }
}
