package com.example.hytp.core.network

import com.example.hytp.core.network.dto.AddCartRequest
import com.example.hytp.core.network.dto.Address
import com.example.hytp.core.network.dto.AddressList
import com.example.hytp.core.network.dto.AddressRequest
import com.example.hytp.core.network.dto.CartItem
import com.example.hytp.core.network.dto.CartList
import com.example.hytp.core.network.dto.Category
import com.example.hytp.core.network.dto.CreateOrderRequest
import com.example.hytp.core.network.dto.CreateOrderResult
import com.example.hytp.core.network.dto.LoginRequest
import com.example.hytp.core.network.dto.LoginResponse
import com.example.hytp.core.network.dto.LogoutRequest
import com.example.hytp.core.network.dto.MockConfirmRequest
import com.example.hytp.core.network.dto.Order
import com.example.hytp.core.network.dto.OrderPreview
import com.example.hytp.core.network.dto.OrderPreviewRequest
import com.example.hytp.core.network.dto.PageData
import com.example.hytp.core.network.dto.PayConfirmResult
import com.example.hytp.core.network.dto.PayRequest
import com.example.hytp.core.network.dto.PayResult
import com.example.hytp.core.network.dto.ProductDetail
import com.example.hytp.core.network.dto.ProductListItem
import com.example.hytp.core.network.dto.RefreshRequest
import com.example.hytp.core.network.dto.RefreshResponse
import com.example.hytp.core.network.dto.RefundRequest
import com.example.hytp.core.network.dto.RefundResult
import com.example.hytp.core.network.dto.Review
import com.example.hytp.core.network.dto.ReviewRequest
import com.example.hytp.core.network.dto.ShopPublic
import com.example.hytp.core.network.dto.SmsSendRequest
import com.example.hytp.core.network.dto.SmsSendResponse
import com.example.hytp.core.network.dto.UpdateCartRequest
import com.example.hytp.core.network.dto.UpdateProfileRequest
import com.example.hytp.core.network.dto.UserProfile
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * 后端 REST 接口（对齐 api 入口路由）。所有响应统一 [ApiResponse]。
 */
interface HytpApiService {

    @POST("sms/send")
    suspend fun smsSend(@Body body: SmsSendRequest): ApiResponse<SmsSendResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): ApiResponse<RefreshResponse>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest): ApiResponse<Unit>

    @GET("user/profile")
    suspend fun getProfile(): ApiResponse<UserProfile>

    @PUT("user/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): ApiResponse<UserProfile>

    // ---------------- 交易区（只读浏览，白名单免登录） ----------------

    /** 分类树。 */
    @GET("categories")
    suspend fun getCategories(): ApiResponse<List<Category>>

    /**
     * 商品列表（筛选 + 分页）。
     * query 支持 categoryId/formeDynasty/formeType/style/tradeType/keyword/sort/page/pageSize，
     * 由调用方按需拼装，空值不传。
     */
    @GET("products")
    suspend fun getProducts(@QueryMap query: Map<String, String>): ApiResponse<PageData<ProductListItem>>

    /** 商品详情。 */
    @GET("products/{id}")
    suspend fun getProductDetail(@Path("id") id: Long): ApiResponse<ProductDetail>

    /** 商品评价列表。 */
    @GET("products/{id}/reviews")
    suspend fun getProductReviews(
        @Path("id") id: Long,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): ApiResponse<PageData<Review>>

    /** 店铺主页。 */
    @GET("shops/{id}")
    suspend fun getShop(@Path("id") id: Long): ApiResponse<ShopPublic>

    /** 店铺在售商品。 */
    @GET("shops/{id}/products")
    suspend fun getShopProducts(
        @Path("id") id: Long,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): ApiResponse<PageData<ProductListItem>>

    // ---------------- 交易闭环（阶段3，均需登录，走 AuthInterceptor 自动带 token） ----------------

    // 购物车
    @GET("cart")
    suspend fun getCart(): ApiResponse<CartList>

    @POST("cart")
    suspend fun addToCart(@Body body: AddCartRequest): ApiResponse<CartItem>

    @PUT("cart/{id}")
    suspend fun updateCartQty(@Path("id") id: Long, @Body body: UpdateCartRequest): ApiResponse<CartItem>

    @DELETE("cart/{id}")
    suspend fun deleteCartItem(@Path("id") id: Long): ApiResponse<Unit>

    @DELETE("cart")
    suspend fun clearCart(): ApiResponse<Unit>

    // 收货地址
    @GET("addresses")
    suspend fun getAddresses(): ApiResponse<AddressList>

    @POST("addresses")
    suspend fun createAddress(@Body body: AddressRequest): ApiResponse<Address>

    @PUT("addresses/{id}")
    suspend fun updateAddress(@Path("id") id: Long, @Body body: AddressRequest): ApiResponse<Address>

    @DELETE("addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: Long): ApiResponse<Unit>

    @POST("addresses/{id}/default")
    suspend fun setDefaultAddress(@Path("id") id: Long): ApiResponse<Address>

    // 订单
    @POST("orders/preview")
    suspend fun previewOrder(@Body body: OrderPreviewRequest): ApiResponse<OrderPreview>

    /** 创建订单，Idempotency-Key 头防重复提交（客户端生成 UUID）。 */
    @POST("orders")
    suspend fun createOrder(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: CreateOrderRequest,
    ): ApiResponse<CreateOrderResult>

    @GET("orders")
    suspend fun getOrders(@QueryMap query: Map<String, String>): ApiResponse<PageData<Order>>

    @GET("orders/{orderNo}")
    suspend fun getOrderDetail(@Path("orderNo") orderNo: String): ApiResponse<Order>

    @POST("orders/{orderNo}/cancel")
    suspend fun cancelOrder(@Path("orderNo") orderNo: String): ApiResponse<Order>

    @POST("orders/{orderNo}/confirm")
    suspend fun confirmOrder(@Path("orderNo") orderNo: String): ApiResponse<Order>

    @POST("orders/{orderNo}/refund")
    suspend fun refundOrder(@Path("orderNo") orderNo: String, @Body body: RefundRequest): ApiResponse<RefundResult>

    @POST("orders/{orderNo}/review")
    suspend fun submitReview(@Path("orderNo") orderNo: String, @Body body: ReviewRequest): ApiResponse<Review>

    // 支付（Mock）
    @POST("pay")
    suspend fun pay(@Body body: PayRequest): ApiResponse<PayResult>

    @POST("pay/mock/confirm")
    suspend fun mockConfirmPay(@Body body: MockConfirmRequest): ApiResponse<PayConfirmResult>
}
