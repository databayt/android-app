package org.hogwarts.android.feature.announcements.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val type: AnnouncementType,
    val category: String? = null,
    val authorId: String? = null,
    val authorName: String? = null,
    val targetAudience: String? = null,
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val venue: String? = null,
    val isImportant: Boolean = false,
    val attachmentUrl: String? = null,
    val status: AnnouncementStatus = AnnouncementStatus.ACTIVE
)

enum class AnnouncementType {
    ANNOUNCEMENT,
    EVENT,
    NEWS,
    ALERT;

    companion object {
        fun fromString(value: String): AnnouncementType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: ANNOUNCEMENT
    }
}

enum class AnnouncementStatus {
    ACTIVE,
    ARCHIVED,
    DRAFT;

    companion object {
        fun fromString(value: String): AnnouncementStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
    }
}
