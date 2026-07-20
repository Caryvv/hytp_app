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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.hytp.core.network.dto.ProductDetail
import com.example.hytp.core.network.dto.ProductSku
import com.example.hytp.core.network.dto.Review
import com.example.hytp.core.network.dto.ShopPublic
import com.example.hytp.feature.shop.vm.ProductDetailViewModel

/**
 * 商品详情页（对齐 08 §3.2）：图集/标题价格/规格/图文/评价（情感关键词）/商家卡片。
 * 阶段3：底部加购/立即购买 + SKU 选择弹层。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onShopClick: (Long) -> Unit,
    onGoCart: () -> Unit,
    onRentBooked: (String) -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showSkuSheet by remember { mutableStateOf(false) }
    var showRentSheet by remember { mutableStateOf(false) }
    // 租赁商品 tradeType=2
    val isRent = state.detail?.tradeType == 2

    LaunchedEffect(state.cartMessage) {
        state.cartMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeCartMessage()
        }
    }
    LaunchedEffect(state.rentMessage, state.rentOrderNo) {
        state.rentMessage?.let { snackbar.showSnackbar(it) }
        val no = state.rentOrderNo
        if (no != null) {
            viewModel.consumeRent()
            onRentBooked(no)
        } else if (state.rentMessage != null) {
            viewModel.consumeRent()
        }
    }

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
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (state.detail != null) {
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isRent) {
                        // 租赁商品：预约租赁
                        Button(
                            onClick = { showRentSheet = true },
                            enabled = !state.rentRunning,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(if (state.rentRunning) "处理中…" else "预约租赁") }
                    } else {
                        OutlinedButton(onClick = onGoCart) { Text("购物车") }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = { showSkuSheet = true },
                            enabled = !state.cartRunning,
                        ) { Text("加入购物车") }
                    }
                }
            }
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

    if (showSkuSheet && state.detail != null) {
        SkuSheet(
            detail = state.detail!!,
            sheetState = rememberModalBottomSheetState(),
            onDismiss = { showSkuSheet = false },
            onConfirm = { skuId, qty ->
                showSkuSheet = false
                viewModel.addToCart(skuId, qty)
            },
        )
    }

    if (showRentSheet && state.detail != null) {
        RentSheet(
            detail = state.detail!!,
            sheetState = rememberModalBottomSheetState(),
            onDismiss = { showRentSheet = false },
            onConfirm = { skuId, days, deposit ->
                showRentSheet = false
                viewModel.bookRent(skuId, days, deposit)
            },
        )
    }
}

/**
 * 租赁预约弹层：选 SKU(有则必选) + 租赁天数(步进) + 押金(默认按日租金×2)。
 * 展示租金合计 = 日租金 × 天数，实付 = 租金 + 押金。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RentSheet(
    detail: ProductDetail,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onConfirm: (skuId: Long?, days: Int, deposit: String) -> Unit,
) {
    var selectedSku by remember { mutableStateOf<ProductSku?>(null) }
    var days by remember { mutableStateOf(1) }
    val hasSku = detail.skus.isNotEmpty()
    val dailyRent = (selectedSku?.price ?: detail.price).toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
    val rentTotal = dailyRent.multiply(java.math.BigDecimal(days))
    // 押金默认 = 日租金 × 2
    val deposit = dailyRent.multiply(java.math.BigDecimal(2))
    val payAmount = rentTotal.add(deposit)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("¥${dailyRent.toPlainString()} / 天", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(detail.title, style = MaterialTheme.typography.titleMedium)

            if (hasSku) {
                Spacer(Modifier.height(16.dp))
                Text("规格", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                detail.skus.forEach { sku ->
                    val label = sku.spec.entries.joinToString(" / ") { "${it.key}:${it.value}" }
                    val selected = selectedSku?.id == sku.id
                    OutlinedButton(
                        onClick = { selectedSku = if (selected) null else sku },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    ) { Text(if (selected) "● $label" else label) }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("租赁天数", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = { if (days > 1) days-- }, enabled = days > 1) { Text("−") }
                Text("  $days 天  ", style = MaterialTheme.typography.bodyLarge)
                OutlinedButton(onClick = { days++ }) { Text("+") }
            }

            Spacer(Modifier.height(12.dp))
            Text("租金 ¥${rentTotal.toPlainString()} + 押金 ¥${deposit.toPlainString()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("实付 ¥${payAmount.toPlainString()}（押金归还后退回）", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onConfirm(selectedSku?.id, days, deposit.setScale(2).toPlainString()) },
                enabled = !hasSku || selectedSku != null,
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars),
            ) {
                Text(if (hasSku && selectedSku == null) "请选择规格" else "确认预约")
            }
        }
    }
}

/**
 * SKU 选择弹层：无规格商品直接选数量；有规格必须先选一个 SKU。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkuSheet(
    detail: ProductDetail,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onConfirm: (skuId: Long?, qty: Int) -> Unit,
) {
    var selectedSku by remember { mutableStateOf<ProductSku?>(null) }
    var qty by remember { mutableStateOf(1) }
    val hasSku = detail.skus.isNotEmpty()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            val shownPrice = selectedSku?.price ?: detail.price
            Text("¥$shownPrice", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(detail.title, style = MaterialTheme.typography.titleMedium)

            if (hasSku) {
                Spacer(Modifier.height(16.dp))
                Text("规格", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                detail.skus.forEach { sku ->
                    val label = sku.spec.entries.joinToString(" / ") { "${it.key}:${it.value}" }
                    val selected = selectedSku?.id == sku.id
                    OutlinedButton(
                        onClick = { selectedSku = if (selected) null else sku },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    ) {
                        Text(if (selected) "● $label（库存 ${sku.stock}）" else "$label（库存 ${sku.stock}）")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("数量", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = { if (qty > 1) qty-- }, enabled = qty > 1) { Text("−") }
                Text("  $qty  ", style = MaterialTheme.typography.bodyLarge)
                OutlinedButton(onClick = { qty++ }) { Text("+") }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onConfirm(selectedSku?.id, qty) },
                enabled = !hasSku || selectedSku != null,
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars),
            ) {
                Text(if (hasSku && selectedSku == null) "请选择规格" else "加入购物车")
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
