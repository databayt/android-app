package org.hogwarts.android.core.designsystem.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class SheetDetent {
    PEEK, MEDIUM, LARGE
}

@Composable
fun DetentBottomSheet(
    initialDetent: SheetDetent = SheetDetent.PEEK,
    peekHeight: Dp = 80.dp,
    onDetentChange: (SheetDetent) -> Unit = {},
    onDismiss: (() -> Unit)? = null,
    content: @Composable (SheetDetent) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val mediumHeight = screenHeight * 0.5f
    val largeHeight = screenHeight * 0.9f

    var currentDetent by remember { mutableStateOf(initialDetent) }
    val targetHeight = when (currentDetent) {
        SheetDetent.PEEK -> peekHeight
        SheetDetent.MEDIUM -> mediumHeight
        SheetDetent.LARGE -> largeHeight
    }

    val springSpec = spring<Dp>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = springSpec,
        label = "sheetHeight"
    )

    var dragOffset by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        val threshold = 100f
                        val newDetent = when {
                            dragOffset < -threshold -> when (currentDetent) {
                                SheetDetent.PEEK -> SheetDetent.MEDIUM
                                SheetDetent.MEDIUM -> SheetDetent.LARGE
                                SheetDetent.LARGE -> SheetDetent.LARGE
                            }
                            dragOffset > threshold -> when (currentDetent) {
                                SheetDetent.LARGE -> SheetDetent.MEDIUM
                                SheetDetent.MEDIUM -> SheetDetent.PEEK
                                SheetDetent.PEEK -> {
                                    onDismiss?.invoke()
                                    SheetDetent.PEEK
                                }
                            }
                            else -> currentDetent
                        }
                        currentDetent = newDetent
                        onDetentChange(newDetent)
                        dragOffset = 0f
                    },
                    onVerticalDrag = { _, amount ->
                        dragOffset += amount
                    }
                )
            },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag indicator
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            ) {}
            Spacer(modifier = Modifier.height(8.dp))

            content(currentDetent)
        }
    }
}
