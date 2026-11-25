package org.hogwarts.android.feature.admin.data.remote.dto

import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.admin.domain.model.AnalysisCounts
import org.hogwarts.android.feature.admin.domain.model.AttendanceDay
import org.hogwarts.android.feature.admin.domain.model.AttentionPerson
import org.hogwarts.android.feature.admin.domain.model.JoinsDay
import org.hogwarts.android.feature.admin.domain.model.ReadinessLevel
import org.hogwarts.android.feature.admin.domain.model.ReadinessReason
import org.hogwarts.android.feature.admin.domain.model.SchoolAnalysis

/** `GET /api/mobile/admin/analysis` — hogwarts `api/mobile/admin/analysis/route.ts`. */
@Serializable
data class AnalysisResponseDto(
    val attendanceRate14d: Double? = null,
    val readinessPct: Double = 0.0,
    val attendanceByDay: List<AttendanceDayDto> = emptyList(),
    val joinsByDay: List<JoinsDayDto> = emptyList(),
    val needsAttention: List<AttentionDto> = emptyList(),
    val counts: AnalysisCountsDto = AnalysisCountsDto(),
) {
    fun toDomain() = SchoolAnalysis(
        attendanceRate14d = attendanceRate14d,
        readinessPct = readinessPct,
        attendanceByDay = attendanceByDay.map { AttendanceDay(it.date, it.joined, it.expected) },
        joinsByDay = joinsByDay.map { JoinsDay(it.date, it.app, it.androidWeb, it.computer) },
        needsAttention = needsAttention.map { it.toDomain() },
        counts = AnalysisCounts(counts.members, counts.onApp, counts.pushOn, counts.active7d),
    )
}

@Serializable
data class AttendanceDayDto(val date: String = "", val joined: Int = 0, val expected: Int = 0)

@Serializable
data class JoinsDayDto(val date: String = "", val app: Int = 0, val androidWeb: Int = 0, val computer: Int = 0)

@Serializable
data class AttentionDto(
    val userId: String = "",
    val name: String = "",
    val role: String = "",
    val sectionName: String? = null,
    val level: String = "warn",
    val reasons: List<String> = emptyList(),
) {
    fun toDomain() = AttentionPerson(
        userId = userId,
        name = name,
        role = role,
        sectionName = sectionName,
        level = ReadinessLevel.fromCode(level),
        reasons = reasons.mapNotNull(ReadinessReason::fromCode),
    )
}

@Serializable
data class AnalysisCountsDto(val members: Int = 0, val onApp: Int = 0, val pushOn: Int = 0, val active7d: Int = 0)
