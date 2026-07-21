package com.example.hytp.core.network.dto

/**
 * 认证相关 DTO（对齐后端 api/controllers/AuthController、SmsController）。
 * 字段名与后端 JSON 键一致，用 Moshi 反射解析。
 */

/** POST /sms/send 请求。 */
data class SmsSendRequest(
    val phone: String,
    val scene: String = "login",
)

/** POST /sms/send 响应 data（Mock 模式回带 devCode，正式为空）。 */
data class SmsSendResponse(
    val devCode: String? = null,
)

/** POST /auth/login 请求（验证码/密码登录合一）。 */
data class LoginRequest(
    val phone: String,
    val code: String? = null,
    val password: String? = null,
    val loginType: String = "code",
)

/** POST /auth/login 响应 data。 */
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: UserProfile,
    val isNewUser: Boolean = false,
)

/** POST /auth/refresh 请求。 */
data class RefreshRequest(
    val refreshToken: String,
)

/** POST /auth/refresh 响应 data。 */
data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
)

/** POST /auth/logout 请求。 */
data class LogoutRequest(
    val refreshToken: String,
)

/** 用户资料（GET/PUT /user/profile 响应 data，对齐 User::toProfileArray）。 */
data class UserProfile(
    val id: Long,
    val phone: String = "",
    val nickname: String = "",
    val avatar: String? = null,
    val gender: Int = 0,
    val birthday: String? = null,
    val city: String? = null,
    val memberLevel: Int = 0,
    val memberExpireAt: Long? = null,
    val balance: String = "0.00",
)

/** PUT /user/profile 请求（仅传要改的字段）。 */
data class UpdateProfileRequest(
    val nickname: String? = null,
    val avatar: String? = null,
    val gender: Int? = null,
    val city: String? = null,
    val birthday: String? = null,
)
