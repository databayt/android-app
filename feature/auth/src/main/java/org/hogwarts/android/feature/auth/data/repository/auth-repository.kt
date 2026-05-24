package org.hogwarts.android.feature.auth.data.repository

import org.hogwarts.android.feature.auth.domain.model.AuthResult

/**
 * Repository interface for authentication operations.
 */
interface AuthRepository {
    /** Authenticate user with email and password. */
    suspend fun login(email: String, password: String): AuthResult

    /** Authenticate with Google ID token. */
    suspend fun loginWithGoogle(idToken: String): AuthResult

    /** Authenticate with Facebook access token. */
    suspend fun loginWithFacebook(accessToken: String): AuthResult

    /** Register a new user. */
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        schoolId: String
    ): AuthResult

    /** Request password reset OTP via email. */
    suspend fun requestPasswordReset(email: String)

    /** Verify OTP code. */
    suspend fun verifyOtp(email: String, otp: String)

    /** Set new password after OTP verification. */
    suspend fun setNewPassword(email: String, otp: String, newPassword: String)

    /** Refresh access token using refresh token. */
    suspend fun refreshToken(): AuthResult

    /** Log out current user and clear session. */
    suspend fun logout()

    /** Get available schools for registration. */
    suspend fun getSchools(): List<SchoolInfo>

    /** Check if user is currently authenticated. */
    val isAuthenticated: Boolean

    /** Get current user's school ID. */
    val currentSchoolId: String?

    /** Get current user's role. */
    val currentUserRole: String?
}

/**
 * School info for registration dropdown.
 */
data class SchoolInfo(
    val id: String,
    val name: String
)
