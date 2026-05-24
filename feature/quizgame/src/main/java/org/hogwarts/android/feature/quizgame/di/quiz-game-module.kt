package org.hogwarts.android.feature.quizgame.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.quizgame.data.remote.QuizGameApi
import org.hogwarts.android.feature.quizgame.data.repository.QuizGameRepository
import org.hogwarts.android.feature.quizgame.data.repository.QuizGameRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QuizGameModule {

    @Provides
    @Singleton
    fun provideQuizGameApi(retrofit: Retrofit): QuizGameApi =
        retrofit.create(QuizGameApi::class.java)

    @Provides
    @Singleton
    fun provideQuizGameRepository(
        api: QuizGameApi,
        tenantContext: TenantContext
    ): QuizGameRepository = QuizGameRepositoryImpl(api, tenantContext)
}
