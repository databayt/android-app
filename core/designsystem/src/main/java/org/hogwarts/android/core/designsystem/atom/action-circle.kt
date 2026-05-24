package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Circular action button (70dp) with icon centered and label underneath (max 2 lines, 30dp).
 * Circle background: fills/vibrant/tertiary. Gap: 7dp.
 *
 * Source: Figma iOS 26 — Activity View Action Buttons (node 15:2534)
 */
@Composable
fun ActionCircle(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(78.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(70.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(25.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 13.sp,
                lineHeight = 15.sp,
                letterSpacing = (-0.1).sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(78.dp)
                .height(30.dp)
        )
    }
}

data class ActionCircleItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Horizontal scrollable row of circular action buttons.
 * Padding: 24dp start. Gap: 14dp.
 *
 * Source: Figma iOS 26 — Activity View Action Buttons (node 15:2534)
 */
@Composable
fun ActionCircleRow(
    actions: List<ActionCircleItem>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = AppleSpacing.Large),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(actions) { action ->
            ActionCircle(
                label = action.label,
                icon = action.icon,
                onClick = action.onClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionCirclePreview() {
    HogwartsTheme {
        Surface {
            ActionCircle(
                label = "Copy",
                icon = Icons.Outlined.ContentCopy,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionCircleRowPreview() {
    HogwartsTheme {
        ActionCircleRow(
            actions = listOf(
                ActionCircleItem("Copy", Icons.Outlined.ContentCopy) {},
                ActionCircleItem("Add to Favorites", Icons.Outlined.FavoriteBorder) {}
            )
        )
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun ActionCircleRowRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ActionCircleRow(
                actions = listOf(
                    ActionCircleItem("نسخ", Icons.Outlined.ContentCopy) {},
                    ActionCircleItem("إضافة للمفضلة", Icons.Outlined.FavoriteBorder) {}
                )
            )
        }
    }
}
