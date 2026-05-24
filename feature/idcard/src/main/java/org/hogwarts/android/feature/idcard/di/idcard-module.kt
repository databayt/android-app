package org.hogwarts.android.feature.idcard.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.idcard.data.remote.IdCardApi
import org.hogwarts.android.feature.idcard.data.repository.IdCardRepository
import org.hogwarts.android.feature.idcard.data.repository.IdCardRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IdCardModule {

    @Provides
    @Singleton
    fun provideIdCardApi(retrofit: Retrofit): IdCardApi =
        retrofit.create(IdCardApi::class.java)

    @Provides
    @Singleton
    fun provideIdCardRepository(
        api: IdCardApi,
        tenantContext: TenantContext
    ): IdCardRepository = IdCardRepositoryImpl(api, tenantContext)
}
