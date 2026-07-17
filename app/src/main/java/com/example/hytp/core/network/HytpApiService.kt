package com.example.hytp.core.network

import com.example.hytp.core.network.dto.LoginRequest
import com.example.hytp.core.network.dto.LoginResponse
import com.example.hytp.core.network.dto.LogoutRequest
import com.example.hytp.core.network.dto.RefreshRequest
import com.example.hytp.core.network.dto.RefreshResponse
import com.example.hytp.core.network.dto.SmsSendRequest
import com.example.hytp.core.network.dto.SmsSendResponse
import com.example.hytp.core.network.dto.UpdateProfileRequest
import com.example.hytp.core.network.dto.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * 后端 REST 接口（对齐 api 入口路由）。所有响应统一 [ApiResponse]。
 */
interface HytpApiService {

    @POST("sms/send")
    suspend fun smsSend(@Body body: SmsSendRequest): ApiResponse<SmsSendResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): ApiResponse<RefreshResponse>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest): ApiResponse<Unit>

    @GET("user/profile")
    suspend fun getProfile(): ApiResponse<UserProfile>

    @PUT("user/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): ApiResponse<UserProfile>
}
