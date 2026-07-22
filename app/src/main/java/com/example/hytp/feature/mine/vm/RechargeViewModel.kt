package com.example.hytp.feature.mine.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.PaymentRepository
import com.example.hytp.core.network.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RechargeUiState(
    val submitting: Boolean = false,
    val error: String? = null,
    val doneCoin: Int? = null, // 充值成功到账的同袍币数，用于提示与回传刷新
)

/**
 * 同袍币充值页 ViewModel（Mock 通道，直接到账）。
 * 100 同袍币 = 1 元。
 */
@HiltViewModel
class RechargeViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RechargeUiState())
    val uiState: StateFlow<RechargeUiState> = _uiState.asStateFlow()

    fun recharge(coin: Int) {
        if (coin <= 0) return
        _uiState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val r = paymentRepository.recharge(coin)) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(submitting = false, doneCoin = r.data.coin) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(submitting = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(submitting = false, error = "网络异常，请重试") }
            }
        }
    }
}

/** 充值档位（同袍币）。 */
val RECHARGE_TIERS = listOf(100, 500, 1000, 5000, 10000)
