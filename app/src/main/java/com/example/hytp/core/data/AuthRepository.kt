package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.LoginRequest
import com.example.hytp.core.network.dto.LoginResponse
import com.example.hytp.core.network.dto.LogoutRequest
import com.example.hytp.core.network.dto.SmsSendRequest
import com.example.hytp.core.network.dto.SmsSendResponse
import com.example.hytp.core.network.dto.UserProfile
import com.example.hytp.core.network.safeApiCall
import kotlinx.coroutines.flow.Flow

/**
 * 账号认证仓库：串联 [HytpApiService] 与 [TokenStore]。
 * 登录成功持久化 token；退出清理本地凭证。
 */
class AuthRepository(
    private val api: HytpApiService,
    private val tokenStore: TokenStore,
) {

    val isLoggedInFlow: Flow<Boolean> = tokenStore.isLoggedInFlow

    /** 发送验证码。成功时（Mock 模式）data 可能带 devCode。 */
    suspend fun sendSmsCode(phone: String, scene: String = "login"): ApiResult<SmsSendResponse> =
        safeApiCall { api.smsSend(SmsSendRequest(phone = phone, scene = scene)) }

    /** 验证码登录/注册合一。成功后持久化 token。 */
    suspend fun loginByCode(phone: String, code: String): ApiResult<LoginResponse> {
        val result = safeApiCall {
            api.login(LoginRequest(phone = phone, code = code, loginType = "code"))
        }
        if (result is ApiResult.Success) {
            tokenStore.saveTokens(result.data.accessToken, result.data.refreshToken)
        }
        return result
    }

    /** 当前用户资料。 */
    suspend fun getProfile(): ApiResult<UserProfile> =
        safeApiCall { api.getProfile() }

    /** 退出登录：拉黑 refreshToken 并清理本地凭证（无论后端是否成功都清本地）。 */
    suspend fun logout() {
        val refresh = tokenStore.readRefreshToken()
        if (!refresh.isNullOrBlank()) {
            runCatching { safeApiCall { api.logout(LogoutRequest(refreshToken = refresh)) } }
        }
        tokenStore.clear()
    }
}
