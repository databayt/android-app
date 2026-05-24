package org.hogwarts.android.core.designsystem.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

data class ContextMenuItem(
    val label: String,
    val icon: ImageVector? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun ContextMenuHost(
    items: List<ContextMenuItem>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showMenu = true
                        }
                    )
                }
        ) {
            content()
        }

        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn(animationSpec = tween(150)) +
                    scaleIn(initialScale = 0.9f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = fadeOut(animationSpec = tween(100)) +
                   scaleOut(targetScale = 0.9f, animationSpec = tween(100))
        ) {
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { showMenu = false }
            )

            // Menu
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(min = 200.dp, max = 280.dp)
                    .clip(RoundedCornerShape(14.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 16.dp
            ) {
                Column {
                    items.forEachIndexed { index, item ->
                        ContextMenuItemRow(
                            item = item,
                            onClick = {
                                showMenu = false
                                item.onClick()
                            }
                        )
                        if (index < items.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItemRow(
    item: ContextMenuItem,
    onClick: () -> Unit
) {
    val textColor = if (item.isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
        item.icon?.let { icon ->
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
