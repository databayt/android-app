package org.hogwarts.android.feature.attendance.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.AttendanceDao
import org.hogwarts.android.feature.attendance.data.remote.AdvancedAttendanceApi
import org.hogwarts.android.feature.attendance.data.remote.AttendanceApi
import org.hogwarts.android.feature.attendance.data.repository.AdvancedAttendanceRepository
import org.hogwarts.android.feature.attendance.data.repository.AdvancedAttendanceRepositoryImpl
import org.hogwarts.android.feature.attendance.data.repository.AttendanceRepository
import org.hogwarts.android.feature.attendance.data.repository.AttendanceRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AttendanceModule {

    @Provides
    @Singleton
    fun provideAttendanceApi(retrofit: Retrofit): AttendanceApi =
        retrofit.create(AttendanceApi::class.java)

    @Provides
    @Singleton
    fun provideAttendanceRepository(
        api: AttendanceApi,
        dao: AttendanceDao,
        tenantContext: TenantContext
    ): AttendanceRepository = AttendanceRepositoryImpl(api, dao, tenantContext)

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
