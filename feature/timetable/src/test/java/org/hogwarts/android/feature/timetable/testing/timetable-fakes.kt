package org.hogwarts.android.feature.timetable.testing

import org.hogwarts.android.core.data.tenant.CurrentUser
import org.hogwarts.android.core.data.tenant.SessionManager
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.timetable.data.remote.dto.ChildDto
import org.hogwarts.android.feature.timetable.data.remote.dto.ClosureDto
import org.hogwarts.android.feature.timetable.data.remote.dto.LiveClassDto
import org.hogwarts.android.feature.timetable.data.remote.dto.SlotDto
import org.hogwarts.android.feature.timetable.data.remote.dto.TimetableBundle
import org.hogwarts.android.feature.timetable.data.remote.dto.TodayPeriodDto
import org.hogwarts.android.feature.timetable.data.remote.dto.TodayTimetableDto
import org.hogwarts.android.feature.timetable.data.repository.TimetableRepository
import org.hogwarts.android.feature.timetable.data.repository.TimetableResult
import org.hogwarts.android.feature.timetable.data.repository.TimetableScope

class FakeSessionManager(role: UserRole) : SessionManager {
    override var currentUser: CurrentUser? = CurrentUser("user-1", "user-1@school.test", "school-1", role, "Nour", "Haddad")
    override val isAuthenticated: Boolean get() = currentUser != null
    override suspend fun setUser(user: CurrentUser) { currentUser = user }
    override suspend fun clearSession() { currentUser = null }
}

fun tenant(role: UserRole) = TenantContext(FakeSessionManager(role))

class FakeTimetableRepository : TimetableRepository {
    var mine: TimetableResult = TimetableResult.Fresh(TimetableBundle())
    var guardian: (String?) -> TimetableResult = { TimetableResult.Fresh(TimetableBundle()) }
    val cache = mutableMapOf<TimetableScope, TimetableBundle>()
    var mineCalls = 0
    val guardianCalls = mutableListOf<String?>()

    override suspend fun cached(scope: TimetableScope): TimetableBundle? = cache[scope]
    override suspend fun loadMine(): TimetableResult { mineCalls++; return mine }
    override suspend fun loadGuardian(childId: String?): TimetableResult { guardianCalls += childId; return guardian(childId) }
}

private fun t(hhmm: String) = "1970-01-01T$hhmm:00.000Z"

/** Fictional school day: three periods, a break before the third. */
object Fixtures {
    private val periods = listOf(
        Triple("Period 1", "07:30", "08:15"),
        Triple("Period 2", "08:20", "09:05"),
        Triple("Period 3", "09:35", "10:20"),
    )
    private val breakRow = TodayPeriodDto("br", "Break", t("09:05"), t("09:35"), isBreak = true)

    private val subjectsEn = listOf("Mathematics", "Science", "English", "Art", "History")
    private val subjectsAr = listOf("الرياضيات", "العلوم", "اللغة الإنجليزية", "التربية الفنية", "التاريخ")

    fun studentWeek(arabic: Boolean, today: Int = 1, closure: String? = null, liveOnToday: Boolean = true): TimetableBundle {
        val subjects = if (arabic) subjectsAr else subjectsEn
        val teachers = if (arabic) listOf("أ. سلمى", "أ. كريم", "أ. هدى") else listOf("Ms. Rowan", "Mr. Idris", "Ms. Lina")
        val slots = (0..4).flatMap { day ->
            periods.mapIndexedNotNull { p, (name, start, end) ->
                if (day == 3 && p == 2) return@mapIndexedNotNull null // one free period
                SlotDto(
                    id = "slot-$day-$p",
                    dayOfWeek = day,
                    subjectName = subjects[(day + p) % subjects.size],
                    teacherName = teachers[(day + p) % teachers.size],
                    sectionName = if (arabic) "الخامس أ" else "Grade 5 A",
                    classroom = if (arabic) "ب12" else "B12",
                    periodName = name,
                    startTime = t(start),
                    endTime = t(end),
                    liveClass = if (liveOnToday && day == today && p == 1) LiveClassDto("session-1", "livekit", null, "scheduled") else null,
                )
            }
        }
        val todayRows = periods.mapIndexed { p, (name, start, end) ->
            val slot = slots.firstOrNull { it.dayOfWeek == today && it.periodName == name }
            TodayPeriodDto(
                periodId = "p$p", periodName = name, startTime = t(start), endTime = t(end),
                subject = slot?.subjectName, className = slot?.sectionName, teacher = slot?.teacherName, room = slot?.classroom,
                timetableId = slot?.id, liveClass = slot?.liveClass,
            )
        }.toMutableList().apply { add(2, breakRow) }
        return TimetableBundle(
            slots = slots,
            today = TodayTimetableDto(dayOfWeek = today, closure = closure?.let { ClosureDto(it, "HOLIDAY") }, periods = todayRows),
        )
    }

    fun teacherWeek(arabic: Boolean, today: Int = 1): TimetableBundle {
        val base = studentWeek(arabic, today)
        val sections = if (arabic) listOf("الخامس أ", "السادس ب") else listOf("Grade 5 A", "Grade 6 B")
        // A teacher teaches periods 1 and 3 only.
        val slots = base.slots.filter { it.periodName?.endsWith("2") != true }.mapIndexed { i, s -> s.copy(sectionName = sections[i % 2], teacherName = null) }
        val today = base.today!!.copy(
            periods = base.today!!.periods.map { row ->
                val slot = slots.firstOrNull { it.dayOfWeek == today && it.periodName == row.periodName && !row.isBreak }
                if (slot == null) row.copy(subject = null, className = null, teacher = null, room = null, timetableId = null, liveClass = null)
                else row.copy(className = slot.sectionName, teacher = null, timetableId = slot.id)
            },
        )
        return TimetableBundle(slots = slots, today = today)
    }

    fun children(arabic: Boolean) = listOf(
        ChildDto("child-1", if (arabic) "ليان" else "Layan", if (arabic) "حداد" else "Haddad", grade = if (arabic) "الصف الخامس" else "Grade 5"),
        ChildDto("child-2", if (arabic) "آدم" else "Adam", if (arabic) "حداد" else "Haddad", grade = if (arabic) "الصف الثاني" else "Grade 2"),
    )
}
