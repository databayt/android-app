package org.hogwarts.android.feature.admission.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.admission.data.remote.AdmissionApi
import org.hogwarts.android.feature.admission.data.repository.AdmissionRepository
import org.hogwarts.android.feature.admission.data.repository.AdmissionRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdmissionModule {

    @Provides
    @Singleton
    fun provideAdmissionApi(retrofit: Retrofit): AdmissionApi =
        retrofit.create(AdmissionApi::class.java)

    @Provides
    @Singleton
    fun provideAdmissionRepository(
        api: AdmissionApi,
        tenantContext: TenantContext
    ): AdmissionRepository = AdmissionRepositoryImpl(api, tenantContext)
}
