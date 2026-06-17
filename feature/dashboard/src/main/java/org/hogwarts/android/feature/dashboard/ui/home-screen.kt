package org.hogwarts.android.feature.dashboard.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hogwarts.android.feature.dashboard.ui.components.HomeGrid
import org.hogwarts.android.feature.dashboard.ui.components.buildHomeTiles

/**
 * iOS-style home screen body: the 4-column tile grid, rendered over the wallpaper
 * supplied by the parent scaffold. Search pill + dock are owned by the scaffold
 * bottomBar, so the grid gets full clearance without fighting for layout space.
 */
@Composable
fun HomeScreen(
    uiState: DashboardUiState,
    onNavigateToStudents: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToGrades: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToStream: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAtomStudio: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToEvents: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tiles = buildHomeTiles(
        state = uiState,
        onNavigateToStudents = onNavigateToStudents,
        onNavigateToAttendance = onNavigateToAttendance,
        onNavigateToGrades = onNavigateToGrades,
        onNavigateToFees = onNavigateToFees,
        onNavigateToTimetable = onNavigateToTimetable,
        onNavigateToMessages = onNavigateToMessages,
        onNavigateToStream = onNavigateToStream,
        onNavigateToSubjects = onNavigateToSubjects,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToAtomStudio = onNavigateToAtomStudio,
        onNavigateToAnnouncements = onNavigateToAnnouncements,
        onNavigateToLibrary = onNavigateToLibrary,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToNotifications = onNavigateToNotifications,
        onNavigateToExams = onNavigateToExams,
        onNavigateToEvents = onNavigateToEvents
    )

    HomeGrid(
        tiles = tiles,
        state = uiState,
        modifier = modifier
            .fillMaxSize()
            .fillMaxWidth()
    )
}
