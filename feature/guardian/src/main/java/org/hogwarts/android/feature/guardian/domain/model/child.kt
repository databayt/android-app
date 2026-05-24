package org.hogwarts.android.feature.guardian.domain.model

/**
 * Domain model representing a guardian's child.
 */
data class Child(
    val id: String,
    val studentId: String,
    val givenName: String,
    val familyName: String,
    val grade: String,
    val section: String,
    val avatarUrl: String? = null,
    val attendanceRate: Float = 0f,
    val latestGrade: String? = null,
    val feeBalance: Double = 0.0,
    val className: String = ""
) {
    val displayName: String
        get() = "$givenName $familyName"

    val classDisplay: String
        get() = if (section.isNotBlank()) "$grade - $section" else grade
}

/**
 * Summary of a child's current academic status.
 */
data class ChildSummary(
    val childId: String,
    val attendanceRate: Float,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val lateCount: Int = 0,
    val excusedCount: Int = 0,
    val gpa: Float? = null,
    val latestGradeLetter: String? = null,
    val outstandingFees: Double = 0.0,
    val overdueFees: Double = 0.0
)
