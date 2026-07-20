package com.example.hytp.core.network.dto

/**
 * 购物车 DTO —— 对齐后端 CartService::decorate / list 输出。
 * 字段驼峰对齐后端；金额用 String（后端 decimal 序列化为字符串）。
 */

/** 购物车项（含商品展示快照 + 有效性/库存）。 */
data class CartItem(
    val id: Long,
    val productId: Long = 0,
    val skuId: Long? = null,
    val qty: Int = 1,
    val tradeType: Int = 1,
    val title: String = "",
    val cover: String = "",
    val spec: Map<String, String> = emptyMap(),
    val price: String = "0.00",
    val stock: Int = 0,
    val shopId: Long = 0,
    val valid: Boolean = true,
)

/** 购物车列表响应 data：{ list }。 */
data class CartList(
    val list: List<CartItem> = emptyList(),
)

/** 加购请求体。 */
data class AddCartRequest(
    val productId: Long,
    val skuId: Long? = null,
    val qty: Int = 1,
)

/** 改数量请求体。 */
data class UpdateCartRequest(
    val qty: Int,
)
