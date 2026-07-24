package com.example.hytp.feature.home.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.AuthRepository
import com.example.hytp.core.data.HomeRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.BizCode
import com.example.hytp.core.network.dto.BannerItem
import com.example.hytp.core.network.dto.Feed
import com.example.hytp.core.network.dto.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null,
    val loggedOut: Boolean = false,
    val sessionExpired: Boolean = false,
    // Banner
    val banners: List<BannerItem> = emptyList(),
    // 推荐流
    val feedItems: List<Feed> = emptyList(),
    val feedLoading: Boolean = false,
    val feedLoadingMore: Boolean = false,
    val feedError: String? = null,
    val feedPage: Int = 1,
    val feedHasMore: Boolean = true,
    val refreshing: Boolean = false,
    val unreadCount: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val pageSize = 10

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            // 三个请求并行
            val profileJob = launch { loadProfile() }
            val bannersJob = launch { loadBanners() }
            val feedJob = launch { loadFeed(page = 1) }
            profileJob.join()
            bannersJob.join()
            feedJob.join()
            // 全部完成后取消 loading（仅当没有全局 error 时）
            if (_uiState.value.error == null) {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private suspend fun loadProfile() {
        when (val result = authRepository.getProfile()) {
            is ApiResult.Success ->
                _uiState.update { it.copy(profile = result.data) }
            is ApiResult.Error ->
                if (result.code == BizCode.UNAUTHORIZED) {
                    _uiState.update { it.copy(sessionExpired = true) }
                } else {
                    _uiState.update { it.copy(error = result.message) }
                }
            is ApiResult.Failure ->
                _uiState.update { it.copy(error = "网络异常，请重试") }
        }
    }

    private suspend fun loadBanners() {
        when (val r = homeRepository.getBanners()) {
            is ApiResult.Success ->
                _uiState.update { it.copy(banners = r.data) }
            else -> { /* banner 失败不阻塞整个页面 */ }
        }
    }

    private suspend fun loadFeed(page: Int) {
        val s = _uiState.value
        if (s.feedLoading || s.feedLoadingMore) return
        if (page == 1) {
            _uiState.update { it.copy(feedLoading = true, feedError = null) }
        } else {
            _uiState.update { it.copy(feedLoadingMore = true) }
        }
        when (val r = homeRepository.getHomeFeed(page, pageSize)) {
            is ApiResult.Success -> {
                val d = r.data
                val merged = if (page == 1) d.list else s.feedItems + d.list
                _uiState.update {
                    it.copy(
                        feedLoading = false,
                        feedLoadingMore = false,
                        refreshing = false,
                        feedItems = merged,
                        feedPage = page,
                        feedHasMore = merged.size < d.pagination.total && d.list.isNotEmpty(),
                    )
                }
            }
            is ApiResult.Error -> {
                _uiState.update {
                    it.copy(feedLoading = false, feedLoadingMore = false, refreshing = false, feedError = r.message)
                }
            }
            is ApiResult.Failure -> {
                _uiState.update {
                    it.copy(feedLoading = false, feedLoadingMore = false, refreshing = false, feedError = "网络异常，请重试")
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(refreshing = true, feedPage = 1, feedHasMore = true) }
        viewModelScope.launch {
            launch { loadProfile() }
            launch { loadBanners() }
            launch { loadFeed(page = 1) }
        }
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.feedLoading || s.feedLoadingMore || !s.feedHasMore) return
        viewModelScope.launch { loadFeed(s.feedPage + 1) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(loggedOut = true) }
        }
    }
}
