package org.hogwarts.android.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import org.hogwarts.android.core.common.brand.AppVersionInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tells the backend which build is calling (`X-App-Version` = versionCode, `X-App-Package` =
 * applicationId) so it can see who is behind on updates. The values come from the app module's
 * BuildConfig via [AppVersionInfo]; this library's own BuildConfig does not carry them.
 */
@Singleton
class AppVersionInterceptor @Inject constructor(
    private val info: AppVersionInfo,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("X-App-Version", info.versionCode.toString())
            .header("X-App-Package", info.applicationId)
            .build()
        return chain.proceed(request)
    }
}
