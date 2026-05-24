package org.hogwarts.android.feature.lessons.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.lessons.data.remote.LessonsApi
import org.hogwarts.android.feature.lessons.data.repository.LessonsRepository
import org.hogwarts.android.feature.lessons.data.repository.LessonsRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LessonsModule {

    @Provides
    @Singleton
    fun provideLessonsApi(retrofit: Retrofit): LessonsApi =
        retrofit.create(LessonsApi::class.java)

    @Provides
    @Singleton
    fun provideLessonsRepository(
        api: LessonsApi,
        tenantContext: TenantContext
    ): LessonsRepository = LessonsRepositoryImpl(api, tenantContext)
}
