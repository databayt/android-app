package org.hogwarts.android.feature.attendance.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.attendance.data.outbox.AttendanceSyncScheduler
import org.hogwarts.android.feature.attendance.data.outbox.WorkManagerAttendanceSyncScheduler
import org.hogwarts.android.feature.attendance.data.remote.AdvancedAttendanceApi
import org.hogwarts.android.feature.attendance.data.remote.AttendanceApi
import org.hogwarts.android.feature.attendance.data.repository.AdvancedAttendanceRepository
import org.hogwarts.android.feature.attendance.data.repository.AdvancedAttendanceRepositoryImpl
import org.hogwarts.android.feature.attendance.data.repository.AttendanceRepository
import org.hogwarts.android.feature.attendance.data.repository.AttendanceRepositoryImpl
import retrofit2.Retrofit
import java.time.Clock
import javax.inject.Qualifier
import javax.inject.Singleton

/** The wall clock the attendance landing reads "today" and "now" from. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AttendanceClock

@Module
@InstallIn(SingletonComponent::class)
object AttendanceModule {

    @Provides
    @Singleton
    fun provideAttendanceApi(retrofit: Retrofit): AttendanceApi =
        retrofit.create(AttendanceApi::class.java)

    @Provides
    @AttendanceClock
    fun provideAttendanceClock(): Clock = Clock.systemDefaultZone()

    // ─── EPIC-27: Advanced Attendance ────────────────────────────

    @Provides
    @Singleton
    fun provideAdvancedAttendanceApi(retrofit: Retrofit): AdvancedAttendanceApi =
        retrofit.create(AdvancedAttendanceApi::class.java)

    @Provides
    @Singleton
    fun provideAdvancedAttendanceRepository(
        api: AdvancedAttendanceApi,
        tenantContext: TenantContext
    ): AdvancedAttendanceRepository = AdvancedAttendanceRepositoryImpl(api, tenantContext)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AttendanceBindings {

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository

    @Binds
    abstract fun bindAttendanceSyncScheduler(impl: WorkManagerAttendanceSyncScheduler): AttendanceSyncScheduler
}
