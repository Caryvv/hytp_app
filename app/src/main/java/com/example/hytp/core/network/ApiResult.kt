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

    // 交易 1200+
    const val STOCK_NOT_ENOUGH = 1201
    const val PRODUCT_OFF_SHELF = 1202
    const val ORDER_STATUS_INVALID = 1203
    const val CART_EMPTY = 1208
    const val CART_ITEM_INVALID = 1209
    const val ORDER_NOT_FOUND = 1210
    const val ADDRESS_NOT_FOUND = 1211
    const val ADDRESS_REQUIRED = 1212
    const val SKU_NOT_FOUND = 1213
    const val REVIEW_ALREADY_EXISTS = 1214
    const val REVIEW_NOT_ALLOWED = 1215

    // 支付 1300+
    const val PAY_FAIL = 1301
    const val PAY_ORDER_NOT_FOUND = 1302
    const val PAY_AMOUNT_MISMATCH = 1303
    const val PAY_ALREADY_PAID = 1304
    const val REFUND_STATUS_INVALID = 1305
}

/** 取成功数据，失败返回 null。 */
fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data
