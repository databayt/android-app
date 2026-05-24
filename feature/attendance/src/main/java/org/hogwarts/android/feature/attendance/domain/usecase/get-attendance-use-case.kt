package org.hogwarts.android.feature.attendance.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.attendance.data.repository.AttendanceRepository
import org.hogwarts.android.feature.attendance.domain.model.AttendanceRecord
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case to get attendance records for a student.
 */
class GetAttendanceUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    operator fun invoke(
        studentId: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Flow<Resource<List<AttendanceRecord>>> =
        repository.getStudentAttendance(studentId, startDate, endDate)
}
