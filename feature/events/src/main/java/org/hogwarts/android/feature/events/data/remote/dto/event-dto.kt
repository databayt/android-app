package org.hogwarts.android.feature.events.data.remote.dto

import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.events.domain.model.Event
import org.hogwarts.android.feature.events.domain.model.EventRegistration
import org.hogwarts.android.feature.events.domain.model.EventStatus
import org.hogwarts.android.feature.events.domain.model.EventType
import org.hogwarts.android.feature.events.domain.model.RegistrationStatus

@Serializable
data class EventDto(
    val id: String,
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
    val status: String = "UPCOMING"
) {
    fun toDomain(): Event = Event(
        id = id,
        title = title,
        description = description,
        type = runCatching { EventType.valueOf(type.uppercase()) }.getOrDefault(EventType.OTHER),
        startDate = startDate,
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        location = location,
        isAllDay = isAllDay,
        maxAttendees = maxAttendees,
        currentAttendees = currentAttendees,
        isRegistered = isRegistered,
        registrationRequired = registrationRequired,
        imageUrl = imageUrl,
        organizerName = organizerName,
        status = runCatching { EventStatus.valueOf(status.uppercase()) }.getOrDefault(EventStatus.UPCOMING)
    )
}

@Serializable
data class EventRegistrationDto(
    val id: String,
    val eventId: String,
    val userId: String,
    val registeredAt: String,
    val status: String = "REGISTERED"
) {
    fun toDomain(): EventRegistration = EventRegistration(
        id = id,
        eventId = eventId,
        userId = userId,
        registeredAt = registeredAt,
        status = runCatching { RegistrationStatus.valueOf(status.uppercase()) }.getOrDefault(RegistrationStatus.REGISTERED)
    )
}
