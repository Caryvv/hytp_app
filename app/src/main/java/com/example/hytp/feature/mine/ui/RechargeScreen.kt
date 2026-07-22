package com.example.hytp.feature.mine.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hytp.core.ui.HanfuButton
import com.example.hytp.core.ui.HanfuButtonSize
import com.example.hytp.feature.mine.vm.RECHARGE_TIERS
import com.example.hytp.feature.mine.vm.RechargeViewModel
import com.example.hytp.ui.theme.Spacing

/**
 * 同袍币充值页（Mock 通道，直接到账）。100 同袍币 = 1 元。
 * 选档位 → 充值 → 成功回调 onRecharged(到账同袍币) 让上级刷新余额并返回。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeScreen(
    onBack: () -> Unit,
    onRecharged: (Int) -> Unit,
    viewModel: RechargeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf(RECHARGE_TIERS.first()) }

    LaunchedEffect(state.doneCoin) {
        state.doneCoin?.let { onRecharged(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("充值同袍币") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.lg),
        ) {
            Text(
                text = "100 同袍币 = 1 元",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.lg))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.weight(1f),
            ) {
                items(RECHARGE_TIERS) { coin ->
                    TierCard(
                        coin = coin,
                        selected = coin == selected,
                        onClick = { selected = coin },
                    )
                }
            }

            state.error?.let {
                Spacer(Modifier.height(Spacing.sm))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(Spacing.md))
            HanfuButton(
                text = if (state.submitting) "充值中…" else "充值 ¥${yuan(selected)}",
                onClick = { viewModel.recharge(selected) },
                size = HanfuButtonSize.Large,
                enabled = !state.submitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TierCard(coin: Int, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant
    Surface(
        color = if (selected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$coin 同袍币",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "¥${yuan(coin)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** 同袍币 → 元展示（100:1）。 */
private fun yuan(coin: Int): String {
    val fen = coin // 1 同袍币 = 1 分
    return "%.2f".format(fen / 100.0)
}
