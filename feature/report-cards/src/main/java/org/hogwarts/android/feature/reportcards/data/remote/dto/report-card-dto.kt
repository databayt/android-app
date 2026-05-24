package org.hogwarts.android.feature.reportcards.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.reportcards.domain.model.AttendanceSummary
import org.hogwarts.android.feature.reportcards.domain.model.ReportCard
import org.hogwarts.android.feature.reportcards.domain.model.ReportCardStatus
import org.hogwarts.android.feature.reportcards.domain.model.SubjectReport

/**
 * DTOs for report cards API responses.
 */

@Serializable
data class ReportCardListResponse(
    val data: List<ReportCardSummaryDto>,
    val total: Int? = null
)

/**
 * Summary DTO for list views (no full subject details).
 */
@Serializable
data class ReportCardSummaryDto(
    val id: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("term_id") val termId: String,
    @SerialName("term_name") val termName: String,
    @SerialName("academic_year") val academicYear: String,
    val gpa: Float? = null,
    val rank: Int? = null,
    @SerialName("total_students") val totalStudents: Int? = null,
    val status: String
) {
    fun toDomain(): ReportCard = ReportCard(
        id = id,
        studentId = studentId,
        studentName = studentName,
        termId = termId,
        termName = termName,
        academicYear = academicYear,
        gpa = gpa,
        rank = rank,
        totalStudents = totalStudents,
        status = ReportCardStatus.fromString(status)
    )
}

/**
 * Full detail DTO including subject reports and attendance.
 */
@Serializable
data class ReportCardDetailDto(
    val id: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("term_id") val termId: String,
    @SerialName("term_name") val termName: String,
    @SerialName("academic_year") val academicYear: String,
    val gpa: Float? = null,
    val rank: Int? = null,
    @SerialName("total_students") val totalStudents: Int? = null,
    val status: String,
    val subjects: List<SubjectReportDto> = emptyList(),
    @SerialName("overall_remarks") val overallRemarks: String? = null,
    @SerialName("attendance_summary") val attendanceSummary: AttendanceSummaryDto? = null
) {
    fun toDomain(): ReportCard = ReportCard(
        id = id,
        studentId = studentId,
        studentName = studentName,
        termId = termId,
        termName = termName,
        academicYear = academicYear,
        gpa = gpa,
        rank = rank,
        totalStudents = totalStudents,
        status = ReportCardStatus.fromString(status),
        subjects = subjects.map { it.toDomain() },
        overallRemarks = overallRemarks,
        attendanceSummary = attendanceSummary?.toDomain()
    )
}

@Serializable
data class SubjectReportDto(
    @SerialName("subject_id") val subjectId: String,
    @SerialName("subject_name") val subjectName: String,
    @SerialName("teacher_name") val teacherName: String,
    val marks: Float,
    @SerialName("max_marks") val maxMarks: Float,
    val grade: String,
    val percentage: Float,
    val remarks: String? = null
) {
    fun toDomain(): SubjectReport = SubjectReport(
        subjectId = subjectId,
        subjectName = subjectName,
        teacherName = teacherName,
        marks = marks,
        maxMarks = maxMarks,
        grade = grade,
        percentage = percentage,
        remarks = remarks
    )
}

@Serializable
data class AttendanceSummaryDto(
    @SerialName("total_days") val totalDays: Int,
    @SerialName("present_days") val presentDays: Int,
    @SerialName("absent_days") val absentDays: Int,
    @SerialName("late_days") val lateDays: Int
) {
    fun toDomain(): AttendanceSummary = AttendanceSummary(
        totalDays = totalDays,
        presentDays = presentDays,
        absentDays = absentDays,
        lateDays = lateDays
    )
}
