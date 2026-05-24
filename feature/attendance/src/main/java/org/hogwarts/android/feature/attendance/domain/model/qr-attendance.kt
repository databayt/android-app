package org.hogwarts.android.feature.attendance.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class QrAttendancePayload(
    val classId: String,
    val className: String,
    val date: LocalDate,
    val validUntil: LocalTime,
    val token: String
) {
    fun isValid(): Boolean {
        val now = LocalTime.now()
        return date == LocalDate.now() && now.isBefore(validUntil)
    }
}

data class QrScanResult(
    val success: Boolean,
    val payload: QrAttendancePayload? = null,
    val error: String? = null
) {
    companion object {
        fun success(payload: QrAttendancePayload) = QrScanResult(success = true, payload = payload)
        fun error(message: String) = QrScanResult(success = false, error = message)
    }
}
