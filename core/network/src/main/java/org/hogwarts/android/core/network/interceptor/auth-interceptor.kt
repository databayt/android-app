package org.hogwarts.android.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import org.hogwarts.android.core.common.api.TokenProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attaches the JWT Bearer token. Expired tokens are handled by [TokenAuthenticator],
 * which OkHttp invokes on 401.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider.accessToken
        if (token == null || original.header(HEADER_AUTHORIZATION) != null) {
            return chain.proceed(original)
        }
        return chain.proceed(
            original.newBuilder()
                .header(HEADER_AUTHORIZATION, "Bearer $token")
                .build()
        )
    }

    companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
    }
}
