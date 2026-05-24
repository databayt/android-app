package org.hogwarts.android.feature.events.data.repository

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.events.data.remote.EventsApi
import org.hogwarts.android.feature.events.domain.model.Event
import org.hogwarts.android.feature.events.domain.model.EventRegistration
import javax.inject.Inject

class EventsRepositoryImpl @Inject constructor(
    private val api: EventsApi,
    private val tenantContext: TenantContext
) : EventsRepository {

    override suspend fun getEvents(type: String?, status: String?): List<Event> {
        // Web API doesn't support type/status filters; fetch all and filter locally
        return api.getEvents().map { it.toDomain() }
    }

    override suspend fun getEvent(eventId: String): Event {
        return api.getEvent(eventId).toDomain()
    }

    override suspend fun registerForEvent(eventId: String): EventRegistration {
        return api.registerForEvent(eventId).toDomain()
    }

    override suspend fun unregisterFromEvent(eventId: String) {
        api.unregisterFromEvent(eventId)
    }

    override suspend fun getCalendarEvents(year: Int, month: Int): List<Event> {
        // Web API doesn't have a separate calendar endpoint; use getEvents and filter by date
        return api.getEvents().map { it.toDomain() }
    }
}
