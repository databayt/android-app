package org.hogwarts.android.feature.lessons.domain.model

/**
 * Status of a lesson plan in its lifecycle.
 */
enum class LessonPlanStatus {
    DRAFT, PUBLISHED, COMPLETED
}

/**
 * Type of activity within a lesson plan.
 */
enum class ActivityType {
    LECTURE, DISCUSSION, GROUPWORK, INDIVIDUAL, ASSESSMENT, BREAK
}

/**
 * Type of teaching resource.
 */
enum class ResourceType {
    PDF, VIDEO, LINK, IMAGE, DOCUMENT
}

/**
 * Progress status for a curriculum topic.
 */
enum class TopicStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED
}

/**
 * Domain model for a lesson plan.
 */
data class LessonPlan(
    val id: String,
    val schoolId: String,
    val classId: String,
    val subjectName: String,
    val topic: String,
    val date: String,
    val objectives: List<String> = emptyList(),
    val activities: List<LessonActivity> = emptyList(),
    val resources: List<LessonResource> = emptyList(),
    val homework: String? = null,
    val teacherNotes: String? = null,
    val status: LessonPlanStatus = LessonPlanStatus.DRAFT
)

/**
 * An activity within a lesson plan with duration in minutes.
 */
data class LessonActivity(
    val description: String,
    val duration: Int,
    val type: ActivityType
)

/**
 * A teaching resource attached to a lesson plan.
 */
data class LessonResource(
    val id: String,
    val name: String,
    val type: ResourceType,
    val url: String,
    val fileSize: Long? = null
)

/**
 * A term's curriculum map containing subjects and their topics.
 */
data class CurriculumMap(
    val termId: String,
    val termName: String,
    val subjects: List<CurriculumSubject> = emptyList()
)

/**
 * A subject within the curriculum map.
 */
data class CurriculumSubject(
    val subjectId: String,
    val subjectName: String,
    val topics: List<CurriculumTopic> = emptyList()
)

/**
 * A weekly topic within a curriculum subject.
 */
data class CurriculumTopic(
    val title: String,
    val weekNumber: Int,
    val status: TopicStatus = TopicStatus.NOT_STARTED
)
