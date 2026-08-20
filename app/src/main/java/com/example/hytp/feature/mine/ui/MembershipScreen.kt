package com.example.hytp.feature.mine.ui

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hytp.core.network.dto.MembershipPlanItem
import com.example.hytp.core.ui.HanfuButton
import com.example.hytp.core.ui.HanfuButtonSize
import com.example.hytp.feature.mine.vm.MembershipViewModel
import com.example.hytp.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 会员开通/续费页。用同袍币购买，每月 30 元 = 3000 币。
 * 会员权益：AI 试衣每日免费 5 次（普通 3 次）、超额 8 币/次（普通 10 币）。
 * 开通成功回调 onPurchased(到期时间戳) 让上级刷新资料并返回。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    onBack: () -> Unit,
    onPurchased: (Long) -> Unit,
    viewModel: MembershipViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.doneExpireAt) {
        state.doneExpireAt?.let { onPurchased(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("会员中心") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.loading -> CircularProgressIndicator()
                state.plan != null -> {
                    val plan = state.plan!!
                    // 默认选中年费档（更划算），无则退回第一档
                    var selected by remember(plan.plans) {
                        mutableStateOf(plan.plans.firstOrNull { it.key == "year" }?.key ?: plan.plans.firstOrNull()?.key ?: "month")
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.lg),
                    ) {
                        // 当前会员状态
                        Text(
                            text = if (plan.isPremium) "你是高级会员" else "开通高级会员",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (plan.isPremium && plan.memberExpireAt != null) {
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                text = "有效期至 ${formatDate(plan.memberExpireAt)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(Modifier.height(Spacing.lg))

                        // 套餐选择
                        plan.plans.forEach { item ->
                            PlanCard(
                                item = item,
                                selected = item.key == selected,
                                onClick = { selected = item.key },
                            )
                            Spacer(Modifier.height(Spacing.sm))
                        }

                        Spacer(Modifier.height(Spacing.md))

                        // 权益卡片
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(8.dp)),
                        ) {
                            Column(Modifier.padding(Spacing.lg)) {
                                Text(
                                    text = "会员权益",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.height(Spacing.sm))
                                Benefit("全场商城购物 95 折")
                                Benefit("AI 试衣每日免费 5 次（普通 3 次）")
                                Benefit("试衣超额 8 币/次（普通 10 币）")
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        state.error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(Spacing.sm))
                        }

                        val picked = plan.plans.firstOrNull { it.key == selected }
                        HanfuButton(
                            text = when {
                                state.submitting -> "开通中…"
                                picked == null -> "立即开通"
                                plan.isPremium -> "续费${picked.durationText} · ¥${picked.priceYuan}"
                                else -> "立即开通 · ¥${picked.priceYuan}"
                            },
                            onClick = { viewModel.purchase(selected) },
                            size = HanfuButtonSize.Large,
                            enabled = !state.submitting,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error ?: "加载失败", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(Spacing.md))
                    HanfuButton(text = "重试", onClick = { viewModel.loadPlan() }, size = HanfuButtonSize.Small)
                }
            }
        }
    }
}

@Composable
private fun PlanCard(item: MembershipPlanItem, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant
    Surface(
        color = if (selected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.durationText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            // 年费更划算，打个角标（月费 30 → 年 360，年费 300 省 60）
            if (item.key == "year") {
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = "省 60",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "¥${item.priceYuan}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun Benefit(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("· ", style = MaterialTheme.typography.bodyMedium)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
    Spacer(Modifier.height(Spacing.xs))
}

/** 会员到期时间戳（秒）→ yyyy-MM-dd。 */
private fun formatDate(epochSec: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(epochSec * 1000))
