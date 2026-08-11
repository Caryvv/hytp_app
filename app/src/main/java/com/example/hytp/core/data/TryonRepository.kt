package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.AddAvatarRequest
import com.example.hytp.core.network.dto.IdResult
import com.example.hytp.core.network.dto.PageData
import com.example.hytp.core.network.dto.SubmitTryonRequest
import com.example.hytp.core.network.dto.TryonTask
import com.example.hytp.core.network.dto.UserAvatar
import com.example.hytp.core.network.safeApiCall

/**
 * AI 试衣仓库（需登录，token 由 AuthInterceptor 自动附加）。
 * 提交任务后由 ViewModel 定时 poll 轮询结果（阿里云异步任务，单次几十秒）。
 */
class TryonRepository(
    private val api: HytpApiService,
) {
    /** 提交试衣任务，返回处理中的任务（前端据 id 轮询）。 */
    suspend fun submit(productId: Long, personUrl: String): ApiResult<TryonTask> =
        safeApiCall { api.submitTryon(SubmitTryonRequest(productId, personUrl)) }

    /** 轮询任务结果。 */
    suspend fun poll(taskId: Long): ApiResult<TryonTask> =
        safeApiCall { api.pollTryon(taskId) }

    /** 我的试衣历史。 */
    suspend fun myTasks(page: Int = 1, pageSize: Int = 20): ApiResult<PageData<TryonTask>> =
        safeApiCall { api.getMyTryonTasks(page, pageSize) }

    /** 软删除试衣记录。 */
    suspend fun deleteTask(id: Long): ApiResult<IdResult> =
        safeApiCall { api.deleteTryonTask(id) }

    /** 我的可复用形象列表。 */
    suspend fun avatars(): ApiResult<List<UserAvatar>> {
        return when (val r = safeApiCall { api.getAvatars() }) {
            is ApiResult.Success -> ApiResult.Success(r.data.list)
            is ApiResult.Error -> r
            is ApiResult.Failure -> r
        }
    }

    /** 新增形象。 */
    suspend fun addAvatar(imageUrl: String): ApiResult<UserAvatar> =
        safeApiCall { api.addAvatar(AddAvatarRequest(imageUrl)) }

    /** 删除形象。 */
    suspend fun deleteAvatar(id: Long): ApiResult<IdResult> =
        safeApiCall { api.deleteAvatar(id) }
}
