package org.hogwarts.android.feature.dashboard.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.feature.dashboard.data.remote.DashboardApi
import org.hogwarts.android.feature.dashboard.data.repository.DashboardRepository
import org.hogwarts.android.feature.dashboard.data.repository.DashboardRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DashboardModule {
    @Provides
    @Singleton
    fun provideDashboardApi(retrofit: Retrofit): DashboardApi =
        retrofit.create(DashboardApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardBindings {
    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}
