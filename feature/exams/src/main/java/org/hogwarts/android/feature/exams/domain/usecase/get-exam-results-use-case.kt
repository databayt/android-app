package org.hogwarts.android.feature.exams.domain.usecase

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.exams.data.remote.AdvancedExamsApi
import org.hogwarts.android.feature.exams.data.remote.dto.ExamAnswerDto
import org.hogwarts.android.feature.exams.data.remote.dto.toDomain
import org.hogwarts.android.feature.exams.domain.model.DetailedExamResult
import javax.inject.Inject

/**
 * Retrieves detailed exam results including per-question answer review,
 * overall score summary, grade, rank, and answer explanations.
 */
class GetExamResultsUseCase @Inject constructor(
    private val api: AdvancedExamsApi,
    private val tenantContext: TenantContext
) {
    /**
     * Wrapper containing full result details plus raw answer DTOs
     * for the UI to display question text, correct answers, and explanations.
     */
    data class ExamResultWithDetails(
        val result: DetailedExamResult,
        val answerDetails: List<ExamAnswerDto>
    )

    suspend operator fun invoke(examId: String): Resource<ExamResultWithDetails> {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            val response = api.getExamResults(examId, schoolId)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                Resource.Success(
                    ExamResultWithDetails(
                        result = dto.toDomain(),
                        answerDetails = dto.answers
                    )
                )
            } else {
                Resource.Error(
                    Exception(response.message() ?: "Failed to load exam results")
                )
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}
