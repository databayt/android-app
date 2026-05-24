package org.hogwarts.android.feature.library.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.library.data.remote.LibraryApi
import org.hogwarts.android.feature.library.data.repository.LibraryRepository
import org.hogwarts.android.feature.library.data.repository.LibraryRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LibraryModule {

    @Provides
    @Singleton
    fun provideLibraryApi(retrofit: Retrofit): LibraryApi =
        retrofit.create(LibraryApi::class.java)

    @Provides
    @Singleton
    fun provideLibraryRepository(
        api: LibraryApi,
        tenantContext: TenantContext
    ): LibraryRepository = LibraryRepositoryImpl(api, tenantContext)
}
