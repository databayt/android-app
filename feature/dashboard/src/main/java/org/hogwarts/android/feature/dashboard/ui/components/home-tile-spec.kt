package org.hogwarts.android.feature.dashboard.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.theme.AppleBlue
import org.hogwarts.android.core.designsystem.theme.AppleGreen
import org.hogwarts.android.core.designsystem.theme.AppleIndigo
import org.hogwarts.android.core.designsystem.theme.AppleOrange
import org.hogwarts.android.core.designsystem.theme.ApplePink
import org.hogwarts.android.core.designsystem.theme.ApplePurple
import org.hogwarts.android.core.designsystem.theme.AppleRed
import org.hogwarts.android.core.designsystem.theme.AppleYellow
import org.hogwarts.android.feature.dashboard.R
import org.hogwarts.android.feature.dashboard.ui.DashboardUiState

/**
 * One tile on the home grid.
 *
 * When [iconRes] is non-null the tile renders as a full-bleed drawable (Figma-authored
 * tile art: colored background + glyph baked into a single image). Otherwise it falls
 * back to the Material [icon] rendered on an [iosTileBrush] over [background].
 *
 * [badgeCount] is read from [DashboardUiState] at compose time so counts update
 * reactively with the view model.
 */
data class HomeTileSpec(
    // Stable identifier; drives role-visibility filtering and server-driven
    // ordering. See [HomeTileVisibility].
    val id: HomeTileId,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val background: Color,
    val onClick: () -> Unit,
    val badgeCount: Int = 0,
    @DrawableRes val iconRes: Int? = null
)

/**
 * Builds the ordered tile list for the home grid.
 *
 * Index 0–3 fill the 2x2 cluster to the right of the Smart Stack widget.
 * Index 4–11 fill rows 3 and 4 (full-width).
 * Index 12–15 fill row 5 — these are the tiles displaced when the widget claimed
 * the top-left 2x2 (Students, Attendance, Schedule, Messages).
 */
