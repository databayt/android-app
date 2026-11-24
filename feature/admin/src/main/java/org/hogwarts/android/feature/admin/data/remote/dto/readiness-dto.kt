package org.hogwarts.android.feature.admin.data.remote.dto

import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.admin.domain.model.ClassReadiness
import org.hogwarts.android.feature.admin.domain.model.ReadinessDay
import org.hogwarts.android.feature.admin.domain.model.ReadinessLevel
import org.hogwarts.android.feature.admin.domain.model.ReadinessPerson
import org.hogwarts.android.feature.admin.domain.model.ReadinessReason
import org.hogwarts.android.feature.admin.domain.model.ReadinessSession

/** `GET /api/mobile/admin/readiness?date=` — hogwarts `api/mobile/admin/readiness/route.ts`. */
@Serializable
data class ReadinessResponseDto(
    val date: String,
    val classes: List<ClassReadinessDto> = emptyList(),
) {
    fun toDomain() = ReadinessDay(date, classes.map { it.toDomain() })
}

@Serializable
data class ReadinessPersonDto(
    val name: String = "",
    val level: String = "ok",
    val reasons: List<String> = emptyList(),
    val username: String? = null,
    val canCover: Boolean = false,
) {
    fun toDomain() = ReadinessPerson(
        name = name,
        level = ReadinessLevel.fromCode(level),
        reasons = reasons.mapNotNull(ReadinessReason::fromCode),
        username = username,
        canCover = canCover,
    )
}

@Serializable
data class ReadinessSessionDto(
    val start: String = "",
    val subject: String? = null,
    val teacher: ReadinessPersonDto? = null,
) {
    fun toDomain() = ReadinessSession(start, subject, teacher?.toDomain())
}

@Serializable
data class ClassReadinessDto(
    val sectionId: String,
    val name: String = "",
    val supervisor: ReadinessPersonDto? = null,
    val sessions: List<ReadinessSessionDto> = emptyList(),
    val students: List<ReadinessPersonDto> = emptyList(),
) {
    fun toDomain() = ClassReadiness(
        sectionId = sectionId,
        name = name,
        supervisor = supervisor?.toDomain(),
        sessions = sessions.map { it.toDomain() },
        students = students.map { it.toDomain() },
    )
}
