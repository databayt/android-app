package org.hogwarts.android.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import org.hogwarts.android.core.common.api.TokenProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that adds JWT Bearer token to all API requests.
 *
 * The token is retrieved from TokenProvider (implemented in security module).
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header for auth endpoints
        if (originalRequest.url.encodedPath.contains("/auth/")) {
            return chain.proceed(originalRequest)
        }

        val token = tokenProvider.accessToken

        val request = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(request)

        // Handle 401 Unauthorized - trigger token refresh or logout
        if (response.code == 401) {
            tokenProvider.onUnauthorized()
        }

        return response
    }
}
