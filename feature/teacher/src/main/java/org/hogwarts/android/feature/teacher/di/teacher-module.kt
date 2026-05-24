package org.hogwarts.android.feature.teacher.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.teacher.data.remote.TeacherApi
import org.hogwarts.android.feature.teacher.data.repository.TeacherRepository
import org.hogwarts.android.feature.teacher.data.repository.TeacherRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TeacherModule {

    @Provides
    @Singleton
    fun provideTeacherApi(retrofit: Retrofit): TeacherApi =
        retrofit.create(TeacherApi::class.java)

    @Provides
    @Singleton
    fun provideTeacherRepository(
        api: TeacherApi,
        tenantContext: TenantContext
    ): TeacherRepository = TeacherRepositoryImpl(api, tenantContext)
}
