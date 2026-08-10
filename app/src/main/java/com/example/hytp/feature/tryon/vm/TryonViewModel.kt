package com.example.hytp.feature.tryon.vm

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.TryonRepository
import com.example.hytp.core.data.UploadRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.TryonTask
import com.example.hytp.core.network.dto.UserAvatar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TryonUiState(
    val avatars: List<UserAvatar> = emptyList(),
    val selectedPersonUrl: String? = null, // 选中的形象照 URL（已存的或刚上传的）
    val uploading: Boolean = false,        // 上传新照中
    val submitting: Boolean = false,       // 提交任务中
    val polling: Boolean = false,          // 轮询结果中
    val resultUrl: String? = null,         // 成功结果图
    val error: String? = null,
    val message: String? = null,
)

/**
 * AI 试衣：选/传人物照 → 提交(服装图后端从 product.tryon_model_url 取) → 轮询结果。
 * 阿里云异步任务单次几十秒，轮询 2.5s 间隔、上限防死循环。
 */
@HiltViewModel
class TryonViewModel @Inject constructor(
    private val tryonRepository: TryonRepository,
    private val uploadRepository: UploadRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<String>("productId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(TryonUiState())
    val uiState: StateFlow<TryonUiState> = _uiState.asStateFlow()

    private companion object {
        const val POLL_INTERVAL_MS = 2500L
        const val POLL_MAX = 40 // 40 * 2.5s = 100s 上限
    }

    init {
        loadAvatars()
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }

    fun loadAvatars() {
        viewModelScope.launch {
            when (val r = tryonRepository.avatars()) {
                is ApiResult.Success -> _uiState.update { s ->
                    // 默认选中第一张形象（若还没选）
                    s.copy(avatars = r.data, selectedPersonUrl = s.selectedPersonUrl ?: r.data.firstOrNull()?.imageUrl)
                }
                else -> Unit
            }
        }
    }

    fun selectAvatar(url: String) {
        _uiState.update { it.copy(selectedPersonUrl = url) }
    }

    /** 拍照/选图上传新人物照：传 OSS → 存为可复用形象 → 选中。 */
    fun uploadNewPhoto(uri: Uri) {
        _uiState.update { it.copy(uploading = true, error = null) }
        viewModelScope.launch {
            when (val up = uploadRepository.uploadImage(uri)) {
                is ApiResult.Success -> {
                    val url = up.data.url
                    // 存为可复用形象（失败不阻断，仍可用本次 url 试穿）
                    tryonRepository.addAvatar(url)
                    loadAvatars()
                    _uiState.update { it.copy(uploading = false, selectedPersonUrl = url) }
                }
                is ApiResult.Error -> _uiState.update { it.copy(uploading = false, error = up.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(uploading = false, error = "上传失败，请重试") }
            }
        }
    }

    fun deleteAvatar(avatar: UserAvatar) {
        viewModelScope.launch {
            if (tryonRepository.deleteAvatar(avatar.id) is ApiResult.Success) {
                _uiState.update { s ->
                    val left = s.avatars.filter { it.id != avatar.id }
                    s.copy(
                        avatars = left,
                        selectedPersonUrl = if (s.selectedPersonUrl == avatar.imageUrl) left.firstOrNull()?.imageUrl else s.selectedPersonUrl,
                    )
                }
            }
        }
    }

    /** 开始试穿：提交任务 → 轮询直到成功/失败。 */
    fun startTryon() {
        val person = _uiState.value.selectedPersonUrl
        if (person.isNullOrBlank()) {
            _uiState.update { it.copy(message = "请先选择或上传一张照片") }
            return
        }
        if (_uiState.value.submitting || _uiState.value.polling) return
        _uiState.update { it.copy(submitting = true, error = null, resultUrl = null) }
        viewModelScope.launch {
            when (val r = tryonRepository.submit(productId, person)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(submitting = false, polling = true) }
                    pollUntilDone(r.data.id)
                }
                is ApiResult.Error -> _uiState.update { it.copy(submitting = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(submitting = false, error = "网络异常，请重试") }
            }
        }
    }

    private suspend fun pollUntilDone(taskId: Long) {
        repeat(POLL_MAX) {
            delay(POLL_INTERVAL_MS)
            when (val r = tryonRepository.poll(taskId)) {
                is ApiResult.Success -> {
                    val task = r.data
                    when (task.status) {
                        TryonTask.STATUS_SUCCESS -> {
                            _uiState.update { it.copy(polling = false, resultUrl = task.resultUrl) }
                            return
                        }
                        TryonTask.STATUS_FAILED -> {
                            _uiState.update { it.copy(polling = false, error = task.failReason.ifBlank { "AI 试衣失败，请重试" }) }
                            return
                        }
                        // 处理中：继续轮询
                    }
                }
                else -> Unit // 单次轮询失败忽略，下次继续
            }
        }
        // 超时
        _uiState.update { it.copy(polling = false, error = "试衣超时，请稍后在「我的试衣」查看") }
    }
}
