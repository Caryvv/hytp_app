package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.CreateGroupRequest
import com.example.hytp.core.network.dto.GroupMemberItem
import com.example.hytp.core.network.dto.GroupMessage
import com.example.hytp.core.network.dto.GroupMessageList
import com.example.hytp.core.network.dto.PageData
import com.example.hytp.core.network.dto.SendGroupMessageRequest
import com.example.hytp.core.network.dto.SocialGroup
import com.example.hytp.core.network.safeApiCall

/**
 * 社群仓库（需登录）。
 */
class GroupRepository(
    private val api: HytpApiService,
) {
    suspend fun list(type: Int? = null, city: String? = null, page: Int = 1, pageSize: Int = 20): ApiResult<PageData<SocialGroup>> =
        safeApiCall {
            api.getGroups(buildMap {
                type?.let { if (it > 0) put("type", it.toString()) }
                city?.takeIf { it.isNotBlank() }?.let { put("city", it) }
                put("page", page.toString())
                put("pageSize", pageSize.toString())
            })
        }

    suspend fun create(body: CreateGroupRequest): ApiResult<SocialGroup> =
        safeApiCall { api.createGroup(body) }

    /** 我加入的社群（附未读，用于消息中心）。 */
    suspend fun myGroups(page: Int = 1, pageSize: Int = 20): ApiResult<PageData<SocialGroup>> =
        safeApiCall { api.getMyGroups(buildMap { put("page", page.toString()); put("pageSize", pageSize.toString()) }) }

    suspend fun detail(id: Long): ApiResult<SocialGroup> =
        safeApiCall { api.getGroupDetail(id) }

    suspend fun join(id: Long): ApiResult<SocialGroup> =
        safeApiCall { api.joinGroup(id) }

    suspend fun quit(id: Long): ApiResult<SocialGroup> =
        safeApiCall { api.quitGroup(id) }

    suspend fun members(id: Long, page: Int = 1, pageSize: Int = 50): ApiResult<PageData<GroupMemberItem>> =
        safeApiCall { api.getGroupMembers(id, buildMap { put("page", page.toString()); put("pageSize", pageSize.toString()) }) }

    suspend fun getMessages(id: Long, afterId: Long = 0): ApiResult<GroupMessageList> =
        safeApiCall {
            api.getGroupMessages(id, buildMap { if (afterId > 0) put("afterId", afterId.toString()) })
        }

    suspend fun sendMessage(id: Long, content: String): ApiResult<GroupMessage> =
        safeApiCall { api.sendGroupMessage(id, SendGroupMessageRequest(content)) }
}
