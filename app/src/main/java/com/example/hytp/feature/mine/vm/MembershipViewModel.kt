package com.example.hytp.feature.mine.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.PaymentRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.MembershipPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MembershipUiState(
    val loading: Boolean = true,
    val plan: MembershipPlan? = null,
    val submitting: Boolean = false,
    val error: String? = null,
    val doneExpireAt: Long? = null, // 开通成功后的到期时间戳，用于提示与回传刷新
)

/**
 * 会员开通/续费页 ViewModel。用同袍币购买，每月 30 元 = 3000 币。
 */
@HiltViewModel
class MembershipViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MembershipUiState())
    val uiState: StateFlow<MembershipUiState> = _uiState.asStateFlow()

    init {
        loadPlan()
    }

    fun loadPlan() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = paymentRepository.membershipPlan()) {
                is ApiResult.Success -> _uiState.update { it.copy(loading = false, plan = r.data) }
                is ApiResult.Error -> _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    fun purchase(plan: String) {
        if (_uiState.value.submitting) return
        _uiState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val r = paymentRepository.purchaseMembership(plan)) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(submitting = false, doneExpireAt = r.data.memberExpireAt) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(submitting = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(submitting = false, error = "网络异常，请重试") }
            }
        }
    }
}
