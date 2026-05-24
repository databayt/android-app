package org.hogwarts.android.feature.guardian.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.guardian.data.remote.GuardianApi
import org.hogwarts.android.feature.guardian.data.repository.GuardianRepository
import org.hogwarts.android.feature.guardian.data.repository.GuardianRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GuardianModule {

    @Provides
    @Singleton
    fun provideGuardianApi(retrofit: Retrofit): GuardianApi =
        retrofit.create(GuardianApi::class.java)

    @Provides
    @Singleton
    fun provideGuardianRepository(
        api: GuardianApi,
        tenantContext: TenantContext
    ): GuardianRepository = GuardianRepositoryImpl(api, tenantContext)
}
