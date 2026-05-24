package org.hogwarts.android.feature.notifications.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.NotificationDao
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

    @Provides
    @Singleton
    fun provideNotificationsRepository(
        api: NotificationsApi,
        dao: NotificationDao,
        tenantContext: TenantContext
    ): NotificationsRepository = NotificationsRepositoryImpl(api, dao, tenantContext)
}
