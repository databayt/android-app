package org.hogwarts.android.feature.exams.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.exams.data.repository.ExamsRepository
import org.hogwarts.android.feature.exams.domain.model.Exam
import javax.inject.Inject

class GetExamsUseCase @Inject constructor(
    private val repository: ExamsRepository
) {
    operator fun invoke(status: String? = null): Flow<Resource<List<Exam>>> =
        repository.getExams(status)
}

class GetExamDetailUseCase @Inject constructor(
    private val repository: ExamsRepository
) {
    operator fun invoke(examId: String): Flow<Resource<Exam?>> =
        repository.getExam(examId)
}
