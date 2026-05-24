package org.hogwarts.android.feature.stream.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.CourseDao
import org.hogwarts.android.feature.stream.data.local.StreamDemoSeeder
import org.hogwarts.android.feature.stream.data.remote.StreamApi
import org.hogwarts.android.feature.stream.data.repository.StreamRepository
import org.hogwarts.android.feature.stream.data.repository.StreamRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StreamModule {

    @Provides
    @Singleton
    fun provideStreamApi(retrofit: Retrofit): StreamApi =
        retrofit.create(StreamApi::class.java)

    @Provides
    @Singleton
    fun provideStreamRepository(
        api: StreamApi,
        tenantContext: TenantContext,
        courseDao: CourseDao,
        demoSeeder: StreamDemoSeeder
    ): StreamRepository = StreamRepositoryImpl(api, tenantContext, courseDao, demoSeeder)
}
