package org.hogwarts.android.feature.subjects.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.subjects.data.remote.SubjectsApi
import org.hogwarts.android.feature.subjects.data.repository.SubjectsRepository
import org.hogwarts.android.feature.subjects.data.repository.SubjectsRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SubjectsModule {

    @Provides
    @Singleton
    fun provideSubjectsApi(retrofit: Retrofit): SubjectsApi =
        retrofit.create(SubjectsApi::class.java)

    @Provides
    @Singleton
    fun provideSubjectsRepository(
        api: SubjectsApi,
        tenantContext: TenantContext
    ): SubjectsRepository = SubjectsRepositoryImpl(api, tenantContext)
}
