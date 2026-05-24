package org.hogwarts.android.feature.guardian.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.guardian.ui.ChildAttendanceScreen
import org.hogwarts.android.feature.guardian.ui.ChildFeesScreen
import org.hogwarts.android.feature.guardian.ui.ChildGradesScreen
import org.hogwarts.android.feature.guardian.ui.ChildTimetableScreen
import org.hogwarts.android.feature.guardian.ui.ChildrenScreen
import org.hogwarts.android.feature.guardian.ui.GuardianMessagesScreen
import org.hogwarts.android.feature.guardian.ui.GuardianNotificationsScreen
import org.hogwarts.android.feature.guardian.ui.MeetingBookingScreen
import org.hogwarts.android.feature.guardian.ui.ConsentFormsScreen
import org.hogwarts.android.feature.guardian.ui.TripPermissionScreen
import org.hogwarts.android.feature.guardian.ui.CommunicationPreferencesScreen

@Serializable data object GuardianChildren
@Serializable data class GuardianChildAttendance(val childId: String)
@Serializable data class GuardianChildGrades(val childId: String)
@Serializable data class GuardianChildFees(val childId: String)
@Serializable data class GuardianChildTimetable(val childId: String)
@Serializable data object GuardianMessages
@Serializable data object GuardianNotifications
@Serializable data object GuardianMeetingBooking
@Serializable data object GuardianConsentForms
@Serializable data object GuardianTripPermissions
@Serializable data object GuardianCommunicationPreferences

fun NavGraphBuilder.guardianChildrenScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChild: (String) -> Unit
) {
    composable<GuardianChildren> {
        ChildrenScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToChild = onNavigateToChild
        )
    }
}

fun NavGraphBuilder.guardianChildAttendanceScreen(
    onNavigateBack: () -> Unit
) {
    composable<GuardianChildAttendance> {
        ChildAttendanceScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.guardianChildGradesScreen(
    onNavigateBack: () -> Unit
) {
    composable<GuardianChildGrades> {
        ChildGradesScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.guardianChildFeesScreen(
    onNavigateBack: () -> Unit
) {
    composable<GuardianChildFees> {
        ChildFeesScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.guardianChildTimetableScreen(
    onNavigateBack: () -> Unit
) {
    composable<GuardianChildTimetable> {
        ChildTimetableScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.guardianMessagesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    composable<GuardianMessages> {
        GuardianMessagesScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToChat = onNavigateToChat
        )
    }
}

fun NavGraphBuilder.guardianNotificationsScreen(
    onNavigateBack: () -> Unit
) {
    composable<GuardianNotifications> {
        GuardianNotificationsScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.meetingBookingScreen(
    onNavigateBack: () -> Unit
) {
    composable<GuardianMeetingBooking> {
        MeetingBookingScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.consentFormsScreen(
    onNavigateBack: () -> Unit
) {
    composable<GuardianConsentForms> {
        ConsentFormsScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.tripPermissionsScreen(
    onNavigateBack: () -> Unit
) {
    composable<GuardianTripPermissions> {
        TripPermissionScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.communicationPreferencesScreen(
    onNavigateBack: () -> Unit
) {
    composable<GuardianCommunicationPreferences> {
        CommunicationPreferencesScreen(onNavigateBack = onNavigateBack)
    }
}
