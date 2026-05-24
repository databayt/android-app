package org.hogwarts.android.feature.reportcards.domain.model

/**
 * Domain model for a student's report card.
 *
 * Represents a complete term/semester report containing
 * subject-wise grades, GPA, rank, attendance summary, and remarks.
 */
data class ReportCard(
    val id: String,
    val studentId: String,
    val studentName: String,
    val termId: String,
    val termName: String,
    val academicYear: String,
    val gpa: Float? = null,
    val rank: Int? = null,
    val totalStudents: Int? = null,
    val status: ReportCardStatus,
    val subjects: List<SubjectReport> = emptyList(),
    val overallRemarks: String? = null,
    val attendanceSummary: AttendanceSummary? = null
) {
    /**
     * Formatted rank display (e.g. "3 / 45")
     */
    val rankDisplay: String?
        get() = if (rank != null && totalStudents != null) "$rank / $totalStudents" else null

    /**
     * Overall percentage across all subjects
     */
    val overallPercentage: Float?
        get() {
            if (subjects.isEmpty()) return null
            val totalMarks = subjects.sumOf { it.marks.toDouble() }
            val totalMaxMarks = subjects.sumOf { it.maxMarks.toDouble() }
            return if (totalMaxMarks > 0) ((totalMarks / totalMaxMarks) * 100).toFloat() else null
        }
}

/**
 * Report card publication status.
 */
enum class ReportCardStatus {
    PUBLISHED,
    DRAFT;

    companion object {
        fun fromString(value: String): ReportCardStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: DRAFT
    }
}

/**
 * Individual subject report within a report card.
 */
data class SubjectReport(
    val subjectId: String,
    val subjectName: String,
    val teacherName: String,
    val marks: Float,
    val maxMarks: Float,
    val grade: String,
    val percentage: Float,
    val remarks: String? = null
) {
    /**
     * Whether the student passed this subject (>= 40% by default)
     */
    val isPassed: Boolean
        get() = percentage >= 40f
}

/**
 * Attendance summary for the report card term.
 */
data class AttendanceSummary(
    val totalDays: Int,
    val presentDays: Int,
    val absentDays: Int,
    val lateDays: Int
) {
    /**
     * Attendance percentage
     */
    val attendancePercentage: Float
        get() = if (totalDays > 0) (presentDays.toFloat() / totalDays) * 100 else 0f
}

/**
 * Grading scheme used by the school.
 */
enum class GradingScheme {
    GPA_4,
    GPA_5,
    CGPA,
    CCE,
    PERCENTAGE;

    companion object {
        fun fromString(value: String): GradingScheme =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: PERCENTAGE
    }
}
