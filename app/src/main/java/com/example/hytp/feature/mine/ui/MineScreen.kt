package com.example.hytp.feature.mine.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hytp.core.ui.DynastyTag
import com.example.hytp.core.ui.HanfuButton
import com.example.hytp.core.ui.HanfuButtonSize
import com.example.hytp.core.ui.HanfuButtonVariant
import com.example.hytp.core.ui.TagSemantic
import com.example.hytp.feature.mine.vm.MineViewModel
import com.example.hytp.ui.theme.Spacing

/**
 * 「我的」页面（docs/dev/15 §7.9）。
 * 资料区 + 订单入口图标 + 功能列表 + 退出登录。
 */
@Composable
fun MineScreen(
    onLoggedOut: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenRecharge: () -> Unit = {},
    onOpenTasks: () -> Unit = {},
    onOpenMessages: () -> Unit = {},
    refreshSignal: Int? = null,
    onRefreshConsumed: () -> Unit = {},
    viewModel: MineViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedOut, state.sessionExpired) {
        if (state.loggedOut || state.sessionExpired) onLoggedOut()
    }

    // 充值成功返回：刷新余额
    LaunchedEffect(refreshSignal) {
        if (refreshSignal != null) {
            viewModel.loadProfile()
            onRefreshConsumed()
        }
    }

    when {
        state.loading ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        state.error != null ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(Spacing.md))
                    HanfuButton(
                        text = "重试",
                        onClick = { viewModel.loadProfile() },
                        variant = HanfuButtonVariant.Outline,
                        size = HanfuButtonSize.Small,
                    )
                }
            }

        state.profile != null -> {
            val p = state.profile!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
            ) {
                // ── 资料区 ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 头像占位
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = p.nickname.take(1),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.width(Spacing.md))
                    Column {
                        Text(
                            text = p.nickname,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(Modifier.height(Spacing.xs))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${toCoin(p.balance)} 同袍币",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            HanfuButton(
                                text = "充值",
                                onClick = onOpenRecharge,
                                variant = HanfuButtonVariant.Outline,
                                size = HanfuButtonSize.Small,
                            )
                        }
                        Spacer(Modifier.height(Spacing.xs))
                        DynastyTag(
                            text = if (p.memberLevel == 1) "高级会员" else "普通用户",
                            semantic = if (p.memberLevel == 1) TagSemantic.Member else TagSemantic.Info,
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.xl))

                // ── 订单入口 ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    OrderEntry("购买", onClick = onOpenOrders)
                    OrderEntry("租赁", onClick = onOpenOrders)
                    OrderEntry("定制", onClick = {})
                    OrderEntry("文旅", onClick = {})
                }

                Spacer(Modifier.height(Spacing.xl))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(Spacing.sm))

                // ── 功能列表 ──
                MineListItem("🎯", "任务中心", onClick = onOpenTasks)
                MineListItem("❤", "我的收藏")
                MineListItem("👥", "我的关注")
                MineListItem("🔔", "消息中心", onClick = onOpenMessages)
                MineListItem("⚙", "设置")

                Spacer(Modifier.weight(1f))

                // ── 退出登录 ──
                HanfuButton(
                    text = "退出登录",
                    onClick = { viewModel.logout() },
                    variant = HanfuButtonVariant.Text,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** 元字符串 → 同袍币整数（100 同袍币 = 1 元）。 */
private fun toCoin(yuan: String): Int =
    ((yuan.toDoubleOrNull() ?: 0.0) * 100).toLong().toInt()

@Composable
private fun OrderEntry(label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(Spacing.sm),
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = when (label) {
                    "购买" -> "🛍"
                    "租赁" -> "📦"
                    "定制" -> "✂"
                    "文旅" -> "🏯"
                    else -> "📋"
                },
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MineListItem(icon: String, label: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md, horizontal = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(Spacing.md))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
