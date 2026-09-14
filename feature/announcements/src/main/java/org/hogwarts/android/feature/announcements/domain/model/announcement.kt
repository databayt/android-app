package org.hogwarts.android.feature.announcements.domain.model

import java.time.Instant

/** One announcement as the web card and reading page draw it. */
data class Announcement(
    val id: String,
    val title: String,
    val body: String,
    /** `school` · `class` · `role`. */
    val scope: String,
    /** `low` · `normal` · `high` · `urgent`. */
    val priority: String,
    val targetRole: String?,
    val isPublished: Boolean,
    val isPinned: Boolean,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val isRead: Boolean,
) {
    /** The web badges only these two levels on a card and a reading page. */
    val isNotablePriority: Boolean get() = priority.lowercase() == "high" || priority.lowercase() == "urgent"
}

/** One page of the list. */
data class AnnouncementsPage(
    val items: List<Announcement>,
    val total: Int,
    val page: Int,
    val perPage: Int,
) {
    val hasMore: Boolean get() = page * perPage < total
}
