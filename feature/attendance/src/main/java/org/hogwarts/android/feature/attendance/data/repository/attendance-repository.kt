package org.hogwarts.android.feature.attendance.data.repository

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.attendance.domain.model.AttendanceRecord
import org.hogwarts.android.feature.attendance.domain.model.AttendanceSummary
import org.hogwarts.android.feature.attendance.domain.model.BulkAttendanceRequest
import org.hogwarts.android.feature.attendance.domain.model.ExcuseRequest
import org.hogwarts.android.feature.attendance.domain.model.MarkAttendanceRequest
import java.time.LocalDate

/**
 * Repository interface for attendance data.
 */
interface AttendanceRepository {

    /**
     * Get attendance history for a student (offline-first).
     */
    fun getStudentAttendance(
        studentId: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Flow<Resource<List<AttendanceRecord>>>

    /**
     * Get attendance for a class on a specific date (offline-first).
     */
    fun getClassAttendance(
        classId: String,
        date: LocalDate
    ): Flow<Resource<List<AttendanceRecord>>>

    /**
     * Get attendance summary for a student.
     */
    suspend fun getAttendanceSummary(studentId: String): AttendanceSummary

    /**
     * Mark attendance for a single student.
     */
    suspend fun markAttendance(request: MarkAttendanceRequest): AttendanceRecord

    /**
     * Mark attendance for an entire class (bulk).
     */
    suspend fun markBulkAttendance(request: BulkAttendanceRequest): List<AttendanceRecord>

    /**
     * Submit an excuse request for a student absence.
     */
    suspend fun submitExcuse(request: ExcuseRequest): ExcuseRequest
}
