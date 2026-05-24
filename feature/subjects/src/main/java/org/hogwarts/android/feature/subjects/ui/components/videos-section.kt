package org.hogwarts.android.feature.subjects.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.hogwarts.android.feature.subjects.domain.model.VideoItem

/**
 * Horizontal row of video cards. Mirrors the videos section in
 * catalog-content-sections.tsx (lines 303–365).
 */
@Composable
fun VideosSection(
    videos: List<VideoItem>,
    accentColor: Color,
    viewsLabel: String,
    modifier: Modifier = Modifier,
    onVideoClick: ((VideoItem) -> Unit)? = null,
) {
    if (videos.isEmpty()) return
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
    ) {
        items(items = videos, key = { it.id }) { video ->
            VideoCard(
                video = video,
                accentColor = accentColor,
                viewsLabel = viewsLabel,
                onClick = onVideoClick?.let { { it(video) } },
            )
        }
    }
}

@Composable
private fun VideoCard(
    video: VideoItem,
    accentColor: Color,
    viewsLabel: String,
    onClick: (() -> Unit)?,
) {
    val shape = RoundedCornerShape(12.dp)
    val fallback = parseColor(video.color) ?: accentColor
    var boxModifier = Modifier
        .width(240.dp)
        .aspectRatio(3f / 2f)
        .clip(shape)
        .background(fallback)
    if (onClick != null) {
        boxModifier = boxModifier.clickable(onClick = onClick)
    }
    Box(modifier = boxModifier) {
        if (!video.thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // Title centered over image
        Text(
            text = video.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 12.dp),
        )
        // Bottom bar with play + duration + views
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.55f),
                        ),
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = formatDuration(video.durationSeconds),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f),
            )
            Text(
                text = "·",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f),
            )
            Text(
                text = "${video.viewCount} $viewsLabel",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

/**
 * Formats seconds as `m:ss` (or `m:00` when seconds are zero).
 * Mirrors formatDuration in catalog-content-sections.tsx (lines 110–114).
 */
private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (s > 0) "%d:%02d".format(m, s) else "$m:00"
}
