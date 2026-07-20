package com.example.hytp.core.network.dto

/**
 * 社交域 DTO（阶段4 P0）—— 对齐后端 Feed/FeedComment/User::toPublicArray + Service 附加字段。
 * 时间戳 Long 秒级；图片 URL List<String>；字段全默认值（Moshi 反射容错）。
 */

/** 同袍公开资料（对齐 User::toPublicArray + profile 接口附加 isFollowed/isSelf）。 */
data class SocialProfile(
    val id: Long,
    val nickname: String = "",
    val avatar: String? = null,
    val gender: Int = 0,
    val city: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val feedCount: Int = 0,
    val isFollowed: Boolean = false,
    val isSelf: Boolean = false,
)

/** 动态（对齐 Feed::toDetailArray + Service 附加 author/isLiked/isFavorited）。 */
data class Feed(
    val id: Long,
    val userId: Long = 0,
    val content: String = "",
    val mediaType: Int = 1,
    val media: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val productIds: List<Long> = emptyList(),
    val city: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val favoriteCount: Int = 0,
    val shareCount: Int = 0,
    val status: Int = 1,
    val createdAt: Long = 0,
    val author: SocialProfile? = null,
    val isLiked: Boolean = false,
    val isFavorited: Boolean = false,
)

/** 动态评论（对齐 FeedComment::toArray + author）。 */
data class FeedComment(
    val id: Long,
    val feedId: Long = 0,
    val userId: Long = 0,
    val parentId: Long? = null,
    val content: String = "",
    val createdAt: Long = 0,
    val author: SocialProfile? = null,
)

/** 发布动态请求体。 */
data class PublishFeedRequest(
    val content: String,
    val media: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val productIds: List<Long> = emptyList(),
    val city: String = "",
    val mediaType: Int = 1,
)

/** 发表评论请求体。 */
data class AddCommentRequest(
    val content: String,
    val parentId: Long? = null,
)

// 互动响应
data class LikeResult(val liked: Boolean = false, val likeCount: Int = 0)
data class FavoriteResult(val favorited: Boolean = false, val favoriteCount: Int = 0)
data class ShareResult(val shareCount: Int = 0)
data class FollowResult(val followed: Boolean = false, val followerCount: Int = 0)
