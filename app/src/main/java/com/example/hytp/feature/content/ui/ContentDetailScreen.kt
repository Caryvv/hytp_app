package com.example.hytp.feature.content.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.hytp.core.network.dto.ContentDetail
import com.example.hytp.core.ui.DynastyTag
import com.example.hytp.core.ui.ErrorView
import com.example.hytp.core.ui.HanfuButton
import com.example.hytp.core.ui.HanfuButtonSize
import com.example.hytp.core.ui.HanfuButtonVariant
import com.example.hytp.core.ui.LoadingView
import com.example.hytp.core.ui.SectionTitle
import com.example.hytp.core.ui.TagSemantic
import com.example.hytp.feature.content.vm.ContentDetailViewModel

/**
 * 文旅/文化 内容详情页：图集/标题/正文 + 点赞收藏行 + 底部报名栏（报名弹层收集姓名/手机/人数）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentDetailScreen(
    onBack: () -> Unit,
    viewModel: ContentDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showSignupSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            val c = state.content
            if (c != null) {
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 点赞 / 收藏
                    Text(
                        text = if (c.isLiked) "♥ ${c.likeCount}" else "♡ ${c.likeCount}",
                        color = if (c.isLiked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clickable { viewModel.toggleLike() },
                    )
                    Text(
                        text = if (c.isFavorited) "★ ${c.favoriteCount}" else "☆ ${c.favoriteCount}",
                        color = if (c.isFavorited) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clickable { viewModel.toggleFavorite() },
                    )
                    Spacer(Modifier.weight(1f))
                    if (c.isSignedUp) {
                        HanfuButton(
                            text = "已报名 · 取消",
                            onClick = { viewModel.cancelSignup() },
                            variant = HanfuButtonVariant.Outline,
                            size = HanfuButtonSize.Large,
                            enabled = !state.submitting,
                        )
                    } else {
                        HanfuButton(
                            text = if (state.submitting) "提交中…" else "立即报名",
                            onClick = { showSignupSheet = true },
                            variant = HanfuButtonVariant.Emphasis,
                            size = HanfuButtonSize.Large,
                            enabled = !state.submitting,
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading -> LoadingView()
                state.error != null -> ErrorView(message = state.error!!, onRetry = viewModel::load)
                state.content != null -> DetailContent(state.content!!)
            }
        }
    }

    if (showSignupSheet && state.content != null) {
        SignupSheet(
            sheetState = rememberModalBottomSheetState(),
            onDismiss = { showSignupSheet = false },
            onConfirm = { name, phone, qty ->
                showSignupSheet = false
                viewModel.signup(name, phone, qty)
            },
        )
    }
}

@Composable
private fun DetailContent(c: ContentDetail) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            AsyncImage(
                model = c.cover.ifBlank { c.images.firstOrNull() },
                contentDescription = c.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
            )
        }
        item {
            Column(Modifier.padding(16.dp)) {
                Text(c.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (c.city.isNotBlank()) {
                        DynastyTag(c.city, semantic = TagSemantic.Info)
                        Spacer(Modifier.width(6.dp))
                    }
                    if (c.category.isNotBlank()) {
                        DynastyTag(c.category, semantic = TagSemantic.GreenInfo)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "已报名 ${c.signupCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (c.detail.isNotBlank()) {
            item {
                HorizontalDivider()
                Column(Modifier.padding(16.dp)) {
                    SectionTitle("详情介绍")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = c.detail.replace(Regex("<[^>]*>"), ""),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        // 图集（cover 与 images 是独立字段，全部展示，不跳过任何一张）
        if (c.images.isNotEmpty()) {
            items(c.images.size) { i ->
                AsyncImage(
                    model = c.images[i],
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

/** 报名弹层：姓名 / 手机 / 人数（步进）。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignupSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, quantity: Int) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf(1) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("填写报名信息", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("姓名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("手机号") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("报名人数", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                HanfuButton("−", { if (qty > 1) qty-- }, variant = HanfuButtonVariant.Outline, size = HanfuButtonSize.Small, enabled = qty > 1)
                Text("  $qty  ", style = MaterialTheme.typography.bodyLarge)
                HanfuButton("+", { qty++ }, variant = HanfuButtonVariant.Outline, size = HanfuButtonSize.Small)
            }
            Spacer(Modifier.height(16.dp))
            HanfuButton(
                text = "确认报名",
                onClick = { onConfirm(name, phone, qty) },
                variant = HanfuButtonVariant.Emphasis,
                size = HanfuButtonSize.Large,
                enabled = name.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars),
            )
        }
    }
}
