package org.hogwarts.android.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that adds X-School-Id header for multi-tenant API calls.
 *
 * CRITICAL: This ensures all API requests are scoped to the current tenant.
 */
@Singleton
class TenantInterceptor @Inject constructor(
    private val tenantProvider: TenantProvider
) : Interceptor {

    companion object {
        const val HEADER_SCHOOL_ID = "X-School-Id"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip tenant header for auth endpoints
        if (originalRequest.url.encodedPath.contains("/auth/")) {
            return chain.proceed(originalRequest)
        }

        val schoolId = tenantProvider.schoolId

        val request = if (schoolId != null) {
            originalRequest.newBuilder()
                .addHeader(HEADER_SCHOOL_ID, schoolId)
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}

/**
 * Interface for providing tenant context.
 * Implemented by TenantContext in data module.
 */
interface TenantProvider {
    val schoolId: String?
}
