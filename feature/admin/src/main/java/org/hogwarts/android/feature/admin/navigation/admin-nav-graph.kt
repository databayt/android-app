package org.hogwarts.android.feature.admin.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.admin.ui.AdminDashboardScreen
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

fun NavGraphBuilder.adminDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSchoolInfo: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToClassRoster: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    composable<AdminDashboard> {
        AdminDashboardScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToSchoolInfo = onNavigateToSchoolInfo,
            onNavigateToStaff = onNavigateToStaff,
            onNavigateToClassRoster = onNavigateToClassRoster,
            onNavigateToStats = onNavigateToStats
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
