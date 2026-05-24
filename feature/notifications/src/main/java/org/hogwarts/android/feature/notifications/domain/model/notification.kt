package org.hogwarts.android.feature.notifications.domain.model

import java.time.Instant

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val isRead: Boolean = false,
    val deepLink: String? = null,
    val createdAt: Instant
)

enum class NotificationType {
    ANNOUNCEMENT,
    ATTENDANCE,
    GRADE,
    FEE,
    MESSAGE,
    TIMETABLE,
    GENERAL;

    companion object {
        fun fromString(value: String): NotificationType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: GENERAL
    }
}

enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT;

    companion object {
        fun fromString(value: String?): NotificationPriority =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: NORMAL
    }
}

enum class NotificationChannel {
    IN_APP,
    EMAIL,
    PUSH,
    SMS,
    WHATSAPP;

    val wireName: String get() = name.lowercase()

    companion object {
        fun fromString(value: String): NotificationChannel =
            entries.find { it.wireName.equals(value, ignoreCase = true) } ?: IN_APP
    }
}
