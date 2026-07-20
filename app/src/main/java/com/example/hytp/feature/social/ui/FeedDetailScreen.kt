package com.example.hytp.feature.social.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.hytp.core.network.dto.Feed
import com.example.hytp.core.network.dto.FeedComment
import com.example.hytp.feature.social.vm.FeedDetailViewModel

/**
 * 动态详情：内容 + 图集 + 互动 + 评论列表 + 评论输入；作者本人可删除。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedDetailScreen(
    onBack: () -> Unit,
    onAuthorClick: (Long) -> Unit,
    viewModel: FeedDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.consumeMessage() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("动态") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
                },
                actions = {
                    if (viewModel.isOwner && state.feed != null) {
                        TextButton(onClick = { viewModel.deleteFeed() }) { Text("删除", color = MaterialTheme.colorScheme.error) }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (state.feed != null) {
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        placeholder = { Text("说点什么…") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.addComment(comment); comment = "" },
                        enabled = !state.submitting && comment.isNotBlank(),
                    ) { Text("发送") }
                }
            }
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

                state.feed != null ->
                    DetailContent(
                        feed = state.feed!!,
                        comments = state.comments,
                        onAuthorClick = onAuthorClick,
                        onLike = { viewModel.toggleLike() },
                        onFavorite = { viewModel.toggleFavorite() },
                    )
            }
        }
    }
}

@Composable
private fun DetailContent(
    feed: Feed,
    comments: List<FeedComment>,
    onAuthorClick: (Long) -> Unit,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onAuthorClick(feed.userId) }) {
                    AsyncImage(
                        model = feed.author?.avatar,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(feed.author?.nickname ?: "同袍", style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.height(12.dp))
                Text(feed.content, style = MaterialTheme.typography.bodyLarge)
            }
        }
        items(feed.media) { img ->
            AsyncImage(
                model = img,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(horizontal = 16.dp, vertical = 4.dp).clip(RoundedCornerShape(8.dp)),
            )
        }
        item {
            if (feed.tags.isNotEmpty()) {
                Text(
                    feed.tags.joinToString(" ") { "#$it" },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(
                    "${if (feed.isLiked) "♥" else "♡"} ${feed.likeCount}",
                    color = if (feed.isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onLike() },
                )
                Text(
                    "${if (feed.isFavorited) "★" else "☆"} ${feed.favoriteCount}",
                    color = if (feed.isFavorited) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onFavorite() },
                )
                Text("↗ ${feed.shareCount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider()
            Text("评论（${feed.commentCount}）", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(16.dp))
        }
        items(comments, key = { it.id }) { c ->
            CommentRow(c)
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun CommentRow(c: FeedComment) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        AsyncImage(
            model = c.author?.avatar,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(28.dp).clip(CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(c.author?.nickname ?: "同袍", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(c.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
