package org.hogwarts.android.feature.students.domain.model

import java.time.LocalDate

/**
 * Domain model for a student.
 */
data class Student(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val phone: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: String? = null,
    val enrollmentNumber: String? = null,
    val classId: String? = null,
    val className: String? = null,
    val section: String? = null,
    val guardianName: String? = null,
    val guardianPhone: String? = null,
    val status: StudentStatus = StudentStatus.ACTIVE,
    val avatarUrl: String? = null
) {
    val fullName: String get() = "$firstName $lastName"

    val initials: String
        get() = "${firstName.firstOrNull()?.uppercase() ?: ""}${lastName.firstOrNull()?.uppercase() ?: ""}"
}

enum class StudentStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    GRADUATED,
    TRANSFERRED;

    companion object {
        fun fromString(value: String): StudentStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
    }
}
