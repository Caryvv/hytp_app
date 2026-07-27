package com.example.hytp.feature.mine.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hytp.core.ui.HanfuButton
import com.example.hytp.core.ui.HanfuButtonSize
import com.example.hytp.feature.mine.vm.WithdrawViewModel
import com.example.hytp.ui.theme.Spacing

/**
 * 同袍币提现页（Mock 即时扣减）。100 同袍币 = 1 元。
 * 输入提现同袍币数 → 提现 → 成功回调 onWithdrawn(扣减同袍币) 让上级刷新余额并返回。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    onBack: () -> Unit,
    onWithdrawn: (Int) -> Unit,
    viewModel: WithdrawViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.doneCoin) {
        state.doneCoin?.let { onWithdrawn(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("提现同袍币") },
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
                text = "可提现 ${viewModel.balanceCoin} 同袍币（¥${yuanOf(viewModel.balanceCoin)}）",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.lg))

            OutlinedTextField(
                value = state.input,
                onValueChange = viewModel::onInputChange,
                label = { Text("提现同袍币数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    TextButton(onClick = viewModel::withdrawAll) { Text("全部") }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            state.input.toIntOrNull()?.takeIf { it > 0 }?.let {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = "到账 ¥${yuanOf(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            state.error?.let {
                Spacer(Modifier.height(Spacing.sm))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.weight(1f))
            HanfuButton(
                text = if (state.submitting) "提现中…" else "确认提现",
                onClick = { viewModel.withdraw() },
                size = HanfuButtonSize.Large,
                enabled = !state.submitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** 同袍币 → 元展示（100:1）。 */
private fun yuanOf(coin: Int): String = "%.2f".format(coin / 100.0)
