package org.hogwarts.android.feature.timetable.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.TimetableDao
import org.hogwarts.android.feature.timetable.data.remote.TimetableApi
import org.hogwarts.android.feature.timetable.data.repository.TimetableRepository
import org.hogwarts.android.feature.timetable.data.repository.TimetableRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimetableModule {

    @Provides
    @Singleton
    fun provideTimetableApi(retrofit: Retrofit): TimetableApi =
        retrofit.create(TimetableApi::class.java)

    @Provides
    @Singleton
    fun provideTimetableRepository(
        api: TimetableApi,
        dao: TimetableDao,
        tenantContext: TenantContext
    ): TimetableRepository = TimetableRepositoryImpl(api, dao, tenantContext)
}
