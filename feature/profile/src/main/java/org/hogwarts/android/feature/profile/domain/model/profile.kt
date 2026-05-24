package org.hogwarts.android.feature.profile.domain.model

/**
 * Mirrors web `BaseProfile` + role-specific shapes.
 * Web reference: src/components/school-dashboard/profile/types.ts
 */
data class UserProfile(
    val id: String,
    val userId: String,
    val schoolId: String,
    val email: String,
    val username: String? = null,
    val role: ProfileRole,
    val avatarUrl: String? = null,
    val coverImageUrl: String? = null,
    val bio: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val school: SchoolInfo? = null,
    val student: StudentDetails? = null,
    val teacher: TeacherDetails? = null,
    val guardian: GuardianDetails? = null,
    val staff: StaffDetails? = null,
    val socialLinks: SocialLinks = SocialLinks(),
    val settings: ProfileSettings = ProfileSettings(),
    val visibility: ProfileVisibility = ProfileVisibility.SCHOOL,
    val completionPercentage: Int = 0
) {
    val displayName: String get() = when (role) {
        ProfileRole.STUDENT -> student?.fullName
        ProfileRole.TEACHER -> teacher?.fullName
        ProfileRole.PARENT -> guardian?.fullName
        ProfileRole.STAFF -> staff?.fullName
        ProfileRole.ADMIN -> username
    } ?: username ?: email

    val initials: String get() = displayName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
}

enum class ProfileRole {
    STUDENT, TEACHER, PARENT, STAFF, ADMIN;

    companion object {
        fun fromString(value: String?): ProfileRole = when (value?.uppercase()) {
            "STUDENT" -> STUDENT
            "TEACHER" -> TEACHER
            "GUARDIAN", "PARENT" -> PARENT
            "STAFF", "ACCOUNTANT" -> STAFF
            "ADMIN", "SCHOOL_ADMIN", "SUPER_ADMIN" -> ADMIN
            else -> STUDENT
        }
    }
}

enum class ProfileVisibility { PUBLIC, SCHOOL, CONNECTIONS, PRIVATE }

data class SocialLinks(
    val website: String? = null,
    val linkedin: String? = null,
    val twitter: String? = null,
    val github: String? = null
)

data class ProfileSettings(
    val theme: String = "system",
    val language: String = "ar",
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val showEmail: Boolean = false,
    val showPhone: Boolean = false,
    val allowMessages: Boolean = true
)

data class StudentDetails(
    val id: String,
    val firstName: String,
    val lastName: String,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val phone: String? = null,
    val photoUrl: String? = null,
    val status: String? = null,
    val sectionId: String? = null,
    val sectionName: String? = null,
    val gradeName: String? = null
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

data class TeacherDetails(
    val id: String,
    val firstName: String,
    val lastName: String,
    val gender: String? = null,
    val birthDate: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val status: String? = null,
    val department: String? = null
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

data class GuardianDetails(
    val id: String,
    val firstName: String,
    val lastName: String,
    val relationship: String? = null,
    val phone: String? = null,
    val occupation: String? = null,
    val children: List<ChildOverview> = emptyList()
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

data class ChildOverview(
    val studentId: String,
    val name: String,
    val grade: String? = null,
    val section: String? = null,
    val attendanceRate: Float = 0f,
    val academicPerformance: AcademicPerformance = AcademicPerformance.AVERAGE,
    val pendingFees: Double? = null
)

enum class AcademicPerformance { EXCELLENT, GOOD, AVERAGE, NEEDS_IMPROVEMENT }

data class StaffDetails(
    val id: String,
    val firstName: String,
    val lastName: String,
    val employeeId: String? = null,
    val department: String? = null,
    val designation: String? = null,
    val employmentType: String? = null
) {
    val fullName: String get() = "$firstName $lastName".trim()
}
