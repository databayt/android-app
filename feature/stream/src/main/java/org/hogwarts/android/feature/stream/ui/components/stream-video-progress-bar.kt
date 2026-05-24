package org.hogwarts.android.feature.stream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * Mirrors `components/stream/shared/video-player/video-progress-bar.tsx`:
 *   - 5dp rounded-full track at white/30, identical at rest and during drag
 *     (the web constants set heightRest and heightHover to the same value).
 *   - Solid white fill for watched time.
 *   - 18x14 white pill thumb with a subtle drop shadow — always visible.
 * Tap or horizontal drag seeks; the single `onSeek(fraction)` is enough
 * because Android doesn't need the web's scrub thumbnail hooks.
 */
@Composable
fun StreamVideoProgressBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 5.dp,
    thumbWidth: Dp = 18.dp,
    thumbHeight: Dp = 14.dp
) {
    val density = LocalDensity.current
    val thumbWidthPx = with(density) { thumbWidth.toPx() }
    val clamped = progress.coerceIn(0f, 1f)

    var widthPx by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbHeight + 8.dp)
            .padding(horizontal = 4.dp)
            .onSizeChanged { widthPx = it.width }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val w = size.width.toFloat().coerceAtLeast(1f)
                    onSeek((offset.x / w).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        onSeek((offset.x / w).coerceIn(0f, 1f))
                    }
                ) { change, _ ->
                    change.consume()
                    val w = size.width.toFloat().coerceAtLeast(1f)
                    onSeek((change.position.x / w).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track (background)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color.White.copy(alpha = 0.30f))
        ) {
            // Active fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = clamped)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color.White)
            )
        }

        // Pill thumb — x-offset matches the fraction of the track (minus the
        // thumb's own width so it never clips past the end).
        val maxX = (widthPx - thumbWidthPx).coerceAtLeast(0f)
        val offsetX = (clamped * maxX).toInt()
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX, 0) }
                .size(width = thumbWidth, height = thumbHeight)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(percent = 50),
                    clip = false
                )
                .clip(RoundedCornerShape(percent = 50))
                .background(Color.White)
        )
    }
}
