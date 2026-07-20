package com.example.hytp.feature.order.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.OrderRepository
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
)

/**
 * 评价提交页：评分 + 内容。orderNo + productId 经导航参数注入。
 * 提交后端触发情感分析（占位）。
 */
@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val orderNo: String = savedStateHandle.get<String>("orderNo") ?: ""
    val productId: Long = savedStateHandle.get<String>("productId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun submit(rating: Int, content: String) {
        _uiState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val r = orderRepository.review(orderNo, productId, rating, content)) {
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
