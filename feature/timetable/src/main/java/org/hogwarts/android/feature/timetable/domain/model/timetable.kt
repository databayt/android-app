package org.hogwarts.android.feature.timetable.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Domain model for a timetable entry (class period).
 */
data class TimetableEntry(
    val id: String,
    val subjectName: String,
    val teacherName: String,
    val roomNumber: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val section: String? = null
) {
    val duration: String
        get() = "${startTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))} - ${endTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}"
}

/**
 * Grouped timetable for a full week.
 */
data class WeeklyTimetable(
    val entries: Map<DayOfWeek, List<TimetableEntry>>
) {
    fun forDay(day: DayOfWeek): List<TimetableEntry> =
        entries[day]?.sortedBy { it.startTime } ?: emptyList()
}
