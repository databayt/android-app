package org.hogwarts.android.core.designsystem.component

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 20.dp,
    opacity: Float = 0.15f,
    cornerRadius: Dp = 16.dp,
    animated: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    val animatedOpacity = if (animated) {
        val infiniteTransition = rememberInfiniteTransition(label = "glass")
        infiniteTransition.animateFloat(
            initialValue = opacity * 0.8f,
            targetValue = opacity * 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glassOpacity"
        ).value
    } else opacity

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.graphicsLayer {
                        renderEffect = android.graphics.RenderEffect
                            .createBlurEffect(
                                blurRadius.toPx(),
                                blurRadius.toPx(),
                                android.graphics.Shader.TileMode.CLAMP
                            )
                            .asComposeRenderEffect()
                    }
                } else Modifier
            )
    ) {
        // Glass background
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            surfaceColor.copy(alpha = animatedOpacity),
                            surfaceColor.copy(alpha = animatedOpacity * 0.7f)
                        )
                    )
                )
        )

        // Border highlight
        Surface(
            modifier = Modifier.matchParentSize(),
            shape = shape,
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                color = borderColor.copy(alpha = 0.3f)
            )
        ) {}

        // Content
        Box(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
