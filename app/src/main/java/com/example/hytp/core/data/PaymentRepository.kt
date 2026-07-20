package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.MockConfirmRequest
import com.example.hytp.core.network.dto.PayConfirmResult
import com.example.hytp.core.network.dto.PayRequest
import com.example.hytp.core.network.dto.PayResult
import com.example.hytp.core.network.safeApiCall

/**
 * 支付仓库（Mock 通道，需登录）。
 * 真实通道时 mockConfirm 由服务端 notify 替代，客户端只保留 pay + 轮询订单。
 */
class PaymentRepository(
    private val api: HytpApiService,
) {
    suspend fun pay(orderNo: String, channel: Int = 1): ApiResult<PayResult> =
        safeApiCall { api.pay(PayRequest(orderNo, channel)) }

    /** Mock：模拟第三方回调，触发服务端改单。 */
    suspend fun mockConfirm(payNo: String): ApiResult<PayConfirmResult> =
        safeApiCall { api.mockConfirmPay(MockConfirmRequest(payNo)) }
}
