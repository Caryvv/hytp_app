package com.example.hytp.feature.home.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.AuthRepository
import com.example.hytp.core.network.ApiResult
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
)

/**
 * 首页占位逻辑：加载当前用户资料（验证带 token 请求链路）、退出登录。
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.getProfile()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(loading = false, profile = result.data) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(loading = false, error = result.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(loggedOut = true) }
        }
    }
}
