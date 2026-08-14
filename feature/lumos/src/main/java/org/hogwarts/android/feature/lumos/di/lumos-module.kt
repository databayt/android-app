package org.hogwarts.android.feature.lumos.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.feature.lumos.data.remote.LumosApi
import org.hogwarts.android.feature.lumos.data.repository.LumosRepository
import org.hogwarts.android.feature.lumos.data.repository.LumosRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LumosModule {

    @Binds
    @Singleton
    abstract fun bindLumosRepository(
        lumosRepositoryImpl: LumosRepositoryImpl
    ): LumosRepository

    companion object {
        @Provides
        @Singleton
        fun provideLumosApi(retrofit: Retrofit): LumosApi {
            return retrofit.create(LumosApi::class.java)
        }
    }
}
