package com.example.hytp.core.network.dto

/**
 * AI 试衣 DTO —— 对齐后端 TryonTask::toArray / UserAvatar::toArray。
 * 字段全默认值（Moshi 反射容错）。分页复用 PageData/Pagination。
 */

/** 试衣任务（status: 0处理中 1成功 2失败）。 */
data class TryonTask(
    val id: Long,
    val productId: Long = 0,
    val personUrl: String = "",
    val garmentUrl: String = "",
    val status: Int = 0,
    val resultUrl: String = "",
    val failReason: String = "",
    val createdAt: Long = 0,
) {
    companion object {
        const val STATUS_PENDING = 0
        const val STATUS_SUCCESS = 1
        const val STATUS_FAILED = 2
    }
}

/** 可复用形象照。 */
data class UserAvatar(
    val id: Long,
    val imageUrl: String = "",
    val createdAt: Long = 0,
)

/** 形象列表响应（后端 { list: [...] }）。 */
data class AvatarList(
    val list: List<UserAvatar> = emptyList(),
)

/** 提交试衣请求体。 */
data class SubmitTryonRequest(
    val productId: Long,
    val personUrl: String,
)

/** 新增形象请求体。 */
data class AddAvatarRequest(
    val imageUrl: String,
)

/** 删除类接口返回体 { id }（不能用 Unit —— Moshi 无法为 Unit 建转换器，请求会在发出前抛异常）。 */
data class IdResult(
    val id: Long = 0,
)
