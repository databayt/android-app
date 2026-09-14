package org.hogwarts.android.feature.auth.data.repository

import kotlinx.serialization.json.Json
import org.hogwarts.android.feature.auth.data.remote.dto.ApiErrorDto
import org.hogwarts.android.feature.auth.domain.model.AuthError

/** Which route answered — the same status means different things per route. */
enum class AuthEndpoint { Login, Social, Reset, NewPassword }

private val errorJson = Json { ignoreUnknownKeys = true; isLenient = true }

/** The `error` string of a `{ error }` body, if it parses. */
internal fun parseErrorMessage(body: String?): String? =
    body?.takeIf { it.isNotBlank() }?.let {
        runCatching { errorJson.decodeFromString(ApiErrorDto.serializer(), it).error }.getOrNull()
    }

/**
 * Status + `{ error }` → [AuthError], read against the route sources:
 * - `POST auth` 401 "Invalid email or password" | "Please login with your OAuth provider…", 403 "Account is suspended"
 * - `POST auth/google` 401 bad token / unverified email, 403 suspended
 * - `POST auth/new-password` 401 "Invalid or expired…" | "…has expired…", 403 protected account
 * - any route 429 from the rate limiter
 */
fun authErrorFor(endpoint: AuthEndpoint, status: Int, message: String?): AuthError {
    val text = message.orEmpty().lowercase()
    return when (status) {
        429 -> AuthError.TooManyRequests
        404 -> AuthError.AccountNotFound
        403 -> if (endpoint == AuthEndpoint.NewPassword) AuthError.PermissionDenied else AuthError.Suspended
        401 -> when (endpoint) {
            AuthEndpoint.Login ->
                if ("oauth" in text || "provider" in text) AuthError.SocialOnly else AuthError.InvalidCredentials
            AuthEndpoint.NewPassword ->
                if ("has expired" in text) AuthError.CodeExpired else AuthError.InvalidCode
            AuthEndpoint.Social, AuthEndpoint.Reset -> AuthError.Generic
        }
        400 -> when (endpoint) {
            AuthEndpoint.Login -> AuthError.InvalidCredentials
            AuthEndpoint.NewPassword -> AuthError.InvalidCode
            AuthEndpoint.Social, AuthEndpoint.Reset -> AuthError.Generic
        }
        else -> AuthError.Generic
    }
}
