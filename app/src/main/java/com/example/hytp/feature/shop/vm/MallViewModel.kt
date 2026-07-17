package com.example.hytp.feature.shop.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.ShopRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.Category
import com.example.hytp.core.network.dto.ProductListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 排序选项（对齐后端 ProductQueryService::SORT_MAP 白名单）。 */
enum class SortOption(val value: String, val label: String) {
    NEW("new", "最新"),
    SALES("sales", "销量"),
    RATING("rating", "口碑"),
    PRICE_ASC("price", "价格↑"),
    PRICE_DESC("price-desc", "价格↓"),
}

data class MallUiState(
    val loadingCategories: Boolean = false,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Int = 0, // 0 表示全部
    val sort: SortOption = SortOption.NEW,
    val products: List<ProductListItem> = emptyList(),
    val loading: Boolean = false,      // 首屏/刷新加载
    val loadingMore: Boolean = false,  // 加载更多
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
)

/**
 * 商城页：加载分类树 + 商品列表（筛选 + 简单 page 累加分页）。
 */
@HiltViewModel
class MallViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
) : ViewModel() {

    private val pageSize = 20

    private val _uiState = MutableStateFlow(MallUiState())
    val uiState: StateFlow<MallUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        refresh()
    }

    fun loadCategories() {
        _uiState.update { it.copy(loadingCategories = true) }
        viewModelScope.launch {
            when (val r = shopRepository.getCategories()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(loadingCategories = false, categories = r.data) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(loadingCategories = false) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(loadingCategories = false) }
            }
        }
    }

    /** 选择分类（0=全部），重置列表。 */
    fun selectCategory(categoryId: Int) {
        if (categoryId == _uiState.value.selectedCategoryId) return
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        refresh()
    }

    /** 切换排序，重置列表。 */
    fun selectSort(sort: SortOption) {
        if (sort == _uiState.value.sort) return
        _uiState.update { it.copy(sort = sort) }
        refresh()
    }

    /** 刷新（回到第一页）。 */
    fun refresh() {
        _uiState.update { it.copy(loading = true, error = null, page = 1, hasMore = true) }
        viewModelScope.launch {
            val s = _uiState.value
            when (val r = fetch(page = 1)) {
                is ApiResult.Success -> {
                    val list = r.data.list
                    _uiState.update {
                        it.copy(
                            loading = false,
                            products = list,
                            page = 1,
                            hasMore = list.size >= pageSize &&
                                (r.data.pagination.total > list.size),
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

    /** 加载下一页。 */
    fun loadMore() {
        val s = _uiState.value
        if (s.loading || s.loadingMore || !s.hasMore) return
        _uiState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            val next = s.page + 1
            when (val r = fetch(page = next)) {
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

    private suspend fun fetch(page: Int) = shopRepository.getProducts(
        categoryId = _uiState.value.selectedCategoryId.takeIf { it > 0 },
        sort = _uiState.value.sort.value,
        page = page,
        pageSize = pageSize,
    )
}
