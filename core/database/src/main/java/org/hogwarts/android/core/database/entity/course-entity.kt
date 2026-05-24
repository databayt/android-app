package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["category", "schoolId"])
    ]
)
data class CourseEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val title: String,
    val description: String,
    val instructorName: String,
    val thumbnailUrl: String? = null,
    val category: String,
    val enrollmentCount: Int = 0,
    val lessonCount: Int = 0,
    val totalDuration: Long = 0,
    val status: String,
    // Comma-separated grade levels (e.g. "1,2,3"). Stored flat because Room
    // would otherwise require a TypeConverter and the filter only reads it.
    val grades: String? = null,
    val lastSyncedAt: Long
)

@Entity(
    tableName = "chapters",
    indices = [
        Index(value = ["courseId"])
    ]
)
data class ChapterEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val orderIndex: Int,
    val lessonCount: Int = 0,
    val completedLessons: Int = 0
)

@Entity(
    tableName = "lessons",
    indices = [
        Index(value = ["chapterId"])
    ]
)
data class LessonEntity(
    @PrimaryKey val id: String,
    val chapterId: String,
    val title: String,
    val type: String,
    val duration: Long = 0,
    val contentUrl: String? = null,
    val thumbnailUrl: String? = null,
    val orderIndex: Int,
    val isCompleted: Boolean = false,
    val isLocked: Boolean = false
)

@Entity(
    tableName = "enrollments",
    indices = [
        Index(value = ["courseId", "userId"]),
        Index(value = ["userId"])
    ]
)
data class EnrollmentEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val userId: String,
    val progress: Float = 0f,
    val startedAt: Long,
    val lastAccessedAt: Long,
    val completedAt: Long? = null
)

@Entity(
    tableName = "lesson_progress",
    indices = [
        Index(value = ["lessonId"]),
        Index(value = ["enrollmentId"])
    ]
)
data class LessonProgressEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val enrollmentId: String,
    val status: String,
    val score: Float? = null,
    val startedAt: Long? = null,
    val completedAt: Long? = null
)
