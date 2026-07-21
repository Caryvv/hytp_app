package com.example.hytp.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hytp.ui.theme.HytpThemeLight

/**
 * 区块标题（docs/dev/15 §6.4）。
 * 左侧缠枝纹装饰线（primary 30% 透明）+ 标题（Brand 字体）+ 右侧可选操作。
 */
@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        // 缠枝纹细装饰线（简化形态：两条不同粗细的竖线并排）
        Canvas(
            modifier = Modifier.width(10.dp).height(22.dp),
        ) {
            val color = primaryColor.copy(alpha = 0.3f)
            drawLine(
                color = color,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height),
                strokeWidth = 2.dp.toPx(),
            )
            drawLine(
                color = color.copy(alpha = 0.5f),
                start = Offset(size.width / 2 + 3.dp.toPx(), 4.dp.toPx()),
                end = Offset(size.width / 2 + 3.dp.toPx(), size.height - 4.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f),
        )

        if (action != null && onAction != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable { onAction() }
                    .padding(4.dp),
            )
        }
    }
}

// ── Preview ──

@Preview(showBackground = true)
@Composable
private fun SectionTitlePreview() {
    HytpThemeLight {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("SectionTitle", style = MaterialTheme.typography.headlineSmall)
            SectionTitle("萌新指南")
            SectionTitle("同袍社交", action = "更多 ›") { }
            SectionTitle("汉服课堂", action = "查看全部") { }
        }
    }
}
