package com.example.hytp.feature.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 应用内更新弹窗宿主：挂在主骨架顶层，进程内只检查一次。
 * 有新版时弹 AlertDialog；forceUpdate 时不可取消。
 */
@Composable
fun UpdateGate(viewModel: UpdateViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.checkOnce() }

    val info = state.info ?: return
    val force = info.forceUpdate

    AlertDialog(
        onDismissRequest = { if (!state.downloading) viewModel.dismiss() },
        title = { Text("发现新版本 ${info.versionName}") },
        text = {
            Column {
                Text(
                    text = info.updateLog.ifBlank { "优化体验，修复若干问题。" },
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (state.downloading) {
                    Spacer(Modifier.height(12.dp))
                    Text("下载中 ${state.progress}%", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { state.progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                state.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.downloadAndInstall(context) },
                enabled = !state.downloading,
            ) { Text(if (state.downloading) "下载中…" else "立即更新") }
        },
        dismissButton = if (!force) {
            { TextButton(onClick = { viewModel.dismiss() }, enabled = !state.downloading) { Text("稍后") } }
        } else {
            null
        },
    )
}
