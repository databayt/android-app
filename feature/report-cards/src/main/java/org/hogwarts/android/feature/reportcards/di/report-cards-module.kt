package org.hogwarts.android.feature.reportcards.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.reportcards.data.remote.ReportCardsApi
import org.hogwarts.android.feature.reportcards.data.repository.ReportCardsRepository
import org.hogwarts.android.feature.reportcards.data.repository.ReportCardsRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReportCardsModule {

    @Provides
    @Singleton
    fun provideReportCardsApi(retrofit: Retrofit): ReportCardsApi =
        retrofit.create(ReportCardsApi::class.java)

    @Provides
    @Singleton
    fun provideReportCardsRepository(
        api: ReportCardsApi,
        tenantContext: TenantContext
    ): ReportCardsRepository = ReportCardsRepositoryImpl(api, tenantContext)
}
