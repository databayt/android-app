package org.hogwarts.android.feature.notifications.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.feature.notifications.data.remote.NotificationsApi
import org.hogwarts.android.feature.notifications.data.repository.NotificationsRepository
import org.hogwarts.android.feature.notifications.data.repository.NotificationsRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationsModule {
    @Provides
    @Singleton
    fun provideNotificationsApi(retrofit: Retrofit): NotificationsApi =
        retrofit.create(NotificationsApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsBindings {
    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(impl: NotificationsRepositoryImpl): NotificationsRepository
}
