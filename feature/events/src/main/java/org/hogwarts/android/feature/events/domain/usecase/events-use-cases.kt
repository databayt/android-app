package org.hogwarts.android.feature.events.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.events.data.repository.EventsRepository
import org.hogwarts.android.feature.events.domain.model.Event
import org.hogwarts.android.feature.events.domain.model.EventRegistration
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val repository: EventsRepository
) {
    suspend operator fun invoke(type: String? = null, status: String? = null): Result<List<Event>> {
        return try {
            Result.Success(repository.getEvents(type, status))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetEventDetailUseCase @Inject constructor(
    private val repository: EventsRepository
) {
    suspend operator fun invoke(eventId: String): Result<Event> {
        return try {
            Result.Success(repository.getEvent(eventId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class RegisterForEventUseCase @Inject constructor(
    private val repository: EventsRepository
) {
    suspend operator fun invoke(eventId: String): Result<EventRegistration> {
        return try {
            Result.Success(repository.registerForEvent(eventId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class UnregisterFromEventUseCase @Inject constructor(
    private val repository: EventsRepository
) {
    suspend operator fun invoke(eventId: String): Result<Unit> {
        return try {
            repository.unregisterFromEvent(eventId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetCalendarEventsUseCase @Inject constructor(
    private val repository: EventsRepository
) {
    suspend operator fun invoke(year: Int, month: Int): Result<List<Event>> {
        return try {
            Result.Success(repository.getCalendarEvents(year, month))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
