package com.example.hytp.feature.cart.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.CartRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val loading: Boolean = false,
    val items: List<CartItem> = emptyList(),
    val error: String? = null,
) {
    /** 有效项合计金额。 */
    val totalAmount: String
        get() {
            var cents = 0L
            items.filter { it.valid }.forEach {
                val price = it.price.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
                cents += (price.multiply(java.math.BigDecimal(it.qty)))
                    .multiply(java.math.BigDecimal(100)).toLong()
            }
            return java.math.BigDecimal(cents).divide(java.math.BigDecimal(100)).toPlainString()
        }

    val hasValidItems: Boolean get() = items.any { it.valid }
}

/**
 * 购物车页：列表 + 改数量 + 删除 + 结算入口。
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = cartRepository.getCart()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(loading = false, items = r.data.list) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    fun changeQty(item: CartItem, qty: Int) {
        if (qty < 1) return
        // 乐观更新
        _uiState.update { s -> s.copy(items = s.items.map { if (it.id == item.id) it.copy(qty = qty) else it }) }
        viewModelScope.launch {
            when (cartRepository.updateQty(item.id, qty)) {
                is ApiResult.Success -> Unit
                else -> load() // 失败回源刷新
            }
        }
    }

    fun remove(item: CartItem) {
        viewModelScope.launch {
            when (cartRepository.remove(item.id)) {
                is ApiResult.Success ->
                    _uiState.update { s -> s.copy(items = s.items.filterNot { it.id == item.id }) }
                else -> load()
            }
        }
    }
}
