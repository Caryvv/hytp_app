package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.QaAnswer
import com.example.hytp.core.network.dto.QaRequest
import com.example.hytp.core.network.dto.QaTurn
import com.example.hytp.core.network.safeApiCall

/**
 * AI 能力仓库（需登录）。智能问答：单次一问一答，history 传上下文。
 */
class AiRepository(
    private val api: HytpApiService,
) {
    suspend fun qa(question: String, history: List<QaTurn>): ApiResult<QaAnswer> =
        safeApiCall { api.aiQa(QaRequest(question = question, history = history)) }
}
