package org.hogwarts.android.feature.announcements.ui

import org.hogwarts.android.feature.announcements.domain.model.Announcement
import org.hogwarts.android.feature.announcements.domain.model.AnnouncementType

data class AnnouncementsUiState(
    val announcements: List<Announcement> = emptyList(),
    val selectedFilter: AnnouncementType? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
