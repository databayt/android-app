package org.hogwarts.android.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.dashboard.ui.DashboardScreen

/**
 * Type-safe route for the dashboard destination.
 */
@Serializable data object Dashboard

/**
 * Dashboard navigation graph entry.
 *
 * Registers the dashboard composable with all required navigation callbacks.
 */
fun NavGraphBuilder.dashboardScreen(
    onNavigateToStudents: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToGrades: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStream: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToAtomStudio: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToEvents: () -> Unit
) {
    composable<Dashboard> {
        DashboardScreen(
            onNavigateToStudents = onNavigateToStudents,
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToGrades = onNavigateToGrades,
            onNavigateToFees = onNavigateToFees,
            onNavigateToTimetable = onNavigateToTimetable,
            onNavigateToMessages = onNavigateToMessages,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToStream = onNavigateToStream,
            onNavigateToSubjects = onNavigateToSubjects,
            onNavigateToAtomStudio = onNavigateToAtomStudio,
            onNavigateToAnnouncements = onNavigateToAnnouncements,
            onNavigateToLibrary = onNavigateToLibrary,
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToNotifications = onNavigateToNotifications,
            onNavigateToExams = onNavigateToExams,
            onNavigateToEvents = onNavigateToEvents
        )
    }
}
