package com.example.hytp.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hytp.ui.theme.HytpThemeLight

/**
 * 底部导航 Tab 定义。
 */
enum class BottomTab(
    val label: String,
    val icon: String,
) {
    Home("首页", "🏠"),
    Social("社交", "👥"),
    Mall("商城", "🏪"),
    Mine("我的", "👤"),
}

/**
 * 国风底部导航栏（docs/dev/15 §6.7）。
 * 4 Tab：首页 / 社交 / 商城 / 我的。
 * 选中黛青（primary），未选中烟灰；文字 labelSmall。
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
                    Text(
                        text = tab.icon,
                        style = MaterialTheme.typography.titleLarge,
                    )
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
