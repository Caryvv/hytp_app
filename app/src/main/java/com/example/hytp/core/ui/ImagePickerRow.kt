package com.example.hytp.core.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * 公共选图上传行：显示已上传图（缩略图 + 点击右上角 × 移除）+ 加号格（选图，上传中转圈）。
 * 上传逻辑在各 ViewModel（复用 UploadRepository）；本组件只管选图与展示。
 * 参考 FeedPublishScreen 原实现抽取，供发动态/评价晒图/退款凭证复用。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImagePickerRow(
    uploadedUrls: List<String>,
    uploading: Boolean,
    onPickImages: (List<Uri>) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxCount: Int = 9,
) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) onPickImages(uris)
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        uploadedUrls.forEach { url ->
            Box(modifier = Modifier.size(72.dp)) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.small),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                        .clickable { onRemove(url) },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Text(
                        "×",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        if (uploadedUrls.size < maxCount) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                    .clickable(enabled = !uploading) { picker.launch("image/*") },
                contentAlignment = Alignment.Center,
            ) {
                if (uploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.material3.Text(
                            "＋",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                        androidx.compose.material3.Text(
                            "图片",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
