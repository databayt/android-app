package org.hogwarts.android.core.designsystem.atom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.theme.AppleRed
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * [AppIcon] with an iOS-style red numeric badge anchored to the top-end of the tile.
 * Badge is suppressed when [count] <= 0 so callers can pass unread counts directly.
 *
 * Anchor auto-mirrors to top-start in RTL via Material3's [BadgedBox].
 */
@Composable
fun BadgedAppIcon(
    label: String,
    iconUrl: String,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    @DrawableRes iconRes: Int? = null,
    fallbackIcon: ImageVector? = null,
    fallbackTint: Color = Color.White,
    fallbackBackground: Color = MaterialTheme.colorScheme.primary,
    fallbackBrush: Brush? = null,
    labelColor: Color = Color.White
) {
    BadgedBox(
        modifier = modifier,
        badge = {
            if (count > 0) {
                Badge(
                    modifier = Modifier
                        .offset(x = 2.dp, y = (-4).dp)
                        .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp),
                    containerColor = AppleRed,
                    contentColor = Color.White
                ) {
                    Text(
                        text = if (count > 99) "99+" else count.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.sp
                        )
                    )
                }
            }
        }
    ) {
        AppIcon(
            label = label,
            iconUrl = iconUrl,
            onClick = onClick,
            iconRes = iconRes,
            fallbackIcon = fallbackIcon,
            fallbackTint = fallbackTint,
            fallbackBackground = fallbackBackground,
            fallbackBrush = fallbackBrush,
            labelColor = labelColor
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
private fun BadgedAppIconPreview() {
    HogwartsTheme {
        BadgedAppIcon(
            label = "Messages",
            iconUrl = "",
            count = 3,
            fallbackBackground = Color(0xFF34C759)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
private fun BadgedAppIconLargeCountPreview() {
    HogwartsTheme {
        BadgedAppIcon(
            label = "Notifications",
            iconUrl = "",
            count = 128,
            fallbackBackground = Color(0xFFFF3B30)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
private fun BadgedAppIconZeroPreview() {
    HogwartsTheme {
        BadgedAppIcon(
            label = "Settings",
            iconUrl = "",
            count = 0,
            fallbackBackground = Color(0xFF8E8E93)
        )
    }
}
