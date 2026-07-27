package com.example.hytp.feature.mine.vm

import androidx.lifecycle.SavedStateHandle
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

data class WithdrawUiState(
    val input: String = "",             // 用户输入的提现同袍币数（文本）
    val submitting: Boolean = false,
    val error: String? = null,
    val doneCoin: Int? = null,          // 提现成功扣减的同袍币数，用于回传刷新余额
)

/**
 * 同袍币提现页 ViewModel（Mock 即时扣减）。100 同袍币 = 1 元。
 * balanceCoin 经导航参数注入，用于上限校验与展示。
 */
@HiltViewModel
class WithdrawViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val balanceCoin: Int = savedStateHandle.get<String>("balanceCoin")?.toIntOrNull() ?: 0

    private val _uiState = MutableStateFlow(WithdrawUiState())
    val uiState: StateFlow<WithdrawUiState> = _uiState.asStateFlow()

    /** 仅接受数字输入。 */
    fun onInputChange(text: String) {
        if (text.all { it.isDigit() }) _uiState.update { it.copy(input = text, error = null) }
    }

    /** 全部提现。 */
    fun withdrawAll() = _uiState.update { it.copy(input = balanceCoin.toString(), error = null) }

    fun withdraw() {
        val coin = _uiState.value.input.toIntOrNull() ?: 0
        when {
            coin <= 0 -> {
                _uiState.update { it.copy(error = "请输入提现同袍币数") }
                return
            }
            coin > balanceCoin -> {
                _uiState.update { it.copy(error = "超出可提现余额") }
                return
            }
        }
        _uiState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val r = paymentRepository.withdraw(coin)) {
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
