package org.hogwarts.android.feature.events.domain.model

/**
 * Event type matching web app's 7 event categories.
 */
enum class EventType {
    ACADEMIC,
    SPORTS,
    CULTURAL,
    PARENT_MEETING,
    HOLIDAY,
    EXAM_SCHEDULE,
    OTHER
}

/**
 * Domain model for an event.
 */
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val type: EventType,
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
    val status: EventStatus = EventStatus.UPCOMING
)

enum class EventStatus {
    UPCOMING, ONGOING, COMPLETED, CANCELLED
}

/**
 * Event registration record.
 */
data class EventRegistration(
    val id: String,
    val eventId: String,
    val userId: String,
    val registeredAt: String,
    val status: RegistrationStatus = RegistrationStatus.REGISTERED
)

enum class RegistrationStatus {
    REGISTERED, CANCELLED, WAITLISTED
}
