package com.example.hytp.feature.shop.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.ShopRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.ProductListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",       // 输入框当前文本
    val submitted: String = "",   // 已提交的关键词（用于分页 & 判断是否搜过）
    val products: List<ProductListItem> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
)

/**
 * 商品搜索页：按关键词查商品（复用 ShopRepository.getProducts keyword），简单 page 累加分页。
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
) : ViewModel() {

    private val pageSize = 20

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(q: String) = _uiState.update { it.copy(query = q) }

    /** 提交搜索（回到第一页）。空关键词不搜。 */
    fun search() {
        val kw = _uiState.value.query.trim()
        if (kw.isEmpty()) return
        _uiState.update {
            it.copy(submitted = kw, loading = true, error = null, page = 1, hasMore = true, products = emptyList())
        }
        viewModelScope.launch {
            when (val r = fetch(kw, page = 1)) {
                is ApiResult.Success -> {
                    val list = r.data.list
                    _uiState.update {
                        it.copy(
                            loading = false,
                            products = list,
                            page = 1,
                            hasMore = list.size >= pageSize && r.data.pagination.total > list.size,
                        )
                    }
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
        if (s.loading || s.loadingMore || !s.hasMore || s.submitted.isEmpty()) return
        _uiState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            val next = s.page + 1
            when (val r = fetch(s.submitted, page = next)) {
                is ApiResult.Success -> {
                    val merged = s.products + r.data.list
                    _uiState.update {
                        it.copy(
                            loadingMore = false,
                            products = merged,
                            page = next,
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

    private suspend fun fetch(keyword: String, page: Int) =
        shopRepository.getProducts(keyword = keyword, page = page, pageSize = pageSize)
}
