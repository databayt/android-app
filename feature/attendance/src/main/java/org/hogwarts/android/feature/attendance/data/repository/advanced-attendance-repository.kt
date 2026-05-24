package org.hogwarts.android.feature.attendance.data.repository

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

/**
 * Repository interface for EPIC-27: Advanced Attendance data.
 */
interface AdvancedAttendanceRepository {

    // ─── Gamification ────────────────────────────────────────────

    /**
     * Get all badges (earned and available) for a student.
     */
    suspend fun getBadges(studentId: String? = null): List<AttendanceBadge>

    /**
     * Get streak information for a student.
     */
    suspend fun getStreaks(studentId: String? = null): AttendanceStreak

    // ─── Hall Pass ───────────────────────────────────────────────

    /**
     * Request a new hall pass.
     */
    suspend fun requestHallPass(
        studentId: String,
        destination: DestinationType,
        reason: String
    ): HallPass

    /**
     * Approve or deny a hall pass.
     */
    suspend fun approveHallPass(hallPassId: String, approved: Boolean): HallPass

    /**
     * Get hall passes with optional filters.
     */
    suspend fun getHallPasses(
        status: HallPassStatus? = null,
        studentId: String? = null
    ): List<HallPass>

    // ─── Interventions ───────────────────────────────────────────

    /**
     * Get attendance interventions.
     */
    suspend fun getInterventions(
        status: InterventionStatus? = null
    ): List<AttendanceIntervention>

    // ─── Analytics ───────────────────────────────────────────────

    /**
     * Get advanced analytics data.
     */
    suspend fun getAnalytics(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        studentId: String? = null
    ): AdvancedAttendanceAnalytics

    // ─── Methods ─────────────────────────────────────────────────

    /**
     * Get available attendance methods.
     */
    suspend fun getMethods(): List<AttendanceMethod>

    /**
     * Update attendance method configuration.
     */
    suspend fun updateMethods(methods: List<AttendanceMethod>): List<AttendanceMethod>
}
