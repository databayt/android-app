package org.hogwarts.android.feature.exams.ui

import org.hogwarts.android.feature.exams.domain.model.Exam

data class ExamsUiState(
    val isLoading: Boolean = true,
    val exams: List<Exam> = emptyList(),
    val selectedStatusFilter: String? = null,
    val error: String? = null
)
