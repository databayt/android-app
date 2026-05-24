package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for cached event data.
 *
 * CRITICAL: schoolId is MANDATORY for multi-tenant isolation.
 */
@Entity(
    tableName = "events",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["type", "schoolId"]),
        Index(value = ["startDate", "schoolId"])
    ]
)
data class EventEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val title: String,
    val description: String = "",
    val type: String,
    val startDate: String,
    val endDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val location: String? = null,
    val isAllDay: Boolean = false,
    val maxAttendees: Int? = null,
    val currentAttendees: Int = 0,
    val isRegistered: Boolean = false,
    val registrationRequired: Boolean = false,
    val imageUrl: String? = null,
    val organizerName: String? = null,
    val status: String = "UPCOMING",

    /** Sync metadata */
    val lastSyncedAt: Instant
)

/**
 * Room entity for event registration records.
 */
@Entity(
    tableName = "event_registrations",
    indices = [
        Index(value = ["eventId", "schoolId"]),
        Index(value = ["userId", "schoolId"])
    ]
)
data class EventRegistrationEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val eventId: String,
    val userId: String,
    val registeredAt: String,
    val status: String = "REGISTERED"
)
