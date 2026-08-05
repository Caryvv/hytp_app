package com.example.hytp.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.hytp.R
import com.example.hytp.core.network.dto.BannerItem
import com.example.hytp.core.ui.DynastyTag
import com.example.hytp.core.ui.HomeSearchBar
import com.example.hytp.core.ui.SectionTitle
import com.example.hytp.core.ui.TagSemantic
import com.example.hytp.feature.home.vm.HomeViewModel
import com.example.hytp.feature.social.ui.FeedCard
import com.example.hytp.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenMall: () -> Unit,
    onOpenSocial: () -> Unit,
    onOpenMessages: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onFeedClick: (Long) -> Unit = {},
    onAuthorClick: (Long) -> Unit = {},
    onOpenBeginner: () -> Unit = {},
    onOpenTravel: () -> Unit = {},
    onOpenCulture: () -> Unit = {},
    refreshSignal: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyStaggeredGridState()

    // 发动态成功返回首页后刷新推荐流
    LaunchedEffect(refreshSignal) {
        if (refreshSignal) {
            viewModel.refresh()
            onRefreshConsumed()
        }
    }

    // 检测滚动到底部触发 loadMore
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= gridState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore, state.feedHasMore, state.feedLoadingMore) {
        if (shouldLoadMore && state.feedHasMore && !state.feedLoadingMore && state.feedItems.isNotEmpty()) {
            viewModel.loadMore()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when {
            state.loading && state.profile == null ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            state.error != null && state.profile == null ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }

            else -> {
                PullToRefreshBox(
                    isRefreshing = state.refreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(horizontal = Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalItemSpacing = Spacing.sm,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // ── 顶栏：搜索 + 铃铛 ──
                    item(key = "top_bar", span = StaggeredGridItemSpan.FullLine) {
                        Column {
                        Spacer(Modifier.height(Spacing.md))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f)) {
                                HomeSearchBar(onClick = onOpenSearch)
                            }
                            Spacer(Modifier.width(Spacing.md))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { onOpenMessages() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.icon_bell),
                                    contentDescription = "消息",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                                if (state.unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.tertiary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = if (state.unreadCount > 9) "9+" else state.unreadCount.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiary,
                                        )
                                    }
                                }
                            }
                        }
                        }
                    }

                    // ── 欢迎行 ──
                    val p = state.profile
                    if (p != null) {
                        item(key = "welcome", span = StaggeredGridItemSpan.FullLine) {
                            Column {
                            Spacer(Modifier.height(Spacing.md))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "欢迎，${p.nickname}",
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                                Spacer(Modifier.width(Spacing.sm))
                                DynastyTag(
                                    text = if (p.memberLevel == 1) "高级会员" else "普通用户",
                                    semantic = if (p.memberLevel == 1) TagSemantic.Member else TagSemantic.Info,
                                )
                            }
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                text = "同袍币：${((p.balance.toDoubleOrNull() ?: 0.0) * 100).toLong()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                            }
                        }
                    }

                    // ── Banner 轮播 ──
                    if (state.banners.isNotEmpty()) {
                        item(key = "banner", span = StaggeredGridItemSpan.FullLine) {
                            Column {
                                Spacer(Modifier.height(Spacing.lg))
                                BannerCarousel(banners = state.banners)
                            }
                        }
                    }

                    // ── 功能导航 5 入口 ──
                    item(key = "nav_entries", span = StaggeredGridItemSpan.FullLine) {
                        Column {
                        Spacer(Modifier.height(Spacing.xl))
                        SectionTitle("探索")
                        Spacer(Modifier.height(Spacing.md))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            HomeEntry(R.drawable.icon_beginner, "萌新入门", onClick = onOpenBeginner)
                            HomeEntry(R.drawable.icon_social, "同袍社交", onClick = onOpenSocial)
                            HomeEntry(R.drawable.icon_shop, "汉服商城", onClick = onOpenMall)
                            HomeEntry(R.drawable.icon_travel, "文旅服务", onClick = onOpenTravel)
                            HomeEntry(R.drawable.icon_culture, "文化传承", onClick = onOpenCulture)
                        }
                        }
                    }

                    // ── 推荐流标题 ──
                    item(key = "feed_section_title", span = StaggeredGridItemSpan.FullLine) {
                        Column {
                            Spacer(Modifier.height(Spacing.lg))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(Spacing.lg))
                            SectionTitle("为你推荐")
                        }
                    }

                    // ── 推荐流 ──
                    when {
                        state.feedLoading && state.feedItems.isEmpty() ->
                            item(key = "feed_loading", span = StaggeredGridItemSpan.FullLine) {
                                Box(
                                    Modifier.fillMaxWidth().height(120.dp),
                                    contentAlignment = Alignment.Center,
                                ) { CircularProgressIndicator() }
                            }

                        state.feedError != null && state.feedItems.isEmpty() ->
                            item(key = "feed_error", span = StaggeredGridItemSpan.FullLine) {
                                Box(
                                    Modifier.fillMaxWidth().height(120.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        state.feedError!!,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }

                        state.feedItems.isEmpty() ->
                            item(key = "feed_empty", span = StaggeredGridItemSpan.FullLine) {
                                Box(
                                    Modifier.fillMaxWidth().height(120.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "暂无推荐内容",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                        else -> {
                            items(state.feedItems, key = { "feed_${it.id}" }) { feed ->
                                FeedCard(
                                    feed = feed,
                                    onClick = { onFeedClick(feed.id) },
                                    onAuthorClick = { onAuthorClick(feed.userId) },
                                    onLike = { },
                                    onFavorite = { },
                                )
                            }
                            if (state.feedLoadingMore) {
                                item(key = "feed_load_more", span = StaggeredGridItemSpan.FullLine) {
                                    Box(
                                        Modifier.fillMaxWidth().height(48.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }

                    // 底部留白
                    item(key = "bottom_spacer", span = StaggeredGridItemSpan.FullLine) {
                        Spacer(Modifier.height(Spacing.xxl))
                    }
                }
                }
            }
        }
    }
}

// ── Banner 轮播 ──

@Composable
private fun BannerCarousel(banners: List<BannerItem>) {
    val scope = rememberCoroutineScope()
    // 用 Int.MAX_VALUE / 2 作为起始偏移，模拟无限循环
    val itemCount = if (banners.size > 1) Int.MAX_VALUE else 1
    val pagerState = rememberPagerState(initialPage = if (banners.size > 1) itemCount / 2 else 0) {
        itemCount
    }

    // 自动翻页（4s）
    if (banners.size > 1) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(4000)
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(140.dp),
        ) { page ->
            val realIdx = page % banners.size
            val banner = banners[realIdx]
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            ) {
                Box(Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = banner.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Text(
                        text = banner.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }

        if (banners.size > 1) {
            Spacer(Modifier.height(Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(banners.size) { idx ->
                    val isActive = pagerState.currentPage % banners.size == idx
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isActive) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                            ),
                    )
                }
            }
        }
    }
}

// ── 功能入口 ──

@Composable
private fun HomeEntry(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(Spacing.xs),
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier.size(44.dp),
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
