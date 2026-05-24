package org.hogwarts.android.feature.subjects.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.domain.model.SubjectLevel

/**
 * Localized label for a level. Matches `levelLabel()` in
 * components/school-dashboard/listings/subjects/catalog-subjects-grid.tsx.
 */
@Composable
fun levelLabel(level: SubjectLevel): String = stringResource(
    id = when (level) {
        SubjectLevel.ELEMENTARY -> R.string.subjects_level_elementary
        SubjectLevel.MIDDLE -> R.string.subjects_level_middle
        SubjectLevel.HIGH -> R.string.subjects_level_high
    }
)

/**
 * Localized grade label. Mirrors `gradeLabel()` in catalog-subjects-grid.tsx,
 * including Arabic ordinal words (الأول..الثاني عشر) via [arabicOrdinal].
 *
 * Both English and Arabic resource templates use `%s` so the formatting is
 * type-safe regardless of which locale's strings.xml the system picks at runtime.
 */
@Composable
fun gradeLabel(grades: List<Int>, isArabic: Boolean): String? {
    if (grades.isEmpty()) return null
    val first = grades.first()
    val last = grades.last()
    val firstLabel = if (isArabic) arabicOrdinal(first) ?: first.toString() else first.toString()
    val lastLabel = if (isArabic) arabicOrdinal(last) ?: last.toString() else last.toString()
    return if (grades.size == 1) {
        stringResource(R.string.subjects_grade_single, firstLabel)
    } else {
        stringResource(R.string.subjects_grade_range, firstLabel, lastLabel)
    }
}

private val AR_ORDINALS = mapOf(
    1 to "الأول",
    2 to "الثاني",
    3 to "الثالث",
    4 to "الرابع",
    5 to "الخامس",
    6 to "السادس",
    7 to "السابع",
    8 to "الثامن",
    9 to "التاسع",
    10 to "العاشر",
    11 to "الحادي عشر",
    12 to "الثاني عشر",
)

private fun arabicOrdinal(grade: Int): String? = AR_ORDINALS[grade]

/**
 * Compact star rating display: ★ 4.2 (42)
 */
@Composable
fun SubjectRating(
    rating: Float,
    ratingCount: Int,
    modifier: Modifier = Modifier,
    starColor: Color = MaterialTheme.colorScheme.tertiary,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = starColor,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (ratingCount > 0) {
            Text(
                text = stringResource(R.string.subjects_rating_count, ratingCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
