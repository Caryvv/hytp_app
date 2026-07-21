package com.example.hytp.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.hytp.core.ui.SectionTitle
import com.example.hytp.core.ui.TagSemantic
import com.example.hytp.feature.home.vm.HomeViewModel
import com.example.hytp.ui.theme.Spacing

/**
 * 首页占位（docs/dev/15 §7.3）：当前为简版欢迎页 + 功能入口，
 * 正式首页（搜索/轮播/导航/推荐流）在阶段2 实现。
 */
@Composable
fun HomeScreen(
    onLoggedOut: () -> Unit,
    onOpenMall: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenSocial: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenGroups: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedOut, state.sessionExpired) {
        if (state.loggedOut || state.sessionExpired) onLoggedOut()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when {
                state.loading -> CircularProgressIndicator()

                state.error != null -> {
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(Spacing.lg))
                    HanfuButton(
                        text = "重试",
                        onClick = { viewModel.loadProfile() },
                        variant = HanfuButtonVariant.Outline,
                        size = HanfuButtonSize.Small,
                    )
                }

                state.profile != null -> {
                    val p = state.profile!!
                    Text(
                        text = "欢迎，${p.nickname}",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = "手机号：${p.phone}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    DynastyTag(
                        text = if (p.memberLevel == 1) "高级会员" else "普通用户",
                        semantic = if (p.memberLevel == 1)
                            TagSemantic.Member
                        else
                            TagSemantic.Info,
                    )
                    Spacer(Modifier.height(Spacing.xl))
                    SectionTitle("功能入口")
                    Spacer(Modifier.height(Spacing.md))

                    HanfuButton(
                        text = "进入汉服商城",
                        onClick = onOpenMall,
                        variant = HanfuButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.md))
                    HanfuButton(
                        text = "同袍动态",
                        onClick = onOpenSocial,
                        variant = HanfuButtonVariant.Outline,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.md))
                    HanfuButton(
                        text = "消息",
                        onClick = onOpenMessages,
                        variant = HanfuButtonVariant.Outline,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.md))
                    HanfuButton(
                        text = "同袍社群",
                        onClick = onOpenGroups,
                        variant = HanfuButtonVariant.Outline,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.md))
                    HanfuButton(
                        text = "我的订单",
                        onClick = onOpenOrders,
                        variant = HanfuButtonVariant.Outline,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.md))
                    HanfuButton(
                        text = "退出登录",
                        onClick = { viewModel.logout() },
                        variant = HanfuButtonVariant.Text,
                    )
                }
            }
        }
    }
}
