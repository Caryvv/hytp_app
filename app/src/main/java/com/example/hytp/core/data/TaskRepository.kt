package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.ClaimRequest
import com.example.hytp.core.network.dto.ClaimResult
import com.example.hytp.core.network.dto.TaskListResult
import com.example.hytp.core.network.safeApiCall

/**
 * 任务仓库（赚同袍币，需登录）。
 * 签到走 claim 主动领取；发动态/关注/首单由后端行为埋点自动发奖。
 */
class TaskRepository(
    private val api: HytpApiService,
) {
    suspend fun getTasks(): ApiResult<TaskListResult> =
        safeApiCall { api.getTasks() }

    suspend fun claim(taskKey: String): ApiResult<ClaimResult> =
        safeApiCall { api.claimTask(ClaimRequest(taskKey)) }
}
