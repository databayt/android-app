package org.hogwarts.android.feature.events.data.repository

import org.hogwarts.android.feature.events.domain.model.Event
import org.hogwarts.android.feature.events.domain.model.EventRegistration

/**
 * Repository interface for events data.
 */
interface EventsRepository {
    suspend fun getEvents(type: String? = null, status: String? = null): List<Event>
    suspend fun getEvent(eventId: String): Event
    suspend fun registerForEvent(eventId: String): EventRegistration
    suspend fun unregisterFromEvent(eventId: String)
    suspend fun getCalendarEvents(year: Int, month: Int): List<Event>
}
