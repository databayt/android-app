package org.hogwarts.android.feature.grades.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.grades.R
import org.hogwarts.android.feature.grades.domain.model.AssessmentType
import org.hogwarts.android.feature.grades.domain.model.GradeRecord
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * A single grade record row in the grades list.
 */
@Composable
fun GradeRow(
    record: GradeRecord,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    AppleListRow(
        showDivider = showDivider,
        trailingContent = {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    text = stringResource(R.string.grades_score_format, record.score.toInt(), record.maxScore.toInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = percentageColor(record.percentage)
                )
                Text(
                    text = stringResource(R.string.grades_percentage_format, record.percentage.toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
        ) {
            Text(
                text = record.assessmentName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = record.subjectName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.grades_type_date_format,
                    assessmentTypeLabel(record.assessmentType),
                    record.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun assessmentTypeLabel(type: AssessmentType): String = when (type) {
    AssessmentType.EXAM -> stringResource(R.string.grades_type_exam)
    AssessmentType.QUIZ -> stringResource(R.string.grades_type_quiz)
    AssessmentType.ASSIGNMENT -> stringResource(R.string.grades_type_assignment)
    AssessmentType.PROJECT -> stringResource(R.string.grades_type_project)
    AssessmentType.MIDTERM -> stringResource(R.string.grades_type_midterm)
    AssessmentType.FINAL -> stringResource(R.string.grades_type_final)
}

@Composable
private fun percentageColor(percentage: Float): Color = when {
    percentage >= 90 -> MaterialTheme.colorScheme.tertiary
    percentage >= 75 -> MaterialTheme.colorScheme.primary
    percentage >= 60 -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.error
}
