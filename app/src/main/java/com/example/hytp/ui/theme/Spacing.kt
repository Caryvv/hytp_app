package com.example.hytp.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 统一间距 Token（8dp 栅格，docs/dev/15 §5.2）。
 * 使用方式：`Spacing.lg` 等，避免页面硬编码 dp 值。
 */
object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
}
