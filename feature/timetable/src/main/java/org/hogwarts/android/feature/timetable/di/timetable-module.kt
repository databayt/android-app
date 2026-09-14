package org.hogwarts.android.feature.timetable.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.feature.timetable.data.remote.TimetableApi
import org.hogwarts.android.feature.timetable.data.repository.TimetableRepository
import org.hogwarts.android.feature.timetable.data.repository.TimetableRepositoryImpl
import retrofit2.Retrofit
import java.time.Clock
import javax.inject.Qualifier
import javax.inject.Singleton

/** The device clock the views compare wall-clock periods against; tests pin it. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TimetableClock

@Module
@InstallIn(SingletonComponent::class)
object TimetableModule {
    @Provides
    @Singleton
    fun provideTimetableApi(retrofit: Retrofit): TimetableApi =
        retrofit.create(TimetableApi::class.java)

    @Provides
    @TimetableClock
    fun provideClock(): Clock = Clock.systemDefaultZone()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TimetableBindings {
    @Binds
    @Singleton
    abstract fun bindTimetableRepository(impl: TimetableRepositoryImpl): TimetableRepository
}
