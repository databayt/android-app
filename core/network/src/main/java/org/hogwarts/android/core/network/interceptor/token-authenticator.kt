package org.hogwarts.android.core.network.interceptor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.hogwarts.android.core.common.api.TokenProvider
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Refreshes the access token when the API answers 401, then retries once.
 *
 * Concurrent 401s serialize on [lock]; a request whose token was already
 * replaced by another thread is retried with the new token without a second
 * refresh. Refresh uses `PUT /api/mobile/auth` with `X-Refresh-Token` on a bare
 * client (no interceptors, so no recursion). The session ends only when the
 * server rejects the refresh token — a network failure keeps the user signed in.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val json: Json
) : Authenticator {

    private val lock = Any()

    private val refreshClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        val request = response.request
        // Login/social/OTP 401s mean bad credentials, not an expired token.
        if (request.url.encodedPath.startsWith(AUTH_PATH)) return null
        // Already retried once with a fresh token.
        if (response.priorResponse != null) return null

        val sentToken = request.header(AuthInterceptor.HEADER_AUTHORIZATION)
            ?.removePrefix("Bearer ")

        synchronized(lock) {
            val current = tokenProvider.accessToken
            if (current != null && current != sentToken) {
                return request.withToken(current)
            }

            val refreshToken = tokenProvider.refreshToken
            if (refreshToken.isNullOrEmpty()) {
                tokenProvider.onSessionExpired()
                return null
            }

            return when (val result = refresh(request, refreshToken)) {
                is RefreshResult.Success -> {
                    tokenProvider.saveRefreshedTokens(
                        accessToken = result.body.accessToken,
                        refreshToken = result.body.refreshToken,
                        expiresAtMillis = result.body.expiresAt
                    )
                    request.withToken(result.body.accessToken)
                }
                RefreshResult.Rejected -> {
                    tokenProvider.onSessionExpired()
                    null
                }
                RefreshResult.Unavailable -> null
            }
        }
    }

    private fun refresh(failed: Request, refreshToken: String): RefreshResult {
        val url = failed.url.newBuilder()
            .encodedPath(AUTH_PATH)
            .query(null)
            .build()
        val call = Request.Builder()
            .url(url)
            .header(HEADER_REFRESH_TOKEN, refreshToken)
            .put(ByteArray(0).toRequestBody())
            .build()

        return try {
            refreshClient.newCall(call).execute().use { res ->
                when {
                    res.isSuccessful -> {
                        val body = res.body?.string()
                            ?: return RefreshResult.Unavailable
                        RefreshResult.Success(json.decodeFromString<RefreshResponse>(body))
                    }
                    res.code == 401 || res.code == 403 -> RefreshResult.Rejected
                    else -> RefreshResult.Unavailable
                }
            }
        } catch (e: IOException) {
            Timber.w(e, "Token refresh failed: network")
            RefreshResult.Unavailable
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Token refresh failed: unreadable response")
            RefreshResult.Unavailable
        }
    }

    private fun Request.withToken(token: String): Request =
        newBuilder().header(AuthInterceptor.HEADER_AUTHORIZATION, "Bearer $token").build()

    private sealed interface RefreshResult {
        data class Success(val body: RefreshResponse) : RefreshResult
        data object Rejected : RefreshResult
        data object Unavailable : RefreshResult
    }

    @Serializable
    private data class RefreshResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("refresh_token") val refreshToken: String? = null,
        @SerialName("expires_at") val expiresAt: Long
    )

    companion object {
        const val AUTH_PATH = "/api/mobile/auth"
        const val HEADER_REFRESH_TOKEN = "X-Refresh-Token"
    }
}
