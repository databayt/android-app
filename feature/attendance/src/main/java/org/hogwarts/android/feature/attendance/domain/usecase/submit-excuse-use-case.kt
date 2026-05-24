package org.hogwarts.android.feature.attendance.domain.usecase

import org.hogwarts.android.feature.attendance.domain.model.ExcuseRequest
import org.hogwarts.android.feature.attendance.data.repository.AttendanceRepository
import javax.inject.Inject

class SubmitExcuseUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(request: ExcuseRequest): Result<ExcuseRequest> {
        return try {
            val result = repository.submitExcuse(request)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
