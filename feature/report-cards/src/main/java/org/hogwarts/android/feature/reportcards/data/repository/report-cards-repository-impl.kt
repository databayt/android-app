package org.hogwarts.android.feature.reportcards.data.repository

import okhttp3.ResponseBody
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.reportcards.data.remote.ReportCardsApi
import org.hogwarts.android.feature.reportcards.domain.model.ReportCard
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [ReportCardsRepository].
 *
 * All API calls are scoped by schoolId from [TenantContext]
 * for multi-tenant isolation.
 */
@Singleton
class ReportCardsRepositoryImpl @Inject constructor(
    private val api: ReportCardsApi,
    private val tenantContext: TenantContext
) : ReportCardsRepository {

    override suspend fun getReportCards(
        termId: String?,
        studentId: String?
    ): List<ReportCard> {
        val schoolId = tenantContext.requireSchoolId()
        val response = api.getReportCards(
            schoolId = schoolId,
            termId = termId,
            studentId = studentId
        )
        val body = response.body()
            ?: throw Exception("Failed to load report cards: ${response.code()}")
        return body.data.map { it.toDomain() }
    }

    override suspend fun getReportCardDetail(reportCardId: String): ReportCard {
        val schoolId = tenantContext.requireSchoolId()
        val response = api.getReportCardDetail(
            id = reportCardId,
            schoolId = schoolId
        )
        val body = response.body()
            ?: throw Exception("Failed to load report card detail: ${response.code()}")
        return body.toDomain()
    }

    override suspend fun downloadReportCardPdf(reportCardId: String): ResponseBody {
        val schoolId = tenantContext.requireSchoolId()
        val response = api.downloadReportCardPdf(
            id = reportCardId,
            schoolId = schoolId
        )
        return response.body()
            ?: throw Exception("Failed to download PDF: ${response.code()}")
    }
}
