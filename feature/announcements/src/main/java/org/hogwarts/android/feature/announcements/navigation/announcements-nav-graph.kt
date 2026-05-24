package org.hogwarts.android.feature.announcements.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.announcements.ui.AnnouncementDetailScreen
import org.hogwarts.android.feature.announcements.ui.AnnouncementsScreen

@Serializable data object Announcements
@Serializable data class AnnouncementDetail(val announcementId: String)

fun NavGraphBuilder.announcementsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAnnouncement: (String) -> Unit
) {
    composable<Announcements> {
        AnnouncementsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAnnouncement = onNavigateToAnnouncement
        )
    }
}

fun NavGraphBuilder.announcementDetailScreen(
    onNavigateBack: () -> Unit
) {
    composable<AnnouncementDetail> {
        AnnouncementDetailScreen(
            onNavigateBack = onNavigateBack
        )
    }
}
