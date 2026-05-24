package org.hogwarts.android.feature.attendance.data.repository

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.attendance.data.remote.AdvancedAttendanceApi
import org.hogwarts.android.feature.attendance.data.remote.dto.HallPassRequestDto
import org.hogwarts.android.feature.attendance.data.remote.dto.UpdateMethodsRequestDto
import org.hogwarts.android.feature.attendance.data.remote.dto.toDomain
import org.hogwarts.android.feature.attendance.data.remote.dto.toDto
import org.hogwarts.android.feature.attendance.domain.model.AdvancedAttendanceAnalytics
import org.hogwarts.android.feature.attendance.domain.model.AttendanceBadge
import org.hogwarts.android.feature.attendance.domain.model.AttendanceIntervention
import org.hogwarts.android.feature.attendance.domain.model.AttendanceMethod
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStreak
import org.hogwarts.android.feature.attendance.domain.model.DestinationType
import org.hogwarts.android.feature.attendance.domain.model.HallPass
import org.hogwarts.android.feature.attendance.domain.model.HallPassStatus
import org.hogwarts.android.feature.attendance.domain.model.InterventionStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AdvancedAttendanceRepository].
 *
 * All API calls are scoped by schoolId via TenantInterceptor header.
 * TenantContext is validated to ensure multi-tenant isolation.
 */
@Singleton
class AdvancedAttendanceRepositoryImpl @Inject constructor(
    private val api: AdvancedAttendanceApi,
    private val tenantContext: TenantContext
) : AdvancedAttendanceRepository {

    // ─── Gamification ────────────────────────────────────────────

    override suspend fun getBadges(studentId: String?): List<AttendanceBadge> {
        tenantContext.requireSchoolId()
        val response = api.getBadges(studentId = studentId)
        val body = response.body() ?: throw Exception("Failed to load badges")
        return body.data.map { it.toDomain() }
    }

    override suspend fun getStreaks(studentId: String?): AttendanceStreak {
        tenantContext.requireSchoolId()
        val response = api.getStreaks(studentId = studentId)
        val body = response.body() ?: throw Exception("Failed to load streaks")
        return body.toDomain()
    }

    // ─── Hall Pass ───────────────────────────────────────────────

    override suspend fun requestHallPass(
        studentId: String,
        destination: DestinationType,
        reason: String
    ): HallPass {
        tenantContext.requireSchoolId()
        val response = api.requestHallPass(
            HallPassRequestDto(
                studentId = studentId,
                destination = destination.name,
                reason = reason
            )
        )
        val body = response.body() ?: throw Exception("Failed to request hall pass")
        return body.toDomain()
    }

    override suspend fun approveHallPass(hallPassId: String, approved: Boolean): HallPass {
        tenantContext.requireSchoolId()
        val response = api.approveHallPass(hallPassId, approved)
        val body = response.body() ?: throw Exception("Failed to approve hall pass")
        return body.toDomain()
    }

    override suspend fun getHallPasses(
        status: HallPassStatus?,
        studentId: String?
    ): List<HallPass> {
        tenantContext.requireSchoolId()
        val response = api.getHallPasses(
            status = status?.name,
            studentId = studentId
        )
        val body = response.body() ?: throw Exception("Failed to load hall passes")
        return body.data.map { it.toDomain() }
    }

    // ─── Interventions ───────────────────────────────────────────

    override suspend fun getInterventions(
        status: InterventionStatus?
    ): List<AttendanceIntervention> {
        tenantContext.requireSchoolId()
        val response = api.getInterventions(status = status?.name)
        val body = response.body() ?: throw Exception("Failed to load interventions")
        return body.data.map { it.toDomain() }
    }

    // ─── Analytics ───────────────────────────────────────────────

    override suspend fun getAnalytics(
        startDate: LocalDate?,
        endDate: LocalDate?,
        studentId: String?
    ): AdvancedAttendanceAnalytics {
        tenantContext.requireSchoolId()
        val response = api.getAnalytics(
            startDate = startDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            endDate = endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            studentId = studentId
        )
        val body = response.body() ?: throw Exception("Failed to load analytics")
        return body.toDomain()
    }

    // ─── Methods ─────────────────────────────────────────────────

    override suspend fun getMethods(): List<AttendanceMethod> {
        tenantContext.requireSchoolId()
        val response = api.getMethods()
        val body = response.body() ?: throw Exception("Failed to load methods")
        return body.data.map { it.toDomain() }
    }

    override suspend fun updateMethods(methods: List<AttendanceMethod>): List<AttendanceMethod> {
        tenantContext.requireSchoolId()
        val response = api.updateMethods(
            UpdateMethodsRequestDto(methods = methods.map { it.toDto() })
        )
        val body = response.body() ?: throw Exception("Failed to update methods")
        return body.data.map { it.toDomain() }
    }
}
