package org.hogwarts.android.feature.grades.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for grades API responses.
 */
@Serializable
data class GradeRecordDto(
    val id: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("subject_id") val subjectId: String,
    @SerialName("subject_name") val subjectName: String,
    @SerialName("assessment_type") val assessmentType: String,
    @SerialName("assessment_name") val assessmentName: String,
    val score: Float,
    @SerialName("max_score") val maxScore: Float,
    val grade: String? = null,
    val date: String,
    val term: String? = null,
    val remarks: String? = null
)

@Serializable
data class GradeListResponse(
    val data: List<GradeRecordDto>,
    val total: Int? = null
)

@Serializable
data class SubjectGradeSummaryDto(
    @SerialName("subject_id") val subjectId: String,
    @SerialName("subject_name") val subjectName: String,
    @SerialName("average_percentage") val averagePercentage: Float,
    @SerialName("letter_grade") val letterGrade: String,
    @SerialName("total_assessments") val totalAssessments: Int
)

@Serializable
data class GpaSummaryDto(
    val gpa: Float,
    @SerialName("total_credits") val totalCredits: Int,
    val subjects: List<SubjectGradeSummaryDto>,
    val term: String? = null
)
