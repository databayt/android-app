package org.hogwarts.android.feature.auth.domain.model

import org.hogwarts.android.core.data.tenant.UserRole

/**
 * Result of successful authentication.
 */
data class AuthResult(
    val userId: String,
    val email: String,
    val schoolId: String,
    val role: UserRole,
    val givenName: String?,
    val familyName: String?,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Long,
    val grade: Int? = null
) {
    val displayName: String
        get() = listOfNotNull(givenName, familyName).joinToString(" ").ifEmpty { email }
}

/**
 * Login request payload.
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Credentials for local storage (encrypted).
 */
data class StoredCredentials(
    val email: String,
    val hashedPassword: String
)
