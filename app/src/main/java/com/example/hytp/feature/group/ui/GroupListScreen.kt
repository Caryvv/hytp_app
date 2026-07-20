package com.example.hytp.feature.group.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.hytp.core.network.dto.SocialGroup
import com.example.hytp.feature.group.vm.GroupListViewModel

/** 社群类型文案。 */
private fun groupTypeText(type: Int): String = when (type) {
    1 -> "地域"
    2 -> "形制"
    3 -> "兴趣"
    4 -> "男同袍"
    else -> "其他"
}

/**
 * 社群列表 + 建群。点进社群详情/群聊。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    onBack: () -> Unit,
    onGroupClick: (Long) -> Unit,
    viewModel: GroupListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("同袍社群") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) { Text("＋", style = MaterialTheme.typography.headlineMedium) }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

                state.error != null ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Text("点击重试", modifier = Modifier.clickable { viewModel.load() }, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                state.groups.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("还没有社群，创建第一个吧", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                else ->
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(state.groups, key = { it.id }) { g ->
                            GroupRow(g, onClick = { onGroupClick(g.id) })
                            HorizontalDivider()
                        }
                    }
            }
        }
    }

    if (showCreate) {
        CreateGroupDialog(
            creating = state.creating,
            onDismiss = { showCreate = false },
            onConfirm = { name, type, city, intro ->
                viewModel.create(name, type, city, intro) { gid ->
                    showCreate = false
                    onGroupClick(gid)
                }
            },
        )
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
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(g.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                "[${groupTypeText(g.type)}] ${g.city.ifBlank { "" }} · ${g.memberCount}人",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (g.isJoined) {
            Text("已加入", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun CreateGroupDialog(
    creating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: Int, city: String, intro: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var intro by remember { mutableStateOf("") }
    val type = 3 // 默认兴趣群

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建社群") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("群名称") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("城市（选填）") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = intro, onValueChange = { intro = it }, label = { Text("群简介（选填）") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, type, city, intro) }, enabled = !creating && name.isNotBlank()) {
                Text(if (creating) "创建中…" else "创建")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}
