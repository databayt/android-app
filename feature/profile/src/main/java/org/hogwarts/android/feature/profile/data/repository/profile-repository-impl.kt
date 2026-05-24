package org.hogwarts.android.feature.profile.data.repository

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.profile.data.remote.ProfileApi
import org.hogwarts.android.feature.profile.data.remote.dto.ActivityBreakdownDto
import org.hogwarts.android.feature.profile.data.remote.dto.ActivityFeedItemDto
import org.hogwarts.android.feature.profile.data.remote.dto.ContributionGraphDto
import org.hogwarts.android.feature.profile.data.remote.dto.DailyContributionDto
import org.hogwarts.android.feature.profile.data.remote.dto.PinnedItemDto
import org.hogwarts.android.feature.profile.data.remote.dto.ProfileDto
import org.hogwarts.android.feature.profile.data.remote.dto.UpdateProfileDto
import org.hogwarts.android.feature.profile.domain.model.ActivityBreakdown
import org.hogwarts.android.feature.profile.domain.model.ActivityFeedItem
import org.hogwarts.android.feature.profile.domain.model.ActivityType
import org.hogwarts.android.feature.profile.domain.model.ContributionGraphData
import org.hogwarts.android.feature.profile.domain.model.ContributionSummary
import org.hogwarts.android.feature.profile.domain.model.DailyContribution
import org.hogwarts.android.feature.profile.domain.model.GuardianDetails
import org.hogwarts.android.feature.profile.domain.model.PeakDay
import org.hogwarts.android.feature.profile.domain.model.PinnedItem
import org.hogwarts.android.feature.profile.domain.model.PinnedItemType
import org.hogwarts.android.feature.profile.domain.model.PinnedStat
import org.hogwarts.android.feature.profile.domain.model.ProfileRole
import org.hogwarts.android.feature.profile.domain.model.SchoolInfo
import org.hogwarts.android.feature.profile.domain.model.StaffDetails
import org.hogwarts.android.feature.profile.domain.model.StudentDetails
import org.hogwarts.android.feature.profile.domain.model.TeacherDetails
import org.hogwarts.android.feature.profile.domain.model.UserProfile
import retrofit2.Response
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    private val tenantContext: TenantContext
) : ProfileRepository {

    override suspend fun getOwnProfile(): UserProfile {
        tenantContext.requireSchoolId()
        return api.getProfile().requireBody().toDomain()
    }

    override suspend fun getProfileById(userId: String): UserProfile {
        tenantContext.requireSchoolId()
        return api.getProfileById(userId).requireBody().toDomain()
    }

    override suspend fun updateProfile(username: String?, bio: String?): UserProfile {
        tenantContext.requireSchoolId()
        return api.updateProfile(UpdateProfileDto(username = username, bio = bio))
            .requireBody().toDomain()
    }

    override suspend fun getContributions(userId: String?, year: Int?): ContributionGraphData {
        tenantContext.requireSchoolId()
        return runCatching { api.getContributions(userId, year).requireBody().toDomain() }
            .getOrElse { emptyContributions(year ?: Calendar.getInstance().get(Calendar.YEAR)) }
    }

    override suspend fun getActivity(userId: String?, limit: Int): List<ActivityFeedItem> {
        tenantContext.requireSchoolId()
        return runCatching { api.getActivity(userId, limit).requireBody().items.map { it.toDomain() } }
            .getOrDefault(emptyList())
    }

    override suspend fun getPinnedItems(userId: String?): List<PinnedItem> {
        tenantContext.requireSchoolId()
        return runCatching { api.getPinned(userId).requireBody().items.map { it.toDomain() } }
            .getOrDefault(emptyList())
    }

    private fun emptyContributions(year: Int) = ContributionGraphData(
        year = year,
        role = ProfileRole.fromString(tenantContext.userRole?.name),
        totalActivities = 0,
        contributions = emptyList(),
        summary = ContributionSummary(
            activeDays = 0,
            longestStreak = 0,
            currentStreak = 0,
            averagePerDay = 0f
        )
    )
}

private fun <T> Response<T>.requireBody(): T = body() ?: throw IllegalStateException(
    "Empty body for ${raw().request.url} (HTTP ${code()})"
)

private fun ProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    userId = id,
    schoolId = school?.id ?: "",
    email = email,
    username = username,
    role = ProfileRole.fromString(role),
    avatarUrl = avatarUrl ?: student?.photoUrl ?: teacher?.photoUrl,
    bio = bio,
    school = school?.let { SchoolInfo(id = it.id, name = it.nameEn ?: it.name, logoUrl = it.logoUrl) },
    student = student?.let {
        StudentDetails(
            id = it.id,
            firstName = it.givenName,
            lastName = it.familyName,
            gender = it.gender,
            dateOfBirth = it.dateOfBirth,
            phone = it.phone,
            photoUrl = it.photoUrl,
            status = it.status,
            sectionId = it.section?.id,
            sectionName = it.section?.name,
            gradeName = it.section?.grade
        )
    },
    teacher = teacher?.let {
        TeacherDetails(
            id = it.id,
            firstName = it.givenName,
            lastName = it.familyName,
            gender = it.gender,
            birthDate = it.dateOfBirth,
            email = it.email,
            photoUrl = it.photoUrl,
            status = it.status,
            department = it.department
        )
    },
    guardian = guardian?.let {
        GuardianDetails(
            id = it.id,
            firstName = it.givenName,
            lastName = it.familyName,
            relationship = it.relationship,
            phone = it.phone,
            occupation = it.occupation
        )
    },
    staff = staff?.let {
        StaffDetails(
            id = it.id,
            firstName = it.givenName,
            lastName = it.familyName,
            employeeId = it.employeeId,
            department = it.department,
            designation = it.designation,
            employmentType = it.employmentType
        )
    }
)

private fun ContributionGraphDto.toDomain(): ContributionGraphData = ContributionGraphData(
    year = year,
    role = ProfileRole.fromString(role),
    totalActivities = totalActivities,
    contributions = contributions.map { it.toDomain() },
    summary = ContributionSummary(
        activeDays = summary.activeDays,
        longestStreak = summary.longestStreak,
        currentStreak = summary.currentStreak,
        averagePerDay = summary.averagePerDay,
        peakDay = summary.peakDay?.let { PeakDay(it.date, it.count) }
    )
)

private fun DailyContributionDto.toDomain(): DailyContribution = DailyContribution(
    date = date,
    count = count,
    level = level.coerceIn(0, 4),
    activities = activities.map { it.toDomain() }
)

private fun ActivityBreakdownDto.toDomain(): ActivityBreakdown = ActivityBreakdown(
    type = parseActivityType(type),
    count = count,
    label = label
)

private fun ActivityFeedItemDto.toDomain(): ActivityFeedItem = ActivityFeedItem(
    id = id,
    type = parseActivityType(type),
    title = title,
    description = description,
    timestampMillis = timestamp,
    link = link
)

private fun parseActivityType(value: String): ActivityType =
    runCatching { ActivityType.valueOf(value.uppercase()) }
        .getOrDefault(ActivityType.ATTENDANCE)

private fun PinnedItemDto.toDomain(): PinnedItem = PinnedItem(
    id = id,
    itemType = runCatching { PinnedItemType.valueOf(itemType.uppercase()) }
        .getOrDefault(PinnedItemType.PROJECT),
    title = title,
    description = description,
    category = category,
    stats = stats.map { PinnedStat(it.label, it.value) },
    isPublic = isPublic
)
