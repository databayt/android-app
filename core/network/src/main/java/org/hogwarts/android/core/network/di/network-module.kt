package org.hogwarts.android.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.hogwarts.android.core.network.BuildConfig
import org.hogwarts.android.core.network.interceptor.AcceptLanguageInterceptor
import org.hogwarts.android.core.network.interceptor.AuthInterceptor
import org.hogwarts.android.core.network.interceptor.TokenAuthenticator
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        isLenient = true
    }

    /**
     * Debug-only request logging. Auth routes log headers only: their bodies
     * carry passwords, OTP codes and fresh tokens, which must never reach logcat.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        redactHeader("Authorization")
        redactHeader(TokenAuthenticator.HEADER_REFRESH_TOKEN)
    }

    // Tenant is carried by the JWT's schoolId claim; the server ignores any
    // tenant header. Certificate pinning is off until release hardening pins
    // Cloudflare's intermediate/root keys for balqalam.com.
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        acceptLanguageInterceptor: AcceptLanguageInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(acceptLanguageInterceptor)
        .addInterceptor(authBodySafe(loggingInterceptor))
        .authenticator(tokenAuthenticator)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private const val AUTH_PATH_PREFIX = "/api/mobile/auth"

    private fun authBodySafe(logger: HttpLoggingInterceptor): Interceptor {
        val headersOnly = HttpLoggingInterceptor().apply {
            level = if (logger.level == HttpLoggingInterceptor.Level.NONE) {
                HttpLoggingInterceptor.Level.NONE
            } else {
                HttpLoggingInterceptor.Level.HEADERS
            }
            redactHeader("Authorization")
            redactHeader(TokenAuthenticator.HEADER_REFRESH_TOKEN)
        }
        return Interceptor { chain ->
            val path = chain.request().url.encodedPath
            if (path.startsWith(AUTH_PATH_PREFIX)) headersOnly.intercept(chain) else logger.intercept(chain)
        }
    }
}
