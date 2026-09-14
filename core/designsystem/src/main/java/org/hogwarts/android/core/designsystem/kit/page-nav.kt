package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

@Immutable
data class PageNavItem(val key: String, val label: String, val badge: Int = 0)

/**
 * A section's sub-pages as underlined text tabs scrolling sideways — mirrors
 * `atom/page-nav.tsx`: 24dp apart, active label semibold primary with a 2dp
 * underline, inactive medium muted, bottom hairline across the row.
 */
@Composable
fun PageNav(
    items: List<PageNavItem>,
    selectedKey: String,
    onSelect: (PageNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            items.forEach { item ->
                val active = item.key == selectedKey
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .semantics { selected = active }
                        .clickable(role = Role.Tab) { onSelect(item) },
                ) {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            item.label,
                            style = type.body.copy(fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium),
                            color = if (active) colors.primary else colors.mutedForeground,
                            maxLines = 1,
                        )
                        if (item.badge > 0) {
                            CountBadge(item.badge, Modifier.padding(start = 6.dp))
                        }
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (active) colors.primary else colors.primary.copy(alpha = 0f)),
                    )
                }
            }
        }
        Hairline(colors.border)
    }
}
