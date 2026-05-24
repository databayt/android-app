package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Icon + text list row (52dp height). Icon occupies a 44dp touch target area.
 * Separator at top (indented past icon area). Padding: pl=16, pr=11.
 *
 * Source: Figma iOS 26 — Activity View Action List (node 15:2541)
 */
@Composable
fun ActionListItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 60.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable(onClick = onClick)
                .padding(start = AppleSpacing.Standard, end = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    letterSpacing = (-0.43).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

data class ActionListData(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Grouped list of action items in a rounded container (26dp corners).
 * Background: fills/secondary — semi-transparent gray overlay.
 *
 * Source: Figma iOS 26 — Activity View Action Group (node 15:2540)
 */
@Composable
fun ActionGroup(
    actions: List<ActionListData>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color(0x29787880)
    ) {
        Column {
            actions.forEachIndexed { index, action ->
                ActionListItem(
                    label = action.label,
                    icon = action.icon,
                    onClick = action.onClick,
                    showDivider = index > 0
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionGroupPreview() {
    HogwartsTheme {
        Surface {
            Column(
                modifier = Modifier.padding(AppleSpacing.Standard),
                verticalArrangement = Arrangement.spacedBy(AppleSpacing.Comfortable)
            ) {
                ActionGroup(
                    actions = listOf(
                        ActionListData("Add to Reading List", Icons.Outlined.BookmarkBorder) {},
                        ActionListData("Add Bookmark", Icons.Outlined.BookmarkBorder) {},
                        ActionListData("Add to Favorites", Icons.Outlined.FavoriteBorder) {},
                        ActionListData("Add to Home Screen", Icons.Outlined.FavoriteBorder) {}
                    )
                )
                ActionGroup(
                    actions = listOf(
                        ActionListData("Markup", Icons.Outlined.FavoriteBorder) {},
                        ActionListData("Print", Icons.Outlined.Print) {}
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun ActionGroupRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(modifier = Modifier.padding(AppleSpacing.Standard)) {
                ActionGroup(
                    actions = listOf(
                        ActionListData("إضافة للمفضلة", Icons.Outlined.FavoriteBorder) {},
                        ActionListData("إضافة إشارة مرجعية", Icons.Outlined.BookmarkBorder) {}
                    )
                )
            }
        }
    }
}
