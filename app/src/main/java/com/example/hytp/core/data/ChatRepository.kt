package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.ChatMessage
import com.example.hytp.core.network.dto.ChatMessageList
import com.example.hytp.core.network.dto.Conversation
import com.example.hytp.core.network.dto.OpenConversationRequest
import com.example.hytp.core.network.dto.OpenConversationResult
import com.example.hytp.core.network.dto.PageData
import com.example.hytp.core.network.dto.SendMessageRequest
import com.example.hytp.core.network.safeApiCall

/**
 * 私信仓库（需登录）。轮询拉取，afterId 增量。
 */
class ChatRepository(
    private val api: HytpApiService,
) {
    suspend fun getConversations(page: Int = 1, pageSize: Int = 20): ApiResult<PageData<Conversation>> =
        safeApiCall { api.getConversations(buildMap { put("page", page.toString()); put("pageSize", pageSize.toString()) }) }

    suspend fun openConversation(targetId: Long): ApiResult<OpenConversationResult> =
        safeApiCall { api.openConversation(OpenConversationRequest(targetId)) }

    suspend fun getMessages(conversationId: Long, afterId: Long = 0): ApiResult<ChatMessageList> =
        safeApiCall {
            api.getChatMessages(buildMap {
                put("conversationId", conversationId.toString())
                if (afterId > 0) put("afterId", afterId.toString())
            })
        }

    suspend fun sendMessage(conversationId: Long, content: String): ApiResult<ChatMessage> =
        safeApiCall { api.sendChatMessage(SendMessageRequest(conversationId, content)) }
}
