package org.hogwarts.android.feature.attendance.ui

import org.hogwarts.android.feature.attendance.domain.model.AttendanceMethod
import org.hogwarts.android.feature.attendance.domain.model.MethodType

/**
 * UI state for the Attendance Method Selector screen.
 */
data class AttendanceMethodSelectorUiState(
    val isLoading: Boolean = true,
    val methods: List<AttendanceMethod> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val permissionStatuses: Map<MethodType, PermissionStatus> = emptyMap()
) {
    val defaultMethod: AttendanceMethod?
        get() = methods.find { it.isDefault }

    val enabledMethods: List<AttendanceMethod>
        get() = methods.filter { it.enabled }
}

/**
 * Permission status for each attendance method that requires device capabilities.
 */
enum class PermissionStatus {
    GRANTED,
    DENIED,
    NOT_REQUESTED,
    NOT_AVAILABLE
}
