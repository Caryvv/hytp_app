package com.example.hytp.feature.tryon.vm

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.TryonRepository
import com.example.hytp.core.data.UploadRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.TryonQuota
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
    val progress: Float = 0f,              // 估算进度 0~1（阿里云不返真实进度，按耗时估算）
    val etaSeconds: Int = 0,               // 预计剩余秒数
    val resultUrl: String? = null,         // 成功结果图
    val quota: TryonQuota? = null,         // 今日配额（剩余免费次数/超额单价），null 时不展示提示
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
        const val TICK_MS = 500L            // 进度刷新节拍
        const val POLL_EVERY_TICKS = 5      // 每 5 拍(2.5s)查一次真结果
        const val ESTIMATE_MS = 25000L      // 官方 15~30s，取 25s 做进度估算（非真实进度）
        const val MAX_TICKS = 200           // 100s 上限，防死循环
    }

    init {
        loadAvatars()
        loadQuota()
    }

    /** 加载今日配额（剩余免费次数/超额单价），失败静默（提示条不显示即可）。 */
    private fun loadQuota() {
        viewModelScope.launch {
            (tryonRepository.quota() as? ApiResult.Success)?.let { r ->
                _uiState.update { it.copy(quota = r.data) }
            }
        }
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
        _uiState.update {
            it.copy(submitting = true, error = null, resultUrl = null, progress = 0f, etaSeconds = (ESTIMATE_MS / 1000).toInt())
        }
        viewModelScope.launch {
            when (val r = tryonRepository.submit(productId, person)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(submitting = false, polling = true) }
                    loadQuota() // 已消耗一次，刷新顶部剩余次数/单价提示
                    pollUntilDone(r.data.id)
                }
                is ApiResult.Error -> _uiState.update { it.copy(submitting = false, error = r.message) }
                is ApiResult.Failure -> _uiState.update { it.copy(submitting = false, error = "网络异常，请重试") }
            }
        }
    }

    /**
     * 轮询结果 + 估算进度。阿里云不返真实进度，按 ESTIMATE_MS 线性估算，封顶 0.95 等真结果，
     * 出图跳满。每 TICK_MS 刷新进度/倒计时，每 POLL_EVERY_TICKS 拍查一次真状态。
     */
    private suspend fun pollUntilDone(taskId: Long) {
        val start = System.currentTimeMillis()
        var ticks = 0
        while (ticks < MAX_TICKS) {
            delay(TICK_MS)
            ticks++
            val elapsed = System.currentTimeMillis() - start
            val progress = (elapsed.toFloat() / ESTIMATE_MS).coerceAtMost(0.95f)
            val eta = ((ESTIMATE_MS - elapsed) / 1000).coerceAtLeast(0).toInt()
            _uiState.update { it.copy(progress = progress, etaSeconds = eta) }

            if (ticks % POLL_EVERY_TICKS == 0) {
                when (val r = tryonRepository.poll(taskId)) {
                    is ApiResult.Success -> when (r.data.status) {
                        TryonTask.STATUS_SUCCESS -> {
                            _uiState.update { it.copy(polling = false, progress = 1f, etaSeconds = 0, resultUrl = r.data.resultUrl) }
                            return
                        }
                        TryonTask.STATUS_FAILED -> {
                            _uiState.update { it.copy(polling = false, error = r.data.failReason.ifBlank { "AI 试衣失败，请重试" }) }
                            return
                        }
                        // 处理中：继续
                    }
                    // 后端明确业务失败（如 1804 生成失败）：停下报错，不空轮到超时
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(polling = false, error = r.message) }
                        return
                    }
                    // 网络抖动：忽略，下次继续轮
                    is ApiResult.Failure -> Unit
                }
            }
        }
        _uiState.update { it.copy(polling = false, error = "试衣超时，请稍后在「我的试衣」查看") }
    }
}
