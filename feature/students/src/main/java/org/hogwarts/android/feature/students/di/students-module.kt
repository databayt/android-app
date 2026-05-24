package org.hogwarts.android.feature.students.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.StudentDao
import org.hogwarts.android.feature.students.data.remote.StudentsApi
import org.hogwarts.android.feature.students.data.repository.StudentsRepository
import org.hogwarts.android.feature.students.data.repository.StudentsRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StudentsModule {

    @Provides
    @Singleton
    fun provideStudentsApi(retrofit: Retrofit): StudentsApi =
        retrofit.create(StudentsApi::class.java)

    @Provides
    @Singleton
    fun provideStudentsRepository(
        api: StudentsApi,
        dao: StudentDao,
        tenantContext: TenantContext
    ): StudentsRepository = StudentsRepositoryImpl(api, dao, tenantContext)
}
