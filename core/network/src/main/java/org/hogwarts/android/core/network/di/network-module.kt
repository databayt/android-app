package org.hogwarts.android.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.hogwarts.android.core.network.BuildConfig
import org.hogwarts.android.core.network.interceptor.AcceptLanguageInterceptor
import org.hogwarts.android.core.network.interceptor.AuthInterceptor
import org.hogwarts.android.core.network.interceptor.TenantInterceptor
import org.hogwarts.android.core.network.security.SecurityConfig
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://ed.databayt.org/"
    private const val TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        // Debug builds skip pinning for development flexibility
        if (BuildConfig.DEBUG) {
            return CertificatePinner.Builder().build()
        }
        // Release builds enforce certificate pinning with TLS 1.2+
        return CertificatePinner.Builder()
            .add(SecurityConfig.API_HOST_PATTERN, SecurityConfig.PRIMARY_PIN)
            .add(SecurityConfig.API_HOST_PATTERN, SecurityConfig.BACKUP_PIN)
            .build()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tenantInterceptor: TenantInterceptor,
        acceptLanguageInterceptor: AcceptLanguageInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        certificatePinner: CertificatePinner
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(tenantInterceptor)
        .addInterceptor(acceptLanguageInterceptor)
        .addInterceptor(loggingInterceptor)
        .certificatePinner(certificatePinner)
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
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
