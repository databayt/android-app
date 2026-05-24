package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lesson_plans",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["classId", "schoolId"]),
        Index(value = ["date", "schoolId"])
    ]
)
data class LessonPlanEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val classId: String,
    val subjectName: String,
    val topic: String,
    val date: String,
    val objectives: String,
    val activities: String,
    val homework: String? = null,
    val teacherNotes: String? = null,
    val status: String,
    val lastSyncedAt: Long
)

@Entity(
    tableName = "lesson_resources",
    indices = [
        Index(value = ["lessonPlanId"])
    ]
)
data class LessonResourceEntity(
    @PrimaryKey val id: String,
    val lessonPlanId: String,
    val name: String,
    val type: String,
    val url: String,
    val fileSize: Long? = null
)
