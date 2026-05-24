package org.hogwarts.android.feature.exams.domain.usecase

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.exams.data.remote.AdvancedExamsApi
import org.hogwarts.android.feature.exams.data.remote.dto.SubmitAnswerItemDto
import org.hogwarts.android.feature.exams.data.remote.dto.SubmitAnswersRequest
import org.hogwarts.android.feature.exams.data.remote.dto.ViolationReportRequest
import org.hogwarts.android.feature.exams.data.remote.dto.toDomain
import org.hogwarts.android.feature.exams.domain.model.OnlineExam
import org.hogwarts.android.feature.exams.domain.model.ViolationType
import java.time.Instant
import javax.inject.Inject

class StartOnlineExamUseCase @Inject constructor(
    private val api: AdvancedExamsApi,
    private val tenantContext: TenantContext
) {
    suspend operator fun invoke(examId: String): Resource<OnlineExam> {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            val response = api.getOnlineExamSession(examId, schoolId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error(
                    Exception(response.message() ?: "Failed to start online exam")
                )
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}

class SubmitExamViolationUseCase @Inject constructor(
    private val api: AdvancedExamsApi,
    private val tenantContext: TenantContext
) {
    data class ViolationResult(
        val violationCount: Int,
        val maxViolations: Int,
        val examTerminated: Boolean
    )

    suspend operator fun invoke(
        examId: String,
        type: ViolationType,
        description: String
    ): Resource<ViolationResult> {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            val response = api.reportViolation(
                examId = examId,
                schoolId = schoolId,
                request = ViolationReportRequest(
                    examId = examId,
                    type = type.name,
                    description = description,
                    timestamp = Instant.now().toString()
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Resource.Success(
                    ViolationResult(
                        violationCount = body.violationCount,
                        maxViolations = body.maxViolations,
                        examTerminated = body.examTerminated
                    )
                )
            } else {
                Resource.Error(
                    Exception(response.message() ?: "Failed to report violation")
                )
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}
