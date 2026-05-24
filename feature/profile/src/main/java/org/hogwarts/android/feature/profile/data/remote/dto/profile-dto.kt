package org.hogwarts.android.feature.profile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire shape from GET /api/mobile/profile.
 * Backend reference: src/app/api/mobile/profile/route.ts
 */
@Serializable
data class ProfileDto(
    val id: String,
    val email: String,
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val role: String,
    val bio: String? = null,
    val school: SchoolDto? = null,
    val student: StudentDto? = null,
    val teacher: TeacherDto? = null,
    val guardian: GuardianDto? = null,
    val staff: StaffDto? = null
)

@Serializable
data class SchoolDto(
    val id: String,
    val name: String,
    @SerialName("name_en") val nameEn: String? = null,
    val domain: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null
)

@Serializable
data class StudentDto(
    val id: String,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    val gender: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    val phone: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    val status: String? = null,
    val section: SectionDto? = null
)

@Serializable
data class SectionDto(
    val id: String,
    val name: String,
    val grade: String? = null
)

@Serializable
data class TeacherDto(
    val id: String,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    val gender: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    val email: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    val status: String? = null,
    val department: String? = null
)

@Serializable
data class GuardianDto(
    val id: String,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    val relationship: String? = null,
    val phone: String? = null,
    val occupation: String? = null
)

@Serializable
data class StaffDto(
    val id: String,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    @SerialName("employee_id") val employeeId: String? = null,
    val department: String? = null,
    val designation: String? = null,
    @SerialName("employment_type") val employmentType: String? = null
)

@Serializable
data class UpdateProfileDto(
    val username: String? = null,
    val bio: String? = null
)

// ============================================================================
// Contribution / Activity / Pinned DTOs
// (Backend stubs — return empty arrays today; client-side fallback in repo.)
// ============================================================================

@Serializable
data class ContributionGraphDto(
    val year: Int,
    val role: String,
    @SerialName("total_activities") val totalActivities: Int,
    val contributions: List<DailyContributionDto> = emptyList(),
    val summary: ContributionSummaryDto
)

@Serializable
data class DailyContributionDto(
    val date: String,
    val count: Int,
    val level: Int,
    val activities: List<ActivityBreakdownDto> = emptyList()
)

@Serializable
data class ContributionSummaryDto(
    @SerialName("active_days") val activeDays: Int,
    @SerialName("longest_streak") val longestStreak: Int,
    @SerialName("current_streak") val currentStreak: Int,
    @SerialName("average_per_day") val averagePerDay: Float,
    @SerialName("peak_day") val peakDay: PeakDayDto? = null
)

@Serializable
data class PeakDayDto(val date: String, val count: Int)

@Serializable
data class ActivityBreakdownDto(
    val type: String,
    val count: Int,
    val label: String
)

@Serializable
data class ActivityFeedDto(
    val items: List<ActivityFeedItemDto> = emptyList()
)

@Serializable
data class ActivityFeedItemDto(
    val id: String,
    val type: String,
    val title: String,
    val description: String? = null,
    val timestamp: Long,
    val link: String? = null
)

@Serializable
data class PinnedItemsDto(
    val items: List<PinnedItemDto> = emptyList()
)

@Serializable
data class PinnedItemDto(
    val id: String,
    @SerialName("item_type") val itemType: String,
    val title: String,
    val description: String? = null,
    val category: String,
    val stats: List<PinnedStatDto> = emptyList(),
    @SerialName("is_public") val isPublic: Boolean = true
)

@Serializable
data class PinnedStatDto(val label: String, val value: String)
