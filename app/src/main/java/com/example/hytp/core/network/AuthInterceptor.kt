package com.example.hytp.core.network

import com.example.hytp.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 统一请求头拦截器（对齐 docs/dev/03-后端API规范 §6、04 §5）。
 * - Authorization: Bearer <accessToken>（有 token 时）
 * - X-Platform / X-App-Version / X-Device-Id
 *
 * accessToken 通过 [tokenProvider] 同步读取（TokenStore 维护内存缓存，避免每请求阻塞 IO）。
 * deviceId 由 DI 注入（Settings.Secure.ANDROID_ID）。
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?,
    private val deviceId: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
            .header("X-Platform", "android")
            .header("X-App-Version", BuildConfig.VERSION_NAME)
            .header("X-Device-Id", deviceId)

        tokenProvider()?.takeIf { it.isNotBlank() }?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }

        // 阶段2 待接：TokenAuthenticator —— 401/1002 时用 refreshToken 自动续签并重放。
        return chain.proceed(builder.build())
    }
}
