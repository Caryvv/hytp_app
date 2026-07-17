package com.example.hytp.core.network

import com.example.hytp.BuildConfig
import com.example.hytp.core.data.TokenStore
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

/**
 * 统一请求头拦截器 + access token 过期自动续签（对齐 03 §5、§6）。
 *
 * 请求头：Authorization: Bearer <accessToken>、X-Platform / X-App-Version / X-Device-Id。
 *
 * 续签机制（后端业务码在 body、HTTP 恒 200，故不能用 OkHttp Authenticator）：
 * 收到响应后 peek body，若 code==1002（token 失效）：
 *   1) 用 refreshToken 裸调 POST /auth/refresh（独立 OkHttpClient，避免递归拦截）
 *   2) 成功 → 持久化新 token → 用新 token 重放原请求一次
 *   3) 失败（refresh 也失效）→ 清本地凭证，返回原响应（上层 UI 跳登录）
 * synchronized 串行化刷新，避免并发多次刷新；仅重放一次防死循环。
 * 免鉴权路径（login/refresh/sms）不参与续签。
 */
class AuthInterceptor(
    private val tokenStore: TokenStore,
    private val deviceId: String,
) : Interceptor {

    /** 裸客户端：仅用于刷新调用，不挂本拦截器，防止递归。 */
    private val bareClient: OkHttpClient by lazy { OkHttpClient.Builder().build() }

    private val refreshLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requested = withHeaders(original, tokenStore.currentAccessToken())
        val response = chain.proceed(requested)

        // 免鉴权路径不续签
        if (isAuthFreePath(original)) return response
        // 未失效直接返回
        if (!isUnauthorized(response)) return response

        // 尝试刷新（串行化）
        val newAccess = synchronized(refreshLock) {
            val currentAccess = tokenStore.currentAccessToken()
            val usedToken = requested.header("Authorization")?.removePrefix("Bearer ")
            // 若当前 token 已被其他线程刷新，直接用当前的重放，否则自己刷
            if (currentAccess != null && currentAccess.isNotBlank() && currentAccess != usedToken) {
                currentAccess
            } else {
                doRefresh()
            }
        } ?: return response // 刷新失败，返回原 1002 响应

        // 用新 token 重放一次
        response.close()
        return chain.proceed(withHeaders(original, newAccess))
    }

    private fun withHeaders(request: Request, token: String?): Request {
        val b = request.newBuilder()
            .header("X-Platform", "android")
            .header("X-App-Version", BuildConfig.VERSION_NAME)
            .header("X-Device-Id", deviceId)
        token?.takeIf { it.isNotBlank() }?.let { b.header("Authorization", "Bearer $it") }
        return b.build()
    }

    /** peek 响应体，判断业务码是否为未登录/失效(1002)。 */
    private fun isUnauthorized(response: Response): Boolean {
        return try {
            val peeked = response.peekBody(PEEK_LIMIT).string()
            if (peeked.isBlank()) return false
            JSONObject(peeked).optInt("code", 0) == BizCode.UNAUTHORIZED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 用 refreshToken 换新 token 对；成功返回新 accessToken（已持久化），
     * 失败（refresh 也失效）清凭证返回 null；网络异常返回 null 不清凭证。
     */
    private fun doRefresh(): String? {
        val refreshToken = tokenStore.currentRefreshToken()?.takeIf { it.isNotBlank() } ?: return null

        val body = JSONObject().put("refreshToken", refreshToken).toString()
            .toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url(BuildConfig.BASE_URL + "auth/refresh")
            .header("X-Platform", "android")
            .header("X-Device-Id", deviceId)
            .post(body)
            .build()

        return try {
            bareClient.newCall(req).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                val json = JSONObject(text)
                if (json.optInt("code", -1) == BizCode.SUCCESS) {
                    val data = json.optJSONObject("data")
                    val access = data?.optString("accessToken").orEmpty()
                    val refresh = data?.optString("refreshToken").orEmpty()
                    if (access.isNotBlank() && refresh.isNotBlank()) {
                        tokenStore.updateTokensBlocking(access, refresh)
                        access
                    } else {
                        tokenStore.clearBlocking()
                        null
                    }
                } else {
                    tokenStore.clearBlocking() // refresh 也失效
                    null
                }
            }
        } catch (e: Exception) {
            null // 网络异常，保留凭证，本次不重放
        }
    }

    private fun isAuthFreePath(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.endsWith("/auth/refresh") ||
            path.endsWith("/auth/login") ||
            path.endsWith("/sms/send")
    }

    private companion object {
        const val PEEK_LIMIT = 1L * 1024 * 1024 // 1MB
    }
}
