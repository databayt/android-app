package org.hogwarts.android.feature.exams.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class Exam(
    val id: String,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val venue: String? = null,
    val instructions: String? = null,
    val type: ExamType = ExamType.WRITTEN,
    val status: ExamStatus = ExamStatus.UPCOMING,
    val maxMarks: Int? = null,
    val passingMarks: Int? = null,
    val result: ExamResult? = null
)

data class ExamResult(
    val marksObtained: Double,
    val grade: String? = null,
    val remarks: String? = null,
    val isPassed: Boolean
)

enum class ExamType { WRITTEN, ORAL, PRACTICAL, QUIZ, ASSIGNMENT }

enum class ExamStatus { UPCOMING, IN_PROGRESS, COMPLETED, CANCELLED }
