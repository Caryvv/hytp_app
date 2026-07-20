package com.example.hytp.feature.social.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.SocialRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.Feed
import com.example.hytp.core.network.dto.SocialProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileUiState(
    val loading: Boolean = false,
    val profile: SocialProfile? = null,
    val feeds: List<Feed> = emptyList(),
    val error: String? = null,
)

/**
 * 同袍公开主页：资料卡 + 统计 + 关注态 + TA 的动态列表。关注乐观更新。
 */
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val targetId: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = socialRepository.getUserProfile(targetId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(loading = false, profile = r.data) }
                    loadFeeds()
                }
                is ApiResult.Error -> _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    private fun loadFeeds() {
        viewModelScope.launch {
            when (val r = socialRepository.getUserFeeds(targetId, page = 1, pageSize = 20)) {
                is ApiResult.Success -> _uiState.update { it.copy(feeds = r.data.list) }
                else -> Unit
            }
        }
    }

    /** 关注/取关乐观更新。 */
    fun toggleFollow() {
        val p = _uiState.value.profile ?: return
        if (p.isSelf) return
        val target = !p.isFollowed
        _uiState.update { it.copy(profile = p.copy(isFollowed = target, followerCount = (p.followerCount + if (target) 1 else -1).coerceAtLeast(0))) }
        viewModelScope.launch {
            val r = if (target) socialRepository.follow(targetId) else socialRepository.unfollow(targetId)
            if (r is ApiResult.Success) {
                _uiState.update { s -> s.profile?.let { s.copy(profile = it.copy(isFollowed = r.data.followed, followerCount = r.data.followerCount)) } ?: s }
            } else {
                _uiState.update { it.copy(profile = p) }
            }
        }
    }
}
