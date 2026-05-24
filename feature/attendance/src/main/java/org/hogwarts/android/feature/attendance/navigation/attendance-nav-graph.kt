package org.hogwarts.android.feature.attendance.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.attendance.ui.AttendanceAnalyticsScreen
import org.hogwarts.android.feature.attendance.ui.AttendanceMethodSelectorScreen
import org.hogwarts.android.feature.attendance.ui.AttendanceScreen
import org.hogwarts.android.feature.attendance.ui.GamificationScreen
import org.hogwarts.android.feature.attendance.ui.HallPassScreen
import org.hogwarts.android.feature.attendance.ui.InterventionsScreen
import org.hogwarts.android.feature.attendance.ui.kiosk.KioskModeScreen

@Serializable data object Attendance
@Serializable data object AttendanceGamification
@Serializable data object AttendanceHallPass
@Serializable data object AttendanceInterventions
@Serializable data object AttendanceAnalytics
@Serializable data object AttendanceMethodSettings
@Serializable data object AttendanceKiosk

/**
 * Attendance navigation graph entry (existing).
 */
fun NavGraphBuilder.attendanceScreen(
    onNavigateBack: () -> Unit
) {
    composable<Attendance> {
        AttendanceScreen(onNavigateBack = onNavigateBack)
    }
}

/**
 * Gamification screen showing badges, streaks, and points.
 */
fun NavGraphBuilder.gamificationScreen(
    onNavigateBack: () -> Unit
) {
    composable<AttendanceGamification> {
        GamificationScreen(onNavigateBack = onNavigateBack)
    }
}

/**
 * Hall Pass screen for requesting, approving, and tracking hall passes.
 */
fun NavGraphBuilder.hallPassScreen(
    onNavigateBack: () -> Unit
) {
    composable<AttendanceHallPass> {
        HallPassScreen(onNavigateBack = onNavigateBack)
    }
}

/**
 * Interventions screen for monitoring at-risk students.
 */
fun NavGraphBuilder.interventionsScreen(
    onNavigateBack: () -> Unit
) {
    composable<AttendanceInterventions> {
        InterventionsScreen(onNavigateBack = onNavigateBack)
    }
}

/**
 * Advanced Attendance Analytics screen with heatmap and charts.
 */
fun NavGraphBuilder.attendanceAnalyticsScreen(
    onNavigateBack: () -> Unit
) {
    composable<AttendanceAnalytics> {
        AttendanceAnalyticsScreen(onNavigateBack = onNavigateBack)
    }
}

/**
 * Attendance Method Selector screen for configuring capture methods.
 */
fun NavGraphBuilder.attendanceMethodSettingsScreen(
    onNavigateBack: () -> Unit
) {
    composable<AttendanceMethodSettings> {
        AttendanceMethodSelectorScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.kioskModeScreen(
    onExitKiosk: () -> Unit
) {
    composable<AttendanceKiosk> {
        KioskModeScreen(onExitKiosk = onExitKiosk)
    }
}
