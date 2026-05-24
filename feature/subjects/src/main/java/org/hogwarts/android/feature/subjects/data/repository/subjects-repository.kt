package org.hogwarts.android.feature.subjects.data.repository

import org.hogwarts.android.feature.subjects.domain.model.MySubjectSummary
import org.hogwarts.android.feature.subjects.domain.model.Subject
import org.hogwarts.android.feature.subjects.domain.model.SubjectDetail

/**
 * Repository interface for subject data operations.
 */
interface SubjectsRepository {

    /**
     * Get all subjects for the current school, optionally filtered by search query.
     */
    suspend fun getSubjects(
        search: String? = null,
        department: String? = null
    ): List<Subject>

    /**
     * Get detailed information about a specific subject.
     */
    suspend fun getSubjectDetail(subjectId: String): SubjectDetail

    /**
     * Get the current user's enrolled/assigned subjects with summary stats.
     */
    suspend fun getMySubjects(): List<MySubjectSummary>
}
