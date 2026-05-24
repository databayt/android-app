package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors
import org.hogwarts.android.feature.messaging.domain.model.MessageStatus

@Composable
fun MessageStatusIcon(
    status: MessageStatus,
    modifier: Modifier = Modifier,
) {
    val waColors = LocalWhatsAppColors.current
    val iconSize = 16.dp

    when (status) {
        MessageStatus.SENDING -> {
            val transition = rememberInfiniteTransition(label = "sending")
            val alpha by transition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                label = "pulse",
            )
            Icon(
                Icons.Filled.AccessTime,
                contentDescription = "Sending",
                modifier = modifier.size(iconSize).alpha(alpha),
                tint = waColors.textSecondary,
            )
        }
        MessageStatus.SENT -> Icon(
            Icons.Filled.Check,
            contentDescription = "Sent",
            modifier = modifier.size(iconSize),
            tint = waColors.textSecondary,
        )
        MessageStatus.DELIVERED -> Icon(
            Icons.Filled.DoneAll,
            contentDescription = "Delivered",
            modifier = modifier.size(iconSize),
            tint = waColors.textSecondary,
        )
        MessageStatus.READ -> Icon(
            Icons.Filled.DoneAll,
            contentDescription = "Read",
            modifier = modifier.size(iconSize),
            tint = waColors.surfaceProduct,
        )
        MessageStatus.FAILED -> Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = "Failed",
            modifier = modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.error,
        )
    }
}
