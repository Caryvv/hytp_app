package com.example.hytp.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hytp.feature.home.vm.HomeViewModel

/**
 * 首页占位（阶段1）：显示当前登录用户昵称，验证带 token 请求链路；提供退出登录。
 * 正式首页（搜索/轮播/导航/推荐流 + 底部 Tab）在阶段2 实现。
 */
@Composable
fun HomeScreen(
    onLoggedOut: () -> Unit,
    onOpenMall: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenSocial: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedOut, state.sessionExpired) {
        // 主动退出，或会话失效（自动续签失败）→ 回登录页
        if (state.loggedOut || state.sessionExpired) onLoggedOut()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when {
            state.loading -> CircularProgressIndicator()

            state.error != null -> {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.loadProfile() }) { Text("重试") }
            }

            state.profile != null -> {
                val p = state.profile!!
                Text(
                    text = "欢迎，${p.nickname}",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "手机号：${p.phone}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = if (p.memberLevel == 1) "高级会员" else "普通用户",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(32.dp))
                Button(onClick = onOpenMall) { Text("进入汉服商城") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onOpenSocial) { Text("同袍动态") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onOpenOrders) { Text("我的订单") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { viewModel.logout() }) { Text("退出登录") }
            }
        }
    }
}
