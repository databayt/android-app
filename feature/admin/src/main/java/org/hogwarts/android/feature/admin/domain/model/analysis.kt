package org.hogwarts.android.feature.admin.domain.model

import androidx.annotation.StringRes
import kotlin.math.roundToInt
import org.hogwarts.android.feature.admin.R

/** One school day of class attendance: seats that joined over seats expected. */
data class AttendanceDay(val date: String, val joined: Int, val expected: Int)

/** How people got into the day's classes: the app, an Android browser, or a computer / iPhone. */
data class JoinsDay(val date: String, val app: Int, val androidWeb: Int, val computer: Int) {
    val total: Int get() = app + androidWeb + computer
}

/** A member the school should follow up; [reasons] are the same chips as «جاهزية الصفوف». */
data class AttentionPerson(
    val userId: String,
    val name: String,
    val role: String,
    val sectionName: String?,
    val level: ReadinessLevel,
    val reasons: List<ReadinessReason>,
)

data class AnalysisCounts(val members: Int, val onApp: Int, val pushOn: Int, val active7d: Int)

/** The school's analysis (progress) report — `GET /api/mobile/admin/analysis`. */
data class SchoolAnalysis(
    /** Percent over the last 14 days; null when no class was held. */
    val attendanceRate14d: Double?,
    val readinessPct: Double,
    val attendanceByDay: List<AttendanceDay>,
    val joinsByDay: List<JoinsDay>,
    val needsAttention: List<AttentionPerson>,
    val counts: AnalysisCounts,
) {
    val hasAttendanceData: Boolean get() = attendanceByDay.any { it.expected > 0 || it.joined > 0 }
    val hasJoinData: Boolean get() = joinsByDay.any { it.total > 0 }
}

/** `80%`, or an em dash when there is no number to show. */
fun percentText(value: Double?): String = if (value == null || value.isNaN()) "—" else "${value.roundToInt()}%"

/** `12/40` for "x of y". */
fun ratioText(part: Int, whole: Int): String = "$part/$whole"

/** Bar height as a 0..1 fraction of the tallest bar; a zero or negative max never divides. */
fun barFraction(value: Int, max: Int): Float =
    if (max <= 0 || value <= 0) 0f else (value.toFloat() / max).coerceIn(0f, 1f)

/** Slot a day is drawn in: Canvas is not mirrored, so right-to-left reverses the order by hand. */
fun slotIndex(index: Int, count: Int, rtl: Boolean): Int = if (rtl) count - 1 - index else index

/** `MM-DD` of a `YYYY-MM-DD`; anything else is shown as is. */
fun shortDate(date: String): String =
    if (date.length >= 10 && date[4] == '-' && date[7] == '-') date.substring(5, 10) else date

/** The role chip text; null for a role the app has no word for, which then shows the raw code. */
@StringRes
fun roleLabelRes(role: String): Int? = when (role.uppercase()) {
    "TEACHER" -> R.string.analysis_role_teacher
    "STUDENT" -> R.string.analysis_role_student
    "GUARDIAN" -> R.string.analysis_role_guardian
    "STAFF" -> R.string.analysis_role_staff
    else -> null
}
