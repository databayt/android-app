package org.hogwarts.android.feature.grades.domain.usecase

import org.hogwarts.android.feature.grades.domain.model.GradeRecord
import javax.inject.Inject

data class SubmitGradeRequest(
    val studentId: String,
    val subjectId: String,
    val assessmentName: String,
    val assessmentType: org.hogwarts.android.feature.grades.domain.model.AssessmentType,
    val score: Float,
    val maxScore: Float,
    val term: String,
    val note: String? = null
)

class SubmitGradeUseCase @Inject constructor() {
    suspend operator fun invoke(request: SubmitGradeRequest): Result<GradeRecord> {
        return try {
            // TODO: Wire to real API endpoint when available
            val record = GradeRecord(
                id = "grade-${System.currentTimeMillis()}",
                studentId = request.studentId,
                subjectId = request.subjectId,
                subjectName = "",
                assessmentType = request.assessmentType,
                assessmentName = request.assessmentName,
                score = request.score,
                maxScore = request.maxScore,
                date = java.time.LocalDate.now(),
                term = request.term
            )
            Result.success(record)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
