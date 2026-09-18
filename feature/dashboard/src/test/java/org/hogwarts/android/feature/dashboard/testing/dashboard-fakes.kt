package org.hogwarts.android.feature.dashboard.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.hogwarts.android.core.data.tenant.CurrentUser
import org.hogwarts.android.core.data.tenant.SessionManager
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.dashboard.data.remote.DashboardDto
import org.hogwarts.android.feature.dashboard.data.remote.DashboardSectionsDto
import org.hogwarts.android.feature.dashboard.data.remote.InvoiceDto
import org.hogwarts.android.feature.dashboard.data.remote.NextActionDto
import org.hogwarts.android.feature.dashboard.data.remote.PeriodDto
import org.hogwarts.android.feature.dashboard.data.remote.QuickActionDto
import org.hogwarts.android.feature.dashboard.data.remote.ResourceUsageDto
import org.hogwarts.android.feature.dashboard.data.remote.TodayTimetableDto
import org.hogwarts.android.feature.dashboard.data.repository.DashboardRepository
import org.hogwarts.android.feature.dashboard.data.repository.DashboardResult
import org.hogwarts.android.feature.dashboard.data.repository.DashboardSectionsRepository
import org.hogwarts.android.feature.dashboard.data.repository.SectionsResult

class FakeSessionManager(role: UserRole) : SessionManager {
    override var currentUser: CurrentUser? = CurrentUser("user-1", "user-1@school.test", "school-1", role, "Nour", "Haddad")
    override val isAuthenticated: Boolean get() = currentUser != null
    override suspend fun setUser(user: CurrentUser) { currentUser = user }
    override suspend fun clearSession() { currentUser = null }
}

fun tenant(role: UserRole) = TenantContext(FakeSessionManager(role))

class FakeDashboardRepository(private var result: DashboardResult) : DashboardRepository {
    private val _latest = MutableStateFlow<DashboardDto?>(null)
    override val latest: StateFlow<DashboardDto?> = _latest
    var cached: DashboardDto? = null
    var refreshes = 0

    override fun clearSession() { _latest.value = null }
    override suspend fun cached(): DashboardDto? = cached
    override suspend fun refresh(): DashboardResult {
        refreshes++
        (result as? DashboardResult.Fresh)?.let { _latest.value = it.data }
        return result
    }
}

class FakeSectionsRepository(var result: SectionsResult = SectionsResult.Unavailable) : DashboardSectionsRepository {
    var loads = 0
    override suspend fun load(): SectionsResult { loads++; return result }
}

/** Invented data. android-app is a public repo — nothing here comes from a real school. */
object Fixtures {
    private fun t(hhmm: String) = "1970-01-01T$hhmm:00.000Z"

    /**
     * A fictional Sunday: three classes, one free period and a break before
     * the last. Nothing here is a real school's timetable.
     */
    val teacherDay = TodayTimetableDto(
        dayOfWeek = 0,
        isToday = true,
        periods = listOf(
            PeriodDto("p1", "Period 1", t("07:15"), t("08:05"), subject = "علم الأصوات", className = "الصف الثامن - ج", teacher = "أ. ريما قصار", room = "ن4", timetableId = "tt-1"),
            PeriodDto("p2", "Period 2", t("08:10"), t("09:00")),
            PeriodDto("p3", "Period 3", t("09:05"), t("09:55"), subject = "البصريات", className = "الصف السابع - د", teacher = "أ. ريما قصار", room = "مختبر 7", timetableId = "tt-2"),
            PeriodDto("br", "استراحة", t("09:55"), t("10:35"), isBreak = true),
            PeriodDto("p4", "Period 4", t("10:35"), t("11:25"), subject = "الخرائط القديمة", className = "الصف الثامن - ج", teacher = "أ. ريما قصار", room = "ن9", timetableId = "tt-3"),
        ),
    )

    /** The same day seen by a student: the subject over its teacher. */
    val studentDay = teacherDay.copy(
        isToday = false,
        dayOfWeek = 1,
        periods = teacherDay.periods.map { it.copy(className = null) },
    )

    val teacher = DashboardDto(
        role = "TEACHER",
        eventsToday = 2,
        nextActions = listOf(
            NextActionDto("attendanceDue", "2", "/attendance"),
            NextActionDto("pendingGrading", "14", "/grades"),
        ),
        quickActions = listOf(
            QuickActionDto("attendance", "Attendance", href = "/attendance"),
            QuickActionDto("grades", "Grades", href = "/grades"),
            QuickActionDto("assignments", "Assignments", href = "/assignments"),
            QuickActionDto("timetable", "Schedule", href = "/timetable"),
        ),
        todayTimetable = teacherDay,
    )

    val student = DashboardDto(
        role = "STUDENT",
        eventsToday = 0,
        nextActions = listOf(NextActionDto("assignmentDue", "علم الأصوات", "/assignments")),
        quickActions = listOf(
            QuickActionDto("assignments", "Assignments", href = "/assignments"),
            QuickActionDto("exams", "Exams", href = "/exams"),
            QuickActionDto("profile", "Profile", href = "/profile"),
            QuickActionDto("settings", "Settings", href = "/settings"),
        ),
        todayTimetable = studentDay,
    )

    val admin = DashboardDto(
        role = "ADMIN",
        eventsToday = 1,
        nextActions = listOf(NextActionDto("pendingApprovals", "4", "/admission")),
        quickActions = listOf(
            QuickActionDto("school", "School", href = "/school"),
            QuickActionDto("settings", "Settings", href = "/settings"),
            QuickActionDto("finance", "Finance", href = "/finance"),
            QuickActionDto("staff", "Staff", href = "/staff"),
        ),
    )

    val accountant = DashboardDto(
        role = "ACCOUNTANT",
        eventsToday = 0,
        nextActions = listOf(NextActionDto("overdueInvoices", "3", "/finance/invoice")),
        quickActions = listOf(
            QuickActionDto("finance_invoice", "Invoices", href = "/finance/invoice"),
            QuickActionDto("finance_fees", "Fees", href = "/finance/fees"),
            QuickActionDto("finance", "Finance", href = "/finance"),
            QuickActionDto("finance_receipt", "Receipts", href = "/finance/receipt"),
        ),
    )

    val teacherSections = DashboardSectionsDto(
        resourceUsage = listOf(
            ResourceUsageDto("lessonsThisWeek", "Lessons This Week", used = 18.0, limit = 24.0, unit = "lessons"),
            ResourceUsageDto("ungradedWork", "Ungraded Work", used = 41.0, limit = 50.0, unit = "submissions"),
            ResourceUsageDto("classCoverage", "Class Coverage", used = 172.0, limit = 180.0, unit = "students"),
        ),
        invoices = listOf(
            InvoiceDto("inv-1", "2031-03-04", "Invoice 9901", amount = 320.0, currency = "SAR", status = "paid"),
            InvoiceDto("inv-2", "2031-02-18", "Kite Workshop Materials", amount = 90.5, currency = "SAR", status = "open"),
        ),
    )

    val adminSections = DashboardSectionsDto(
        resourceUsage = listOf(
            ResourceUsageDto("activeUsers", "Active Users", used = 640.0, limit = 2000.0, unit = "users"),
            ResourceUsageDto("storageUsed", "Storage Used", used = 78.0, limit = 100.0, unit = "GB"),
            ResourceUsageDto("systemHealth", "System Health", used = 98.0, limit = 100.0, unit = "%"),
        ),
        invoices = emptyList(),
    )
}
