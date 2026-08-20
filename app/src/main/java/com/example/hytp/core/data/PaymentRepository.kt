package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.MembershipPlan
import com.example.hytp.core.network.dto.MembershipPurchaseRequest
import com.example.hytp.core.network.dto.MembershipResult
import com.example.hytp.core.network.dto.MockConfirmRequest
import com.example.hytp.core.network.dto.PageData
import com.example.hytp.core.network.dto.PayConfirmResult
import com.example.hytp.core.network.dto.PayRequest
import com.example.hytp.core.network.dto.PayResult
import com.example.hytp.core.network.dto.RechargeRequest
import com.example.hytp.core.network.dto.RechargeResult
import com.example.hytp.core.network.dto.WalletTxn
import com.example.hytp.core.network.dto.WithdrawRequest
import com.example.hytp.core.network.dto.WithdrawResult
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

    /** 充值同袍币（Mock 直接到账）。coin 为同袍币数量（100 同袍币 = 1 元）。 */
    suspend fun recharge(coin: Int): ApiResult<RechargeResult> =
        safeApiCall { api.recharge(RechargeRequest(coin)) }

    /** 提现同袍币（Mock 即时扣减）。余额不足由后端返回 BALANCE_NOT_ENOUGH。 */
    suspend fun withdraw(coin: Int): ApiResult<WithdrawResult> =
        safeApiCall { api.withdraw(WithdrawRequest(coin)) }

    /** 会员套餐 + 当前状态。 */
    suspend fun membershipPlan(): ApiResult<MembershipPlan> =
        safeApiCall { api.getMembershipPlan() }

    /** 用同袍币开通/续费会员。plan = month|year。余额不足由后端返回 BALANCE_NOT_ENOUGH。 */
    suspend fun purchaseMembership(plan: String): ApiResult<MembershipResult> =
        safeApiCall { api.purchaseMembership(MembershipPurchaseRequest(plan)) }

    /** 钱包流水（倒序分页）。 */
    suspend fun walletTransactions(page: Int, pageSize: Int): ApiResult<PageData<WalletTxn>> =
        safeApiCall { api.getWalletTransactions(page, pageSize) }
}
