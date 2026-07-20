package com.example.hytp.feature.group.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.GroupRepository
import com.example.hytp.core.data.UserSessionManager
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.GroupMessage
import com.example.hytp.core.network.dto.SocialGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupChatUiState(
    val loading: Boolean = false,
    val group: SocialGroup? = null,
    val messages: List<GroupMessage> = emptyList(),
    val error: String? = null,
    val sending: Boolean = false,
    val quit: Boolean = false,
)

/**
 * 社群详情 + 群聊：详情含 isJoined/myRole；已加入才轮询群消息。加入/退出/发消息。
 */
@HiltViewModel
class GroupChatViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    sessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val groupId: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L
    val myUserId: Long = sessionManager.currentUserId() ?: 0L

    private val _uiState = MutableStateFlow(GroupChatUiState())
    val uiState: StateFlow<GroupChatUiState> = _uiState.asStateFlow()

    private var lastId: Long = 0
    private var polling = false

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = groupRepository.detail(groupId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(loading = false, group = r.data) }
                    if (r.data.isJoined) {
                        loadMessages()
                        startPolling()
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            when (val r = groupRepository.getMessages(groupId, 0)) {
                is ApiResult.Success -> {
                    lastId = r.data.list.lastOrNull()?.id ?: 0
                    _uiState.update { it.copy(messages = r.data.list) }
                }
                else -> Unit
            }
        }
    }

    private fun startPolling() {
        if (polling) return
        polling = true
        viewModelScope.launch {
            while (isActive) {
                delay(3000)
                if (_uiState.value.group?.isJoined != true) continue
                val r = groupRepository.getMessages(groupId, lastId)
                if (r is ApiResult.Success && r.data.list.isNotEmpty()) {
                    lastId = r.data.list.last().id
                    _uiState.update { it.copy(messages = it.messages + r.data.list) }
                }
            }
        }
    }

    fun join() {
        viewModelScope.launch {
            when (val r = groupRepository.join(groupId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(group = r.data) }
                    loadMessages()
                    startPolling()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(error = "网络异常，请重试") }
            }
        }
    }

    fun quit() {
        viewModelScope.launch {
            when (groupRepository.quit(groupId)) {
                is ApiResult.Success -> _uiState.update { it.copy(quit = true) }
                else -> _uiState.update { it.copy(error = "退出失败") }
            }
        }
    }

    fun send(content: String) {
        if (content.isBlank()) return
        _uiState.update { it.copy(sending = true) }
        viewModelScope.launch {
            when (val r = groupRepository.sendMessage(groupId, content.trim())) {
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
