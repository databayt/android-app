package org.hogwarts.android.feature.fees.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun provideFeesApi(retrofit: Retrofit): FeesApi = retrofit.create(FeesApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FeesBindings {
    @Binds
    @Singleton
    abstract fun bindFeesRepository(impl: FeesRepositoryImpl): FeesRepository
}
