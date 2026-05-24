package org.hogwarts.android.feature.teacher.domain.model

/**
 * Domain model representing a class assigned to a teacher.
 */
data class TeacherClass(
    val id: String,
    val name: String,
    val grade: String,
    val section: String,
    val subjectId: String,
    val subjectName: String,
    val studentCount: Int = 0,
    val nextSessionTime: String? = null,
    val nextSessionDay: String? = null
) {
    val displayName: String
        get() = if (section.isNotBlank()) "$grade - $section" else grade
}

/**
 * A student within a teacher's class.
 */
data class ClassStudent(
    val id: String,
    val givenName: String,
    val familyName: String,
    val studentNumber: String? = null,
    val avatarUrl: String? = null,
    val attendanceRate: Float = 0f,
    val latestGrade: String? = null,
    val status: String = "Active"
) {
    val displayName: String get() = "$givenName $familyName"
}

/**
 * Attendance mark for a student.
 */
data class AttendanceMark(
    val studentId: String,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val note: String = ""
)

enum class AttendanceStatus {
    PRESENT, ABSENT, LATE, EXCUSED
}

/**
 * Grade entry for a student.
 */
data class GradeEntry(
    val studentId: String,
    val marks: Float? = null,
    val note: String = ""
)

/**
 * Assessment for grade entry.
 */
data class Assessment(
    val id: String,
    val name: String,
    val maxMarks: Float,
    val date: String? = null
)

/**
 * Teacher's schedule slot.
 */
data class ScheduleSlot(
    val id: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val className: String,
    val subjectName: String,
    val roomNumber: String? = null,
    val classId: String
)
