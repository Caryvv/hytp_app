package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.CreateOrderRequest
import com.example.hytp.core.network.dto.CreateOrderResult
import com.example.hytp.core.network.dto.Order
import com.example.hytp.core.network.dto.OrderLineRequest
import com.example.hytp.core.network.dto.OrderPreview
import com.example.hytp.core.network.dto.OrderPreviewRequest
import com.example.hytp.core.network.dto.PageData
import com.example.hytp.core.network.dto.RefundRequest
import com.example.hytp.core.network.dto.RefundResult
import com.example.hytp.core.network.dto.Review
import com.example.hytp.core.network.dto.ReviewRequest
import com.example.hytp.core.network.safeApiCall
import java.util.UUID

/**
 * 订单仓库（需登录）：结算预览、下单、列表、详情、取消、确认收货、售后、评价。
 */
class OrderRepository(
    private val api: HytpApiService,
) {
    /** 从购物车结算预览。 */
    suspend fun previewFromCart(cartIds: List<Long>? = null): ApiResult<OrderPreview> =
        safeApiCall { api.previewOrder(OrderPreviewRequest(fromCart = true, cartIds = cartIds)) }

    /** 立即购买预览。 */
    suspend fun previewItems(items: List<OrderLineRequest>): ApiResult<OrderPreview> =
        safeApiCall { api.previewOrder(OrderPreviewRequest(items = items)) }

    /**
     * 从购物车下单。Idempotency-Key 客户端生成，防重复提交。
     */
    suspend fun createFromCart(
        addressId: Long,
        cartIds: List<Long>? = null,
        remark: String = "",
        idempotencyKey: String = UUID.randomUUID().toString(),
    ): ApiResult<CreateOrderResult> = safeApiCall {
        api.createOrder(idempotencyKey, CreateOrderRequest(
            addressId = addressId, fromCart = true, cartIds = cartIds, remark = remark,
        ))
    }

    /** 立即购买下单。 */
    suspend fun createFromItems(
        addressId: Long,
        items: List<OrderLineRequest>,
        remark: String = "",
        idempotencyKey: String = UUID.randomUUID().toString(),
    ): ApiResult<CreateOrderResult> = safeApiCall {
        api.createOrder(idempotencyKey, CreateOrderRequest(
            addressId = addressId, items = items, remark = remark,
        ))
    }

    /** 订单列表（type/status 可选）。 */
    suspend fun getOrders(
        type: Int? = null,
        status: Int? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): ApiResult<PageData<Order>> {
        val query = buildMap {
            type?.let { if (it > 0) put("type", it.toString()) }
            status?.let { put("status", it.toString()) }
            put("page", page.toString())
            put("pageSize", pageSize.toString())
        }
        return safeApiCall { api.getOrders(query) }
    }

    suspend fun getDetail(orderNo: String): ApiResult<Order> =
        safeApiCall { api.getOrderDetail(orderNo) }

    suspend fun cancel(orderNo: String): ApiResult<Order> =
        safeApiCall { api.cancelOrder(orderNo) }

    suspend fun confirm(orderNo: String): ApiResult<Order> =
        safeApiCall { api.confirmOrder(orderNo) }

    suspend fun refund(orderNo: String, reason: String): ApiResult<RefundResult> =
        safeApiCall { api.refundOrder(orderNo, RefundRequest(reason = reason)) }

    suspend fun review(orderNo: String, productId: Long, rating: Int, content: String): ApiResult<Review> =
        safeApiCall { api.submitReview(orderNo, ReviewRequest(productId = productId, rating = rating, content = content)) }
}
