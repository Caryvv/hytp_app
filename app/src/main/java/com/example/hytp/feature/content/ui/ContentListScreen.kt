package com.example.hytp.feature.content.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.hytp.core.network.dto.ContentListItem
import com.example.hytp.core.ui.DynastyTag
import com.example.hytp.core.ui.EmptyView
import com.example.hytp.core.ui.ErrorView
import com.example.hytp.core.ui.LoadingView
import com.example.hytp.core.ui.TagSemantic
import com.example.hytp.feature.content.vm.ContentListViewModel

/**
 * 文旅/文化 内容列表页（type 由导航参数固定：1文旅 / 2文化传承）。
 * 双列网格 + 下拉分页；卡片右下角收藏切换。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentListScreen(
    onBack: () -> Unit,
    onItemClick: (Long) -> Unit,
    viewModel: ContentListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val title = if (state.type == 2) "文化传承" else "文旅服务"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading && state.items.isEmpty() -> LoadingView()

                state.error != null && state.items.isEmpty() ->
                    ErrorView(message = state.error!!, onRetry = viewModel::refresh)

                state.items.isEmpty() ->
                    EmptyView(title = "暂无内容", subtitle = "敬请期待")

                else -> {
                    val gridState = rememberLazyGridState()
                    val shouldLoadMore by remember {
                        derivedStateOf {
                            val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            last >= state.items.size - 3
                        }
                    }
                    LaunchedEffect(shouldLoadMore, state.hasMore) {
                        if (shouldLoadMore && state.hasMore) viewModel.loadMore()
                    }

                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            ContentCard(
                                item = item,
                                onClick = { onItemClick(item.id) },
                                onFavorite = { viewModel.toggleFavorite(item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentCard(
    item: ContentListItem,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Column {
            AsyncImage(
                model = item.cover,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(MaterialTheme.shapes.medium),
            )
            Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.city.isNotBlank()) {
                        DynastyTag(item.city, semantic = TagSemantic.Info)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = "报名 ${item.signupCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = if (item.isFavorited) "★ ${item.favoriteCount}" else "☆ ${item.favoriteCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (item.isFavorited) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onFavorite() },
                    )
                }
            }
        }
    }
}
