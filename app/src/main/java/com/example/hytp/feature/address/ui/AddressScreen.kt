package com.example.hytp.feature.address.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hytp.core.network.dto.Address
import com.example.hytp.core.network.dto.AddressRequest
import com.example.hytp.feature.address.vm.AddressViewModel

/**
 * 收货地址页：列表 + 新建表单。点选地址通过 onPick 回传给结算页。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    onBack: () -> Unit,
    onPick: (Long) -> Unit,
    viewModel: AddressViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("收货地址") },
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

                showForm ->
                    AddressForm(
                        saving = state.saving,
                        error = state.error,
                        onSubmit = { req -> viewModel.create(req) { addr -> showForm = false; onPick(addr.id) } },
                        onCancel = { showForm = false },
                    )

                else ->
                    Column(Modifier.fillMaxSize()) {
                        if (state.list.isEmpty()) {
                            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                                Text("还没有收货地址", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            state.list.forEach { addr ->
                                AddressRow(addr, onPick = { onPick(addr.id) }, onRemove = { viewModel.remove(addr.id) })
                                HorizontalDivider()
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { showForm = true },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        ) { Text("新增收货地址") }
                    }
            }
        }
    }
}

@Composable
private fun AddressRow(addr: Address, onPick: () -> Unit, onRemove: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onPick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row {
                Text(addr.name, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(0.dp))
                Text("  ${addr.phone}", style = MaterialTheme.typography.bodyMedium)
                if (addr.isDefault == 1) {
                    Text("  [默认]", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(
                "${addr.province}${addr.city}${addr.district}${addr.detail}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            "删除",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { onRemove() },
        )
    }
}

@Composable
private fun AddressForm(
    saving: Boolean,
    error: String?,
    onSubmit: (AddressRequest) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("收货人") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("手机号") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = province, onValueChange = { province = it }, label = { Text("省") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("市") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("区/县（选填）") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text("详细地址") }, modifier = Modifier.fillMaxWidth())

        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        val canSubmit = name.isNotBlank() && phone.isNotBlank() && province.isNotBlank() &&
            city.isNotBlank() && detail.isNotBlank() && !saving
        Button(
            onClick = {
                onSubmit(AddressRequest(
                    name = name.trim(), phone = phone.trim(), province = province.trim(),
                    city = city.trim(), district = district.trim(), detail = detail.trim(), isDefault = 1,
                ))
            },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (saving) "保存中…" else "保存并使用") }
        Text("取消", modifier = Modifier.fillMaxWidth().clickable { onCancel() }.padding(8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
