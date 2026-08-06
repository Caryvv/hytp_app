package com.example.hytp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable

// ── 月白主题（Light）──
private val LightColorScheme = lightColorScheme(
    primary = DaiQing,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E0E6),
    onPrimaryContainer = DaiQingDark,

    secondary = QingCi,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE8E5),
    onSecondaryContainer = Color(0xFF1C3D3A),

    tertiary = ZhuHong,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6DCDC),
    onTertiaryContainer = ZhuHongDark,

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    background = YueBai,
    onBackground = MoHei,

    // surface 用月白（与背景同色），顶栏/卡片不再近白；层次靠 surfaceVariant + tonalElevation
    surface = YueBai,
    onSurface = MoHei,

    surfaceVariant = XuanZhi,
    onSurfaceVariant = YanHui,

    outline = Color(0xFFD8D2C4),
    outlineVariant = Color(0xFFE7E1D5),

    inverseSurface = MoHei,
    inverseOnSurface = Color(0xFFF0EDE3),
    inversePrimary = DaiQingLight,

    scrim = Color(0xFF000000),
)

// ── 墨夜主题（Dark）──
private val DarkColorScheme = darkColorScheme(
    primary = DaiQingLight,
    onPrimary = Color(0xFF0F1A20),
    primaryContainer = Color(0xFF26404E),
    onPrimaryContainer = Color(0xFFD6E0E6),

    secondary = QingCi,
    onSecondary = Color(0xFF0C2320),
    secondaryContainer = Color(0xFF364E4C),
    onSecondaryContainer = Color(0xFFDCE8E5),

    tertiary = ZhuHongLight,
    onTertiary = Color(0xFF3A0F11),
    tertiaryContainer = Color(0xFF5A2A2C),
    onTertiaryContainer = Color(0xFFF6DCDC),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),

    background = DarkBackground,
    onBackground = DarkOnBackground,

    surface = DarkSurface,
    onSurface = DarkOnBackground,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,

    outline = DarkOutline,
    outlineVariant = Color(0xFF2A2F36),

    inverseSurface = DarkOnBackground,
    inverseOnSurface = MoHei,
    inversePrimary = DaiQing,

    scrim = Color(0xFF000000),
)

/**
 * 汉韵同袍 · 国风主题（docs/dev/15 §3）。
 *
 * @param darkTheme  是否使用暗色主题（默认跟随系统）。
 *                   注意：动态取色（Material You）已关闭，确保品牌国风调性一致性。
 * @param content    内容区
 */
@Composable
fun HytpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HytpTypography,
        shapes = HytpShapes,
        content = content,
    )
}

/**
 * Light 主题强制预览快捷方式（用于 IDE Preview 注解）。
 */
@Composable
fun HytpThemeLight(content: @Composable () -> Unit) {
    HytpTheme(darkTheme = false, content = content)
}
