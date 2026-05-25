package org.hogwarts.android.feature.dashboard.ui

import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.wallpaper.WallpaperCatalog

/**
 * UI state for Dashboard screen.
 */
data class DashboardUiState(
    val userName: String = "",
    val userRole: UserRole = UserRole.STUDENT,
    val schoolName: String = "",
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val todayClasses: Int = 0,
    val pendingAssignments: Int = 0,
    val attendancePercentage: Float = 0f,
    val unreadNotifications: Int = 0,
    val upcomingExams: Int = 0,
    val childrenCount: Int = 0,
    val pendingAttendance: Int = 0,
    val totalStudents: Int = 0,
    val wallpaperId: String = WallpaperCatalog.DEFAULT_ID,
    // Server-driven module manifest; controls tile order on the home grid.
    // Empty list = use the role's default tile set in default order
    // (per HomeTileVisibility). Comes from /api/mobile/dashboard (E08.S06).
    val enabledModules: List<String> = emptyList()
)
