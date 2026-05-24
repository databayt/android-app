package org.hogwarts.android.feature.profile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.profile.data.remote.ProfileApi
import org.hogwarts.android.feature.profile.data.repository.ProfileRepository
import org.hogwarts.android.feature.profile.data.repository.ProfileRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi =
        retrofit.create(ProfileApi::class.java)

    @Provides
    @Singleton
    fun provideProfileRepository(
        api: ProfileApi,
        tenantContext: TenantContext
    ): ProfileRepository = ProfileRepositoryImpl(api, tenantContext)
}
