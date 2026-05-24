package org.hogwarts.android.core.designsystem.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = 16.dp,
    cornerRadius: Dp = 4.dp
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

@Composable
fun SkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(brush)
    )
}

@Composable
fun SkeletonListItem(
    modifier: Modifier = Modifier,
    showAvatar: Boolean = true,
    lines: Int = 2
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showAvatar) {
            SkeletonCircle(size = 48.dp)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonBox(height = 16.dp, width = 160.dp)
            if (lines >= 2) {
                SkeletonBox(height = 12.dp)
            }
            if (lines >= 3) {
                SkeletonBox(height = 12.dp, width = 120.dp)
            }
        }
    }
}

@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    showImage: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showImage) {
            SkeletonBox(height = 160.dp, cornerRadius = 8.dp)
        }
        SkeletonBox(height = 20.dp, width = 200.dp)
        SkeletonBox(height = 14.dp)
        SkeletonBox(height = 14.dp, width = 160.dp)
    }
}

@Composable
fun SkeletonScreen(
    modifier: Modifier = Modifier,
    itemCount: Int = 5,
    showHeader: Boolean = true,
    showAvatars: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (showHeader) {
            SkeletonBox(
                modifier = Modifier.padding(horizontal = 16.dp),
                height = 24.dp,
                width = 180.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonBox(
                modifier = Modifier.padding(horizontal = 16.dp),
                height = 14.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        repeat(itemCount) {
            SkeletonListItem(showAvatar = showAvatars)
        }
    }
}
