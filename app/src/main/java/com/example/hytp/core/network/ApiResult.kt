package com.example.hytp.core.network

/**
 * 统一请求结果封装。
 * - [Success]：业务成功（后端 code==0），携带 data。
 * - [Error]：业务失败（后端 code!=0），携带业务错误码与文案。
 * - [Failure]：网络/解析等非业务异常。
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>
    data class Failure(val throwable: Throwable) : ApiResult<Nothing>
}

/** 业务错误码（对齐后端 common\enums\ErrorCode 常用项）。 */
object BizCode {
    const val SUCCESS = 0
    const val PARAM_INVALID = 1001
    const val UNAUTHORIZED = 1002
    const val FORBIDDEN = 1003
    const val TOO_MANY_REQUESTS = 1004
    const val NOT_FOUND = 1005
    const val SMS_CODE_INVALID = 1102
    const val ACCOUNT_DISABLED = 1103
}

/** 取成功数据，失败返回 null。 */
fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data
