package com.example.hytp.feature.order.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hytp.core.network.dto.Order
import com.example.hytp.feature.order.vm.OrderDetailViewModel

/**
 * 订单详情：状态/收货信息/明细/金额 + 底部操作（付款/取消/确认收货/售后/评价）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    onReview: (orderNo: String, productId: Long) -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showRefund by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("订单详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            state.order?.let { order ->
                OrderActions(
                    order = order,
                    running = state.actionRunning,
                    onPay = { viewModel.pay() },
                    onCancel = { viewModel.cancel() },
                    onConfirm = { viewModel.confirm() },
                    onRefund = { showRefund = true },
                    onReview = { order.items.firstOrNull()?.let { onReview(order.orderNo, it.productId) } },
                    onReturn = { viewModel.returnRent() },
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                state.error != null && state.order == null ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Text("点击重试", modifier = Modifier.clickable { viewModel.load() }, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                state.order != null ->
                    OrderDetailContent(state.order!!)
            }
        }
    }

    if (showRefund) {
        RefundDialog(
            onDismiss = { showRefund = false },
            onConfirm = { reason -> showRefund = false; viewModel.refund(reason) },
        )
    }
}

@Composable
private fun OrderDetailContent(order: Order) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(16.dp)) {
                Text(orderStatusText(order.status), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider()
        }
        // 收货信息
        order.address?.let { addr ->
            item {
                Column(Modifier.padding(16.dp)) {
                    Text("${addr.name}  ${addr.phone}", style = MaterialTheme.typography.titleSmall)
                    Text("${addr.province}${addr.city}${addr.district}${addr.detail}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider()
            }
        }
        // 店铺 + 明细
        item {
            Text(order.shopName.ifBlank { "店铺#${order.shopId}" }, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(16.dp))
        }
        items(order.items.size) { i ->
            val it = order.items[i]
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(it.title, style = MaterialTheme.typography.bodyMedium)
                    if (it.spec.isNotEmpty()) {
                        Text(it.spec.entries.joinToString(" / ") { e -> "${e.key}:${e.value}" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("¥${it.price} ×${it.qty}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        // 金额与单号
        item {
            HorizontalDivider()
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row { Text("订单编号", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text(order.orderNo) }
                Row { Text("商品总额", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text("¥${order.totalAmount}") }
                Row {
                    Text("实付款", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("¥${order.payAmount}", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun OrderActions(
    order: Order,
    running: Boolean,
    onPay: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onRefund: () -> Unit,
    onReview: () -> Unit,
    onReturn: () -> Unit,
) {
    HorizontalDivider()
    Row(
        Modifier.fillMaxWidth().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (order.status) {
            0 -> { // 待付款
                OutlinedButton(onClick = onCancel, enabled = !running) { Text("取消订单") }
                Button(onClick = onPay, enabled = !running) { Text(if (running) "处理中…" else "去支付") }
            }
            1 -> { // 待发货
                OutlinedButton(onClick = onRefund, enabled = !running) { Text("申请售后") }
            }
            2 -> { // 待收货
                OutlinedButton(onClick = onRefund, enabled = !running) { Text("申请售后") }
                Button(onClick = onConfirm, enabled = !running) { Text(if (running) "处理中…" else "确认收货") }
            }
            7 -> { // 租赁·使用中
                Button(onClick = onReturn, enabled = !running) { Text(if (running) "处理中…" else "寄回归还") }
            }
            8 -> Text("待商家确认归还", color = MaterialTheme.colorScheme.onSurfaceVariant)
            4 -> { // 已完成
                Button(onClick = onReview, enabled = !running) { Text("评价") }
            }
            else -> Text("状态：${orderStatusText(order.status)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RefundDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("申请售后") },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("退款原因") },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason.ifBlank { "七天无理由退货" }) }) { Text("提交") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}
