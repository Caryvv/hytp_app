package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.AddCommentRequest
import com.example.hytp.core.network.dto.Feed
import com.example.hytp.core.network.dto.FeedComment
import com.example.hytp.core.network.dto.FavoriteResult
import com.example.hytp.core.network.dto.FollowResult
import com.example.hytp.core.network.dto.LikeResult
import com.example.hytp.core.network.dto.PageData
import com.example.hytp.core.network.dto.PublishFeedRequest
import com.example.hytp.core.network.dto.ShareResult
import com.example.hytp.core.network.dto.SocialProfile
import com.example.hytp.core.network.dto.TipRequest
import com.example.hytp.core.network.dto.TipResult
import com.example.hytp.core.network.safeApiCall

/**
 * 社交仓库（需登录，token 由 AuthInterceptor 自动附加）。
 */
class SocialRepository(
    private val api: HytpApiService,
) {
    /** 推荐流。 */
    suspend fun getRecommendFeeds(page: Int = 1, pageSize: Int = 20): ApiResult<PageData<Feed>> =
        safeApiCall { api.getFeeds(buildMap { put("tab", "recommend"); put("page", page.toString()); put("pageSize", pageSize.toString()) }) }

    /** 关注流。 */
    suspend fun getFollowingFeeds(page: Int = 1, pageSize: Int = 20): ApiResult<PageData<Feed>> =
        safeApiCall { api.getFeeds(buildMap { put("tab", "following"); put("page", page.toString()); put("pageSize", pageSize.toString()) }) }

    suspend fun publish(body: PublishFeedRequest): ApiResult<Feed> =
        safeApiCall { api.publishFeed(body) }

    suspend fun getFeedDetail(id: Long): ApiResult<Feed> =
        safeApiCall { api.getFeedDetail(id) }

    suspend fun deleteFeed(id: Long): ApiResult<Unit> =
        safeApiCall { api.deleteFeed(id) }

    suspend fun like(id: Long): ApiResult<LikeResult> = safeApiCall { api.likeFeed(id) }
    suspend fun unlike(id: Long): ApiResult<LikeResult> = safeApiCall { api.unlikeFeed(id) }
    suspend fun favorite(id: Long): ApiResult<FavoriteResult> = safeApiCall { api.favoriteFeed(id) }
    suspend fun unfavorite(id: Long): ApiResult<FavoriteResult> = safeApiCall { api.unfavoriteFeed(id) }
    suspend fun share(id: Long): ApiResult<ShareResult> = safeApiCall { api.shareFeed(id) }

    /** 打赏动态，UUID 作幂等键防重复扣款（仿 createOrder）。 */
    suspend fun tip(id: Long, coin: Int): ApiResult<TipResult> =
        safeApiCall { api.tipFeed(id, java.util.UUID.randomUUID().toString(), TipRequest(coin)) }

    suspend fun getComments(id: Long, page: Int = 1, pageSize: Int = 20): ApiResult<PageData<FeedComment>> =
        safeApiCall { api.getFeedComments(id, page, pageSize) }

    suspend fun addComment(id: Long, content: String, parentId: Long? = null): ApiResult<FeedComment> =
        safeApiCall { api.addFeedComment(id, AddCommentRequest(content = content, parentId = parentId)) }

    suspend fun follow(userId: Long): ApiResult<FollowResult> = safeApiCall { api.followUser(userId) }
    suspend fun unfollow(userId: Long): ApiResult<FollowResult> = safeApiCall { api.unfollowUser(userId) }

    suspend fun getUserProfile(userId: Long): ApiResult<SocialProfile> =
        safeApiCall { api.getUserPublicProfile(userId) }

    suspend fun getUserFeeds(userId: Long, page: Int = 1, pageSize: Int = 20): ApiResult<PageData<Feed>> =
        safeApiCall { api.getUserFeeds(userId, buildMap { put("page", page.toString()); put("pageSize", pageSize.toString()) }) }
}
