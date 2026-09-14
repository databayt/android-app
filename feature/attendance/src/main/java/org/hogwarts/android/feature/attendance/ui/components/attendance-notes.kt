package org.hogwarts.android.feature.attendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.attendance.R

/** A placeholder while a block loads — `ui/skeleton.tsx`: a muted rounded block. */
@Composable
internal fun SkeletonBlock(modifier: Modifier = Modifier) {
    Box(modifier.clip(HogwartsShapes.Lg).background(HogwartsTheme.colors.muted))
}

/** "Unable to load attendance data" with a retry — the overview's error card. */
@Composable
internal fun LoadFailedNote(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .background(colors.destructive.copy(alpha = 0.06f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = colors.destructive, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.attendance_unable_to_load), style = type.bodyMedium, color = colors.destructive)
        }
        PillButton(stringResource(R.string.attendance_retry), onClick = onRetry, variant = PillVariant.Outline, modifier = Modifier.padding(start = 4.dp))
    }
}
