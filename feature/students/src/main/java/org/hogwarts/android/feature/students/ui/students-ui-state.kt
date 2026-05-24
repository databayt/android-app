package org.hogwarts.android.feature.students.ui

import org.hogwarts.android.feature.students.domain.model.Student

data class StudentsUiState(
    val isLoading: Boolean = true,
    val students: List<Student> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: String? = null,
    val error: String? = null
)
