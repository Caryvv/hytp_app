package com.example.hytp.feature.order.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hytp.feature.order.vm.CheckoutViewModel

/**
 * 结算页：地址选择 + 商品分组预览 + 提交下单。下单成功进订单详情（去支付）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onManageAddress: () -> Unit,
    onOrderCreated: (String) -> Unit,
    pickedAddressId: Long? = null,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // 从地址页选回地址：刷新列表并选中
    androidx.compose.runtime.LaunchedEffect(pickedAddressId) {
        if (pickedAddressId != null) {
            viewModel.loadAddresses()
            viewModel.selectAddress(pickedAddressId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("确认订单") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
        bottomBar = {
            state.preview?.let { preview ->
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("实付：", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "¥${preview.totalAmount}",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { viewModel.submit { orderNos -> orderNos.firstOrNull()?.let(onOrderCreated) } },
                        enabled = state.canSubmit,
                    ) { Text(if (state.submitting) "提交中…" else "提交订单") }
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

                state.error != null && state.preview == null ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Text("点击重试", modifier = Modifier.clickable { viewModel.load() }, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                state.preview != null ->
                    LazyColumn(Modifier.fillMaxSize()) {
                        // 地址
                        item {
                            Column(
                                Modifier.fillMaxWidth().clickable { onManageAddress() }.padding(16.dp),
                            ) {
                                val addr = state.selectedAddress
                                if (addr == null) {
                                    Text("请选择收货地址 ›", color = MaterialTheme.colorScheme.primary)
                                } else {
                                    Text("${addr.name}  ${addr.phone}", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${addr.province}${addr.city}${addr.district}${addr.detail}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text("切换/管理地址 ›", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            HorizontalDivider()
                        }
                        // 按店铺分组的商品
                        state.preview!!.shops.forEach { shop ->
                            item {
                                Text(
                                    shop.shopName,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }
                            items(shop.items.size) { i ->
                                val it = shop.items[i]
                                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        Text(it.title, style = MaterialTheme.typography.bodyMedium)
                                        if (it.spec.isNotEmpty()) {
                                            Text(
                                                it.spec.entries.joinToString(" / ") { e -> "${e.key}:${e.value}" },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    Text("¥${it.price} ×${it.qty}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            item {
                                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    Spacer(Modifier.weight(1f))
                                    Text("小计 ¥${shop.subtotal}", style = MaterialTheme.typography.bodyMedium)
                                }
                                HorizontalDivider()
                            }
                        }
                        if (state.error != null) {
                            item {
                                Text(state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
            }
        }
    }
}
