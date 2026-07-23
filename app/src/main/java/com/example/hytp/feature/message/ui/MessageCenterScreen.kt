package com.example.hytp.feature.message.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.hytp.core.network.dto.Conversation
import com.example.hytp.core.network.dto.SocialGroup
import com.example.hytp.feature.message.vm.MessageCenterViewModel

/** 社群类型文案。 */
private fun groupTypeText(type: Int): String = when (type) {
    1 -> "地域"
    2 -> "形制"
    3 -> "兴趣"
    4 -> "男同袍"
    else -> "其他"
}

/**
 * 消息中心：聚合私信会话 + 我加入的社群，各带未读角标。
 * 每次 resume 刷新，从聊天/群聊返回后角标即时更新。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCenterScreen(
    onBack: () -> Unit,
    onConversationClick: (Long, String) -> Unit,
    onGroupClick: (Long) -> Unit,
    viewModel: MessageCenterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("消息中心") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading && state.conversations.isEmpty() && state.groups.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

                state.error != null ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Text("点击重试", modifier = Modifier.clickable { viewModel.load() }, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                state.conversations.isEmpty() && state.groups.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无消息", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                else ->
                    LazyColumn(Modifier.fillMaxSize()) {
                        if (state.conversations.isNotEmpty()) {
                            item { SectionHeader("私信") }
                            items(state.conversations, key = { "c${it.id}" }) { conv ->
                                ConversationRow(conv, onClick = {
                                    onConversationClick(conv.id, conv.target?.nickname ?: "同袍")
                                })
                                HorizontalDivider()
                            }
                        }
                        if (state.groups.isNotEmpty()) {
                            item { SectionHeader("我的社群") }
                            items(state.groups, key = { "g${it.id}" }) { g ->
                                GroupRow(g, onClick = { onGroupClick(g.id) })
                                HorizontalDivider()
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun ConversationRow(conv: Conversation, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = conv.target?.avatar,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(44.dp).clip(CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(conv.target?.nickname ?: "同袍", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(conv.lastMsg.ifBlank { "开始聊天吧" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        if (conv.unread > 0) {
            Spacer(Modifier.width(8.dp))
            Badge { Text("${conv.unread}") }
        }
    }
}

@Composable
private fun GroupRow(g: SocialGroup, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = g.avatar,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(g.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                "[${groupTypeText(g.type)}] ${g.memberCount}人",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (g.unread > 0) {
            Spacer(Modifier.width(8.dp))
            Badge { Text("${g.unread}") }
        }
    }
}
