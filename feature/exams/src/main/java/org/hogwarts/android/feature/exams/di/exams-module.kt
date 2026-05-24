package org.hogwarts.android.feature.exams.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.ExamDao
import org.hogwarts.android.feature.exams.data.remote.AdvancedExamsApi
import org.hogwarts.android.feature.exams.data.remote.ExamsApi
import org.hogwarts.android.feature.exams.data.repository.ExamsRepository
import org.hogwarts.android.feature.exams.data.repository.ExamsRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExamsModule {

    @Provides
    @Singleton
    fun provideExamsApi(retrofit: Retrofit): ExamsApi =
        retrofit.create(ExamsApi::class.java)

    @Provides
    @Singleton
    fun provideAdvancedExamsApi(retrofit: Retrofit): AdvancedExamsApi =
        retrofit.create(AdvancedExamsApi::class.java)

    @Provides
    @Singleton
    fun provideExamsRepository(
        api: ExamsApi,
        dao: ExamDao,
        tenantContext: TenantContext
    ): ExamsRepository = ExamsRepositoryImpl(api, dao, tenantContext)
}
