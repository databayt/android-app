package org.hogwarts.android.feature.subjects.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.domain.model.ExamItem

/**
 * Fixed order of exam-type tiles, matching EXAM_TYPE_PIPELINE in
 * catalog-content-sections.tsx lines 116–123.
 */
private val EXAM_TYPE_PIPELINE = listOf(
    "final",
    "midterm",
    "chapter_test",
    "quiz",
    "practice",
    "diagnostic",
)

private val TEST_TYPES = setOf("chapter_test", "quiz", "practice")

private data class ExamTypeGroup(
    val key: String,
    val count: Int,
    val avgDuration: Int?,
    val avgQuestions: Int?,
    val avgMarks: Int?,
)

@Composable
fun ExamsSection(
    exams: List<ExamItem>,
    modifier: Modifier = Modifier,
) {
    val groups = EXAM_TYPE_PIPELINE.map { key ->
        val items = exams.filter { it.examType.equals(key, ignoreCase = true) }
        ExamTypeGroup(
            key = key,
            count = items.size,
            avgDuration = items.mapNotNull { it.durationMinutes }.averageIntOrNull(),
            avgQuestions = items.mapNotNull { it.totalQuestions }.averageIntOrNull(),
            avgMarks = items.mapNotNull { it.totalMarks }.averageIntOrNull(),
        )
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
    ) {
        items(items = groups, key = { it.key }) { group ->
            ExamTypeTile(group = group)
        }
    }
}

@Composable
private fun ExamTypeTile(group: ExamTypeGroup) {
    Surface(
        modifier = Modifier
            .width(170.dp)
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(examTypeLabelRes(group.key)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(
                    if (TEST_TYPES.contains(group.key)) R.string.subjects_exam_tests_count
                    else R.string.subjects_exam_exams_count,
                    group.count,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (group.avgDuration != null) {
                MetricRow(
                    icon = Icons.Outlined.AccessTime,
                    text = stringResource(R.string.subjects_exam_min, group.avgDuration),
                )
            }
            if (group.avgQuestions != null) {
                MetricRow(
                    icon = Icons.Outlined.Quiz,
                    text = stringResource(R.string.subjects_exam_questions_short, group.avgQuestions),
                )
            }
            if (group.avgMarks != null) {
                MetricRow(
                    icon = Icons.Filled.CheckCircle,
                    text = stringResource(R.string.subjects_exam_marks, group.avgMarks),
                )
            }
        }
    }
}

@Composable
private fun MetricRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun examTypeLabelRes(key: String): Int = when (key) {
    "final" -> R.string.subjects_exam_type_final
    "midterm" -> R.string.subjects_exam_type_midterm
    "chapter_test" -> R.string.subjects_exam_type_chapter_test
    "quiz" -> R.string.subjects_exam_type_quiz
    "practice" -> R.string.subjects_exam_type_practice
    "diagnostic" -> R.string.subjects_exam_type_diagnostic
    else -> R.string.subjects_exam_type_final
}

private fun List<Int>.averageIntOrNull(): Int? =
    if (isEmpty()) null else (sum().toDouble() / size).toInt()
