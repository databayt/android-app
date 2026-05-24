package org.hogwarts.android.feature.grades.domain.model

import java.time.LocalDate

/**
 * Domain model for grade/result records.
 */
data class GradeRecord(
    val id: String,
    val studentId: String,
    val subjectId: String,
    val subjectName: String,
    val assessmentType: AssessmentType,
    val assessmentName: String,
    val score: Float,
    val maxScore: Float,
    val grade: String? = null,
    val date: LocalDate,
    val term: String? = null,
    val remarks: String? = null
) {
    val percentage: Float
        get() = if (maxScore > 0) (score / maxScore) * 100 else 0f
}

/**
 * Assessment types matching Hogwarts backend.
 */
enum class AssessmentType {
    EXAM,
    QUIZ,
    ASSIGNMENT,
    PROJECT,
    MIDTERM,
    FINAL;

    companion object {
        fun fromString(value: String): AssessmentType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: EXAM
    }
}

/**
 * Subject-level grade summary.
 */
data class SubjectGradeSummary(
    val subjectId: String,
    val subjectName: String,
    val averagePercentage: Float,
    val letterGrade: String,
    val totalAssessments: Int
)

/**
 * Overall GPA summary.
 */
data class GpaSummary(
    val gpa: Float,
    val totalCredits: Int,
    val subjects: List<SubjectGradeSummary>,
    val term: String? = null
)
