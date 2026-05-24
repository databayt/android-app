package org.hogwarts.android.feature.students.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.feature.students.R
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.feature.students.domain.model.Student
import org.hogwarts.android.feature.students.domain.model.StudentStatus

@Composable
fun StudentCard(
    student: Student,
    showDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppleListRow(
        showDivider = showDivider,
        onClick = onClick,
        leadingContent = {
            UserAvatar(
                name = student.fullName,
                imageUrl = student.avatarUrl
            )
        },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val subtitle = buildList {
                    student.className?.let { add(it) }
                    student.section?.let { add(stringResource(R.string.students_card_section_prefix, it)) }
                    student.enrollmentNumber?.let { add(it) }
                }.joinToString(" • ")
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (student.status != StudentStatus.ACTIVE) {
                StatusBadge(
                    text = student.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = when (student.status) {
                        StudentStatus.INACTIVE -> MaterialTheme.colorScheme.outline
                        StudentStatus.SUSPENDED -> MaterialTheme.colorScheme.error
                        StudentStatus.GRADUATED -> MaterialTheme.colorScheme.tertiary
                        StudentStatus.TRANSFERRED -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.outline
                    }
                )
            }
        }
    }
}
