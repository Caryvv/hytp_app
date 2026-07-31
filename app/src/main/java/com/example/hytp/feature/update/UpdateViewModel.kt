package com.example.hytp.feature.update

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.BuildConfig
import com.example.hytp.core.data.AppVersionRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.AppVersionInfo
import com.example.hytp.core.update.ApkInstaller
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpdateUiState(
    val info: AppVersionInfo? = null,      // 非空即展示更新弹窗
    val downloading: Boolean = false,
    val progress: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val repository: AppVersionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private var checked = false

    /** 每次进程只检查一次。 */
    fun checkOnce() {
        if (checked) return
        checked = true
        viewModelScope.launch {
            when (val r = repository.check(BuildConfig.VERSION_CODE)) {
                is ApiResult.Success ->
                    if (r.data.hasUpdate && r.data.latest != null) {
                        _uiState.update { it.copy(info = r.data.latest) }
                    }
                else -> { /* 检查更新失败静默，不打扰用户 */ }
            }
        }
    }

    /** 强制更新时不允许关闭。 */
    fun dismiss() {
        if (_uiState.value.info?.forceUpdate == true) return
        _uiState.update { UpdateUiState() }
    }

    /** 下载并安装。 */
    fun downloadAndInstall(context: Context) {
        val info = _uiState.value.info ?: return
        if (info.downloadUrl.isBlank()) {
            _uiState.update { it.copy(error = "下载地址为空，请稍后重试") }
            return
        }
        if (!ApkInstaller.canInstall(context)) {
            ApkInstaller.requestInstallPermission(context)
            return
        }
        _uiState.update { it.copy(downloading = true, progress = 0, error = null) }
        viewModelScope.launch {
            ApkInstaller.download(context, info.downloadUrl).collect { p ->
                when (p) {
                    is ApkInstaller.Progress.Downloading ->
                        _uiState.update { it.copy(progress = p.percent) }
                    is ApkInstaller.Progress.Done -> {
                        _uiState.update { it.copy(downloading = false) }
                        ApkInstaller.install(context, p.file)
                    }
                    is ApkInstaller.Progress.Failed ->
                        _uiState.update { it.copy(downloading = false, error = p.error) }
                }
            }
        }
    }
}
