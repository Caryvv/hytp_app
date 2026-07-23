package com.example.hytp.feature.message.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.ChatRepository
import com.example.hytp.core.data.GroupRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.Conversation
import com.example.hytp.core.network.dto.SocialGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessageCenterUiState(
    val loading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val groups: List<SocialGroup> = emptyList(),
    val error: String? = null,
)

/**
 * 消息中心：聚合「私信会话」+「我加入的社群」，各带未读角标。
 * 两端点并行拉取；两者皆失败才报错。每次 resume 刷新（保证聊天页返回后角标更新）。
 */
@HiltViewModel
class MessageCenterViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val groupRepository: GroupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageCenterUiState())
    val uiState: StateFlow<MessageCenterUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val convDeferred = async { chatRepository.getConversations() }
            val groupDeferred = async { groupRepository.myGroups() }
            val convResult = convDeferred.await()
            val groupResult = groupDeferred.await()

            val conversations = (convResult as? ApiResult.Success)?.data?.list ?: emptyList()
            val groups = (groupResult as? ApiResult.Success)?.data?.list ?: emptyList()
            val bothFailed = convResult !is ApiResult.Success && groupResult !is ApiResult.Success

            _uiState.update {
                it.copy(
                    loading = false,
                    conversations = conversations,
                    groups = groups,
                    error = if (bothFailed) "加载失败，请重试" else null,
                )
            }
        }
    }
}
