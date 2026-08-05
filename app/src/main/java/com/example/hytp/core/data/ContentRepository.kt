package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.ContentDetail
import com.example.hytp.core.network.dto.ContentFavoriteResult
import com.example.hytp.core.network.dto.ContentLikeResult
import com.example.hytp.core.network.dto.ContentListItem
import com.example.hytp.core.network.dto.EnrollResult
import com.example.hytp.core.network.dto.PageData
import com.example.hytp.core.network.dto.SignupRequest
import com.example.hytp.core.network.safeApiCall

/**
 * 文旅 + 文化传承 内容仓库。
 * 列表/详情免登录可浏览（登录后带互动态）；点赞/收藏/报名需登录（token 由 AuthInterceptor 自动附加）。
 */
class ContentRepository(
    private val api: HytpApiService,
) {

    /**
     * 内容列表（type/city/category 筛选 + 分页）。空/默认值不拼进 query。
     * type：1文旅 2文化传承。
     */
    suspend fun getContents(
        type: Int? = null,
        city: String? = null,
        category: String? = null,
        sort: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): ApiResult<PageData<ContentListItem>> {
        val query = buildMap {
            type?.let { if (it > 0) put("type", it.toString()) }
            city?.takeIf { it.isNotBlank() }?.let { put("city", it) }
            category?.takeIf { it.isNotBlank() }?.let { put("category", it) }
            sort?.takeIf { it.isNotBlank() }?.let { put("sort", it) }
            put("page", page.toString())
            put("pageSize", pageSize.toString())
        }
        return safeApiCall { api.getContents(query) }
    }

    /** 内容详情。 */
    suspend fun getContentDetail(id: Long): ApiResult<ContentDetail> =
        safeApiCall { api.getContentDetail(id) }

    suspend fun like(id: Long): ApiResult<ContentLikeResult> = safeApiCall { api.likeContent(id) }
    suspend fun unlike(id: Long): ApiResult<ContentLikeResult> = safeApiCall { api.unlikeContent(id) }
    suspend fun favorite(id: Long): ApiResult<ContentFavoriteResult> = safeApiCall { api.favoriteContent(id) }
    suspend fun unfavorite(id: Long): ApiResult<ContentFavoriteResult> = safeApiCall { api.unfavoriteContent(id) }

    /** 报名预约，UUID 作幂等键防重复提交（仿 SocialRepository.tip）。 */
    suspend fun signup(id: Long, name: String, phone: String, quantity: Int): ApiResult<EnrollResult> =
        safeApiCall {
            api.signupContent(id, java.util.UUID.randomUUID().toString(), SignupRequest(name, phone, quantity))
        }

    suspend fun cancelSignup(id: Long): ApiResult<EnrollResult> =
        safeApiCall { api.cancelSignupContent(id) }
}
