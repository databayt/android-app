package org.hogwarts.android.feature.grades.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.grades.data.repository.GradesRepository
import org.hogwarts.android.feature.grades.domain.model.GradeRecord
import javax.inject.Inject

/**
 * Use case to get grade records for a student.
 */
class GetGradesUseCase @Inject constructor(
    private val repository: GradesRepository
) {
    operator fun invoke(
        studentId: String,
        term: String? = null,
        subjectId: String? = null
    ): Flow<Resource<List<GradeRecord>>> =
        repository.getStudentGrades(studentId, term, subjectId)
}
