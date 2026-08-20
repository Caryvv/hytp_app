package com.example.hytp.feature.mine.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.PaymentRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.WalletTxn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletTxnUiState(
    val loading: Boolean = true,      // 首屏加载
    val loadingMore: Boolean = false,
    val error: String? = null,
    val txns: List<WalletTxn> = emptyList(),
    val hasMore: Boolean = true,
)

/**
 * 钱包流水页 ViewModel。倒序分页，滚动到底加载下一页。
 */
@HiltViewModel
class WalletTxnViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletTxnUiState())
    val uiState: StateFlow<WalletTxnUiState> = _uiState.asStateFlow()

    private var page = 1
    private val pageSize = 20

    init {
        load()
    }

    /** 首屏/重试：从第一页开始。 */
    fun load() {
        page = 1
        _uiState.update { it.copy(loading = true, error = null, txns = emptyList(), hasMore = true) }
        fetch(reset = true)
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.loading || s.loadingMore || !s.hasMore) return
        _uiState.update { it.copy(loadingMore = true) }
        fetch(reset = false)
    }

    private fun fetch(reset: Boolean) {
        viewModelScope.launch {
            when (val r = paymentRepository.walletTransactions(page, pageSize)) {
                is ApiResult.Success -> {
                    val list = r.data.list
                    _uiState.update {
                        val merged = if (reset) list else it.txns + list
                        it.copy(
                            loading = false,
                            loadingMore = false,
                            txns = merged,
                            hasMore = merged.size < r.data.pagination.total,
                        )
                    }
                    if (list.isNotEmpty()) page++
                }
                // 已有数据时的加载更多失败不清屏，仅首屏空列表才显示错误
                is ApiResult.Error ->
                    _uiState.update { it.copy(loading = false, loadingMore = false, error = if (it.txns.isEmpty()) r.message else null) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(loading = false, loadingMore = false, error = if (it.txns.isEmpty()) "网络异常，请重试" else null) }
            }
        }
    }
}
