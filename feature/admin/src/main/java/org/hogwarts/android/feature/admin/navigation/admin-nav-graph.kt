package org.hogwarts.android.feature.admin.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.admin.ui.AdminDashboardScreen
import org.hogwarts.android.feature.admin.ui.ClassReadinessScreen
import org.hogwarts.android.feature.admin.ui.ClassRosterScreen
import org.hogwarts.android.feature.admin.ui.SchoolInfoScreen
import org.hogwarts.android.feature.admin.ui.SchoolStatsScreen
import org.hogwarts.android.feature.admin.ui.StaffDirectoryScreen

@Serializable data object AdminDashboard
@Serializable data object AdminSchoolInfo
@Serializable data object AdminStaff
@Serializable data object AdminClassRoster
@Serializable data class AdminClassRosterDetail(val classId: String)
@Serializable data object AdminStats
@Serializable data object AdminReadiness

fun NavGraphBuilder.adminDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSchoolInfo: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToClassRoster: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToReadiness: () -> Unit = {}
) {
    composable<AdminDashboard> {
        AdminDashboardScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToSchoolInfo = onNavigateToSchoolInfo,
            onNavigateToStaff = onNavigateToStaff,
            onNavigateToClassRoster = onNavigateToClassRoster,
            onNavigateToStats = onNavigateToStats,
            onNavigateToReadiness = onNavigateToReadiness
        )
    }
}

fun NavGraphBuilder.schoolInfoScreen(
    onNavigateBack: () -> Unit
) {
    composable<AdminSchoolInfo> {
        SchoolInfoScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.staffDirectoryScreen(
    onNavigateBack: () -> Unit
) {
    composable<AdminStaff> {
        StaffDirectoryScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.classRosterScreen(
    onNavigateBack: () -> Unit
) {
    composable<AdminClassRoster> {
        ClassRosterScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.classRosterDetailScreen(
    onNavigateBack: () -> Unit
) {
    composable<AdminClassRosterDetail> {
        ClassRosterScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.schoolStatsScreen(
    onNavigateBack: () -> Unit
) {
    composable<AdminStats> {
        SchoolStatsScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.classReadinessScreen(
    onNavigateBack: () -> Unit
) {
    composable<AdminReadiness> {
        ClassReadinessScreen(onNavigateBack = onNavigateBack)
    }
}
