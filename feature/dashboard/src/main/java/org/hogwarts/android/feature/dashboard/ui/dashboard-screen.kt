package org.hogwarts.android.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.theme.AppleBlue
import org.hogwarts.android.core.designsystem.theme.AppleGreen
import org.hogwarts.android.core.designsystem.theme.AppleOrange
import org.hogwarts.android.core.designsystem.theme.AppleRed
import org.hogwarts.android.feature.dashboard.R
import org.hogwarts.android.feature.dashboard.ui.components.HomeDock
import org.hogwarts.android.feature.dashboard.ui.components.HomeDockItem
import org.hogwarts.android.feature.dashboard.ui.components.HomeSearchPill
import org.hogwarts.android.feature.dashboard.ui.components.HomeWallpaper

/**
 * Main Dashboard screen hosting the iOS-style [HomeScreen] body and the iOS-style [HomeDock].
 *
 * Logout is surfaced from the Settings screen — the dashboard itself no longer owns that flow.
 */
@Composable
fun DashboardScreen(
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
    onNavigateToEvents: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeWallpaper(
        modifier = modifier.fillMaxSize(),
        wallpaperId = uiState.wallpaperId
    ) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HomeSearchPill(
                        label = stringResource(R.string.home_search_hint),
                        // Universal search is E11.S02 (Phase 3 polish). Tap is a
                        // no-op for now so the pill keeps its layout slot.
                        onClick = {}
                    )
                }
                HomeDock(
                    items = listOf(
                        HomeDockItem(
                            icon = HogwartsIcons.Home,
                            background = AppleBlue,
                            contentDescription = stringResource(R.string.dashboard_tab_home),
                            onClick = { /* already on home */ },
                            iconRes = R.drawable.ic_tile_home
                        ),
                        HomeDockItem(
                            icon = HogwartsIcons.Messages,
                            background = AppleGreen,
                            contentDescription = stringResource(R.string.dashboard_tab_messages),
                            onClick = onNavigateToMessages,
                            iconRes = R.drawable.ic_tile_message
                        ),
                        HomeDockItem(
                            icon = HogwartsIcons.Notifications,
                            background = AppleRed,
                            contentDescription = stringResource(R.string.dashboard_tab_alerts),
                            onClick = onNavigateToNotifications,
                            iconRes = R.drawable.ic_tile_alerts
                        ),
                        HomeDockItem(
                            icon = HogwartsIcons.Settings,
                            background = AppleOrange,
                            contentDescription = stringResource(R.string.dashboard_tab_settings),
                            onClick = onNavigateToSettings,
                            iconRes = R.drawable.ic_tile_setting
                        )
                    )
                )
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            HomeScreen(
                uiState = uiState,
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
                onNavigateToEvents = onNavigateToEvents,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
    }
}
