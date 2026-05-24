package org.hogwarts.android.core.common.api

/**
 * Interface for providing JWT tokens to network layer.
 * Implemented by TokenManager in security module.
 */
interface TokenProvider {
    /**
     * Current access token for API requests
     */
    val accessToken: String?

    /**
     * Refresh token for obtaining new access tokens
     */
    val refreshToken: String?

    /**
     * Check if tokens are available
     */
    val hasTokens: Boolean
        get() = accessToken != null

    /**
     * Called when API returns 401 Unauthorized.
     * Implementation should trigger token refresh or logout.
     */
    fun onUnauthorized()
}
