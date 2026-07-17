package com.example.hytp.core.network

import com.example.hytp.core.network.dto.Category
import com.example.hytp.core.network.dto.LoginRequest
import com.example.hytp.core.network.dto.LoginResponse
import com.example.hytp.core.network.dto.LogoutRequest
import com.example.hytp.core.network.dto.PageData
import com.example.hytp.core.network.dto.ProductDetail
import com.example.hytp.core.network.dto.ProductListItem
import com.example.hytp.core.network.dto.RefreshRequest
import com.example.hytp.core.network.dto.RefreshResponse
import com.example.hytp.core.network.dto.Review
import com.example.hytp.core.network.dto.ShopPublic
import com.example.hytp.core.network.dto.SmsSendRequest
import com.example.hytp.core.network.dto.SmsSendResponse
import com.example.hytp.core.network.dto.UpdateProfileRequest
import com.example.hytp.core.network.dto.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
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
}
