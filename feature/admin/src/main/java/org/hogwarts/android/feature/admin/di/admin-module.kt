package org.hogwarts.android.feature.admin.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.admin.data.remote.AdminApi
import org.hogwarts.android.feature.admin.data.repository.AdminRepository
import org.hogwarts.android.feature.admin.data.repository.AdminRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdminModule {

    @Provides
    @Singleton
    fun provideAdminApi(retrofit: Retrofit): AdminApi =
        retrofit.create(AdminApi::class.java)

    @Provides
    @Singleton
    fun provideAdminRepository(
        api: AdminApi,
        tenantContext: TenantContext
    ): AdminRepository = AdminRepositoryImpl(api, tenantContext)
}
