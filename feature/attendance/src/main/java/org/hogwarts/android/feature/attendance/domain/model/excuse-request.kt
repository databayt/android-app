package org.hogwarts.android.feature.attendance.domain.model

import java.time.LocalDate

data class ExcuseRequest(
    val id: String = "",
    val studentId: String,
    val date: LocalDate,
    val reason: String,
    val status: ExcuseStatus = ExcuseStatus.PENDING,
    val reviewedBy: String? = null,
    val reviewNote: String? = null
)

enum class ExcuseStatus {
    PENDING, APPROVED, REJECTED
}
