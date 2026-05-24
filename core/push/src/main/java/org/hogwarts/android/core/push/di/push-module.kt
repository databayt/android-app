package org.hogwarts.android.core.push.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.push.AnalyticsTracker
import org.hogwarts.android.core.push.CrashReporter
import org.hogwarts.android.core.push.NotificationHandler
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
}
