package org.hogwarts.android.feature.exams.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExamDto(
    val id: String,
    val title: String,
    @SerialName("subject_id") val subjectId: String,
    @SerialName("subject_name") val subjectName: String,
    val date: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val venue: String? = null,
    val instructions: String? = null,
    val type: String = "WRITTEN",
    val status: String = "UPCOMING",
    @SerialName("max_marks") val maxMarks: Int? = null,
    @SerialName("passing_marks") val passingMarks: Int? = null,
    @SerialName("marks_obtained") val marksObtained: Double? = null,
    val grade: String? = null,
    val remarks: String? = null,
    @SerialName("is_passed") val isPassed: Boolean? = null
)

@Serializable
data class ExamListResponse(
    val data: List<ExamDto>
)
