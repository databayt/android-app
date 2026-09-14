package org.hogwarts.android.feature.exams.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.feature.exams.data.remote.ExamsApi
import org.hogwarts.android.feature.exams.data.repository.ExamsRepository
import org.hogwarts.android.feature.exams.data.repository.ExamsRepositoryImpl
import retrofit2.Retrofit
import java.time.Clock
import javax.inject.Qualifier
import javax.inject.Singleton

/** "Today" for the exams pages, injectable so tests pin the date. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ExamsClock

@Module
@InstallIn(SingletonComponent::class)
object ExamsModule {

    @Provides
    @Singleton
    fun provideExamsApi(retrofit: Retrofit): ExamsApi = retrofit.create(ExamsApi::class.java)

    @Provides
    @ExamsClock
    fun provideExamsClock(): Clock = Clock.systemDefaultZone()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ExamsBindings {
    @Binds
    @Singleton
    abstract fun bindExamsRepository(impl: ExamsRepositoryImpl): ExamsRepository
}
