package com.example.hytp.feature.ai.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.AiRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.QaTurn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 一条对话消息。fromUser=true 为用户提问，false 为 AI 回答。 */
data class QaMessage(
    val id: Long,
    val content: String,
    val fromUser: Boolean,
)

data class QaUiState(
    val messages: List<QaMessage> = listOf(
        QaMessage(0, "你好，我是汉服知识助手～ 形制、山正区分、身材选款、穿搭配饰，尽管问我。", fromUser = false),
    ),
    val draft: String = "",
    val sending: Boolean = false,
    val error: String? = null,
)

/**
 * 智能问答：单次一问一答，本地维护对话列表；提问时把最近历史作为上下文传给 AI。
 * AI 不可用时后端返兜底引导文案（hitKnowledge=false），仍作为一条 AI 消息展示。
 */
@HiltViewModel
class QaViewModel @Inject constructor(
    private val aiRepository: AiRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QaUiState())
    val uiState: StateFlow<QaUiState> = _uiState.asStateFlow()

    private var nextId = 1L

    /** 更新输入框草稿（存入 ViewModel，切 Tab 不丢）。 */
    fun setDraft(text: String) {
        _uiState.update { it.copy(draft = text) }
    }

    fun ask(question: String) {
        val q = question.trim()
        if (q.isBlank() || _uiState.value.sending) return

        // 先上屏用户消息，清空草稿
        val userMsg = QaMessage(nextId++, q, fromUser = true)
        _uiState.update { it.copy(messages = it.messages + userMsg, draft = "", sending = true, error = null) }

        // 取最近历史（排除开场白 id=0）作为上下文
        val history = _uiState.value.messages
            .filter { it.id != 0L }
            .takeLast(6)
            .map { QaTurn(role = if (it.fromUser) "user" else "assistant", content = it.content) }

        viewModelScope.launch {
            when (val r = aiRepository.qa(q, history)) {
                is ApiResult.Success -> {
                    val aiMsg = QaMessage(nextId++, r.data.answer, fromUser = false)
                    _uiState.update { it.copy(messages = it.messages + aiMsg, sending = false) }
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(sending = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(sending = false, error = "网络异常，请重试") }
            }
        }
    }
}
