package com.example.hytp.feature.splash.vm

import androidx.lifecycle.ViewModel
import com.example.hytp.core.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 启动页逻辑：短暂停留 + 读取本地登录态。
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    /** 返回是否已登录（本地有 token）。含最短展示时长。 */
    suspend fun resolveDestination(): Boolean {
        delay(SPLASH_MIN_MILLIS)
        return authRepository.isLoggedInFlow.first()
    }

    private companion object {
        const val SPLASH_MIN_MILLIS = 1000L
    }
}
