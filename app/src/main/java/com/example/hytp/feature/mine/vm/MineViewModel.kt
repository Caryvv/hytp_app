package com.example.hytp.feature.mine.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.AuthRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.BizCode
import com.example.hytp.core.network.dto.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MineUiState(
    val loading: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null,
    val loggedOut: Boolean = false,
    val sessionExpired: Boolean = false,
)

/**
 * 「我的」页面 ViewModel：加载用户资料 + 退出登录。
 */
@HiltViewModel
class MineViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MineUiState())
    val uiState: StateFlow<MineUiState> = _uiState.asStateFlow()

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
                    if (result.code == BizCode.UNAUTHORIZED) {
                        _uiState.update { it.copy(loading = false, sessionExpired = true) }
                    } else {
                        _uiState.update { it.copy(loading = false, error = result.message) }
                    }
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
