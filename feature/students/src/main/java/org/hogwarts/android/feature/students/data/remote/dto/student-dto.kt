package org.hogwarts.android.feature.students.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentDto(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    val gender: String? = null,
    @SerialName("enrollment_number") val enrollmentNumber: String? = null,
    @SerialName("class_id") val classId: String? = null,
    @SerialName("class_name") val className: String? = null,
    val section: String? = null,
    @SerialName("guardian_name") val guardianName: String? = null,
    @SerialName("guardian_phone") val guardianPhone: String? = null,
    val status: String = "ACTIVE",
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class StudentListResponse(
    val data: List<StudentDto>,
    val total: Int? = null
)

@Serializable
data class CreateStudentDto(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    val gender: String? = null,
    @SerialName("class_id") val classId: String? = null,
    val section: String? = null,
    @SerialName("guardian_name") val guardianName: String? = null,
    @SerialName("guardian_phone") val guardianPhone: String? = null
)

@Serializable
data class UpdateStudentDto(
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val status: String? = null,
    @SerialName("class_id") val classId: String? = null,
    val section: String? = null,
    @SerialName("guardian_name") val guardianName: String? = null,
    @SerialName("guardian_phone") val guardianPhone: String? = null
)
