package com.example.hytp.feature.group.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.GroupRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.CreateGroupRequest
import com.example.hytp.core.network.dto.SocialGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupListUiState(
    val loading: Boolean = false,
    val groups: List<SocialGroup> = emptyList(),
    val error: String? = null,
    val creating: Boolean = false,
)

/**
 * 社群列表 + 建群。
 */
@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupListUiState())
    val uiState: StateFlow<GroupListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = groupRepository.list()) {
                is ApiResult.Success -> _uiState.update { it.copy(loading = false, groups = r.data.list) }
                is ApiResult.Error -> _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    fun create(name: String, type: Int, city: String, intro: String, onCreated: (Long) -> Unit) {
        if (name.isBlank()) return
        _uiState.update { it.copy(creating = true) }
        viewModelScope.launch {
            when (val r = groupRepository.create(CreateGroupRequest(name = name.trim(), type = type, city = city.trim(), intro = intro.trim()))) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(creating = false) }
                    load()
                    onCreated(r.data.id)
                }
                is ApiResult.Error -> _uiState.update { it.copy(creating = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(creating = false, error = "网络异常，请重试") }
            }
        }
    }
}
