package com.example.hytp.feature.shop.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.ShopRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.ProductListItem
import com.example.hytp.core.network.dto.ShopPublic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopDetailUiState(
    val loading: Boolean = false,
    val shop: ShopPublic? = null,
    val products: List<ProductListItem> = emptyList(),
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val loadingMore: Boolean = false,
)

/**
 * 店铺主页：店铺公开信息 + 在售商品列表（简单分页）。
 */
@HiltViewModel
class ShopDetailViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val pageSize = 20
    private val shopId: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(ShopDetailUiState())
    val uiState: StateFlow<ShopDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null, page = 1, hasMore = true) }
        viewModelScope.launch {
            when (val r = shopRepository.getShop(shopId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(loading = false, shop = r.data) }
                    loadProducts(reset = true)
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.loading || s.loadingMore || !s.hasMore) return
        loadProducts(reset = false)
    }

    private fun loadProducts(reset: Boolean) {
        val target = if (reset) 1 else _uiState.value.page + 1
        _uiState.update { if (reset) it else it.copy(loadingMore = true) }
        viewModelScope.launch {
            when (val r = shopRepository.getShopProducts(shopId, page = target, pageSize = pageSize)) {
                is ApiResult.Success -> {
                    val merged = if (reset) r.data.list else _uiState.value.products + r.data.list
                    _uiState.update {
                        it.copy(
                            loadingMore = false,
                            products = merged,
                            page = target,
                            hasMore = merged.size < r.data.pagination.total && r.data.list.isNotEmpty(),
                        )
                    }
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(loadingMore = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(loadingMore = false, error = "网络异常，请重试") }
            }
        }
    }
}
