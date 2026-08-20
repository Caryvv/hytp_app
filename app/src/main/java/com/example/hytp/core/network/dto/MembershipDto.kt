package com.example.hytp.core.network.dto

/**
 * 会员 DTO（对齐后端 api/controllers/MembershipController）。
 * 用同袍币购买：包月 30 元 = 3000 币，包年 300 元 = 30000 币（省 60）。
 */

/** 单个套餐档。 */
data class MembershipPlanItem(
    val key: String,           // month | year
    val priceCoin: Int,
    val priceYuan: String,
    val durationText: String,  // 包月 / 包年
)

/** GET /membership/plan 响应 data。 */
data class MembershipPlan(
    val plans: List<MembershipPlanItem> = emptyList(),
    val isPremium: Boolean = false,
    val memberExpireAt: Long? = null,
)

/** POST /membership/purchase 请求。 */
data class MembershipPurchaseRequest(
    val plan: String = "month",
)

/** POST /membership/purchase 响应 data。 */
data class MembershipResult(
    val memberLevel: Int,
    val memberExpireAt: Long,
    val priceCoin: Int,
)
