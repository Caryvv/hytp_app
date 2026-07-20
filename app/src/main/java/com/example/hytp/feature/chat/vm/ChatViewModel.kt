package com.example.hytp.feature.chat.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.ChatRepository
import com.example.hytp.core.data.UserSessionManager
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val loading: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val error: String? = null,
    val sending: Boolean = false,
)

/**
 * 一对一聊天：首次全量拉取 + 轮询 afterId 增量（每 3 秒）。
 * conversationId 经导航参数注入。
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    sessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val conversationId: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L
    val myUserId: Long = sessionManager.currentUserId() ?: 0L

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var lastId: Long = 0

    init {
        loadInitial()
        startPolling()
    }

    private fun loadInitial() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = chatRepository.getMessages(conversationId, 0)) {
                is ApiResult.Success -> {
                    lastId = r.data.list.lastOrNull()?.id ?: 0
                    _uiState.update { it.copy(loading = false, messages = r.data.list) }
                }
                is ApiResult.Error -> _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    /** 轮询增量消息（3s），ViewModel 销毁时 viewModelScope 自动取消。 */
    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(3000)
                val r = chatRepository.getMessages(conversationId, lastId)
                if (r is ApiResult.Success && r.data.list.isNotEmpty()) {
                    lastId = r.data.list.last().id
                    _uiState.update { it.copy(messages = it.messages + r.data.list) }
                }
            }
        }
    }

    fun send(content: String) {
        if (content.isBlank()) return
        _uiState.update { it.copy(sending = true) }
        viewModelScope.launch {
            when (val r = chatRepository.sendMessage(conversationId, content.trim())) {
                is ApiResult.Success -> {
                    lastId = maxOf(lastId, r.data.id)
                    _uiState.update { it.copy(sending = false, messages = it.messages + r.data) }
                }
                is ApiResult.Error -> _uiState.update { it.copy(sending = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(sending = false, error = "网络异常，请重试") }
            }
        }
    }
}
