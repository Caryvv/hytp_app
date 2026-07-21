package com.example.hytp.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hytp.ui.theme.HytpThemeLight

// ── 加载态 ──

/**
 * 页面级加载指示器（docs/dev/15 §6.6）。
 */
@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    message: String = "加载中",
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── 空态 ──

/**
 * 页面/列表空态（docs/dev/15 §6.6）。
 * 含简洁文案与可选行动按钮.
 */
@Composable
fun EmptyView(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            // 空卷轴装饰线（简化形态）
            Text(
                text = "— □ —",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            subtitle?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            if (actionText != null && onAction != null) {
                Spacer(Modifier.height(16.dp))
                HanfuButton(
                    text = actionText,
                    onClick = onAction,
                    variant = HanfuButtonVariant.Outline,
                    size = HanfuButtonSize.Small,
                )
            }
        }
    }
}

// ── 错误态 ──

/**
 * 错误态视图（docs/dev/15 §6.6）。
 * 含错误信息文案 + 重试操作。
 */
@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryText: String = "重试",
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            // 错误提示线框
            Text(
                text = "!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            HanfuButton(
                text = retryText,
                onClick = onRetry,
                variant = HanfuButtonVariant.Outline,
                size = HanfuButtonSize.Small,
            )
        }
    }
}

// ── Previews ──

@Preview(showBackground = true)
@Composable
private fun LoadingViewPreview() {
    HytpThemeLight { LoadingView() }
}

@Preview(showBackground = true)
@Composable
private fun EmptyViewPreview() {
    HytpThemeLight {
        EmptyView(title = "暂无动态", subtitle = "去关注更多同袍吧", actionText = "去发现", onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorViewPreview() {
    HytpThemeLight {
        ErrorView(message = "网络连接失败", onRetry = {})
    }
}
