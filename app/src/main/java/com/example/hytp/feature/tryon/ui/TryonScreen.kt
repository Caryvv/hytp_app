package com.example.hytp.feature.tryon.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.hytp.core.network.dto.TryonQuota
import com.example.hytp.core.network.dto.UserAvatar
import com.example.hytp.core.ui.HanfuButton
import com.example.hytp.core.ui.HanfuButtonSize
import com.example.hytp.core.ui.HanfuButtonVariant
import com.example.hytp.core.ui.SectionTitle
import com.example.hytp.feature.tryon.vm.TryonViewModel

/**
 * AI 试衣页：选/传人物照 → 开始试穿 → 轮询出图。
 * 服装图后端从 product.tryon_model_url 取，页面不再重复展示（用户刚从商品详情进来）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TryonScreen(
    onBack: () -> Unit,
    onOpenMyTryon: () -> Unit,
    viewModel: TryonViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadNewPhoto(it) }
    }

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.consumeMessage() }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it) }
    }

    val busy = state.submitting || state.polling

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 试衣") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
                actions = {
                    TextButton(onClick = onOpenMyTryon) { Text("我的试衣") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(16.dp),
        ) {
            state.quota?.let { q ->
                QuotaHint(q)
                Spacer(Modifier.height(12.dp))
            }
            SectionTitle("选择你的照片")
            Spacer(Modifier.height(12.dp))
            AvatarRow(
                avatars = state.avatars,
                selectedUrl = state.selectedPersonUrl,
                uploading = state.uploading,
                onSelect = viewModel::selectAvatar,
                onAdd = { picker.launch("image/*") },
                onDelete = viewModel::deleteAvatar,
            )

            Spacer(Modifier.height(24.dp))

            // 结果 / 加载态
            Box(
                Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    busy -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    ) {
                        if (state.submitting) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "正在提交…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            // 轮询中：估算进度条 + 百分比 + 预计剩余秒（阿里云不返真实进度，按耗时估算）
                            Text(
                                "AI 正在为你试穿…",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { state.progress },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${(state.progress * 100).toInt()}%" +
                                    if (state.etaSeconds > 0) " · 预计还需 ${state.etaSeconds} 秒" else " · 即将完成",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    state.resultUrl != null -> AsyncImage(
                        model = state.resultUrl,
                        contentDescription = "试衣结果",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium),
                    )

                    else -> Text(
                        "选好照片，点下方开始试穿",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HanfuButton(
                text = if (busy) "生成中…" else if (state.resultUrl != null) "再试一次" else "开始试穿",
                onClick = viewModel::startTryon,
                variant = HanfuButtonVariant.Emphasis,
                size = HanfuButtonSize.Large,
                enabled = !busy && !state.uploading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun QuotaHint(q: TryonQuota) {
    val text = if (q.freeRemaining > 0) {
        "今日还可免费试穿 ${q.freeRemaining} 次" +
            (if (q.isPremium) "（会员每日 ${q.freeQuota} 次）" else "")
    } else {
        "今日免费次数已用完，继续试穿每次消耗 ${q.price} 同袍币"
    }
    Box(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun AvatarRow(
    avatars: List<UserAvatar>,
    selectedUrl: String?,
    uploading: Boolean,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (UserAvatar) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(avatars, key = { it.id }) { avatar ->
            val selected = avatar.imageUrl == selectedUrl
            Box(Modifier.size(88.dp)) {
                AsyncImage(
                    model = avatar.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .clickable { onSelect(avatar.imageUrl) },
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                        .clickable { onDelete(avatar) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("×", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                    .clickable(enabled = !uploading, onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                if (uploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("＋", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("上传照片", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
