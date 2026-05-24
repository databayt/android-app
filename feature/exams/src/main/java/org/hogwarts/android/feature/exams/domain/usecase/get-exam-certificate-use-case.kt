package org.hogwarts.android.feature.exams.domain.usecase

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.exams.data.remote.AdvancedExamsApi
import org.hogwarts.android.feature.exams.data.remote.dto.toDomain
import org.hogwarts.android.feature.exams.domain.model.ExamCertificate
import javax.inject.Inject

/**
 * Retrieves the exam certificate for a completed and graded exam.
 * Includes verification code and certificate URL for sharing and download.
 */
class GetExamCertificateUseCase @Inject constructor(
    private val api: AdvancedExamsApi,
    private val tenantContext: TenantContext
) {
    suspend operator fun invoke(examId: String): Resource<ExamCertificate> {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            val response = api.getExamCertificate(examId, schoolId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error(
                    Exception(response.message() ?: "Failed to load certificate")
                )
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}
