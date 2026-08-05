package com.example.hytp.feature.beginner.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hytp.R
import com.example.hytp.core.ui.SectionTitle
import com.example.hytp.ui.theme.Spacing

/**
 * 萌新入门引导页（纯静态科普，无后端依赖）。
 * 汉服是什么 → 常见形制 → 上手三步，配国风图标。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeginnerGuideScreen(
    onBack: () -> Unit,
    onOpenSocial: () -> Unit = {},
    onOpenMall: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("萌新入门") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item {
                Spacer(Modifier.height(Spacing.md))
                Text(
                    text = "与子同袍，岂曰无衣",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "第一次接触汉服？这里带你从零认识它。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── 汉服是什么 ──
            item { SectionTitle("汉服是什么") }
            item {
                GuideCard(
                    iconRes = R.drawable.icon_hanfu_style,
                    title = "汉民族传统服饰",
                    body = "汉服即“汉民族传统服饰”，以交领右衽、系带隐扣、上衣下裳为基本特征，" +
                        "历经数千年演变，形成了各具时代风貌的形制体系。它不是某一朝代的“戏服”，" +
                        "而是一整套贯穿历史的着装文化。",
                )
            }

            // ── 常见形制 ──
            item { SectionTitle("常见形制") }
            item {
                GuideCard(
                    iconRes = R.drawable.icon_timeline,
                    title = "襦裙",
                    body = "上襦下裙的两件式结构，是最日常也最百搭的入门形制。按裙腰高低分齐胸、齐腰，" +
                        "四季皆宜，新手最易上手。",
                )
            }
            item {
                GuideCard(
                    iconRes = R.drawable.icon_roadmap,
                    title = "袄裙",
                    body = "明制常见形制，上袄下裙，收口窄袖、立领或圆领，端庄挺括，秋冬穿着尤为合适。",
                )
            }
            item {
                GuideCard(
                    iconRes = R.drawable.icon_classroom,
                    title = "圆领袍 / 直裰",
                    body = "一件式长袍，线条利落，男女皆可。日常通勤、文化活动都不违和，是许多同袍的常备款。",
                )
            }

            // ── 上手三步 ──
            item { SectionTitle("上手三步") }
            item { StepRow(1, "看", "先在社交圈多看同袍的日常穿搭，找到自己喜欢的形制和配色。") }
            item { StepRow(2, "选", "从一套襦裙或袄裙起步，挑正规商家的成衣，避免踩雷。") }
            item { StepRow(3, "穿", "大胆穿出门。汉服是日常服，不必等“特殊场合”。") }

            // ── 下一步 ──
            item { SectionTitle("接下来") }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    NextEntry(
                        iconRes = R.drawable.icon_social,
                        label = "逛逛同袍社交",
                        onClick = onOpenSocial,
                        modifier = Modifier.weight(1f),
                    )
                    NextEntry(
                        iconRes = R.drawable.icon_shop,
                        label = "去汉服商城",
                        onClick = onOpenMall,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(Spacing.xxl))
            }
        }
    }
}

@Composable
private fun GuideCard(iconRes: Int, title: String, body: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(Spacing.lg)) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.size(Spacing.md))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(Spacing.xs))
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StepRow(step: Int, title: String, body: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = step.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.size(Spacing.md))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(Spacing.xs))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NextEntry(iconRes: Int, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        onClick = onClick,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(44.dp),
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
