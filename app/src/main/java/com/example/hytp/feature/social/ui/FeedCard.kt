package com.example.hytp.feature.social.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.hytp.core.network.dto.Feed

/**
 * 动态卡片：作者头像/昵称、文案、首图、点赞/评论/收藏/转发行。
 * 点卡片进详情，点作者进主页，点赞/收藏乐观更新。
 */
@Composable
fun FeedCard(
    feed: Feed,
    onClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
    ) {
        // 作者行
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onAuthorClick() }) {
            AsyncImage(
                model = feed.author?.avatar,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(36.dp).clip(CircleShape),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(feed.author?.nickname ?: "同袍", style = MaterialTheme.typography.titleSmall)
                if (feed.city.isNotBlank()) {
                    Text(feed.city, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        // 文案
        Text(feed.content, style = MaterialTheme.typography.bodyMedium, maxLines = 4)
        // 首图
        feed.media.firstOrNull()?.let { img ->
            Spacer(Modifier.height(8.dp))
            AsyncImage(
                model = img,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(8.dp)),
            )
        }
        // 标签
        if (feed.tags.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                feed.tags.joinToString(" ") { "#$it" },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(8.dp))
        // 互动行
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text = "${if (feed.isLiked) "♥" else "♡"} ${feed.likeCount}",
                color = if (feed.isLiked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable { onLike() },
            )
            Text("💬 ${feed.commentCount}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { onClick() })
            Text(
                text = "${if (feed.isFavorited) "★" else "☆"} ${feed.favoriteCount}",
                color = if (feed.isFavorited) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable { onFavorite() },
            )
            Text("↗ ${feed.shareCount}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
