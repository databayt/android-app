package org.hogwarts.android.feature.attendance.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.attendance.domain.model.AdvancedAttendanceAnalytics
import org.hogwarts.android.feature.attendance.domain.model.AttendanceBadge
import org.hogwarts.android.feature.attendance.domain.model.AttendanceIntervention
import org.hogwarts.android.feature.attendance.domain.model.AttendanceMethod
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStreak
import org.hogwarts.android.feature.attendance.domain.model.BadgeType
import org.hogwarts.android.feature.attendance.domain.model.DestinationType
import org.hogwarts.android.feature.attendance.domain.model.HallPass
import org.hogwarts.android.feature.attendance.domain.model.HallPassStatus
import org.hogwarts.android.feature.attendance.domain.model.InterventionStatus
import org.hogwarts.android.feature.attendance.domain.model.InterventionType
import org.hogwarts.android.feature.attendance.domain.model.MethodType
import org.hogwarts.android.feature.attendance.domain.model.StreakType
import java.time.Instant

/**
 * DTOs for EPIC-27: Advanced Attendance API responses.
 */

// ─── Gamification DTOs ───────────────────────────────────────────

@Serializable
data class AttendanceBadgeDto(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("icon_url") val iconUrl: String,
    @SerialName("earned_at") val earnedAt: String? = null,
    val type: String
)

@Serializable
data class AttendanceBadgeListResponse(
    val data: List<AttendanceBadgeDto>,
    val total: Int? = null
)

@Serializable
data class AttendanceStreakDto(
    @SerialName("current_streak") val currentStreak: Int,
    @SerialName("longest_streak") val longestStreak: Int,
    @SerialName("streak_start_date") val streakStartDate: String? = null,
    @SerialName("streak_type") val streakType: String
)

// ─── Hall Pass DTOs ──────────────────────────────────────────────

@Serializable
data class HallPassDto(
    val id: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("teacher_id") val teacherId: String,
    val destination: String,
    val reason: String,
    @SerialName("requested_at") val requestedAt: String,
    @SerialName("approved_at") val approvedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    val status: String
)

@Serializable
data class HallPassListResponse(
    val data: List<HallPassDto>,
    val total: Int? = null
)

@Serializable
data class HallPassRequestDto(
    @SerialName("student_id") val studentId: String,
    val destination: String,
    val reason: String
)

// ─── Intervention DTOs ───────────────────────────────────────────

@Serializable
data class InterventionDto(
    val id: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("student_name") val studentName: String,
    val type: String,
    val threshold: Float,
    @SerialName("current_rate") val currentRate: Float,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("resolved_at") val resolvedAt: String? = null
)

@Serializable
data class InterventionListResponse(
    val data: List<InterventionDto>,
    val total: Int? = null
)

// ─── Analytics DTOs ──────────────────────────────────────────────

@Serializable
data class AdvancedAnalyticsResponse(
    @SerialName("heatmap_data") val heatmapData: Map<String, Float>,
    @SerialName("day_of_week_pattern") val dayOfWeekPattern: Map<String, Float>,
    @SerialName("subject_correlation") val subjectCorrelation: Map<String, Float>
)

// ─── Method DTOs ─────────────────────────────────────────────────

@Serializable
data class AttendanceMethodDto(
    val type: String,
    val enabled: Boolean,
    @SerialName("is_default") val isDefault: Boolean
)

@Serializable
data class AttendanceMethodListResponse(
    val data: List<AttendanceMethodDto>
)

@Serializable
data class UpdateMethodsRequestDto(
    val methods: List<AttendanceMethodDto>
)

// ─── Mappers: DTO → Domain ───────────────────────────────────────

fun AttendanceBadgeDto.toDomain() = AttendanceBadge(
    id = id,
    name = name,
    description = description,
    iconUrl = iconUrl,
    earnedAt = earnedAt?.let { Instant.parse(it) },
    type = BadgeType.fromString(type)
)

fun AttendanceStreakDto.toDomain() = AttendanceStreak(
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    streakStartDate = streakStartDate?.let { Instant.parse(it) },
    streakType = StreakType.fromString(streakType)
)

fun HallPassDto.toDomain() = HallPass(
    id = id,
    studentId = studentId,
    studentName = studentName,
    teacherId = teacherId,
    destination = DestinationType.fromString(destination),
    reason = reason,
    requestedAt = Instant.parse(requestedAt),
    approvedAt = approvedAt?.let { Instant.parse(it) },
    expiresAt = expiresAt?.let { Instant.parse(it) },
    status = HallPassStatus.fromString(status)
)

fun InterventionDto.toDomain() = AttendanceIntervention(
    id = id,
    studentId = studentId,
    studentName = studentName,
    type = InterventionType.fromString(type),
    threshold = threshold,
    currentRate = currentRate,
    status = InterventionStatus.fromString(status),
    createdAt = Instant.parse(createdAt),
    resolvedAt = resolvedAt?.let { Instant.parse(it) }
)

fun AdvancedAnalyticsResponse.toDomain() = AdvancedAttendanceAnalytics(
    heatmapData = heatmapData,
    dayOfWeekPattern = dayOfWeekPattern,
    subjectCorrelation = subjectCorrelation
)

fun AttendanceMethodDto.toDomain() = AttendanceMethod(
    type = MethodType.fromString(type),
    enabled = enabled,
    isDefault = isDefault
)

// ─── Mappers: Domain → DTO ───────────────────────────────────────

fun AttendanceMethod.toDto() = AttendanceMethodDto(
    type = type.name,
    enabled = enabled,
    isDefault = isDefault
)
