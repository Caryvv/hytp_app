package com.example.hytp.feature.social.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hytp.feature.social.vm.FeedListViewModel
import com.example.hytp.feature.social.vm.FeedTab

/**
 * 动态流：推荐/关注双 Tab + 分页列表 + FAB 发布。
 * onBack 为 null 时隐藏返回按钮（用于 Tab 根页面）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedListScreen(
    onBack: (() -> Unit)?,
    onFeedClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onPublish: () -> Unit,
    refreshSignal: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    viewModel: FeedListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // 发布成功返回后刷新列表
    LaunchedEffect(refreshSignal) {
        if (refreshSignal) {
            viewModel.refresh()
            onRefreshConsumed()
        }
    }

    val reachedEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= state.feeds.size - 3 && state.feeds.isNotEmpty()
        }
    }
    LaunchedEffect(reachedEnd) { if (reachedEnd) viewModel.loadMore() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("同袍动态") },
                navigationIcon = if (onBack != null) {
                    { IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) } }
                } else {
                    {}
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onPublish) { Text("＋", style = MaterialTheme.typography.headlineMedium) }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = FeedTab.entries.indexOf(state.tab)) {
                FeedTab.entries.forEach { tab ->
                    Tab(selected = tab == state.tab, onClick = { viewModel.selectTab(tab) }, text = { Text(tab.label) })
                }
            }
            Box(Modifier.fillMaxSize()) {
                when {
                    state.loading ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

                    state.error != null ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.error!!, color = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.height(12.dp))
                                Text("点击重试", modifier = Modifier.clickable { viewModel.refresh() }, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                    state.feeds.isEmpty() ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (state.tab == FeedTab.FOLLOWING) "关注的同袍还没发动态" else "还没有动态，发第一条吧",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                    else ->
                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            items(state.feeds, key = { it.id }) { feed ->
                                FeedCard(
                                    feed = feed,
                                    onClick = { onFeedClick(feed.id) },
                                    onAuthorClick = { onAuthorClick(feed.userId) },
                                    onLike = { viewModel.toggleLike(feed) },
                                    onFavorite = { viewModel.toggleFavorite(feed) },
                                )
                                HorizontalDivider()
                            }
                        }
                }
            }
        }
    }
}
