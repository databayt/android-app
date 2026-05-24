package org.hogwarts.android.feature.reportcards.data.repository

import okhttp3.ResponseBody
import org.hogwarts.android.feature.reportcards.domain.model.ReportCard

/**
 * Repository interface for report cards data.
 *
 * All operations are tenant-scoped via TenantContext.
 */
interface ReportCardsRepository {

    /**
     * Get report cards list, optionally filtered by term and student.
     *
     * @param termId Optional term filter
     * @param studentId Optional student filter (for guardians viewing children)
     * @return List of report card summaries
     */
    suspend fun getReportCards(
        termId: String? = null,
        studentId: String? = null
    ): List<ReportCard>

    /**
     * Get full report card detail with subject reports and attendance.
     *
     * @param reportCardId The report card ID
     * @return Complete report card with all subjects
     */
    suspend fun getReportCardDetail(reportCardId: String): ReportCard

    /**
     * Download report card as PDF.
     *
     * @param reportCardId The report card ID
     * @return ResponseBody containing the PDF binary data
     */
    suspend fun downloadReportCardPdf(reportCardId: String): ResponseBody
}
