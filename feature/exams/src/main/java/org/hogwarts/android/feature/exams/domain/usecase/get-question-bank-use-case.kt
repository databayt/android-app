package org.hogwarts.android.feature.exams.domain.usecase

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.exams.data.remote.AdvancedExamsApi
import org.hogwarts.android.feature.exams.data.remote.dto.GenerateQuestionsRequest
import org.hogwarts.android.feature.exams.data.remote.dto.toDomain
import org.hogwarts.android.feature.exams.domain.model.QuestionBankItem
import javax.inject.Inject

class GetQuestionBankUseCase @Inject constructor(
    private val api: AdvancedExamsApi,
    private val tenantContext: TenantContext
) {
    suspend operator fun invoke(
        subject: String? = null,
        topic: String? = null,
        difficulty: String? = null
    ): Resource<List<QuestionBankItem>> {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            val response = api.getQuestionBank(
                schoolId = schoolId,
                subject = subject,
                topic = topic,
                difficulty = difficulty
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data.map { it.toDomain() })
            } else {
                Resource.Error(
                    Exception(response.message() ?: "Failed to load question bank")
                )
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}

class GenerateQuestionsUseCase @Inject constructor(
    private val api: AdvancedExamsApi,
    private val tenantContext: TenantContext
) {
    suspend operator fun invoke(
        subject: String,
        topic: String? = null,
        difficulty: String? = null,
        count: Int = 10
    ): Resource<List<QuestionBankItem>> {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            val response = api.generateQuestions(
                schoolId = schoolId,
                request = GenerateQuestionsRequest(
                    subject = subject,
                    topic = topic,
                    difficulty = difficulty,
                    count = count
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data.map { it.toDomain() })
            } else {
                Resource.Error(
                    Exception(response.message() ?: "Failed to generate questions")
                )
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}

class ToggleBookmarkUseCase @Inject constructor(
    private val api: AdvancedExamsApi,
    private val tenantContext: TenantContext
) {
    suspend operator fun invoke(questionId: String): Resource<Boolean> {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            val response = api.toggleBookmark(questionId, schoolId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.isBookmarked)
            } else {
                Resource.Error(
                    Exception(response.message() ?: "Failed to toggle bookmark")
                )
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}
