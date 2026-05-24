package org.hogwarts.android.feature.profile.domain.model

/**
 * GitHub-style contribution heatmap data.
 * Mirrors web `ContributionGraphData` in profile/types.ts.
 */
data class ContributionGraphData(
    val year: Int,
    val role: ProfileRole,
    val totalActivities: Int,
    val contributions: List<DailyContribution>,
    val summary: ContributionSummary
)

data class DailyContribution(
    val date: String, // YYYY-MM-DD
    val count: Int,
    val level: Int, // 0..4
    val activities: List<ActivityBreakdown> = emptyList()
)

data class ContributionSummary(
    val activeDays: Int,
    val longestStreak: Int,
    val currentStreak: Int,
    val averagePerDay: Float,
    val peakDay: PeakDay? = null
)

data class PeakDay(val date: String, val count: Int)

data class ActivityBreakdown(
    val type: ActivityType,
    val count: Int,
    val label: String
)

enum class ActivityType(val label: String) {
    // Student
    ATTENDANCE("Attended class"),
    ASSIGNMENT_SUBMITTED("Submitted assignment"),
    EXAM_COMPLETED("Completed exam"),
    LIBRARY_VISIT("Library visit"),
    CLUB_ACTIVITY("Club activity"),

    // Teacher
    CLASS_TAUGHT("Taught class"),
    GRADE_PUBLISHED("Published grade"),
    ATTENDANCE_TAKEN("Marked attendance"),
    LESSON_CREATED("Created lesson"),

    // Parent
    PORTAL_LOGIN("Portal login"),
    PAYMENT_MADE("Made payment"),
    MESSAGE_SENT("Sent message"),
    EVENT_RSVP("Event RSVP"),

    // Staff
    TASK_COMPLETED("Completed task"),
    REPORT_GENERATED("Generated report"),
    EXPENSE_PROCESSED("Processed expense"),
    MEETING_ATTENDED("Attended meeting");
}

data class ActivityFeedItem(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String? = null,
    val timestampMillis: Long,
    val link: String? = null
)

data class PinnedItem(
    val id: String,
    val itemType: PinnedItemType,
    val title: String,
    val description: String? = null,
    val category: String,
    val stats: List<PinnedStat> = emptyList(),
    val isPublic: Boolean = true
)

data class PinnedStat(val label: String, val value: String)

enum class PinnedItemType {
    COURSE, SUBJECT, PROJECT, ACHIEVEMENT, CERTIFICATE,
    CLASS, CHILD, DEPARTMENT, PUBLICATION, TASK
}
