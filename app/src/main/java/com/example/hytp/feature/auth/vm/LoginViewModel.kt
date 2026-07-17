package com.example.hytp.feature.auth.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.BuildConfig
import com.example.hytp.core.data.AuthRepository
import com.example.hytp.core.network.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 登录页 ViewModel：发验证码（60s 倒计时）、验证码登录/注册合一。
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onPhoneChange(value: String) {
        if (value.length <= 11 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(phone = value, error = null) }
        }
    }

    fun onCodeChange(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(code = value, error = null) }
        }
    }

    fun consumeError() = _uiState.update { it.copy(error = null) }

    /** 发送验证码。 */
    fun sendCode() {
        val state = _uiState.value
        if (!state.canSendCode) return

        _uiState.update { it.copy(sending = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.sendSmsCode(state.phone)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            sending = false,
                            // 仅 debug 展示 Mock 回带的验证码，便于联调
                            devCode = if (BuildConfig.DEBUG) result.data.devCode else null,
                        )
                    }
                    startCountdown()
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(sending = false, error = result.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(sending = false, error = "网络异常，请重试") }
            }
        }
    }

    /** 验证码登录/注册。 */
    fun login() {
        val state = _uiState.value
        if (!state.canLogin) return

        _uiState.update { it.copy(loggingIn = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.loginByCode(state.phone, state.code)) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(loggingIn = false, loginSuccess = true) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(loggingIn = false, error = result.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(loggingIn = false, error = "网络异常，请重试") }
            }
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            _uiState.update { it.copy(countdown = COUNTDOWN_SECONDS) }
            while (_uiState.value.countdown > 0) {
                delay(1000)
                _uiState.update { it.copy(countdown = it.countdown - 1) }
            }
        }
    }

    private companion object {
        const val COUNTDOWN_SECONDS = 60
    }
}
