package com.example.hytp.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hytp.R
import com.example.hytp.ui.theme.HytpThemeLight

/**
 * 底部导航 Tab 定义（docs/dev/15 §5.6）。线性国风图标。
 */
enum class BottomTab(
    val label: String,
    val iconRes: Int,
) {
    Home("首页", R.drawable.nav_home),
    Social("社交", R.drawable.nav_social),
    Qa("智能问答", R.drawable.nav_ai_chat),
    Mall("商城", R.drawable.nav_shop),
    Mine("我的", R.drawable.nav_mine),
}

/**
 * 国风底部导航栏（docs/dev/15 §5.6）。
 * 5 Tab：首页 / 社交 / 智能问答 / 商城 / 我的（智能问答居中，朱红盘扣突出）。
 * 线性图标随选中态染色（primary / onSurfaceVariant）；中间盘扣保留本色不染色。
 */
@Composable
fun HytpBottomBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    NavigationBar(
        modifier = modifier,
        containerColor = colors.surface,
        contentColor = colors.onSurface,
    ) {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == currentTab,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == BottomTab.Qa) {
                        // 朱红盘扣：保留本色（不染色），稍大以突出
                        Image(
                            painter = painterResource(tab.iconRes),
                            contentDescription = tab.label,
                            modifier = Modifier.size(30.dp),
                        )
                    } else {
                        // 线性图标：随选中态染色（由 NavigationBarItem colors 控制 LocalContentColor）
                        Icon(
                            painter = painterResource(tab.iconRes),
                            contentDescription = tab.label,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant,
                    indicatorColor = colors.primaryContainer,
                ),
            )
        }
    }
}

// ── Preview ──

@Preview(showBackground = true)
@Composable
private fun HytpBottomBarPreview() {
    HytpThemeLight {
        HytpBottomBar(
            currentTab = BottomTab.Home,
            onTabSelected = {},
        )
    }
}
