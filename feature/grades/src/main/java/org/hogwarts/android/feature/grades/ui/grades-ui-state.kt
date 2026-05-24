package org.hogwarts.android.feature.grades.ui

import androidx.annotation.StringRes
import org.hogwarts.android.feature.grades.R
import org.hogwarts.android.feature.grades.domain.model.GradeRecord

/**
 * UI state for the Grades screen.
 */
data class GradesUiState(
    val isLoading: Boolean = true,
    val records: List<GradeRecord> = emptyList(),
    val error: String? = null,
    val selectedFilter: GradesFilter = GradesFilter.ALL
)

enum class GradesFilter(@StringRes val labelResId: Int) {
    ALL(R.string.grades_filter_all),
    EXAM(R.string.grades_filter_exam),
    QUIZ(R.string.grades_filter_quiz),
    ASSIGNMENT(R.string.grades_filter_assignment),
    MIDTERM(R.string.grades_filter_midterm),
    FINAL(R.string.grades_filter_final)
}
