package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors

@Composable
fun OnlineIndicator(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
) {
    if (!isOnline) return
    Box(
        modifier = modifier
            .size(size)
            .border(2.dp, Color.White, CircleShape)
            .background(LocalWhatsAppColors.current.surfaceProduct, CircleShape),
    )
}
