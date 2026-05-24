package org.hogwarts.android.feature.grades.data.repository

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.grades.domain.model.GpaSummary
import org.hogwarts.android.feature.grades.domain.model.GradeRecord

/**
 * Repository interface for grades data.
 */
interface GradesRepository {

    /**
     * Get grade records for a student (offline-first).
     */
    fun getStudentGrades(
        studentId: String,
        term: String? = null,
        subjectId: String? = null
    ): Flow<Resource<List<GradeRecord>>>

    /**
     * Get GPA summary for a student.
     */
    suspend fun getGpaSummary(
        studentId: String,
        term: String? = null
    ): GpaSummary
}
