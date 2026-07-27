package com.example.hytp.core.network.dto

/**
 * 订单/支付/地址/售后/评价 DTO —— 对齐后端 OrderService/PaymentService/AddressService/ReviewService。
 * 金额一律 String（后端 decimal）；带参 id 用 Long。
 */

// ---------------- 收货地址 ----------------

/** 收货地址（对齐 Address::toArray）。 */
data class Address(
    val id: Long,
    val userId: Long = 0,
    val name: String = "",
    val phone: String = "",
    val province: String = "",
    val city: String = "",
    val district: String = "",
    val detail: String = "",
    val isDefault: Int = 0,
)

data class AddressList(
    val list: List<Address> = emptyList(),
)

/** 新建/修改地址请求体。 */
data class AddressRequest(
    val name: String,
    val phone: String,
    val province: String,
    val city: String,
    val district: String = "",
    val detail: String,
    val isDefault: Int = 0,
)

// ---------------- 结算预览 ----------------

/** 预览行项。 */
data class PreviewItem(
    val productId: Long = 0,
    val skuId: Long? = null,
    val title: String = "",
    val cover: String = "",
    val spec: Map<String, String> = emptyMap(),
    val price: String = "0.00",
    val qty: Int = 1,
)

/** 按店铺分组的预览。 */
data class PreviewShop(
    val shopId: Long = 0,
    val shopName: String = "",
    val items: List<PreviewItem> = emptyList(),
    val subtotal: String = "0.00",
    val shipFee: String = "0.00",
)

/** 结算预览响应 data：{ shops, totalAmount }。 */
data class OrderPreview(
    val shops: List<PreviewShop> = emptyList(),
    val totalAmount: String = "0.00",
)

/** 预览/下单的直购行项（立即购买用）。 */
data class OrderLineRequest(
    val productId: Long,
    val skuId: Long? = null,
    val qty: Int = 1,
)

/** 结算预览请求体：来自购物车或直接 items。 */
data class OrderPreviewRequest(
    val fromCart: Boolean? = null,
    val cartIds: List<Long>? = null,
    val items: List<OrderLineRequest>? = null,
)

// ---------------- 下单 ----------------

/** 创建订单请求体。 */
data class CreateOrderRequest(
    val addressId: Long,
    val fromCart: Boolean? = null,
    val cartIds: List<Long>? = null,
    val items: List<OrderLineRequest>? = null,
    val remark: String = "",
)

/** 创建订单响应 data：{ orderNos, totalAmount }。 */
data class CreateOrderResult(
    val orderNos: List<String> = emptyList(),
    val totalAmount: String = "0.00",
)

// ---------------- 订单列表/详情 ----------------

/** 订单明细项（对齐 OrderItem::toArray）。 */
data class OrderItemDto(
    val id: Long,
    val productId: Long = 0,
    val skuId: Long? = null,
    val title: String = "",
    val spec: Map<String, String> = emptyMap(),
    val price: String = "0.00",
    val qty: Int = 1,
    val image: String = "",
)

/**
 * 订单（对齐 ShopOrder::toListArray / toDetailArray）。
 * 列表和详情共用，详情多带 items/address/commission。
 */
data class Order(
    val id: Long,
    val orderNo: String = "",
    val shopId: Long = 0,
    val shopName: String = "",
    val type: Int = 1,
    val totalAmount: String = "0.00",
    val payAmount: String = "0.00",
    val commission: String = "0.00",
    val depositAmount: String = "0.00",
    val status: Int = 0,
    val remark: String = "",
    val rentStart: Long? = null,
    val rentEnd: Long? = null,
    val paidAt: Long? = null,
    val shippedAt: Long? = null,
    val finishedAt: Long? = null,
    val returnedAt: Long? = null,
    val createdAt: Long = 0,
    val addressId: Long? = null,
    val address: Address? = null,
    val depositRefunded: Int = 0,
    val items: List<OrderItemDto> = emptyList(),
)

// ---------------- 支付 ----------------

/** 发起支付请求体。 */
data class PayRequest(
    val orderNo: String,
    val channel: Int = 1,
)

/** 发起支付响应 data。 */
data class PayResult(
    val payNo: String = "",
    val orderNo: String = "",
    val amount: String = "0.00",
    val channel: Int = 1,
    val channelText: String = "",
    val balanceBefore: String? = null,
    val balanceAfter: String? = null,
    val mock: Boolean? = null,
)

/** Mock 回调请求体。 */
data class MockConfirmRequest(
    val payNo: String,
)

/** Mock 回调响应 data。 */
data class PayConfirmResult(
    val orderNo: String = "",
    val status: Int = 0,
    val paid: Boolean = false,
)

// ---------------- 同袍币充值（Mock 通道） ----------------

/** 充值请求体。coin 为同袍币数量（100 同袍币 = 1 元）。 */
data class RechargeRequest(
    val coin: Int,
)

/** 充值响应 data（对齐 WalletService::recharge）。 */
data class RechargeResult(
    val rechargeNo: String = "",
    val coin: Int = 0,
    val amountYuan: String = "0.00",
    val balanceCoin: Int = 0,
    val mock: Boolean = false,
)

/** 提现请求体。coin 为同袍币数量（100 同袍币 = 1 元）。 */
data class WithdrawRequest(
    val coin: Int,
)

/** 提现响应 data（对齐 WalletService::withdraw）。 */
data class WithdrawResult(
    val withdrawNo: String = "",
    val coin: Int = 0,
    val amountYuan: String = "0.00",
    val balanceCoin: Int = 0,
    val mock: Boolean = false,
)

// ---------------- 售后 / 评价 ----------------

/** 售后申请请求体。 */
data class RefundRequest(
    val reason: String,
    val amount: String? = null,
    val evidence: List<String>? = null,
)

/** 售后响应（对齐 OrderRefund::toArray）。 */
data class RefundResult(
    val id: Long,
    val orderId: Long = 0,
    val reason: String = "",
    val amount: String = "0.00",
    val status: Int = 0,
    val evidence: List<String> = emptyList(),
    val handleRemark: String = "",
    val createdAt: Long = 0,
)

/** 评价提交请求体。 */
data class ReviewRequest(
    val productId: Long,
    val rating: Int,
    val content: String = "",
    val images: List<String>? = null,
)

// ---------------- 租赁 / 品质保障金（交易 P1） ----------------

/** 租赁下单请求体。rentStart/rentEnd 为秒级时间戳。 */
data class RentOrderRequest(
    val productId: Long,
    val skuId: Long? = null,
    val addressId: Long,
    val rentStart: Long,
    val rentEnd: Long,
    val depositAmount: String = "0",
    val remark: String = "",
)

/** 租赁下单响应 data。 */
data class RentOrderResult(
    val orderNo: String = "",
    val totalAmount: String = "0.00",
    val depositAmount: String = "0.00",
    val payAmount: String = "0.00",
)

/** 品质保障金索赔请求体。 */
data class DepositClaimRequest(
    val reason: String,
    val amount: String? = null,
    val evidence: List<String>? = null,
)

/** 品质保障金索赔响应（对齐 DepositClaim::toArray）。 */
data class DepositClaimResult(
    val id: Long,
    val orderId: Long = 0,
    val amount: String = "0.00",
    val reason: String = "",
    val status: Int = 0,
    val createdAt: Long = 0,
)
