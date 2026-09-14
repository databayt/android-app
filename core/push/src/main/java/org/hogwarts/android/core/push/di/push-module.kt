package org.hogwarts.android.core.push.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.push.AnalyticsTracker
import org.hogwarts.android.core.push.CrashReporter
import org.hogwarts.android.core.push.DeviceTokenApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PushModule {

    @Provides
    @Singleton
    fun provideAnalyticsTracker(): AnalyticsTracker = AnalyticsTracker()

    @Provides
    @Singleton
    fun provideCrashReporter(): CrashReporter = CrashReporter()

    @Provides
    @Singleton
    fun provideDeviceTokenApi(retrofit: Retrofit): DeviceTokenApi =
        retrofit.create(DeviceTokenApi::class.java)
}
