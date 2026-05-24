package org.hogwarts.android.feature.exams.domain.usecase

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.exams.data.remote.AdvancedExamsApi
import org.hogwarts.android.feature.exams.data.remote.dto.SubmitAnswerItemDto
import org.hogwarts.android.feature.exams.data.remote.dto.SubmitAnswersRequest
import java.time.Instant
import javax.inject.Inject

class SubmitExamAnswersUseCase @Inject constructor(
    private val api: AdvancedExamsApi,
    private val tenantContext: TenantContext
) {
    data class SubmitResult(
        val success: Boolean,
        val message: String?,
        val submittedAt: Instant?
    )

    suspend operator fun invoke(
        examId: String,
        answers: Map<String, String?>
    ): Resource<SubmitResult> {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            val answerItems = answers.map { (questionId, answer) ->
                SubmitAnswerItemDto(questionId = questionId, answer = answer)
            }
            val response = api.submitAnswers(
                examId = examId,
                schoolId = schoolId,
                request = SubmitAnswersRequest(
                    examId = examId,
                    answers = answerItems
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Resource.Success(
                    SubmitResult(
                        success = body.success,
                        message = body.message,
                        submittedAt = body.submittedAt?.let { Instant.parse(it) }
                    )
                )
            } else {
                Resource.Error(
                    Exception(response.message() ?: "Failed to submit answers")
                )
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}
