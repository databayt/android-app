package org.hogwarts.android.feature.live.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.feature.live.data.remote.LiveApi
import org.hogwarts.android.feature.live.data.repository.LiveRepository
import org.hogwarts.android.feature.live.data.repository.LiveRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LiveModule {

    @Provides
    @Singleton
    fun provideLiveApi(retrofit: Retrofit): LiveApi = retrofit.create(LiveApi::class.java)

    @Provides
    @Singleton
    fun provideLiveRepository(api: LiveApi): LiveRepository = LiveRepositoryImpl(api)
}
