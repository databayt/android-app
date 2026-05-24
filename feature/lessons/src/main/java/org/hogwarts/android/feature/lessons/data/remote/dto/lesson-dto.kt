package org.hogwarts.android.feature.lessons.data.remote.dto

import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.lessons.domain.model.ActivityType
import org.hogwarts.android.feature.lessons.domain.model.CurriculumMap
import org.hogwarts.android.feature.lessons.domain.model.CurriculumSubject
import org.hogwarts.android.feature.lessons.domain.model.CurriculumTopic
import org.hogwarts.android.feature.lessons.domain.model.LessonActivity
import org.hogwarts.android.feature.lessons.domain.model.LessonPlan
import org.hogwarts.android.feature.lessons.domain.model.LessonPlanStatus
import org.hogwarts.android.feature.lessons.domain.model.LessonResource
import org.hogwarts.android.feature.lessons.domain.model.ResourceType
import org.hogwarts.android.feature.lessons.domain.model.TopicStatus

@Serializable
data class LessonPlanDto(
    val id: String = "",
    val schoolId: String = "",
    val classId: String = "",
    val subjectName: String = "",
    val topic: String = "",
    val date: String = "",
    val objectives: List<String> = emptyList(),
    val activities: List<LessonActivityDto> = emptyList(),
    val resources: List<LessonResourceDto> = emptyList(),
    val homework: String? = null,
    val teacherNotes: String? = null,
    val status: String = "DRAFT"
) {
    fun toDomain(): LessonPlan = LessonPlan(
        id = id,
        schoolId = schoolId,
        classId = classId,
        subjectName = subjectName,
        topic = topic,
        date = date,
        objectives = objectives,
        activities = activities.map { it.toDomain() },
        resources = resources.map { it.toDomain() },
        homework = homework,
        teacherNotes = teacherNotes,
        status = runCatching { LessonPlanStatus.valueOf(status.uppercase()) }
            .getOrDefault(LessonPlanStatus.DRAFT)
    )

    companion object {
        fun fromDomain(plan: LessonPlan): LessonPlanDto = LessonPlanDto(
            id = plan.id,
            schoolId = plan.schoolId,
            classId = plan.classId,
            subjectName = plan.subjectName,
            topic = plan.topic,
            date = plan.date,
            objectives = plan.objectives,
            activities = plan.activities.map { LessonActivityDto.fromDomain(it) },
            resources = plan.resources.map { LessonResourceDto.fromDomain(it) },
            homework = plan.homework,
            teacherNotes = plan.teacherNotes,
            status = plan.status.name
        )
    }
}

@Serializable
data class LessonActivityDto(
    val description: String = "",
    val duration: Int = 0,
    val type: String = "LECTURE"
) {
    fun toDomain(): LessonActivity = LessonActivity(
        description = description,
        duration = duration,
        type = runCatching { ActivityType.valueOf(type.uppercase()) }
            .getOrDefault(ActivityType.LECTURE)
    )

    companion object {
        fun fromDomain(activity: LessonActivity): LessonActivityDto = LessonActivityDto(
            description = activity.description,
            duration = activity.duration,
            type = activity.type.name
        )
    }
}

@Serializable
data class LessonResourceDto(
    val id: String = "",
    val name: String = "",
    val type: String = "DOCUMENT",
    val url: String = "",
    val fileSize: Long? = null
) {
    fun toDomain(): LessonResource = LessonResource(
        id = id,
        name = name,
        type = runCatching { ResourceType.valueOf(type.uppercase()) }
            .getOrDefault(ResourceType.DOCUMENT),
        url = url,
        fileSize = fileSize
    )

    companion object {
        fun fromDomain(resource: LessonResource): LessonResourceDto = LessonResourceDto(
            id = resource.id,
            name = resource.name,
            type = resource.type.name,
            url = resource.url,
            fileSize = resource.fileSize
        )
    }
}

@Serializable
data class CurriculumMapDto(
    val termId: String = "",
    val termName: String = "",
    val subjects: List<CurriculumSubjectDto> = emptyList()
) {
    fun toDomain(): CurriculumMap = CurriculumMap(
        termId = termId,
        termName = termName,
        subjects = subjects.map { it.toDomain() }
    )
}

@Serializable
data class CurriculumSubjectDto(
    val subjectId: String = "",
    val subjectName: String = "",
    val topics: List<CurriculumTopicDto> = emptyList()
) {
    fun toDomain(): CurriculumSubject = CurriculumSubject(
        subjectId = subjectId,
        subjectName = subjectName,
        topics = topics.map { it.toDomain() }
    )
}

@Serializable
data class CurriculumTopicDto(
    val title: String = "",
    val weekNumber: Int = 0,
    val status: String = "NOT_STARTED"
) {
    fun toDomain(): CurriculumTopic = CurriculumTopic(
        title = title,
        weekNumber = weekNumber,
        status = runCatching { TopicStatus.valueOf(status.uppercase()) }
            .getOrDefault(TopicStatus.NOT_STARTED)
    )
}
