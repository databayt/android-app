package org.hogwarts.android.feature.events.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.events.data.remote.EventsApi
import org.hogwarts.android.feature.events.data.repository.EventsRepository
import org.hogwarts.android.feature.events.data.repository.EventsRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EventsModule {

    @Provides
    @Singleton
    fun provideEventsApi(retrofit: Retrofit): EventsApi =
        retrofit.create(EventsApi::class.java)

    @Provides
    @Singleton
    fun provideEventsRepository(
        api: EventsApi,
        tenantContext: TenantContext
    ): EventsRepository = EventsRepositoryImpl(api, tenantContext)
}
