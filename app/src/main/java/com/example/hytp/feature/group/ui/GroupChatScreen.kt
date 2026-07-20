package com.example.hytp.feature.group.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.example.hytp.core.network.dto.GroupMessage
import com.example.hytp.feature.group.vm.GroupChatViewModel

/**
 * 社群详情 + 群聊：顶栏群名 + 加入/退出；已加入显示群聊消息 + 输入，未加入显示加入按钮。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    onBack: () -> Unit,
    viewModel: GroupChatViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }
    val joined = state.group?.isJoined == true

    LaunchedEffect(state.quit) { if (state.quit) onBack() }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.group?.name ?: "社群") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
                },
                actions = {
                    if (joined) {
                        TextButton(onClick = { viewModel.quit() }) {
                            Text(if (state.group?.myRole == 2) "解散" else "退出", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (joined) {
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("群里说点什么…") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    Spacer(Modifier.padding(4.dp))
                    Button(onClick = { viewModel.send(input); input = "" }, enabled = !state.sending && input.isNotBlank()) { Text("发送") }
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

                state.error != null && state.group == null ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    }

                !joined ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.group?.name ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.padding(4.dp))
                            Text(state.group?.intro ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${state.group?.memberCount ?: 0} 位成员", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.padding(8.dp))
                            Button(onClick = { viewModel.join() }) { Text("加入社群") }
                        }
                    }

                else ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.messages, key = { it.id }) { msg ->
                            GroupBubble(msg, isMine = msg.fromUser == viewModel.myUserId)
                        }
                    }
            }
        }
    }
}

@Composable
private fun GroupBubble(msg: GroupMessage, isMine: Boolean) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
            if (!isMine) {
                Text(msg.sender?.nickname ?: "同袍", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(msg.content, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
