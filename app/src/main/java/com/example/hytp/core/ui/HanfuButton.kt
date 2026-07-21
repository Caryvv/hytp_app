package com.example.hytp.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hytp.ui.theme.HytpThemeLight

/**
 * HanfuButton 变体（docs/dev/15 §6.1）。
 */
enum class HanfuButtonVariant {
    /** 主态：填充黛青（primary），用于常规主要操作 */
    Primary,

    /** 点睛态：填充朱红（tertiary），单屏仅一个，用于最关键 CTA（立即购买/发布/关注） */
    Emphasis,

    /** 描边态：outline 描边 + primary 文字 */
    Outline,

    /** 纯文字态：无边框无底 */
    Text,
}

enum class HanfuButtonSize(val height: Dp, val horizontal: Dp) {
    Large(48.dp, 24.dp),
    Medium(40.dp, 16.dp),
    Small(32.dp, 12.dp),
}

/**
 * 国风按钮（docs/dev/15 §6.1）。
 * Primary 用黛青填充，Emphasis 用朱红填充（点睛），Outline/Text 克制。
 */
@Composable
fun HanfuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: HanfuButtonVariant = HanfuButtonVariant.Primary,
    size: HanfuButtonSize = HanfuButtonSize.Medium,
    enabled: Boolean = true,
) {
    when (variant) {
        HanfuButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(size.height),
                enabled = enabled,
                shape = MaterialTheme.shapes.small,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = size.horizontal),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) { Text(text, style = MaterialTheme.typography.labelLarge) }
        }

        HanfuButtonVariant.Emphasis -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(size.height),
                enabled = enabled,
                shape = MaterialTheme.shapes.small,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = size.horizontal),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ),
            ) { Text(text, style = MaterialTheme.typography.labelLarge) }
        }

        HanfuButtonVariant.Outline -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.height(size.height),
                enabled = enabled,
                shape = MaterialTheme.shapes.small,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = size.horizontal),
                border = BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) { Text(text, style = MaterialTheme.typography.labelLarge) }
        }

        HanfuButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.height(size.height),
                enabled = enabled,
                shape = MaterialTheme.shapes.small,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = size.horizontal),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) { Text(text, style = MaterialTheme.typography.labelLarge) }
        }
    }
}

// ── Preview ──

@Preview(showBackground = true)
@Composable
private fun HanfuButtonPreview() {
    HytpThemeLight {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("HanfuButton", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HanfuButton("立即购买", {}, variant = HanfuButtonVariant.Emphasis)
                HanfuButton("加入购物车", {}, variant = HanfuButtonVariant.Outline)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HanfuButton("登录 / 注册", {}, variant = HanfuButtonVariant.Primary)
                HanfuButton("发送验证码", {}, variant = HanfuButtonVariant.Outline, size = HanfuButtonSize.Small)
            }
            HanfuButton("更多 ›", {}, variant = HanfuButtonVariant.Text)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HanfuButton("禁用", {}, enabled = false)
                HanfuButton("禁用", {}, variant = HanfuButtonVariant.Emphasis, enabled = false)
                HanfuButton("禁用", {}, variant = HanfuButtonVariant.Outline, enabled = false)
            }
        }
    }
}
