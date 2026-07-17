package com.example.hytp.core.network

import retrofit2.HttpException
import java.io.IOException

/**
 * 统一包装一次 API 调用：把 [ApiResponse] 转成 [ApiResult]。
 * - code==0 → Success(data)
 * - code!=0 → Error(code, message)
 * - 网络/解析异常 → Failure
 *
 * 用法：`safeApiCall { api.login(...) }`
 */
suspend fun <T> safeApiCall(block: suspend () -> ApiResponse<T>): ApiResult<T> {
    return try {
        val resp = block()
        if (resp.code == BizCode.SUCCESS) {
            @Suppress("UNCHECKED_CAST")
            ApiResult.Success((resp.data ?: Unit) as T)
        } else {
            ApiResult.Error(resp.code, resp.message.ifBlank { "请求失败(${resp.code})" })
        }
    } catch (e: IOException) {
        ApiResult.Failure(e)
    } catch (e: HttpException) {
        ApiResult.Failure(e)
    } catch (e: Exception) {
        ApiResult.Failure(e)
    }
}
