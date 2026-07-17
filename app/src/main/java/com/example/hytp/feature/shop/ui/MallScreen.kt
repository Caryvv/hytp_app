package com.example.hytp.feature.shop.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.hytp.feature.shop.vm.MallUiState
import com.example.hytp.feature.shop.vm.MallViewModel
import com.example.hytp.feature.shop.vm.SortOption

/**
 * 汉服商城页（对齐 08 §3.1）：顶部分类导航 + 排序筛选 + 商品卡片网格（下拉分页）。
 * 只读浏览：点商品进详情。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MallScreen(
    onProductClick: (Long) -> Unit,
    viewModel: MallViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("汉服商城") }) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CategoryBar(
                state = state,
                onSelectCategory = viewModel::selectCategory,
            )
            SortBar(
                selected = state.sort,
                onSelectSort = viewModel::selectSort,
            )
            ProductGrid(
                state = state,
                onProductClick = onProductClick,
                onLoadMore = viewModel::loadMore,
                onRetry = viewModel::refresh,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryBar(
    state: MallUiState,
    onSelectCategory: (Int) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = state.selectedCategoryId == 0,
                onClick = { onSelectCategory(0) },
                label = { Text("全部") },
            )
        }
        items(state.categories, key = { it.id }) { cat ->
            FilterChip(
                selected = state.selectedCategoryId == cat.id,
                onClick = { onSelectCategory(cat.id) },
                label = { Text(cat.name) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortBar(
    selected: SortOption,
    onSelectSort: (SortOption) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(SortOption.entries, key = { it.name }) { opt ->
            FilterChip(
                selected = selected == opt,
                onClick = { onSelectSort(opt) },
                label = { Text(opt.label) },
            )
        }
    }
}

@Composable
private fun ProductGrid(
    state: MallUiState,
    onProductClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
) {
    when {
        state.loading && state.products.isEmpty() ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        state.error != null && state.products.isEmpty() ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "点击重试",
                        modifier = Modifier.clickable { onRetry() },
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

        state.products.isEmpty() ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无商品", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

        else -> {
            val gridState = rememberLazyGridState()
            // 接近底部时触发加载更多
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
