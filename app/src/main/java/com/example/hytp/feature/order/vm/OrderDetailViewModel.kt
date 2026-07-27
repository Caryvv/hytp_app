package com.example.hytp.feature.order.vm

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.OrderRepository
import com.example.hytp.core.data.PaymentRepository
import com.example.hytp.core.data.UploadRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailUiState(
    val loading: Boolean = false,
    val order: Order? = null,
    val error: String? = null,
    val actionRunning: Boolean = false,   // 支付/取消/确认等操作进行中
    val message: String? = null,          // 一次性提示（如"支付成功"）
    /** 售后凭证：正在上传的图片 URI */
    val uploadingImages: List<Uri> = emptyList(),
    /** 售后凭证：已上传成功的图片 URL */
    val uploadedUrls: List<String> = emptyList(),
)

/**
 * 订单详情页：状态/明细 + 操作（付款/取消/确认收货/售后）。
 * 支付：pay 拿参数 → Mock confirm → 重新拉取订单以服务端状态为准。
 */
@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val uploadRepository: UploadRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val orderNo: String = savedStateHandle.get<String>("orderNo") ?: ""

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = orderRepository.getDetail(orderNo)) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(loading = false, order = r.data) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }

    /** 售后凭证：用户选择图片后，逐个上传。 */
    fun uploadEvidence(uris: List<Uri>) {
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

    fun removeEvidence(url: String) = _uiState.update { it.copy(uploadedUrls = it.uploadedUrls - url) }

    /** 支付：发起 → Mock 回调 → 以服务端订单状态为准重新拉取。 */
    fun pay(channel: Int = 1) {
        _uiState.update { it.copy(actionRunning = true, error = null) }
        viewModelScope.launch {
            when (val pr = paymentRepository.pay(orderNo, channel)) {
                is ApiResult.Success -> {
                    when (val cr = paymentRepository.mockConfirm(pr.data.payNo)) {
                        is ApiResult.Success -> {
                            _uiState.update { it.copy(actionRunning = false, message = "支付成功") }
                            load()
                        }
                        is ApiResult.Error ->
                            _uiState.update { it.copy(actionRunning = false, error = cr.message) }
                        is ApiResult.Failure ->
                            _uiState.update { it.copy(actionRunning = false, error = "网络异常，请重试") }
                    }
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(actionRunning = false, error = pr.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(actionRunning = false, error = "网络异常，请重试") }
            }
        }
    }

    fun cancel() = runAction("已取消") { orderRepository.cancel(orderNo) }

    fun confirm() = runAction("已确认收货") { orderRepository.confirm(orderNo) }

    fun returnRent() = runAction("已寄回，等待商家确认") { orderRepository.returnOrder(orderNo) }

    fun refund(reason: String) {
        _uiState.update { it.copy(actionRunning = true, error = null) }
        viewModelScope.launch {
            val evidence = _uiState.value.uploadedUrls.takeIf { it.isNotEmpty() }
            when (val r = orderRepository.refund(orderNo, reason, evidence)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(actionRunning = false, message = "售后申请已提交", uploadedUrls = emptyList()) }
                    load()
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(actionRunning = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(actionRunning = false, error = "网络异常，请重试") }
            }
        }
    }

    private fun runAction(successMsg: String, block: suspend () -> ApiResult<Order>) {
        _uiState.update { it.copy(actionRunning = true, error = null) }
        viewModelScope.launch {
            when (val r = block()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(actionRunning = false, order = r.data, message = successMsg) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(actionRunning = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(actionRunning = false, error = "网络异常，请重试") }
            }
        }
    }
}
