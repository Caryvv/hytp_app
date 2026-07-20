package com.example.hytp.feature.chat.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.ChatRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.Conversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationListUiState(
    val loading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val error: String? = null,
)

/**
 * 会话列表：进入/返回时刷新（会话按 last_at 倒序，含未读数）。
 */
@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationListUiState())
    val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = chatRepository.getConversations()) {
                is ApiResult.Success -> _uiState.update { it.copy(loading = false, conversations = r.data.list) }
                is ApiResult.Error -> _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }
}
