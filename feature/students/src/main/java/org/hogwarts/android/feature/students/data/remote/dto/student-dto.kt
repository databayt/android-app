package org.hogwarts.android.feature.students.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * One student as `GET /api/mobile/students` sends it. The server names the
 * fields `given_name` / `family_name` / `gr_number` / `photo_url` and sends
 * `section` as `{ id, name, grade }`; older builds of this DTO expected
 * `first_name` / `last_name` and a string, so the list failed to parse and
 * the screen said there were no students (Al-Ibda, 11-17). Both spellings
 * are accepted.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class StudentDto(
    val id: String,
    @SerialName("given_name") @JsonNames("first_name") val firstName: String = "",
    @SerialName("family_name") @JsonNames("last_name") val lastName: String = "",
    val email: String? = null,
    val phone: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    val gender: String? = null,
    @SerialName("gr_number") @JsonNames("enrollment_number") val enrollmentNumber: String? = null,
    @SerialName("class_id") val classId: String? = null,
    @SerialName("class_name") val className: String? = null,
    val section: JsonElement? = null,
    @SerialName("guardian_name") val guardianName: String? = null,
    @SerialName("guardian_phone") val guardianPhone: String? = null,
    val status: String = "ACTIVE",
    @SerialName("photo_url") @JsonNames("avatar_url") val avatarUrl: String? = null
) {
    /** The section's name, whether the server sent a string or `{ name }`. */
    val sectionName: String?
        get() = when (val s = section) {
            is JsonPrimitive -> s.content.takeIf { s.isString }
            is JsonObject -> (s["name"] as? JsonPrimitive)?.content
            else -> null
        }

    /** The grade, from `section.grade`, for the class line. */
    val gradeName: String?
        get() = ((section as? JsonObject)?.get("grade") as? JsonPrimitive)?.takeIf { it.isString }?.content
}

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
