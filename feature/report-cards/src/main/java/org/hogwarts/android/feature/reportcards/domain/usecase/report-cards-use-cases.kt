package org.hogwarts.android.feature.reportcards.domain.usecase

import okhttp3.ResponseBody
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.reportcards.data.repository.ReportCardsRepository
import org.hogwarts.android.feature.reportcards.domain.model.ReportCard
import javax.inject.Inject

/**
 * Use case to get report cards list.
 *
 * Supports filtering by term and student (for guardians).
 */
class GetReportCardsUseCase @Inject constructor(
    private val repository: ReportCardsRepository
) {
    suspend operator fun invoke(
        termId: String? = null,
        studentId: String? = null
    ): Result<List<ReportCard>> = org.hogwarts.android.core.common.result.runCatching {
        repository.getReportCards(termId = termId, studentId = studentId)
    }
}

/**
 * Use case to get a single report card with full details.
 *
 * Returns subject-wise grades, attendance summary, and remarks.
 */
class GetReportCardDetailUseCase @Inject constructor(
    private val repository: ReportCardsRepository
) {
    suspend operator fun invoke(reportCardId: String): Result<ReportCard> = org.hogwarts.android.core.common.result.runCatching {
        repository.getReportCardDetail(reportCardId)
    }
}

/**
 * Use case to download a report card as PDF.
 *
 * Returns the raw ResponseBody which the caller saves
 * to the Downloads directory or shares via Intent.
 */
class DownloadReportCardPdfUseCase @Inject constructor(
    private val repository: ReportCardsRepository
) {
    suspend operator fun invoke(reportCardId: String): Result<ResponseBody> = org.hogwarts.android.core.common.result.runCatching {
        repository.downloadReportCardPdf(reportCardId)
    }
}
