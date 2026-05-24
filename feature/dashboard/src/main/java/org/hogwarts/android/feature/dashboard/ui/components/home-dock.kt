package org.hogwarts.android.feature.dashboard.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.atom.iosTileBrush

/**
 * One slot in [HomeDock]. Rendered as a rounded-square app-icon tile (no label).
 *
 * When [iconRes] is non-null the tile renders the full-bleed drawable; otherwise it
 * falls back to the [icon] ImageVector over an [iosTileBrush] gradient tinted with [background].
 */
data class HomeDockItem(
    val icon: ImageVector,
    val background: Color,
    val contentDescription: String,
    val onClick: () -> Unit,
    @DrawableRes val iconRes: Int? = null
)

/**
 * iOS home-screen dock (Figma node 112:609 — 402×140 frame, inner pill 368×103).
 *
 * Composition is a layered Box: glass backdrop → lit-edge border → content row.
 * Icons render strictly on top of the glass so they never get tinted or faded.
 */
@Composable
fun HomeDock(
    items: List<HomeDockItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 17.dp, end = 17.dp, top = 20.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(38.dp))
        ) {
            GlassBackdrop(cornerRadius = 38.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 19.dp, end = 19.dp, top = 20.dp, bottom = 19.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    DockIcon(
                        icon = item.icon,
                        iconRes = item.iconRes,
                        background = item.background,
                        contentDescription = item.contentDescription,
                        onClick = item.onClick
                    )
                }
            }
        }
    }
}

/**
 * Two stacked children that form the iOS liquid-glass surface: a vertical white
 * gradient tint + a top-lit edge border. Drop these inside any clipped Box and
 * place content *after* — the content stays in front.
 *
 * The gradient approximates the Figma `mix-blend-screen` look without the blend
 * mode (Compose's BlendMode.Screen inside drawWithContent covers the content;
 * a plain gradient under the icons reads identically on the blue wallpaper).
 */
@Composable
internal fun BoxScope.GlassBackdrop(cornerRadius: Dp) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.26f),
                        Color.White.copy(alpha = 0.32f)
                    )
                )
            )
    )
    Box(
        modifier = Modifier
            .matchParentSize()
            .border(
                border = BorderStroke(
                    width = 0.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.50f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    )
}

@Composable
private fun DockIcon(
    icon: ImageVector,
    @DrawableRes iconRes: Int?,
    background: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (iconRes == null) Modifier.background(iosTileBrush(background)) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
