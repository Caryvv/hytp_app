package com.example.hytp.feature.tryon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.hytp.core.network.dto.TryonTask
import com.example.hytp.core.ui.EmptyView
import com.example.hytp.core.ui.ErrorView
import com.example.hytp.core.ui.LoadingView
import com.example.hytp.feature.tryon.vm.MyTryonViewModel

/**
 * 我的试衣历史：双列网格。成功显结果图，处理中/失败显状态占位。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTryonScreen(
    onBack: () -> Unit,
    viewModel: MyTryonViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<TryonTask?>(null) }
    var pendingDetail by remember { mutableStateOf<TryonTask?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的试衣") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading && state.tasks.isEmpty() -> LoadingView()
                state.error != null && state.tasks.isEmpty() -> ErrorView(message = state.error!!, onRetry = viewModel::refresh)
                state.tasks.isEmpty() -> EmptyView(title = "还没有试衣记录", subtitle = "去商品详情页试穿汉服吧")
                else -> {
                    val gridState = rememberLazyGridState()
                    val shouldLoadMore by remember {
                        derivedStateOf {
                            val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            last >= state.tasks.size - 3
                        }
                    }
                    LaunchedEffect(shouldLoadMore, state.hasMore) {
                        if (shouldLoadMore && state.hasMore) viewModel.loadMore()
                    }

                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.tasks, key = { it.id }) { task ->
                            TryonHistoryCard(
                                task,
                                onClick = { pendingDetail = task },
                                onDelete = { pendingDelete = task },
                            )
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除试衣记录") },
            text = { Text("确认删除这条试衣记录？删除后不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(task)
                    pendingDelete = null
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            },
        )
    }

    pendingDetail?.let { task ->
        TryonDetailDialog(task = task, onDismiss = { pendingDetail = null })
    }
}

@Composable
private fun TryonDetailDialog(task: TryonTask, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("试衣详情") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // 结果图：成功显大图，否则显状态
                Box(
                    Modifier.fillMaxWidth().aspectRatio(3f / 4f),
                    contentAlignment = Alignment.Center,
                ) {
                    when (task.status) {
                        TryonTask.STATUS_SUCCESS -> AsyncImage(
                            model = task.resultUrl,
                            contentDescription = "试衣结果",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium),
                        )

                        TryonTask.STATUS_FAILED -> StatusHint("生成失败", MaterialTheme.colorScheme.error)
                        else -> StatusHint("处理中…", MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(12.dp))
                // 输入原图：人物 + 服装
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailThumb("人物照", task.personUrl, Modifier.weight(1f))
                    DetailThumb("服装图", task.garmentUrl, Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))
                DetailRow("状态", statusLabel(task.status))
                if (task.status == TryonTask.STATUS_FAILED && task.failReason.isNotBlank()) {
                    DetailRow("失败原因", task.failReason)
                }
                if (task.createdAt > 0) {
                    DetailRow("创建时间", formatTime(task.createdAt))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
    )
}

@Composable
private fun DetailThumb(label: String, url: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(3f / 4f)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (url.isNotBlank()) {
                AsyncImage(
                    model = url,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text("无", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun statusLabel(status: Int): String = when (status) {
    TryonTask.STATUS_SUCCESS -> "已完成"
    TryonTask.STATUS_FAILED -> "生成失败"
    else -> "处理中"
}

private fun formatTime(epochSeconds: Long): String {
    val ms = if (epochSeconds < 1_000_000_000_000L) epochSeconds * 1000 else epochSeconds
    val fmt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return fmt.format(java.util.Date(ms))
}

@Composable
private fun TryonHistoryCard(task: TryonTask, onClick: () -> Unit, onDelete: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(3f / 4f), contentAlignment = Alignment.Center) {
            when (task.status) {
                TryonTask.STATUS_SUCCESS -> AsyncImage(
                    model = task.resultUrl,
                    contentDescription = "试衣结果",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                TryonTask.STATUS_FAILED -> StatusHint("生成失败", MaterialTheme.colorScheme.error)
                else -> StatusHint("处理中…", MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // 右上角删除
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center,
            ) {
                Text("×", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun StatusHint(text: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}
