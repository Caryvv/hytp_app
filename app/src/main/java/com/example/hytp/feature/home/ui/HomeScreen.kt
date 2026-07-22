package com.example.hytp.feature.home.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hytp.core.ui.DynastyTag
import com.example.hytp.core.ui.HanfuButton
import com.example.hytp.core.ui.HanfuButtonSize
import com.example.hytp.core.ui.HanfuButtonVariant
import com.example.hytp.core.ui.HomeSearchBar
import com.example.hytp.core.ui.SectionTitle
import com.example.hytp.core.ui.TagSemantic
import com.example.hytp.feature.home.vm.HomeViewModel
import com.example.hytp.ui.theme.Spacing

/**
 * 首页 Tab 内容（docs/dev/15 §7.3）。
 * 结构：搜索栏 + 欢迎 + Banner 占位 + 功能导航 5 入口 + 推荐流占位。
 */
@Composable
fun HomeScreen(
    onOpenMall: () -> Unit,
    onOpenSocial: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when {
            state.loading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            state.error != null ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(Spacing.lg))
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
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.lg),
                ) {
                    Spacer(Modifier.height(Spacing.md))

                    // ── 搜索栏 ──
                    HomeSearchBar(onClick = { /* TODO: 搜索页 */ })

                    Spacer(Modifier.height(Spacing.lg))

                    // ── 欢迎 ──
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "欢迎，${p.nickname}",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        DynastyTag(
                            text = if (p.memberLevel == 1) "高级会员" else "普通用户",
                            semantic = if (p.memberLevel == 1) TagSemantic.Member else TagSemantic.Info,
                        )
                    }
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "同袍币：${((p.balance.toDoubleOrNull() ?: 0.0) * 100).toLong()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    // ── Banner 占位 ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "国风雅韵 · 汉服之美",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.xl))

                    // ── 功能导航 5 入口 ──
                    SectionTitle("探索")
                    Spacer(Modifier.height(Spacing.md))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        HomeEntry(
                            emoji = "🎓",
                            label = "萌新入门",
                            onClick = { /* TODO: beginner */ },
                        )
                        HomeEntry(
                            emoji = "👥",
                            label = "同袍社交",
                            onClick = onOpenSocial,
                        )
                        HomeEntry(
                            emoji = "🏪",
                            label = "汉服商城",
                            onClick = onOpenMall,
                        )
                        HomeEntry(
                            emoji = "🏯",
                            label = "文旅服务",
                            onClick = { /* TODO: travel */ },
                        )
                        HomeEntry(
                            emoji = "📖",
                            label = "文化传承",
                            onClick = { /* TODO: culture */ },
                        )
                    }

                    Spacer(Modifier.height(Spacing.xl))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(Spacing.lg))

                    // ── 推荐流占位 ──
                    SectionTitle("为你推荐")
                    Spacer(Modifier.height(Spacing.md))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "个性化推荐即将上线",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(Modifier.height(Spacing.xxl))
                }
            }
        }
    }
}

@Composable
private fun HomeEntry(
    emoji: String,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(Spacing.xs),
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
