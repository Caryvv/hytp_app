package com.example.hytp.feature.shop.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hytp.core.ui.EmptyView
import com.example.hytp.core.ui.ErrorView
import com.example.hytp.core.ui.HomeSearchBar
import com.example.hytp.core.ui.LoadingView
import com.example.hytp.feature.shop.vm.SearchUiState
import com.example.hytp.feature.shop.vm.SearchViewModel

/**
 * 商品搜索页：顶栏可编辑搜索栏 + 结果网格（下拉分页）。复用 HomeSearchBar / ProductCard。
 */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onProductClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clickable { onBack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("‹", style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.width(4.dp))
                Box(Modifier.weight(1f)) {
                    HomeSearchBar(
                        query = state.query,
                        onQueryChange = viewModel::onQueryChange,
                        onSearch = { viewModel.search() },
                        placeholder = "搜汉服商品",
                    )
                }
            }
            Results(state, onProductClick, viewModel::loadMore)
        }
    }
}

@Composable
private fun Results(
    state: SearchUiState,
    onProductClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
) {
    when {
        state.loading && state.products.isEmpty() -> LoadingView(message = "搜索中")

        state.error != null && state.products.isEmpty() ->
            ErrorView(message = state.error, onRetry = onLoadMore)

        state.submitted.isEmpty() ->
            EmptyView(title = "搜汉服商品", subtitle = "输入关键词，找形制 · 找同袍所爱")

        state.products.isEmpty() ->
            EmptyView(title = "暂无相关商品", subtitle = "换个关键词试试「${state.submitted}」")

        else -> {
            val gridState = rememberLazyGridState()
            val shouldLoadMore by remember {
                derivedStateOf {
                    val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    last >= state.products.size - 3
                }
            }
            LaunchedEffect(shouldLoadMore, state.hasMore) {
                if (shouldLoadMore && state.hasMore) onLoadMore()
            }

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.products, key = { it.id }) { product ->
                    ProductCard(product = product, onClick = { onProductClick(product.id) })
                }
            }
        }
    }
}
