package com.example.hytp.feature.order.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.OrderRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 订单状态筛选 Tab（null=全部）。 */
enum class OrderTab(val label: String, val status: Int?) {
    ALL("全部", null),
    UNPAID("待付款", 0),
    UNSHIP("待发货", 1),
    SHIPPED("待收货", 2),
    FINISHED("已完成", 4),
}

data class OrderListUiState(
    val tab: OrderTab = OrderTab.ALL,
    val orders: List<Order> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
)

/**
 * 订单列表：按状态 Tab 分页（套用 MallViewModel 的 refresh/loadMore 模式）。
 */
@HiltViewModel
class OrderListViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val pageSize = 20

    private val _uiState = MutableStateFlow(OrderListUiState())
    val uiState: StateFlow<OrderListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun selectTab(tab: OrderTab) {
        if (tab == _uiState.value.tab) return
        _uiState.update { it.copy(tab = tab) }
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(loading = true, error = null, page = 1, hasMore = true) }
        viewModelScope.launch {
            when (val r = fetch(1)) {
                is ApiResult.Success -> {
                    val list = r.data.list
                    _uiState.update {
                        it.copy(
                            loading = false,
                            orders = list,
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
        if (s.loading || s.loadingMore || !s.hasMore) return
        _uiState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            val next = s.page + 1
            when (val r = fetch(next)) {
                is ApiResult.Success -> {
                    val merged = s.orders + r.data.list
                    _uiState.update {
                        it.copy(
                            loadingMore = false,
                            orders = merged,
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

    private suspend fun fetch(page: Int) =
        orderRepository.getOrders(status = _uiState.value.tab.status, page = page, pageSize = pageSize)
}
