package com.example.hytp.core.network

/**
 * 后端统一响应结构（对齐 docs/dev/03-后端API规范 §3）：{ code, message, data }。
 * code == 0 表示成功，非 0 为业务错误码（见后端 ErrorCode）。
 *
 * 用 Moshi 反射适配器解析（KotlinJsonAdapterFactory），无需 codegen。
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String = "",
    val data: T? = null,
)
