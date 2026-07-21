package com.example.hytp.feature.splash.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hytp.feature.splash.vm.SplashViewModel
import com.example.hytp.ui.theme.HytpThemeLight

/**
 * 启动页（docs/dev/15 §7.1）：缠枝纹水印 + LOGO + Slogan。
 * 3s 可跳过，首次启动走隐私协议弹窗（后续补）。
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        val loggedIn = viewModel.resolveDestination()
        if (loggedIn) onNavigateToHome() else onNavigateToLogin()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            // 缠枝纹水印（primary 8% 透明，中心对称）
            val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                // 上下左右四向简笔缠枝弧线
                val dr = 80.dp.toPx()
                for (angle in listOf(0f, 90f, 180f, 270f)) {
                    val rad = Math.toRadians(angle.toDouble()).toFloat()
                    val startX = cx + kotlin.math.cos(rad) * dr * 0.3f
                    val startY = cy + kotlin.math.sin(rad) * dr * 0.3f
                    val endX = cx + kotlin.math.cos(rad) * dr
                    val endY = cy + kotlin.math.sin(rad) * dr
                    drawLine(lineColor, Offset(startX, startY), Offset(endX, endY), strokeWidth = 1.dp.toPx())
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "汉韵同袍",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "着我汉家衣裳 · 兴我礼仪之邦",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    HytpThemeLight {
        SplashScreen(
            onNavigateToLogin = {},
            onNavigateToHome = {},
        )
    }
}
