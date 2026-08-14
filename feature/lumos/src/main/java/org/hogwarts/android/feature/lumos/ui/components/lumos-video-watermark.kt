package org.hogwarts.android.feature.lumos.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dynamic DRM watermark overlay displaying user identifier with subtle opacity.
 * Discourages screen recording and unauthorized distribution of proprietary lessons.
 */
@Composable
fun LumosVideoWatermark(
    userIdentifier: String?,
    modifier: Modifier = Modifier
) {
    if (userIdentifier.isNullOrBlank()) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Text(
            text = userIdentifier,
            color = Color.White.copy(alpha = 0.18f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
