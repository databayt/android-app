package org.hogwarts.android.feature.admin.domain.model

/**
 * School information for the admin module.
 */
data class SchoolInfo(
    val id: String,
    val name: String,
    val domain: String,
    val logo: String? = null,
    val contactEmail: String,
    val contactPhone: String,
    val address: String,
    val subscription: String,
    val academicYear: String,
    val activeTerms: List<String> = emptyList()
)

/**
 * Staff member in the school directory.
 */
data class StaffMember(
    val id: String,
    val givenName: String,
    val familyName: String,
    val email: String,
    val phone: String? = null,
    val role: StaffRole,
    val department: String? = null,
    val avatarUrl: String? = null
) {
    val displayName: String
        get() = "$givenName $familyName"
}

/**
 * Staff roles matching the Hogwarts backend.
 */
enum class StaffRole {
    ADMIN,
    TEACHER,
    COUNSELOR,
    STAFF
}

/**
 * Class roster with enrolled students.
 */
data class ClassRoster(
    val classId: String,
    val className: String,
    val grade: String,
    val section: String,
    val students: List<RosterStudent> = emptyList()
)

/**
 * A student entry within a class roster.
 */
data class RosterStudent(
    val id: String,
    val name: String,
    val enrollmentDate: String,
    val status: String
)

/**
 * School-wide KPI statistics.
 */
data class SchoolStats(
    val enrollmentCount: Int,
    val attendanceRate: Float,
    val averageGpa: Float,
    val feeCollectionRate: Float
)
