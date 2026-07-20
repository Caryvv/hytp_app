package com.example.hytp.feature.shop.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.CartRepository
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
)

/**
 * 商品详情页：加载详情 + 首页评价 + 加购。
 * productId 通过导航参数经 SavedStateHandle 注入。
 */
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun consumeCartMessage() = _uiState.update { it.copy(cartMessage = null) }

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
