package org.hogwarts.android.feature.events.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.events.ui.EventCalendarScreen
import org.hogwarts.android.feature.events.ui.EventDetailScreen
import org.hogwarts.android.feature.events.ui.EventsListScreen

@Serializable data object EventsList
@Serializable data class EventDetail(val eventId: String)
@Serializable data object EventCalendar

fun NavGraphBuilder.eventsListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEvent: (String) -> Unit,
    onNavigateToCalendar: () -> Unit
) {
    composable<EventsList> {
        EventsListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToEvent = onNavigateToEvent,
            onNavigateToCalendar = onNavigateToCalendar
        )
    }
}

fun NavGraphBuilder.eventDetailScreen(
    onNavigateBack: () -> Unit
) {
    composable<EventDetail> {
        EventDetailScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.eventCalendarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEvent: (String) -> Unit
) {
    composable<EventCalendar> {
        EventCalendarScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToEvent = onNavigateToEvent
        )
    }
}
