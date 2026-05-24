package org.hogwarts.android.core.designsystem.atom

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Data class for child items in the selector.
 */
data class ChildSelectorItem(
    val id: String,
    val name: String,
    val avatarUrl: String? = null
)

/**
 * Horizontal scrollable child selector using filter chips.
 * Used across all guardian screens to switch between children.
 */
@Composable
fun ChildSelector(
    children: List<ChildSelectorItem>,
    selectedChildId: String?,
    onChildSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(children, key = { it.id }) { child ->
            val isSelected = child.id == selectedChildId
            FilterChip(
                selected = isSelected,
                onClick = { onChildSelected(child.id) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            name = child.name,
                            imageUrl = child.avatarUrl,
                            size = 24.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = child.name,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Preview(name = "1 Child")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun ChildSelectorOneChildPreview() {
    HogwartsTheme {
        Surface {
            ChildSelector(
                children = listOf(
                    ChildSelectorItem("1", "Ahmed Al-Rashid")
                ),
                selectedChildId = "1",
                onChildSelected = {}
            )
        }
    }
}

@Preview(name = "2 Children")
@Composable
private fun ChildSelectorTwoChildrenPreview() {
    HogwartsTheme {
        Surface {
            ChildSelector(
                children = listOf(
                    ChildSelectorItem("1", "Ahmed"),
                    ChildSelectorItem("2", "Fatima")
                ),
                selectedChildId = "1",
                onChildSelected = {}
            )
        }
    }
}

@Preview(name = "3+ Children")
@Composable
private fun ChildSelectorManyChildrenPreview() {
    HogwartsTheme {
        Surface {
            ChildSelector(
                children = listOf(
                    ChildSelectorItem("1", "Ahmed"),
                    ChildSelectorItem("2", "Fatima"),
                    ChildSelectorItem("3", "Omar"),
                    ChildSelectorItem("4", "Sara")
                ),
                selectedChildId = "2",
                onChildSelected = {}
            )
        }
    }
}
