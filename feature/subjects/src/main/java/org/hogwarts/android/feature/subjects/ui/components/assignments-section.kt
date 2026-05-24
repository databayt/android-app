package org.hogwarts.android.feature.subjects.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.domain.model.AssignmentItem

/**
 * Horizontal row of assignment cards. Mirrors the assignments section in
 * catalog-content-sections.tsx lines 452–494. Empty state = "0 assignments".
 */
@Composable
fun AssignmentsSection(
    assignments: List<AssignmentItem>,
    accentColor: Color,
    assignmentsEmptyLabel: String,
    modifier: Modifier = Modifier,
) {
    if (assignments.isEmpty()) {
        Text(
            text = assignmentsEmptyLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
    ) {
        items(items = assignments, key = { it.id }) { assignment ->
            AssignmentCard(assignment = assignment, accentColor = accentColor)
        }
    }
}

@Composable
private fun AssignmentCard(
    assignment: AssignmentItem,
    accentColor: Color,
) {
    Surface(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (!assignment.assignmentType.isNullOrBlank()) {
                AssignmentTypeBadge(
                    type = assignment.assignmentType,
                    accentColor = accentColor,
                )
            }
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (assignment.estimatedTime != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = stringResource(
                                R.string.subjects_assignment_minutes,
                                assignment.estimatedTime,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (assignment.totalPoints != null) {
                    Text(
                        text = stringResource(
                            R.string.subjects_assignment_points,
                            assignment.totalPoints.toInt(),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignmentTypeBadge(
    type: String,
    accentColor: Color,
) {
    val labelRes = when (type.lowercase()) {
        "homework" -> R.string.subjects_assignment_type_homework
        "project" -> R.string.subjects_assignment_type_project
        "lab" -> R.string.subjects_assignment_type_lab
        "essay" -> R.string.subjects_assignment_type_essay
        "presentation" -> R.string.subjects_assignment_type_presentation
        else -> null
    }
    val label = if (labelRes != null) stringResource(labelRes) else type
    Surface(
        color = accentColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(percent = 50),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = accentColor,
            modifier = Modifier
                .background(Color.Transparent)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}
