package org.hogwarts.android.core.network.translation

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface for the mobile translation endpoint.
 *
 * Lives in `:core:network` (cross-cutting) rather than a feature module
 * because any feature surfacing user-visible content might call it —
 * announcements, assignments, and (post-`Message.lang`-migration)
 * messages.
 *
 * AuthInterceptor adds Authorization automatically; TenantInterceptor
 * adds X-School-Id. Callers never pass them.
 */
interface TranslationApi {

    @POST("api/mobile/translate")
    suspend fun translate(@Body request: TranslateRequest): Response<TranslateResponse>
}
