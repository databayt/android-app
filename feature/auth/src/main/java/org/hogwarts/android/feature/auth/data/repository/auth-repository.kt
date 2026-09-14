package org.hogwarts.android.feature.auth.data.repository

import org.hogwarts.android.feature.auth.domain.model.AuthResult
import org.hogwarts.android.feature.auth.domain.model.SchoolInfo
import org.hogwarts.android.feature.auth.domain.model.SocialLogin

/**
 * Authentication against hogwarts `api/mobile/auth/`. Every failure is an
 * [org.hogwarts.android.feature.auth.domain.model.AuthException].
 */
interface AuthRepository {
    /** Sign in with an email or student username and a password. */
    suspend fun login(identifier: String, password: String): AuthResult

    /**
     * Sign in with a Google ID token. Without [schoolId] a platform-level
     * identity answers [SocialLogin.NeedsSchool]; retry with the chosen id.
     */
    suspend fun loginWithGoogle(idToken: String, schoolId: String? = null): SocialLogin

    /** Register a new user. */
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        schoolId: String
    ): AuthResult

    /** Email a 6-digit reset code (the server answers 200 either way). */
    suspend fun requestPasswordReset(email: String)

    /** Check a code. Consumes it server-side — not part of the reset flow. */
    suspend fun verifyOtp(email: String, otp: String)

    /** Set a new password with the emailed code. */
    suspend fun setNewPassword(email: String, otp: String, newPassword: String)

    /** Refresh access token using refresh token. */
    suspend fun refreshToken(): AuthResult

    /** Log out current user and clear session. */
    suspend fun logout()

    /** Published, active schools. */
    suspend fun getSchools(): List<SchoolInfo>

    /** Check if user is currently authenticated. */
    val isAuthenticated: Boolean

    /** Get current user's school ID. */
    val currentSchoolId: String?

    /** Get current user's role. */
    val currentUserRole: String?
}
