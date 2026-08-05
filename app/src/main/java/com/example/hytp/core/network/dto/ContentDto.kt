package com.example.hytp.core.network.dto

/**
 * 文旅 + 文化传承 内容 DTO —— 对齐后端 Content::toListArray / toDetailArray + Service 附加互动态。
 * type：1文旅 2文化传承。字段全默认值（Moshi 反射容错），List → emptyList()。
 */

/** 内容列表卡片项（对齐 Content::toListArray + isLiked/isFavorited/isSignedUp）。 */
data class ContentListItem(
    val id: Long,
    val type: Int = 1,
    val title: String = "",
    val cover: String = "",
    val city: String = "",
    val category: String = "",
    val likeCount: Int = 0,
    val favoriteCount: Int = 0,
    val signupCount: Int = 0,
    val status: Int = 1,
    val createdAt: Long = 0,
    val isLiked: Boolean = false,
    val isFavorited: Boolean = false,
    val isSignedUp: Boolean = false,
)

/** 内容详情（对齐 Content::toDetailArray + 互动态）。 */
data class ContentDetail(
    val id: Long,
    val type: Int = 1,
    val title: String = "",
    val cover: String = "",
    val city: String = "",
    val category: String = "",
    val images: List<String> = emptyList(),
    val detail: String = "",
    val likeCount: Int = 0,
    val favoriteCount: Int = 0,
    val signupCount: Int = 0,
    val status: Int = 1,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val isLiked: Boolean = false,
    val isFavorited: Boolean = false,
    val isSignedUp: Boolean = false,
)

/** 报名预约请求体。 */
data class SignupRequest(
    val name: String,
    val phone: String,
    val quantity: Int = 1,
)

// 互动响应（对齐后端 ContentService 返回）
data class ContentLikeResult(val liked: Boolean = false, val likeCount: Int = 0)
data class ContentFavoriteResult(val favorited: Boolean = false, val favoriteCount: Int = 0)
data class EnrollResult(val enrolled: Boolean = false, val signupCount: Int = 0)
