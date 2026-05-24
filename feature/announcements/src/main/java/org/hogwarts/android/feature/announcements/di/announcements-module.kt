package org.hogwarts.android.feature.announcements.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.feature.announcements.data.remote.AnnouncementsApi
import org.hogwarts.android.feature.announcements.data.repository.AnnouncementsRepository
import org.hogwarts.android.feature.announcements.data.repository.AnnouncementsRepositoryImpl
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.AnnouncementDao
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnnouncementsModule {

    @Provides
    @Singleton
    fun provideAnnouncementsApi(retrofit: Retrofit): AnnouncementsApi =
        retrofit.create(AnnouncementsApi::class.java)

    @Provides
    @Singleton
    fun provideAnnouncementsRepository(
        api: AnnouncementsApi,
        dao: AnnouncementDao,
        tenantContext: TenantContext
    ): AnnouncementsRepository = AnnouncementsRepositoryImpl(api, dao, tenantContext)
}
