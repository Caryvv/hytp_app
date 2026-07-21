package com.example.hytp.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hytp.ui.theme.HytpThemeLight

/**
 * 首页搜索栏（docs/dev/15 §6.5）。
 * 首页顶栏内联使用；点击进入统一搜索页。
 * 搜索图标使用 Unicode 放大镜字符，避免 material-icons 依赖。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜汉服 · 商家 · 攻略 · 同袍",
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        active = false,
        onActiveChange = {},
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = {
            Text(
                text = "\uD83D\uDD0D", // 🔍
                style = MaterialTheme.typography.titleMedium,
            )
        },
        shape = MaterialTheme.shapes.small,
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {}
}

// ── Preview ──

@Preview(showBackground = true)
@Composable
private fun HomeSearchBarPreview() {
    HytpThemeLight {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("SearchBar", style = MaterialTheme.typography.headlineSmall)
            HomeSearchBar(query = "", onQueryChange = {}, onSearch = {})
        }
    }
}
