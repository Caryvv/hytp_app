package com.example.hytp.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hytp.ui.theme.HytpThemeLight

/**
 * 价格文本（docs/dev/15 §6.9）。
 * 朱红（tertiary）色 + Medium 字重，¥ 符号自动附加。
 */
@Composable
fun PriceText(
    price: String,
    modifier: Modifier = Modifier,
    isEmphasis: Boolean = true,
) {
    val display = if (price.startsWith("¥")) price else "¥$price"
    Text(
        text = display,
        color = if (isEmphasis) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier,
    )
}

/**
 * 大号价格（商品详情/结算核价等场景）。
 */
@Composable
fun PriceTextLarge(
    price: String,
    modifier: Modifier = Modifier,
    isEmphasis: Boolean = true,
) {
    val display = if (price.startsWith("¥")) price else "¥$price"
    Text(
        text = display,
        color = if (isEmphasis) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier,
    )
}

// ── Preview ──

@Preview(showBackground = true)
@Composable
private fun PriceTextPreview() {
    HytpThemeLight {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PriceText("128.00")
            PriceTextLarge("998.00")
            PriceText("128.00", isEmphasis = false)
        }
    }
}
