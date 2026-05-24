package org.hogwarts.android.feature.timetable.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimetableEntryDto(
    val id: String,
    @SerialName("subject_name") val subjectName: String,
    @SerialName("teacher_name") val teacherName: String,
    @SerialName("room_number") val roomNumber: String,
    @SerialName("day_of_week") val dayOfWeek: Int,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val section: String? = null
)

@Serializable
data class TimetableListResponse(
    val data: List<TimetableEntryDto>
)
