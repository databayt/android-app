package org.hogwarts.android.core.designsystem.atom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Dim overlay for modals and sheets — Figma node 16:3081 (402×640).
 *
 * Fill: `overlays/activity-view-controller` = rgba(0,0,0,0.2).
 * Figma: no blur, no shape, full-size.
 */
@Composable
fun HogwartsOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f)) // Figma: rgba(0,0,0,0.2)
                .then(
                    if (onDismiss != null) {
                        Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onDismiss
                        )
                    } else {
                        Modifier
                    }
                )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HogwartsOverlayPreview() {
    HogwartsTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Background Content",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "This content is behind the overlay.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            HogwartsOverlay(isVisible = true, onDismiss = {})
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun HogwartsOverlayRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                ) {
                    Text(text = "محتوى الخلفية", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(text = "هذا المحتوى خلف الطبقة المعتمة.", style = MaterialTheme.typography.bodyMedium)
                }
                HogwartsOverlay(isVisible = true, onDismiss = {})
            }
        }
    }
}
