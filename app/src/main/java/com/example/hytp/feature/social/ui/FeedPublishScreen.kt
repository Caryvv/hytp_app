package com.example.hytp.feature.social.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.hytp.core.ui.HanfuButton
import com.example.hytp.core.ui.HanfuButtonVariant
import com.example.hytp.core.ui.ImagePickerRow
import com.example.hytp.feature.social.vm.FeedPublishViewModel

/**
 * 发布动态页：文案 + 图片选择上传 + 标签 + 城市。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedPublishScreen(
    onBack: () -> Unit,
    onPublished: () -> Unit,
    viewModel: FeedPublishViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var content by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    LaunchedEffect(state.publishedId) { if (state.publishedId != null) onPublished() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布动态") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("分享此刻的同袍生活…") },
                modifier = Modifier.fillMaxWidth().height(140.dp),
            )

            // ── 图片选择 + 上传 ──
            ImagePickerRow(
                uploadedUrls = state.uploadedUrls,
                uploading = state.uploadingImages.isNotEmpty(),
                onPickImages = viewModel::uploadImages,
                onRemove = viewModel::removeImage,
            )

            OutlinedTextField(
                value = tagsText,
                onValueChange = { tagsText = it },
                label = { Text("标签（空格分隔，如 马面裙 明制，选填）") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("城市（选填）") },
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            HanfuButton(
                text = if (state.submitting) "发布中…" else "发布",
                onClick = {
                    val tags = tagsText.split(Regex("[\\s，]+")).map { it.trim() }.filter { it.isNotBlank() }
                    viewModel.publish(content, tags, city)
                },
                enabled = !state.submitting && content.isNotBlank(),
                variant = HanfuButtonVariant.Emphasis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
