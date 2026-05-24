package org.hogwarts.android.core.network.interceptor

import androidx.appcompat.app.AppCompatDelegate
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Forwards the user's per-app locale to the backend so server-rendered content
 * (emails, push payloads, validation messages) matches the in-app language.
 *
 * The locale comes from [AppCompatDelegate.getApplicationLocales], which is
 * the source of truth for the per-app language preference (the platform
 * `LocaleManager` on Android 13+, AppCompat-managed storage on older releases).
 */
@Singleton
class AcceptLanguageInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val tags = AppCompatDelegate.getApplicationLocales()
            .toLanguageTags()
            .ifEmpty { "en" }

        val request = chain.request().newBuilder()
            .header("Accept-Language", tags)
            .build()

        return chain.proceed(request)
    }
}
