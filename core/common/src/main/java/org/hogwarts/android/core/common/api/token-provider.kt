package org.hogwarts.android.core.common.api

/**
 * JWT tokens for the network layer. Implemented by TokenManager in core/security.
 */
interface TokenProvider {
    /** Current access token for API requests. */
    val accessToken: String?

    /** Refresh token for obtaining new access tokens. */
    val refreshToken: String?

    /** True when an access token is stored. */
    val hasTokens: Boolean
        get() = accessToken != null

    /**
     * Persist tokens returned by `PUT /api/mobile/auth`. A null [refreshToken]
     * keeps the stored one.
     */
    fun saveRefreshedTokens(accessToken: String, refreshToken: String?, expiresAtMillis: Long)

    /** The refresh token was rejected: clear tokens so the app returns to login. */
    fun onSessionExpired()
}
