package org.hogwarts.android.core.designsystem.apple

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Apple-style Inset Grouped List mirroring iOS `.listStyle(.insetGrouped)`.
 *
 * Groups items into rounded sections with inset padding, matching Apple HIG.
 */
@Composable
fun AppleInsetGroupedList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = AppleSpacing.Standard,
        vertical = AppleSpacing.Compact
    ),
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Standard),
        content = content
    )
}

/**
 * A section within an [AppleInsetGroupedList].
 *
 * Provides a rounded container with optional header and footer text,
 * matching iOS inset grouped list section style.
 */
@Composable
fun AppleListSection(
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        if (header != null) {
            Text(
                text = header.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = AppleSpacing.Standard,
                    bottom = AppleSpacing.Tiny
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppleShape.ListRow)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column {
                content()
            }
        }

        if (footer != null) {
            Text(
                text = footer,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = AppleSpacing.Standard,
                    top = AppleSpacing.Tiny
                )
            )
        }
    }
}

/**
 * A single row within an [AppleListSection].
 *
 * Provides Apple-style row with leading/trailing content and optional divider.
 */
@Composable
fun AppleListRow(
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val rowModifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(
                horizontal = AppleSpacing.Standard,
                vertical = AppleSpacing.Small
            )

        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Small)
        ) {
            if (leadingContent != null) {
                leadingContent()
            }

            Box(modifier = Modifier.weight(1f)) {
                content()
            }

            if (trailingContent != null) {
                trailingContent()
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = AppleSpacing.Standard),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
        }
    }
}
