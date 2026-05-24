package org.hogwarts.android.feature.auth.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Login request DTO.
 *
 * `identifier` accepts either an email or a per-school username — backend
 * branches on the `@` sign in `/api/mobile/auth`.
 */
@Serializable
data class LoginRequestDto(
    val identifier: String,
    val password: String
)

/**
 * Authentication response DTO.
 */
@Serializable
data class AuthResponseDto(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("refresh_token")
    val refreshToken: String?,

    @SerialName("expires_at")
    val expiresAt: Long,

    val user: UserDto
)

/**
 * Google OAuth request DTO.
 */
@Serializable
data class GoogleAuthRequestDto(
    @SerialName("id_token")
    val idToken: String
)

/**
 * Facebook OAuth request DTO.
 */
@Serializable
data class FacebookAuthRequestDto(
    @SerialName("access_token")
    val accessToken: String
)

/**
 * Sign-up request DTO.
 */
@Serializable
data class SignUpRequestDto(
    val email: String,
    val password: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("school_id")
    val schoolId: String
)

/**
 * Password reset request DTO.
 */
@Serializable
data class ResetPasswordRequestDto(
    val email: String
)

/**
 * OTP verification DTO.
 */
@Serializable
data class VerifyOtpRequestDto(
    val email: String,
    val otp: String
)

/**
 * New password DTO.
 */
@Serializable
data class NewPasswordRequestDto(
    val email: String,
    val otp: String,
    @SerialName("new_password")
    val newPassword: String
)

/**
 * School list item DTO for signup school selector.
 */
@Serializable
data class SchoolDto(
    val id: String,
    val name: String,
    @SerialName("name_en")
    val nameEn: String? = null,
    @SerialName("logo_url")
    val logoUrl: String? = null,
    val domain: String? = null
)

/**
 * User DTO from auth response.
 */
@Serializable
data class UserDto(
    val id: String,
    val email: String? = null,

    @SerialName("school_id")
    val schoolId: String? = null,

    val role: String,

    @SerialName("given_name")
    val givenName: String? = null,

    @SerialName("family_name")
    val familyName: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    // Grade level for STUDENT-role users (1..12); null for other roles.
    // Backend (Hogwarts web /api/mobile/auth) should resolve this from the
    // student's enrolled Class.grade. Until that ships, this stays null and
    // the catalog falls back to "show all courses" for the student.
    val grade: Int? = null
)
