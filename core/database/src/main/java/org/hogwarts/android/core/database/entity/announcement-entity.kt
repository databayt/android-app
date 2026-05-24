package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "announcements",
    indices = [Index(value = ["schoolId"])]
)
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val title: String,
    val content: String,
    val type: String,
    val category: String? = null,
    val authorId: String? = null,
    val authorName: String? = null,
    val targetAudience: String? = null,
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val venue: String? = null,
    val isImportant: Boolean = false,
    val attachmentUrl: String? = null,
    val status: String = "ACTIVE",
    val lastSyncAt: Instant = Instant.now()
)
