package org.hogwarts.android.feature.announcements.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.announcements.ui.AnnouncementDetailScreen
import org.hogwarts.android.feature.announcements.ui.AnnouncementsScreen

/** Web `/announcements`. */
@Serializable data object Announcements

/** Web `/announcements/[id]`. */
@Serializable data class AnnouncementDetail(val announcementId: String)

/**
 * The listing and its reading page. The writers' other tabs (templates,
 * archived, settings) are desktop admin pages: they go out through [onOpenHref]
 * as web paths for the shell to hand off.
 */
fun NavGraphBuilder.announcementsGraph(
    onOpenAnnouncement: (id: String) -> Unit,
    onOpenHref: (href: String) -> Unit,
    onBack: () -> Unit,
) {
    composable<Announcements> {
        AnnouncementsScreen(onOpenAnnouncement = onOpenAnnouncement, onOpenHref = onOpenHref)
    }
    composable<AnnouncementDetail> {
        AnnouncementDetailScreen(onBack = onBack)
    }
}
