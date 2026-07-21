package com.example.hytp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 国风圆角体系（docs/dev/15 §5.1）。
 * 整体偏温润，圆角适中偏大，避免尖锐直角。
 */
val HytpShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // Chip、小标签、角标
    small = RoundedCornerShape(10.dp),        // 输入框、小按钮
    medium = RoundedCornerShape(14.dp),        // 卡片、对话气泡
    large = RoundedCornerShape(20.dp),         // 大卡片、底部弹窗（顶部圆角）
    extraLarge = RoundedCornerShape(28.dp),    // 图片容器、Banner
)
