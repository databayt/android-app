package org.hogwarts.android.feature.fees.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.FeeDao
import org.hogwarts.android.feature.fees.data.remote.FeesApi
import org.hogwarts.android.feature.fees.data.repository.FeesRepository
import org.hogwarts.android.feature.fees.data.repository.FeesRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeesModule {

    @Provides
    @Singleton
    fun provideFeesApi(retrofit: Retrofit): FeesApi =
        retrofit.create(FeesApi::class.java)

    @Provides
    @Singleton
    fun provideFeesRepository(
        api: FeesApi,
        dao: FeeDao,
        tenantContext: TenantContext
    ): FeesRepository = FeesRepositoryImpl(api, dao, tenantContext)
}
