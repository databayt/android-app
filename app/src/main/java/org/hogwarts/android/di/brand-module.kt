package org.hogwarts.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.BuildConfig
import org.hogwarts.android.core.common.brand.AppVersionInfo
import org.hogwarts.android.core.common.brand.BrandConfig
import org.hogwarts.android.core.common.time.SchoolZone
import javax.inject.Singleton

/** Feeds the flavor's BuildConfig (the truth for brand, versionCode and applicationId) to library modules. */
@Module
@InstallIn(SingletonComponent::class)
object BrandModule {

    @Provides
    @Singleton
    fun provideBrandConfig(): BrandConfig = BrandConfig(
        school = BuildConfig.BRAND_SCHOOL,
        locked = BuildConfig.BRAND_LOCKED,
        updateUrl = BuildConfig.BRAND_UPDATE_URL,
        apkUrl = BuildConfig.BRAND_APK_URL,
        timezone = BuildConfig.BRAND_TIMEZONE,
    )

    @Provides
    @Singleton
    fun provideSchoolZone(brand: BrandConfig): SchoolZone = SchoolZone(brand.timezone)

    @Provides
    @Singleton
    fun provideAppVersionInfo(): AppVersionInfo =
        AppVersionInfo(versionCode = BuildConfig.VERSION_CODE, applicationId = BuildConfig.APPLICATION_ID)
}
