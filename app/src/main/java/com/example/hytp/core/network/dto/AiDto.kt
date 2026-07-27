package com.example.hytp.core.network.dto

/** 对话历史一轮（role: user|assistant）。 */
data class QaTurn(
    val role: String,
    val content: String,
)

/** 智能问答请求体。history 传最近若干轮上下文。 */
data class QaRequest(
    val question: String,
    val history: List<QaTurn> = emptyList(),
)

/** 智能问答响应（对齐 AiQaService::ask）。 */
data class QaAnswer(
    val answer: String = "",
    val hitKnowledge: Boolean = false,
)