fun buildHomeTiles(
    state: DashboardUiState,
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
    onNavigateToProfile: () -> Unit
): List<HomeTileSpec> = listOf(
    // --- Top-right 2x2 cluster (next to widget) ---
    HomeTileSpec(
        id = HomeTileId.Grades,
        labelRes = R.string.dashboard_action_grades,
        icon = HogwartsIcons.Grades,
        background = AppleOrange,
        onClick = onNavigateToGrades,
        badgeCount = state.upcomingExams,
        iconRes = R.drawable.ic_tile_grades
    ),
    HomeTileSpec(
        id = HomeTileId.Fees,
        labelRes = R.string.dashboard_action_fees,
        icon = HogwartsIcons.Fees,
        background = AppleGreen,
        onClick = onNavigateToFees,
        iconRes = R.drawable.ic_tile_wallet
    ),
    HomeTileSpec(
        id = HomeTileId.Stream,
        labelRes = R.string.home_action_stream,
        icon = HogwartsIcons.Video,
        background = ApplePurple,
        onClick = onNavigateToStream,
        iconRes = R.drawable.ic_tile_stream
    ),
    HomeTileSpec(
        id = HomeTileId.Subjects,
        labelRes = R.string.dashboard_action_subjects,
        icon = HogwartsIcons.Subjects,
        background = AppleBlue,
        onClick = onNavigateToSubjects,
        iconRes = R.drawable.ic_tile_subject
    ),

    // --- Row 3 ---
    HomeTileSpec(
        id = HomeTileId.Settings,
        labelRes = R.string.dashboard_tab_settings,
        icon = HogwartsIcons.Settings,
        background = Color(0xFF8E8E93),
        onClick = onNavigateToSettings,
        iconRes = R.drawable.ic_tile_setting
    ),
    HomeTileSpec(
        id = HomeTileId.AtomStudio,
        labelRes = R.string.home_action_atom_studio,
        icon = Icons.Filled.Science,
        background = ApplePurple,
        onClick = onNavigateToAtomStudio,
        iconRes = R.drawable.ic_tile_atom
    ),
    HomeTileSpec(
        id = HomeTileId.Notifications,
        labelRes = R.string.home_action_notifications,
        icon = HogwartsIcons.Notifications,
        background = AppleRed,
        onClick = { /* TODO: notifications screen */ },
        badgeCount = state.unreadNotifications,
        iconRes = R.drawable.ic_tile_notifications
    ),
    HomeTileSpec(
        id = HomeTileId.Exams,
        labelRes = R.string.home_action_exams,
        icon = HogwartsIcons.Exams,
        background = AppleIndigo,
        onClick = { /* TODO: exams screen */ },
        badgeCount = state.upcomingExams,
        iconRes = R.drawable.ic_tile_exams
    ),

    // --- Row 4 ---
    HomeTileSpec(
        id = HomeTileId.Assignments,
        labelRes = R.string.home_action_assignments,
        icon = HogwartsIcons.Document,
        background = AppleOrange,
        onClick = { /* TODO: assignments screen */ },
        badgeCount = state.pendingAssignments,
        iconRes = R.drawable.ic_tile_assignments
    ),
    HomeTileSpec(
        id = HomeTileId.Library,
        labelRes = R.string.home_action_library,
        icon = HogwartsIcons.Classes,
        background = AppleYellow,
        onClick = onNavigateToLibrary,
        iconRes = R.drawable.ic_tile_library
    ),
    HomeTileSpec(
        id = HomeTileId.Events,
        labelRes = R.string.home_action_events,
        icon = HogwartsIcons.Star,
        background = ApplePink,
        onClick = { /* TODO: events screen */ },
        iconRes = R.drawable.ic_tile_events
    ),
    HomeTileSpec(
        id = HomeTileId.Profile,
        labelRes = R.string.home_action_profile,
        icon = HogwartsIcons.Profile,
        background = Color(0xFF8E8E93),
        onClick = onNavigateToProfile,
        iconRes = R.drawable.ic_tile_profile
    ),

    // --- Row 5 (displaced by the widget) ---
    HomeTileSpec(
        id = HomeTileId.Students,
        labelRes = R.string.dashboard_action_students,
        icon = HogwartsIcons.Students,
        background = AppleBlue,
        onClick = onNavigateToStudents,
        iconRes = R.drawable.ic_tile_students
    ),
    HomeTileSpec(
        id = HomeTileId.Attendance,
        labelRes = R.string.dashboard_action_attendance,
        icon = HogwartsIcons.Attendance,
        background = AppleGreen,
        onClick = onNavigateToAttendance,
        badgeCount = state.pendingAttendance,
        iconRes = R.drawable.ic_tile_attendance
    ),
    HomeTileSpec(
        id = HomeTileId.Schedule,
        labelRes = R.string.dashboard_action_schedule,
        icon = HogwartsIcons.Timetable,
        background = AppleRed,
        onClick = onNavigateToTimetable,
        badgeCount = state.todayClasses,
        iconRes = R.drawable.ic_tile_schedule
    ),
    HomeTileSpec(
        id = HomeTileId.Messages,
        labelRes = R.string.dashboard_action_messages,
        icon = HogwartsIcons.Messages,
        background = AppleGreen,
        onClick = onNavigateToMessages,
        badgeCount = state.unreadNotifications,
        iconRes = R.drawable.ic_tile_message
    ),

    // --- Row 6 (overflow row, padded with spacers) ---
    HomeTileSpec(
        id = HomeTileId.Announcements,
        labelRes = R.string.home_action_announcements,
        icon = HogwartsIcons.Notifications,
        background = AppleRed,
        onClick = onNavigateToAnnouncements,
        iconRes = R.drawable.ic_tile_announcements
    )
).filterAndOrderForRole(state.userRole, state.enabledModules)
