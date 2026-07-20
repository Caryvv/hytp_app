package com.example.hytp.feature.order.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.AddressRepository
import com.example.hytp.core.data.OrderRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.Address
import com.example.hytp.core.network.dto.OrderPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val loading: Boolean = false,
    val preview: OrderPreview? = null,
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: Long? = null,
    val error: String? = null,
    val submitting: Boolean = false,
    val createdOrderNos: List<String> = emptyList(),
) {
    val selectedAddress: Address? get() = addresses.firstOrNull { it.id == selectedAddressId }
    val canSubmit: Boolean get() = preview != null && selectedAddressId != null && !submitting
}

/**
 * 结算页：从购物车结算预览 + 选地址 + 提交下单。
 * 本轮仅支持从购物车结算（fromCart）。
 */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val addressRepository: AddressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            // 预览
            when (val r = orderRepository.previewFromCart()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(loading = false, preview = r.data) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
            loadAddresses()
        }
    }

    fun loadAddresses() {
        viewModelScope.launch {
            when (val r = addressRepository.list()) {
                is ApiResult.Success ->
                    _uiState.update { s ->
                        val defaultId = s.selectedAddressId
                            ?: r.data.list.firstOrNull { it.isDefault == 1 }?.id
                            ?: r.data.list.firstOrNull()?.id
                        s.copy(addresses = r.data.list, selectedAddressId = defaultId)
                    }
                else -> Unit
            }
        }
    }

    fun selectAddress(id: Long) {
        _uiState.update { it.copy(selectedAddressId = id) }
    }

    /** 提交下单，成功返回订单号列表（导航到支付/订单）。 */
    fun submit(onCreated: (List<String>) -> Unit) {
        val addressId = _uiState.value.selectedAddressId ?: return
        _uiState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val r = orderRepository.createFromCart(addressId = addressId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(submitting = false, createdOrderNos = r.data.orderNos) }
                    onCreated(r.data.orderNos)
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(submitting = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(submitting = false, error = "网络异常，请重试") }
            }
        }
    }
}
