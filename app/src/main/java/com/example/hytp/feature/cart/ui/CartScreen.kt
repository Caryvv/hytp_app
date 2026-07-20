package com.example.hytp.feature.cart.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.hytp.core.network.dto.CartItem
import com.example.hytp.feature.cart.vm.CartViewModel

/**
 * 购物车页：列表 + 改量/删除 + 底部合计与结算入口。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    onProductClick: (Long) -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("购物车") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
        bottomBar = {
            if (state.items.isNotEmpty()) {
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("合计：", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "¥${state.totalAmount}",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.weight(1f))
                    Button(onClick = onCheckout, enabled = state.hasValidItems) {
                        Text("去结算")
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

                state.items.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("购物车还是空的", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                else ->
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(state.items, key = { it.id }) { item ->
                            CartRow(
                                item = item,
                                onProductClick = { onProductClick(item.productId) },
                                onInc = { viewModel.changeQty(item, item.qty + 1) },
                                onDec = { viewModel.changeQty(item, item.qty - 1) },
                                onRemove = { viewModel.remove(item) },
                            )
                            HorizontalDivider()
                        }
                    }
            }
        }
    }
}

@Composable
private fun CartRow(
    item: CartItem,
    onProductClick: () -> Unit,
    onInc: () -> Unit,
    onDec: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = item.cover,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.height(72.dp).aspectRatio(1f).clip(RoundedCornerShape(8.dp))
                .clickable { onProductClick() },
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            if (item.spec.isNotEmpty()) {
                Text(
                    item.spec.entries.joinToString(" / ") { "${it.key}:${it.value}" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!item.valid) {
                Text("已失效", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "¥${item.price}",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                // 数量步进
                OutlinedButton(onClick = onDec, enabled = item.qty > 1, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp), modifier = Modifier.height(32.dp).width(36.dp)) {
                    Text("−")
                }
                Text("  ${item.qty}  ", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = onInc, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp), modifier = Modifier.height(32.dp).width(36.dp)) {
                    Text("+")
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "删除",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { onRemove() },
        )
    }
}
