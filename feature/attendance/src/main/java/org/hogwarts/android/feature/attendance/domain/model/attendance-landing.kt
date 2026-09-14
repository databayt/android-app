package org.hogwarts.android.feature.attendance.domain.model

import java.time.LocalDate

/** A roster row's mark on the quick surface. Tapping cycles present → absent → late. */
enum class MarkStatus {
    Present, Absent, Late;

    fun next(): MarkStatus = when (this) {
        Present -> Absent
        Absent -> Late
        Late -> Present
    }

    companion object {
        /** Server status → the quick surface's three marks; anything else reads as present. */
        fun fromWire(status: String?): MarkStatus = when (status?.uppercase()) {
            "ABSENT" -> Absent
            "LATE" -> Late
            else -> Present
        }
    }
}

/** One section the teacher can mark today — mirrors `QuickSection` in `actions/quick.ts`. */
data class QuickSection(
    val id: String,
    val name: String,
    val gradeName: String,
    val studentCount: Int,
    val markedCount: Int,
    val scheduledToday: Boolean,
    val periodName: String?,
    /** "HH:mm" */
    val periodStart: String?,
    val periodEnd: String?,
    val isCurrent: Boolean,
) {
    val fullyMarked: Boolean get() = studentCount > 0 && markedCount >= studentCount
}

data class RosterStudent(
    val studentId: String,
    val name: String,
    val status: MarkStatus,
)

data class SectionRoster(
    val sectionId: String,
    val markedCount: Int,
    val students: List<RosterStudent>,
)

data class QuickContext(
    val today: LocalDate,
    val isSchoolDay: Boolean,
    val sections: List<QuickSection>,
    /** Rosters already fetched while counting marks, keyed by section id. */
    val rosters: Map<String, SectionRoster>,
)

/** What the teacher submitted: everyone present except these. */
data class QuickMark(
    val sectionId: String,
    val date: LocalDate,
    val absentStudentIds: List<String>,
    val lateStudentIds: List<String>,
) {
    /** One pending item per section and day — a re-mark replaces it. */
    val coalesceKey: String get() = "attendance:$sectionId:$date"
}

data class QuickSummary(
    val total: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
    val guardiansNotified: Int,
)

/** The server's answer to a save, or the reason there was none. */
sealed interface SaveOutcome {
    data class Saved(val summary: QuickSummary) : SaveOutcome

    /** The server already holds a newer mark for this section and day. */
    data object AlreadyNewer : SaveOutcome

    /** No connection: parked in the outbox; the worker delivers it later. */
    data object Queued : SaveOutcome

    data class Rejected(val code: String?) : SaveOutcome
}

/** Today's school-wide (or teacher-scoped) figures for the staff overview. */
data class TodayTotals(
    val marked: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
) {
    /** Late counts as attended, as on the web. */
    val rate: Int get() = if (marked > 0) Math.round((present + late) * 100f / marked) else 0
}

data class AttendanceStats(
    val totalDays: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
    val excused: Int,
) {
    val rate: Int get() = if (totalDays > 0) Math.round((present + late) * 100f / totalDays) else 0
}

data class AttendanceEntry(
    val id: String,
    val date: LocalDate,
    /** Server enum: PRESENT, ABSENT, LATE, EXCUSED, SICK, HOLIDAY. */
    val status: String,
    val className: String?,
)

data class StudentAttendance(
    val stats: AttendanceStats,
    val records: List<AttendanceEntry>,
)

data class ChildAttendance(
    val studentId: String,
    val name: String,
    val className: String,
    val stats: AttendanceStats,
    val recentAbsences: List<AttendanceEntry>,
)
