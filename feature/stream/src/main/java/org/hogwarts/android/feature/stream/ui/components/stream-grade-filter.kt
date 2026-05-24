package org.hogwarts.android.feature.stream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Web parity — grades 1..12, same list as stream/courses/content.tsx. */
private val STREAM_GRADES = (1..12).toList()

@Composable
fun StreamGradeFilter(
    activeGrade: Int?,
    onGradeSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = STREAM_GRADES, key = { it }) { grade ->
            GradePill(
                label = grade.toString(),
                isActive = activeGrade == grade,
                // Tapping an already-active grade clears the filter (matches intuition).
                onClick = { onGradeSelect(if (activeGrade == grade) null else grade) }
            )
        }
    }
}

@Composable
private fun GradePill(label: String, isActive: Boolean, onClick: () -> Unit) {
    val bg = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = fg
        )
    }
}
