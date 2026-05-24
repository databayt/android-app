package org.hogwarts.android.feature.admin.data.remote.dto

import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.admin.domain.model.ClassRoster
import org.hogwarts.android.feature.admin.domain.model.RosterStudent
import org.hogwarts.android.feature.admin.domain.model.SchoolInfo
import org.hogwarts.android.feature.admin.domain.model.SchoolStats
import org.hogwarts.android.feature.admin.domain.model.StaffMember
import org.hogwarts.android.feature.admin.domain.model.StaffRole

@Serializable
data class SchoolInfoDto(
    val id: String,
    val name: String,
    val domain: String = "",
    val logo: String? = null,
    val contactEmail: String = "",
    val contactPhone: String = "",
    val address: String = "",
    val subscription: String = "",
    val academicYear: String = "",
    val activeTerms: List<String> = emptyList()
) {
    fun toDomain(): SchoolInfo = SchoolInfo(
        id = id,
        name = name,
        domain = domain,
        logo = logo,
        contactEmail = contactEmail,
        contactPhone = contactPhone,
        address = address,
        subscription = subscription,
        academicYear = academicYear,
        activeTerms = activeTerms
    )
}

@Serializable
data class StaffMemberDto(
    val id: String,
    val givenName: String = "",
    val familyName: String = "",
    val email: String = "",
    val phone: String? = null,
    val role: String = "STAFF",
    val department: String? = null,
    val avatarUrl: String? = null
) {
    fun toDomain(): StaffMember = StaffMember(
        id = id,
        givenName = givenName,
        familyName = familyName,
        email = email,
        phone = phone,
        role = runCatching { StaffRole.valueOf(role.uppercase()) }.getOrDefault(StaffRole.STAFF),
        department = department,
        avatarUrl = avatarUrl
    )
}

@Serializable
data class RosterStudentDto(
    val id: String,
    val name: String = "",
    val enrollmentDate: String = "",
    val status: String = ""
) {
    fun toDomain(): RosterStudent = RosterStudent(
        id = id,
        name = name,
        enrollmentDate = enrollmentDate,
        status = status
    )
}

@Serializable
data class ClassRosterDto(
    val classId: String,
    val className: String = "",
    val grade: String = "",
    val section: String = "",
    val students: List<RosterStudentDto> = emptyList()
) {
    fun toDomain(): ClassRoster = ClassRoster(
        classId = classId,
        className = className,
        grade = grade,
        section = section,
        students = students.map { it.toDomain() }
    )
}

@Serializable
data class SchoolStatsDto(
    val enrollmentCount: Int = 0,
    val attendanceRate: Float = 0f,
    val averageGpa: Float = 0f,
    val feeCollectionRate: Float = 0f
) {
    fun toDomain(): SchoolStats = SchoolStats(
        enrollmentCount = enrollmentCount,
        attendanceRate = attendanceRate,
        averageGpa = averageGpa,
        feeCollectionRate = feeCollectionRate
    )
}
