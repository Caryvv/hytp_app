package com.example.hytp.core.network

import com.example.hytp.core.network.dto.AppVersionCheck
import com.example.hytp.core.network.dto.BannerItem
import com.example.hytp.core.network.dto.AddCartRequest
import com.example.hytp.core.network.dto.Address
import com.example.hytp.core.network.dto.AddressList
import com.example.hytp.core.network.dto.AddressRequest
import com.example.hytp.core.network.dto.CartItem
import com.example.hytp.core.network.dto.CartList
import com.example.hytp.core.network.dto.Category
import com.example.hytp.core.network.dto.ContentDetail
import com.example.hytp.core.network.dto.ContentFavoriteResult
import com.example.hytp.core.network.dto.ContentLikeResult
import com.example.hytp.core.network.dto.ContentListItem
import com.example.hytp.core.network.dto.EnrollResult
import com.example.hytp.core.network.dto.AddAvatarRequest
import com.example.hytp.core.network.dto.AvatarList
import com.example.hytp.core.network.dto.IdResult
import com.example.hytp.core.network.dto.SubmitTryonRequest
import com.example.hytp.core.network.dto.TryonTask
import com.example.hytp.core.network.dto.UserAvatar
import com.example.hytp.core.network.dto.SignupRequest
import com.example.hytp.core.network.dto.CreateOrderRequest
import com.example.hytp.core.network.dto.CreateOrderResult
import com.example.hytp.core.network.dto.DepositClaimRequest
import com.example.hytp.core.network.dto.DepositClaimResult
import com.example.hytp.core.network.dto.AddCommentRequest
import com.example.hytp.core.network.dto.QaAnswer
import com.example.hytp.core.network.dto.QaRequest
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
import com.example.hytp.core.network.dto.WithdrawRequest
import com.example.hytp.core.network.dto.WithdrawResult
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
import com.example.hytp.core.network.dto.StsToken
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

    // ---------------- 文旅 + 文化传承 内容（列表/详情免登录，互动需登录） ----------------

    /** 内容列表（type/city/category/sort/page/pageSize，空值不传）。 */
    @GET("contents")
    suspend fun getContents(@QueryMap query: Map<String, String>): ApiResponse<PageData<ContentListItem>>

    /** 内容详情。 */
    @GET("contents/{id}")
    suspend fun getContentDetail(@Path("id") id: Long): ApiResponse<ContentDetail>

    @POST("contents/{id}/like")
    suspend fun likeContent(@Path("id") id: Long): ApiResponse<ContentLikeResult>

    @POST("contents/{id}/unlike")
    suspend fun unlikeContent(@Path("id") id: Long): ApiResponse<ContentLikeResult>

    @POST("contents/{id}/favorite")
    suspend fun favoriteContent(@Path("id") id: Long): ApiResponse<ContentFavoriteResult>

    @POST("contents/{id}/unfavorite")
    suspend fun unfavoriteContent(@Path("id") id: Long): ApiResponse<ContentFavoriteResult>

    /** 报名预约，Idempotency-Key 防重复提交。 */
    @POST("contents/{id}/signup")
    suspend fun signupContent(
        @Path("id") id: Long,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: SignupRequest,
    ): ApiResponse<EnrollResult>

    @POST("contents/{id}/cancel-signup")
    suspend fun cancelSignupContent(@Path("id") id: Long): ApiResponse<EnrollResult>

    // ---------------- AI 试衣（需登录） ----------------

    /** 提交试衣任务，返回处理中的任务（含 id，前端据此轮询）。 */
    @POST("tryon/submit")
    suspend fun submitTryon(@Body body: SubmitTryonRequest): ApiResponse<TryonTask>

    /** 轮询任务结果。 */
    @GET("tryon/tasks/{id}")
    suspend fun pollTryon(@Path("id") id: Long): ApiResponse<TryonTask>

    /** 我的试衣历史（分页）。 */
    @GET("tryon/tasks")
    suspend fun getMyTryonTasks(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): ApiResponse<PageData<TryonTask>>

    /** 软删除试衣记录。返回 { id }，不能用 Unit（Moshi 无 Unit 适配器，请求发出前即抛异常）。 */
    @DELETE("tryon/tasks/{id}")
    suspend fun deleteTryonTask(@Path("id") id: Long): ApiResponse<IdResult>

    /** 我的可复用形象列表。 */
    @GET("tryon/avatars")
    suspend fun getAvatars(): ApiResponse<AvatarList>

    /** 新增形象。 */
    @POST("tryon/avatars")
    suspend fun addAvatar(@Body body: AddAvatarRequest): ApiResponse<UserAvatar>

    /** 删除形象。返回 { id }，同 deleteTryonTask 不能用 Unit。 */
    @DELETE("tryon/avatars/{id}")
    suspend fun deleteAvatar(@Path("id") id: Long): ApiResponse<IdResult>

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

    // ---------------- AI 智能问答 ----------------

    @POST("ai/qa")
    suspend fun aiQa(@Body body: QaRequest): ApiResponse<QaAnswer>

    // ---------------- 社群 ----------------

    @GET("groups")
    suspend fun getGroups(@QueryMap query: Map<String, String>): ApiResponse<PageData<SocialGroup>>

    /** 我加入的社群（附未读，用于消息中心）。 */
    @GET("groups/mine")
    suspend fun getMyGroups(@QueryMap query: Map<String, String>): ApiResponse<PageData<SocialGroup>>

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

    // 同袍币提现（Mock 即时扣减）
    @POST("wallet/withdraw")
    suspend fun withdraw(@Body body: WithdrawRequest): ApiResponse<WithdrawResult>

    // 任务系统（赚同袍币）
    @GET("tasks")
    suspend fun getTasks(): ApiResponse<TaskListResult>

    @POST("tasks/claim")
    suspend fun claimTask(@Body body: ClaimRequest): ApiResponse<ClaimResult>

    // 文件上传
    @Multipart
    @POST("upload")
    suspend fun uploadFile(@retrofit2.http.Part file: MultipartBody.Part): ApiResponse<UploadResult>

    /** OSS 直传临时凭证；未配置时后端返 enabled=false，客户端回退中转上传。 */
    @GET("upload/sts")
    suspend fun getStsToken(): ApiResponse<StsToken>

    // 首页
    @GET("home/banners")
    suspend fun getBanners(): ApiResponse<List<BannerItem>>

    @GET("home/feed")
    suspend fun getHomeFeed(@QueryMap query: Map<String, String>): ApiResponse<PageData<Feed>>

    // 应用内更新检查（免登录）
    @GET("app/version/check")
    suspend fun checkAppVersion(
        @Query("platform") platform: String,
        @Query("versionCode") versionCode: Int,
    ): ApiResponse<AppVersionCheck>
}
