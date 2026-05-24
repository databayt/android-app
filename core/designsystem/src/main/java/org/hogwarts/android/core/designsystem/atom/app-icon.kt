package org.hogwarts.android.core.designsystem.atom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Rounded square app icon (64dp, 14dp corner radius) with label underneath.
 * Gap: 5dp. Label: 12sp Medium/510, center-aligned.
 *
 * Source: Figma iOS 26 — App Icon/iPhone (node 1:3538, 64×83).
 *
 * Pass [fallbackBrush] to render an iOS-26-style gradient tile when no [iconUrl]
 * is available; otherwise [fallbackBackground] is used as a solid fill.
 */
@Composable
fun AppIcon(
    label: String,
    iconUrl: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    @DrawableRes iconRes: Int? = null,
    fallbackIcon: ImageVector? = null,
    fallbackTint: Color = Color.White,
    fallbackBackground: Color = MaterialTheme.colorScheme.primary,
    fallbackBrush: Brush? = null,
    labelColor: Color = Color.White
) {
    Column(
        modifier = modifier
            .widthIn(max = 76.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(14.dp),
                        clip = false
                    )
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        } else if (iconUrl.isNotBlank()) {
            AsyncImage(
                model = iconUrl,
                contentDescription = label,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            val tileModifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(14.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(14.dp))
                .then(
                    if (fallbackBrush != null) {
                        Modifier.background(fallbackBrush)
                    } else {
                        Modifier.background(fallbackBackground)
                    }
                )
            Box(
                modifier = tileModifier,
                contentAlignment = Alignment.Center
            ) {
                if (fallbackIcon != null) {
                    Icon(
                        imageVector = fallbackIcon,
                        contentDescription = label,
                        tint = fallbackTint,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * iOS-26-style vertical gradient tile brush: slightly lifted top, saturated bottom.
 * Use as [AppIcon.fallbackBrush] for the glossy home-screen look.
 */
fun iosTileBrush(tint: Color): Brush = Brush.verticalGradient(
    colors = listOf(
        lerp(tint, Color.White, 0.22f),
        tint,
        lerp(tint, Color.Black, 0.08f)
    )
)

data class AppIconItem(
    val label: String,
    val iconUrl: String,
    val onClick: (() -> Unit)? = null,
    val fallbackIcon: ImageVector? = null,
    val fallbackBackground: Color? = null,
    val fallbackBrush: Brush? = null
)

/**
 * Horizontal scrollable row of app icons.
 */
@Composable
fun AppIconRow(
    items: List<AppIconItem>,
    modifier: Modifier = Modifier,
    labelColor: Color = Color.White
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(items) { item ->
            AppIcon(
                label = item.label,
                iconUrl = item.iconUrl,
                onClick = item.onClick,
                fallbackIcon = item.fallbackIcon,
                fallbackBackground = item.fallbackBackground ?: MaterialTheme.colorScheme.primary,
                fallbackBrush = item.fallbackBrush,
                labelColor = labelColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppIconPreview() {
    HogwartsTheme {
        Surface {
            AppIcon(
                label = "App Name",
                iconUrl = ""
            )
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun AppIconRowRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AppIconRow(
                items = listOf(
                    AppIconItem(label = "إيردروب", iconUrl = ""),
                    AppIconItem(label = "الرسائل", iconUrl = ""),
                    AppIconItem(label = "البريد", iconUrl = "")
                )
            )
        }
    }
}
