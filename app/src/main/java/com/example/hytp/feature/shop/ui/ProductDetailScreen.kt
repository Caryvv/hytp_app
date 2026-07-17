package com.example.hytp.feature.shop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.hytp.core.network.dto.ProductDetail
import com.example.hytp.core.network.dto.Review
import com.example.hytp.core.network.dto.ShopPublic
import com.example.hytp.feature.shop.vm.ProductDetailViewModel

/**
 * 商品详情页（对齐 08 §3.2，只读）：图集/标题价格/规格/图文/评价（情感关键词）/商家卡片。
 * 加购/购买按钮属阶段3，本阶段不放。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onShopClick: (Long) -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("商品详情") },
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
                state.loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                state.error != null ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "点击重试",
                                modifier = Modifier.clickable { viewModel.load() },
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                state.detail != null ->
                    DetailContent(
                        detail = state.detail!!,
                        reviews = state.reviews,
                        onShopClick = onShopClick,
                    )
            }
        }
    }
}

@Composable
private fun DetailContent(
    detail: ProductDetail,
    reviews: List<Review>,
    onShopClick: (Long) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        // 主图
        item {
            AsyncImage(
                model = detail.cover.ifBlank { detail.images.firstOrNull() },
                contentDescription = detail.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            )
        }
        // 标题 + 价格
        item {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "¥${detail.price}",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(8.dp))
                Text(detail.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "已售 ${detail.sales} · 评分 ${detail.rating}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // 规格
        if (detail.skus.isNotEmpty()) {
            item {
                HorizontalDivider()
                Column(Modifier.padding(16.dp)) {
                    Text("规格", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    detail.skus.forEach { sku ->
                        val spec = sku.spec.entries.joinToString(" / ") { "${it.key}:${it.value}" }
                        Text(
                            text = "$spec  ¥${sku.price}（库存 ${sku.stock}）",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }
        // 商家卡片
        detail.shop?.let { shop ->
            item {
                HorizontalDivider()
                ShopCard(shop = shop, onClick = { onShopClick(shop.id) })
            }
        }
        // 图文详情
        if (detail.detail.isNotBlank()) {
            item {
                HorizontalDivider()
                Column(Modifier.padding(16.dp)) {
                    Text("商品详情", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    // 富文本简单去标签展示（正式可接 HTML 渲染）
                    Text(
                        text = detail.detail.replace(Regex("<[^>]*>"), ""),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        // 评价区
        item {
            HorizontalDivider()
            Text(
                "评价（${reviews.size}）",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(16.dp),
            )
        }
        items(reviews.size) { i ->
            ReviewItem(reviews[i])
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ShopCard(shop: ShopPublic, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = shop.logo,
            contentDescription = shop.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.height(48.dp).aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(shop.name, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "${shop.region} · 信用分 ${shop.creditScore}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text("进店 ›", color = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewItem(review: Review) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("${"★".repeat(review.rating)}${"☆".repeat(5 - review.rating)}", color = MaterialTheme.colorScheme.error)
        if (review.content.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(review.content, style = MaterialTheme.typography.bodyMedium)
        }
        if (review.keywords.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                review.keywords.forEach { kw ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            kw,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}
