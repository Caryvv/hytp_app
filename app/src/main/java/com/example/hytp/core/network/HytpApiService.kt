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
import com.example.hytp.core.network.dto.DepositClaimRequest
import com.example.hytp.core.network.dto.DepositClaimResult
import com.example.hytp.core.network.dto.AddCommentRequest
import com.example.hytp.core.network.dto.ChatMessage
import com.example.hytp.core.network.dto.ChatMessageList
import com.example.hytp.core.network.dto.Conversation
import com.example.hytp.core.network.dto.CreateGroupRequest
import com.example.hytp.core.network.dto.Feed
import com.example.hytp.core.network.dto.FeedComment
import com.example.hytp.core.network.dto.GroupMemberItem
import com.example.hytp.core.network.dto.GroupMessage
import com.example.hytp.core.network.dto.GroupMessageList
import com.example.hytp.core.network.dto.OpenConversationRequest
import com.example.hytp.core.network.dto.OpenConversationResult
import com.example.hytp.core.network.dto.SendGroupMessageRequest
import com.example.hytp.core.network.dto.SendMessageRequest
import com.example.hytp.core.network.dto.SocialGroup
import com.example.hytp.core.network.dto.FollowResult
import com.example.hytp.core.network.dto.FavoriteResult
import com.example.hytp.core.network.dto.LikeResult
import com.example.hytp.core.network.dto.PublishFeedRequest
import com.example.hytp.core.network.dto.RentOrderRequest
import com.example.hytp.core.network.dto.RentOrderResult
import com.example.hytp.core.network.dto.ShareResult
import com.example.hytp.core.network.dto.SocialProfile
import com.example.hytp.core.network.dto.TipRequest
import com.example.hytp.core.network.dto.TipResult
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
import com.example.hytp.core.network.dto.RechargeRequest
import com.example.hytp.core.network.dto.RechargeResult
import com.example.hytp.core.network.dto.ClaimRequest
import com.example.hytp.core.network.dto.ClaimResult
import com.example.hytp.core.network.dto.TaskListResult
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
import com.example.hytp.core.network.dto.UploadResult
import com.example.hytp.core.network.dto.UserProfile
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
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

    // 租赁 / 品质保障金（交易 P1）
    @POST("orders/rent")
    suspend fun createRent(@Body body: RentOrderRequest): ApiResponse<RentOrderResult>

    @POST("orders/{orderNo}/return")
    suspend fun returnOrder(@Path("orderNo") orderNo: String): ApiResponse<Order>

    @POST("orders/{orderNo}/deposit-claim")
    suspend fun applyDepositClaim(@Path("orderNo") orderNo: String, @Body body: DepositClaimRequest): ApiResponse<DepositClaimResult>

    // ---------------- 社交（阶段4 P0，均需登录） ----------------

    @GET("feeds")
    suspend fun getFeeds(@QueryMap query: Map<String, String>): ApiResponse<PageData<Feed>>

    @POST("feeds")
    suspend fun publishFeed(@Body body: PublishFeedRequest): ApiResponse<Feed>

    @GET("feeds/{id}")
    suspend fun getFeedDetail(@Path("id") id: Long): ApiResponse<Feed>

    @DELETE("feeds/{id}")
    suspend fun deleteFeed(@Path("id") id: Long): ApiResponse<Unit>

    @POST("feeds/{id}/like")
    suspend fun likeFeed(@Path("id") id: Long): ApiResponse<LikeResult>

    @POST("feeds/{id}/unlike")
    suspend fun unlikeFeed(@Path("id") id: Long): ApiResponse<LikeResult>

    @POST("feeds/{id}/favorite")
    suspend fun favoriteFeed(@Path("id") id: Long): ApiResponse<FavoriteResult>

    @POST("feeds/{id}/unfavorite")
    suspend fun unfavoriteFeed(@Path("id") id: Long): ApiResponse<FavoriteResult>

    @POST("feeds/{id}/share")
    suspend fun shareFeed(@Path("id") id: Long): ApiResponse<ShareResult>

    /** 打赏动态，Idempotency-Key 头防重复扣款（客户端生成 UUID）。 */
    @POST("feeds/{id}/tip")
    suspend fun tipFeed(
        @Path("id") id: Long,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: TipRequest,
    ): ApiResponse<TipResult>

    @GET("feeds/{id}/comments")
    suspend fun getFeedComments(
        @Path("id") id: Long,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): ApiResponse<PageData<FeedComment>>

    @POST("feeds/{id}/comments")
    suspend fun addFeedComment(@Path("id") id: Long, @Body body: AddCommentRequest): ApiResponse<FeedComment>

    @POST("users/{id}/follow")
    suspend fun followUser(@Path("id") id: Long): ApiResponse<FollowResult>

    @POST("users/{id}/unfollow")
    suspend fun unfollowUser(@Path("id") id: Long): ApiResponse<FollowResult>

    @GET("users/{id}/profile")
    suspend fun getUserPublicProfile(@Path("id") id: Long): ApiResponse<SocialProfile>

    @GET("users/{id}/feeds")
    suspend fun getUserFeeds(@Path("id") id: Long, @QueryMap query: Map<String, String>): ApiResponse<PageData<Feed>>

    // ---------------- 私信（社交 P1，轮询） ----------------

    @GET("chat/conversations")
    suspend fun getConversations(@QueryMap query: Map<String, String>): ApiResponse<PageData<Conversation>>

    @POST("chat/open")
    suspend fun openConversation(@Body body: OpenConversationRequest): ApiResponse<OpenConversationResult>

    @GET("chat/messages")
    suspend fun getChatMessages(@QueryMap query: Map<String, String>): ApiResponse<ChatMessageList>

    @POST("chat/messages")
    suspend fun sendChatMessage(@Body body: SendMessageRequest): ApiResponse<ChatMessage>

    // ---------------- 社群 ----------------

    @GET("groups")
    suspend fun getGroups(@QueryMap query: Map<String, String>): ApiResponse<PageData<SocialGroup>>

    @POST("groups")
    suspend fun createGroup(@Body body: CreateGroupRequest): ApiResponse<SocialGroup>

    @GET("groups/{id}")
    suspend fun getGroupDetail(@Path("id") id: Long): ApiResponse<SocialGroup>

    @POST("groups/{id}/join")
    suspend fun joinGroup(@Path("id") id: Long): ApiResponse<SocialGroup>

    @POST("groups/{id}/quit")
    suspend fun quitGroup(@Path("id") id: Long): ApiResponse<SocialGroup>

    @GET("groups/{id}/members")
    suspend fun getGroupMembers(@Path("id") id: Long, @QueryMap query: Map<String, String>): ApiResponse<PageData<GroupMemberItem>>

    @GET("groups/{id}/messages")
    suspend fun getGroupMessages(@Path("id") id: Long, @QueryMap query: Map<String, String>): ApiResponse<GroupMessageList>

    @POST("groups/{id}/messages")
    suspend fun sendGroupMessage(@Path("id") id: Long, @Body body: SendGroupMessageRequest): ApiResponse<GroupMessage>

    // 支付（Mock）
    @POST("pay")
    suspend fun pay(@Body body: PayRequest): ApiResponse<PayResult>

    @POST("pay/mock/confirm")
    suspend fun mockConfirmPay(@Body body: MockConfirmRequest): ApiResponse<PayConfirmResult>

    // 同袍币充值（Mock）
    @POST("wallet/recharge")
    suspend fun recharge(@Body body: RechargeRequest): ApiResponse<RechargeResult>

    // 任务系统（赚同袍币）
    @GET("tasks")
    suspend fun getTasks(): ApiResponse<TaskListResult>

    @POST("tasks/claim")
    suspend fun claimTask(@Body body: ClaimRequest): ApiResponse<ClaimResult>

    // 文件上传
    @Multipart
    @POST("upload")
    suspend fun uploadFile(@retrofit2.http.Part file: MultipartBody.Part): ApiResponse<UploadResult>
}
