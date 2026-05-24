package org.hogwarts.android.feature.grades.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.GradeDao
import org.hogwarts.android.feature.grades.data.remote.GradesApi
import org.hogwarts.android.feature.grades.data.repository.GradesRepository
import org.hogwarts.android.feature.grades.data.repository.GradesRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GradesModule {

    @Provides
    @Singleton
    fun provideGradesApi(retrofit: Retrofit): GradesApi =
        retrofit.create(GradesApi::class.java)

    @Provides
    @Singleton
    fun provideGradesRepository(
        api: GradesApi,
        dao: GradeDao,
        tenantContext: TenantContext
    ): GradesRepository = GradesRepositoryImpl(api, dao, tenantContext)
}
