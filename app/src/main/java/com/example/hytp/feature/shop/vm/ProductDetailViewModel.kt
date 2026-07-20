package com.example.hytp.feature.shop.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.AddressRepository
import com.example.hytp.core.data.CartRepository
import com.example.hytp.core.data.OrderRepository
import com.example.hytp.core.data.ShopRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.ProductDetail
import com.example.hytp.core.network.dto.Review
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val loading: Boolean = false,
    val detail: ProductDetail? = null,
    val reviews: List<Review> = emptyList(),
    val error: String? = null,
    val cartMessage: String? = null,   // 加购结果一次性提示
    val cartRunning: Boolean = false,
    val rentMessage: String? = null,   // 租赁下单结果一次性提示
    val rentOrderNo: String? = null,   // 租赁下单成功返回单号（供跳转支付）
    val rentRunning: Boolean = false,
)

/**
 * 商品详情页：加载详情 + 首页评价 + 加购。
 * productId 通过导航参数经 SavedStateHandle 注入。
 */
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val addressRepository: AddressRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun consumeCartMessage() = _uiState.update { it.copy(cartMessage = null) }

    fun consumeRent() = _uiState.update { it.copy(rentMessage = null, rentOrderNo = null) }

    /**
     * 租赁下单：自动用默认收货地址(无则提示去添加)，租期 days 天从今天起，押金 depositAmount。
     */
    fun bookRent(skuId: Long?, days: Int, depositAmount: String) {
        if (days < 1) return
        _uiState.update { it.copy(rentRunning = true) }
        viewModelScope.launch {
            // 取默认地址
            val addrRes = addressRepository.list()
            val addressId = (addrRes as? ApiResult.Success)?.data?.list
                ?.let { list -> list.firstOrNull { it.isDefault == 1 } ?: list.firstOrNull() }?.id
            if (addressId == null) {
                _uiState.update { it.copy(rentRunning = false, rentMessage = "请先在“我的订单-地址”添加收货地址") }
                return@launch
            }
            val now = System.currentTimeMillis() / 1000
            val start = now
            val end = now + days.toLong() * 86400
            when (val r = orderRepository.createRent(productId, addressId, start, end, depositAmount, skuId)) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(rentRunning = false, rentMessage = "租赁下单成功，请支付", rentOrderNo = r.data.orderNo) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(rentRunning = false, rentMessage = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(rentRunning = false, rentMessage = "网络异常，请重试") }
            }
        }
    }

    /** 加入购物车。skuId 可空（无规格商品）。 */
    fun addToCart(skuId: Long?, qty: Int = 1) {
        _uiState.update { it.copy(cartRunning = true) }
        viewModelScope.launch {
            when (val r = cartRepository.add(productId, skuId, qty)) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(cartRunning = false, cartMessage = "已加入购物车") }
                is ApiResult.Error ->
                    _uiState.update { it.copy(cartRunning = false, cartMessage = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(cartRunning = false, cartMessage = "网络异常，请重试") }
            }
        }
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = shopRepository.getProductDetail(productId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(loading = false, detail = r.data) }
                    loadReviews()
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    private fun loadReviews() {
        viewModelScope.launch {
            when (val r = shopRepository.getProductReviews(productId, page = 1, pageSize = 20)) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(reviews = r.data.list) }
                else -> Unit // 评价加载失败不影响主详情
            }
        }
    }
}
