package org.hogwarts.android.feature.timetable.ui

import org.hogwarts.android.feature.timetable.domain.model.TimetableEntry
import java.time.DayOfWeek

data class TimetableUiState(
    val isLoading: Boolean = true,
    val entries: List<TimetableEntry> = emptyList(),
    val selectedDay: DayOfWeek = DayOfWeek.SUNDAY,
    val error: String? = null
)
