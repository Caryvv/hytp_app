package com.example.hytp.core.network.dto

/**
 * 私信 DTO（社交 P1）—— 对齐后端 ChatConversation/ChatMessage + Service 附加字段。
 * 复用 SocialProfile(社交公开资料)。时间戳 Long 秒级。字段全默认值。
 */

/** 会话（对齐 ChatService.conversations：会话 + 对方资料 + 未读数）。 */
data class Conversation(
    val id: Long,
    val lastMsg: String = "",
    val lastAt: Long = 0,
    val target: SocialProfile? = null,
    val unread: Int = 0,
)

/** 打开会话响应（会话 id + 对方资料）。 */
data class OpenConversationResult(
    val id: Long,
    val lastMsg: String = "",
    val lastAt: Long = 0,
    val target: SocialProfile? = null,
)

/** 私信消息（对齐 ChatMessage::toArray）。 */
data class ChatMessage(
    val id: Long,
    val conversationId: Long = 0,
    val fromUser: Long = 0,
    val toUser: Long = 0,
    val content: String = "",
    val msgType: Int = 1,
    val isRead: Int = 0,
    val createdAt: Long = 0,
)

/** 消息列表响应 data：{ list }（无分页，afterId 增量）。 */
data class ChatMessageList(
    val list: List<ChatMessage> = emptyList(),
)

/** 发私信请求体。 */
data class SendMessageRequest(
    val conversationId: Long,
    val content: String,
)

/** 打开会话请求体。 */
data class OpenConversationRequest(
    val targetId: Long,
)
