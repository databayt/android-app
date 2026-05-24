package org.hogwarts.android.feature.attendance.domain.usecase

import org.hogwarts.android.feature.attendance.data.repository.AdvancedAttendanceRepository
import org.hogwarts.android.feature.attendance.domain.model.AttendanceMethod
import javax.inject.Inject

/**
 * Use case to get available attendance capture methods.
 */
class GetMethodsUseCase @Inject constructor(
    private val repository: AdvancedAttendanceRepository
) {
    suspend operator fun invoke(): List<AttendanceMethod> =
        repository.getMethods()
}

/**
 * Use case to update attendance method configuration.
 */
class UpdateMethodsUseCase @Inject constructor(
    private val repository: AdvancedAttendanceRepository
) {
    suspend operator fun invoke(methods: List<AttendanceMethod>): List<AttendanceMethod> =
        repository.updateMethods(methods)
}
