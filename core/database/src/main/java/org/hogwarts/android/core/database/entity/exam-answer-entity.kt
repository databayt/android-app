package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exam_answers",
    indices = [
        Index(value = ["examId"]),
        Index(value = ["examId", "questionId"])
    ]
)
data class ExamAnswerEntity(
    @PrimaryKey val id: String,
    val examId: String,
    val questionId: String,
    val studentAnswer: String? = null,
    val isCorrect: Boolean? = null,
    val marksObtained: Float? = null
)

@Entity(
    tableName = "exam_violations",
    indices = [
        Index(value = ["examId"])
    ]
)
data class ExamViolationEntity(
    @PrimaryKey val id: String,
    val examId: String,
    val type: String,
    val timestamp: Long,
    val description: String? = null
)

@Entity(
    tableName = "question_bank",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["subject", "schoolId"]),
        Index(value = ["difficulty", "schoolId"])
    ]
)
data class QuestionBankEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val subject: String,
    val topic: String,
    val difficulty: String,
    val type: String,
    val question: String,
    val options: String? = null,
    val correctAnswer: String,
    val explanation: String? = null,
    val isBookmarked: Boolean = false,
    val lastSyncedAt: Long
)
