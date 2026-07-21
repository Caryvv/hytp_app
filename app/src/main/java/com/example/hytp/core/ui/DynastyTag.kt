package com.example.hytp.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hytp.ui.theme.HytpThemeLight
import com.example.hytp.ui.theme.LiuLv
import com.example.hytp.ui.theme.TuoHuang
import com.example.hytp.ui.theme.ZhuHong

/**
 * DynastyTag 语义（docs/dev/15 §6.3）。
 */
enum class TagSemantic {
    /** 朝代/形制信息标签（青瓷底） */
    Info,
    /** 山正认证标签（朱红描边+"正"字） */
    Verified,
    /** 会员标签（缃黄底） */
    Member,
    /** 免费/付费标签（柳绿/缃黄） */
    GreenInfo,
}

/**
 * 国风标签 Chip（docs/dev/15 §6.3）。
 * 用于形制科普、商品、图鉴的分类标注。
 */
@Composable
fun DynastyTag(
    text: String,
    modifier: Modifier = Modifier,
    semantic: TagSemantic = TagSemantic.Info,
) {
    val (container, content) = when (semantic) {
        TagSemantic.Info -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        TagSemantic.Verified -> Color.Transparent to ZhuHong
        TagSemantic.Member -> TuoHuang.copy(alpha = 0.18f) to TuoHuang
        TagSemantic.GreenInfo -> LiuLv.copy(alpha = 0.15f) to LiuLv
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = container,
        border = if (semantic == TagSemantic.Verified) {
            androidx.compose.foundation.BorderStroke(1.dp, ZhuHong.copy(alpha = 0.6f))
        } else null,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

// ── Preview ──

@Preview(showBackground = true)
@Composable
private fun DynastyTagPreview() {
    HytpThemeLight {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("DynastyTag", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DynastyTag("唐")
                DynastyTag("明·马面裙")
                DynastyTag("曲裾")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DynastyTag("正品认证", semantic = TagSemantic.Verified)
                DynastyTag("高级会员", semantic = TagSemantic.Member)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DynastyTag("免费", semantic = TagSemantic.GreenInfo)
                DynastyTag("付费", semantic = TagSemantic.Member)
            }
        }
    }
}
