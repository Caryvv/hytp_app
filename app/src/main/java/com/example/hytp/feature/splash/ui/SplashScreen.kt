package com.example.hytp.feature.splash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hytp.R
import com.example.hytp.feature.splash.vm.SplashViewModel
import com.example.hytp.ui.theme.HytpThemeLight

/**
 * 启动页（docs/dev/15 §6.1）：宣纸底 + 缠枝纹边框 + 盘扣 LOGO + 书法品牌名 + Slogan。
 * 跳转即时（resolveDestination 无停留），故不放「跳过」按钮。
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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // 宣纸纹理底
            Image(
                painter = painterResource(R.drawable.bg_xuanpaper),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            // 缠枝纹装饰边框（四角）
            Image(
                painter = painterResource(R.drawable.deco_chanzhi_border),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // 盘扣造型 LOGO
                Image(
                    painter = painterResource(R.drawable.logo_icon),
                    contentDescription = "汉韵同袍",
                    modifier = Modifier.size(120.dp),
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "汉韵同袍",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "与子同袍，岂曰无衣",
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
