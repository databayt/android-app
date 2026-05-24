package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors

@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
) {
    val waColors = LocalWhatsAppColors.current
    val transition = rememberInfiniteTransition(label = "typing")

    Box(
        modifier = modifier
            .background(waColors.surfaceSearchChat, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                val offset by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = -6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 400, delayMillis = index * 150),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "dot_$index",
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .offset(y = offset.dp)
                        .background(waColors.textSecondary, CircleShape),
                )
            }
        }
    }
}
