package org.hogwarts.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.feature.auth.data.remote.AuthApi
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt module for API service dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    // Add more API providers as features are implemented
    // @Provides
    // @Singleton
    // fun provideStudentApi(retrofit: Retrofit): StudentApi =
    //     retrofit.create(StudentApi::class.java)
}
