package com.example.hytp.core.network.dto

/**
 * 社群 DTO（社交 P1）—— 对齐后端 SocialGroup + Service 附加 isJoined/myRole。
 */

/** 社群（对齐 SocialGroup::toArray + decorate 附加）。 */
data class SocialGroup(
    val id: Long,
    val name: String = "",
    val type: Int = 1,
    val ownerId: Long = 0,
    val avatar: String = "",
    val intro: String = "",
    val city: String = "",
    val memberCount: Int = 0,
    val status: Int = 1,
    val createdAt: Long = 0,
    val isJoined: Boolean = false,
    val myRole: Int? = null, // 0成员 1管理 2群主
)

/** 群成员（SocialProfile + role）。用宽松结构：复用 SocialProfile 字段 + role。 */
data class GroupMemberItem(
    val id: Long,
    val nickname: String = "",
    val avatar: String? = null,
    val gender: Int = 0,
    val city: String? = null,
    val role: Int = 0,
)

/** 群消息（对齐 GroupMessage::toArray + sender）。 */
data class GroupMessage(
    val id: Long,
    val groupId: Long = 0,
    val fromUser: Long = 0,
    val content: String = "",
    val msgType: Int = 1,
    val createdAt: Long = 0,
    val sender: SocialProfile? = null,
)

data class GroupMessageList(
    val list: List<GroupMessage> = emptyList(),
)

/** 创建社群请求体。 */
data class CreateGroupRequest(
    val name: String,
    val type: Int = 1,
    val avatar: String = "",
    val intro: String = "",
    val city: String = "",
)

/** 发群消息请求体。 */
data class SendGroupMessageRequest(
    val content: String,
)
