package org.hogwarts.android.feature.subjects.domain.usecase

import org.hogwarts.android.feature.subjects.data.repository.SubjectsRepository
import org.hogwarts.android.feature.subjects.domain.model.Subject
import org.hogwarts.android.feature.subjects.domain.model.SubjectDetail
import javax.inject.Inject

/**
 * Retrieves the list of all subjects for the school catalog.
 */
class GetSubjectsUseCase @Inject constructor(
    private val repository: SubjectsRepository
) {
    suspend operator fun invoke(
        search: String? = null,
        department: String? = null
    ): List<Subject> =
        repository.getSubjects(search = search, department = department)
}

/**
 * Retrieves detailed information for a single subject.
 */
class GetSubjectDetailUseCase @Inject constructor(
    private val repository: SubjectsRepository
) {
    suspend operator fun invoke(subjectId: String): SubjectDetail =
        repository.getSubjectDetail(subjectId)
}
